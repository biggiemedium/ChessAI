package dev.chess.cheat.Network.Impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Network.ChessClient;
import dev.chess.cheat.Network.Model.OpponentStats;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.Util.Annotation.Value;
import dev.chess.cheat.Util.Interface.ILiChessEvents;
import dev.chess.cheat.Util.PropertyLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * TODO:
 * Challenge Player -> Challenge players not just AI | /api/challenge/{username}
 * List Challenges -> Show people who challenge you | /api/challenge | For details about challenge -> /api/challenge/{challengeId}/show
 *
 * BOT:
 * Claim Vicotory if opponent disconnects | /api/bot/game/{gameId}/claim-victory
 * Claim Draw if opponent disconnects for x time | /api/bot/game/{gameId}/claim-draw
 * Send Chat messages | /api/bot/game/{gameId}/chat
 * Event based system | /api/stream/event -> (GameStartEvent, GameFinishEvent, ChallengeEvent, ChallengeCancelEvent, ChallengeAcceptEvent)
 */
public class LiChessClient extends ChessClient {

    private static final String API_BASE = "https://lichess.org/api";

    @Value(key = "LICHESS_API_KEY")
    private String oauthToken;

    private final ExecutorService executor;
    private volatile boolean streaming;
    private volatile boolean streamingEvents;
    private final Gson gson;

    private OpponentStats opponentStats;
    private final List<String> chatMessages = new CopyOnWriteArrayList<>();
    private final List<String> formattedChatMessages = new CopyOnWriteArrayList<>();

    private ILiChessEvents eventListener;

    // Move calculation handling
    private volatile boolean waitingForMoveConfirmation = false;
    private volatile int lastProcessedMoveCount = 0;


    public LiChessClient() {
        this.gson = new Gson();
        this.executor = Executors.newCachedThreadPool();
        PropertyLoader.inject(this);
    }

    public LiChessClient(String oauthToken) {
        this.oauthToken = oauthToken;
        this.gson = new Gson();
        this.executor = Executors.newCachedThreadPool();
    }

    public void setEventListener(ILiChessEvents listener) {
        this.eventListener = listener;
    }

    // ========== Connection Management ==========

    @Override
    public boolean establishConnection() {
        try {
            JsonObject account = httpGet("/account");
            if (account != null) {
                connected = true;
                ourUsername = account.get("id").getAsString();
                System.out.println("Connected as: " + ourUsername);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean closeConnection() {
        streaming = false;
        streamingEvents = false;
        connected = false;
        executor.shutdown();
        resetGameState();
        opponentStats = null;
        return true;
    }

    @Override
    protected void resetGameState() {
        currentGame = null;
        currentGameId = null;
        weAreWhite = false;
        chatMessages.clear();
        formattedChatMessages.clear();
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }

    // ========== Game Actions ==========

    @Override
    public String challengeAI(int level, int timeMinutes, int increment) {
        try {
            String params = String.format("level=%d&clock.limit=%d&clock.increment=%d",
                    level, timeMinutes * 60, increment);
            JsonObject result = httpPost("/challenge/ai", params);
            if (result != null) {
                String gameId = result.get("id").getAsString();
                System.out.println("AI challenge created: " + gameId);
                return gameId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String challengePlayer(String username) {
        try {
            String params = "rated=false&clock.limit=300&clock.increment=0";
            JsonObject result = httpPost("/challenge/" + username, params);
            if (result != null) {
                String challengeId = result.getAsJsonObject("challenge").get("id").getAsString();
                System.out.println("Challenge sent to " + username + ": " + challengeId);
                return challengeId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean streamGame(String gameId, ChessEngine engine) {
        if (!connected) return false;

        this.currentGameId = gameId;
        this.currentGame = new Game(new Board(), new ArrayList<>(), engine, this);
        this.currentGame.setGameId(gameId);

        // Reset state tracking
        this.waitingForMoveConfirmation = false;
        this.lastProcessedMoveCount = 0;

        streaming = true;
        executor.submit(() -> streamGameEvents(gameId));
        return true;
    }

    @Override
    public void stopStreaming() {
        streaming = false;
    }

    @Override
    public boolean makeMove(String move) {
        try {
            waitingForMoveConfirmation = true;
            HttpURLConnection conn = httpConnection("/bot/game/" + currentGameId + "/move/" + move, "POST");
            int code = conn.getResponseCode();
            if (code == 200) {
                System.out.println("Move sent: " + move);
                return true;
            }
            if (code == 400) {
                System.err.println("Invalid move: " + conn.getResponseMessage());
                waitingForMoveConfirmation = false;  // Reset on failure
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            waitingForMoveConfirmation = false;
            return false;
        }
    }

    @Override
    public void calculateAndMakeMove() {
        try {
            System.out.println("Calculating move...");
            var bestMove = currentGame.getAI().findBestMove(
                    currentGame.getBoard(),
                    currentGame.isWhiteTurn(),
                    4
            );

            if (bestMove != null) {
                String uci = moveToUCI(bestMove);
                System.out.println("Playing: " + uci);
                makeMove(uci);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateGameState(String[] moves) {
        if (moves.length > 0) {
            currentGame.updateFromMoves(moves);
        } else {
            currentGame.reset();
        }
    }

    @Override
    protected void handleStatusChange(String status, String winner) {
        currentGame.updateStatus(status, winner);

        if ("started".equals(status) && isOurTurn() && !waitingForMoveConfirmation) {
            executor.submit(() -> {
                try {
                    Thread.sleep(100);
                    if (!waitingForMoveConfirmation && isOurTurn()) {
                        calculateAndMakeMove();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public boolean resignGame(String gameId) {
        try {
            HttpURLConnection conn = httpConnection("/bot/game/" + gameId + "/resign", "POST");
            int code = conn.getResponseCode();
            if (code == 200) {
                System.out.println("Resigned from game: " + gameId);
                return true;
            }
            if(code == 400) {
                System.err.printf("This request is invalid because [%s] \n ", conn.getResponseMessage());
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected String moveToUCI(dev.chess.cheat.Engine.Move move) {
        char fromFile = (char) ('a' + move.getFromCol());
        char toFile = (char) ('a' + move.getToCol());
        int fromRank = 8 - move.getFromRow();
        int toRank = 8 - move.getToRow();
        return "" + fromFile + fromRank + toFile + toRank;
    }

    // ========== Chat ==========

    @Override
    public boolean sendMessage(String message) {
        if (currentGameId == null || message == null || message.trim().isEmpty()) {
            return false;
        }

        try {
            String params = String.format("room=player&text=%s",
                    java.net.URLEncoder.encode(message, "UTF-8"));
            HttpURLConnection conn = httpConnection("/bot/game/" + currentGameId + "/chat", "POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes("utf-8"));
            }

            if (conn.getResponseCode() == 200) {
                System.out.println("Chat sent: " + message);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getGameChat() {
        return new ArrayList<>(chatMessages);
    }

    public List<String> getFormattedGameChat() {
        return new ArrayList<>(formattedChatMessages);
    }

    public void clearChatMessages() {
        chatMessages.clear();
        formattedChatMessages.clear();
    }

    // ========== Opponent Stats ==========

    public OpponentStats fetchOpponentStats(String opponentUsername) {
        try {
            JsonObject userData = httpGet("/user/" + opponentUsername);
            if (userData != null) {
                this.opponentStats = parseOpponentStats(userData);
                return opponentStats;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public OpponentStats getCurrentOpponentStats() {
        return opponentStats;
    }

    public boolean isOpponentBot() {
        return opponentStats != null && opponentStats.isBot();
    }

    private OpponentStats parseOpponentStats(JsonObject data) {
        String username = data.has("id") ? data.get("id").getAsString() : "Unknown";
        OpponentStats stats = new OpponentStats(username, oauthToken);

        stats.setId(username);
        stats.setUsername(username);
        stats.setOnline(data.has("seenAt"));
        stats.setBot(data.has("title") && "BOT".equals(data.get("title").getAsString()));
        stats.setTitle(data.has("title") ? data.get("title").getAsString() : null);

        // Parse game counts
        if (data.has("count")) {
            JsonObject count = data.getAsJsonObject("count");
            stats.setGames(count.has("all") ? count.get("all").getAsInt() : 0);
            stats.setWins(count.has("win") ? count.get("win").getAsInt() : 0);
            stats.setLosses(count.has("loss") ? count.get("loss").getAsInt() : 0);
            stats.setDraws(count.has("draw") ? count.get("draw").getAsInt() : 0);
        }

        // Parse ratings
        if (data.has("perfs")) {
            JsonObject perfs = data.getAsJsonObject("perfs");
            if (perfs.has("blitz")) stats.setBlitzRating(perfs.getAsJsonObject("blitz").get("rating").getAsInt());
            if (perfs.has("rapid")) stats.setRapidRating(perfs.getAsJsonObject("rapid").get("rating").getAsInt());
            if (perfs.has("bullet")) stats.setBulletRating(perfs.getAsJsonObject("bullet").get("rating").getAsInt());
            if (perfs.has("classical")) stats.setClassicalRating(perfs.getAsJsonObject("classical").get("rating").getAsInt());

            // Set default rating
            Integer rating = stats.getBlitzRating() != null ? stats.getBlitzRating() :
                    stats.getRapidRating() != null ? stats.getRapidRating() : 1500;
            stats.setRating(rating);
        }

        return stats;
    }

    private OpponentStats createAIOpponentStats(int aiLevel) {
        OpponentStats stats = new OpponentStats("AI Level " + aiLevel, oauthToken);
        stats.setId("ai-level-" + aiLevel);
        stats.setUsername("Stockfish AI");
        stats.setBot(true);
        stats.setTitle("BOT");
        stats.setOnline(true);

        int estimatedRating = 800 + (aiLevel * 250);
        stats.setRating(estimatedRating);
        stats.setBlitzRating(estimatedRating);

        return stats;
    }

    // ========== Event Streaming ==========

    /**
     * Stream global LiChess events (/api/stream/event)
     */
    public void startGlobalEventStream() {
        streamingEvents = true;
        executor.submit(() -> {
            try {
                HttpURLConnection conn = httpConnection("/stream/event", "GET");
                conn.setReadTimeout(0);

                if (conn.getResponseCode() != 200) {
                    System.err.println("Event stream failed: " + conn.getResponseCode());
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while (streamingEvents && (line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        handleGlobalEvent(gson.fromJson(line, JsonObject.class));
                    }
                }
                reader.close();
            } catch (Exception e) {
                if (streamingEvents) e.printStackTrace();
            } finally {
                streamingEvents = false;
                System.out.println("Event stream ended");
            }
        });
    }

    /**
     * Handle global events and delegate to listener
     */
    private void handleGlobalEvent(JsonObject event) {
        if (eventListener == null) return;

        String type = event.get("type").getAsString();
        System.out.println("Global Event: " + type);

        switch (type) {
            case "gameStart":
                if (event.has("game")) {
                    JsonObject game = event.getAsJsonObject("game");
                    String gameId = game.get("gameId").getAsString();
                    eventListener.onGameStart(gameId, game);
                }
                break;

            case "gameFinish":
                if (event.has("game")) {
                    JsonObject game = event.getAsJsonObject("game");
                    String gameId = game.get("gameId").getAsString();
                    eventListener.onGameFinish(gameId, game);
                }
                break;

            case "challenge":
                if (event.has("challenge")) {
                    JsonObject challenge = event.getAsJsonObject("challenge");
                    String challengeId = challenge.get("id").getAsString();
                    eventListener.onChallengeReceived(challengeId, challenge);
                }
                break;

            case "challengeCanceled":
            case "challengeDeclined":
                if (event.has("challenge")) {
                    JsonObject challenge = event.getAsJsonObject("challenge");
                    String challengeId = challenge.get("id").getAsString();
                    eventListener.onChallengeCanceled(challengeId, challenge);
                }
                break;
        }
    }

    /**
     * Stream per-game events (/api/bot/game/stream/{gameId})
     */
    private void streamGameEvents(String gameId) {
        System.out.println("Streaming game: " + gameId);
        try {
            HttpURLConnection conn = httpConnection("/bot/game/stream/" + gameId, "GET");
            conn.setReadTimeout(0);

            if (conn.getResponseCode() != 200) {
                System.err.println("Game stream failed: " + conn.getResponseCode());
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while (streaming && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    handleGameEvent(gson.fromJson(line, JsonObject.class));
                }
            }
            reader.close();
        } catch (Exception e) {
            if (streaming) e.printStackTrace();
        } finally {
            streaming = false;
            System.out.println("Game stream ended");
        }
    }

    /**
     * Handle per-game events
     */
    private void handleGameEvent(JsonObject event) {
        String type = event.get("type").getAsString();

        switch (type) {
            case "gameFull":
                handleGameFullEvent(event);
                break;

            case "gameState":
                handleGameStateEvent(event);
                break;

            case "chatLine":
                handleChatEvent(event);
                break;

            case "opponentGone":
                System.out.println("Opponent is gone!");
                break;
        }
    }

    private void handleGameFullEvent(JsonObject event) {
        weAreWhite = event.has("white") && event.getAsJsonObject("white").has("id") &&
                event.getAsJsonObject("white").get("id").getAsString().equalsIgnoreCase(ourUsername);

        System.out.println("We are: " + (weAreWhite ? "WHITE" : "BLACK"));

        // Fetch opponent stats
        String opponentColor = weAreWhite ? "black" : "white";
        JsonObject opponent = event.has(opponentColor) ? event.getAsJsonObject(opponentColor) : null;

        if (opponent != null) {
            if (opponent.has("aiLevel")) {
                int aiLevel = opponent.get("aiLevel").getAsInt();
                this.opponentStats = createAIOpponentStats(aiLevel);
            } else if (opponent.has("id")) {
                fetchOpponentStats(opponent.get("id").getAsString());
            }
        }

        // Process initial state
        if (event.has("state")) {
            handleGameStateEvent(event.getAsJsonObject("state"));
        }
    }

    private void handleGameStateEvent(JsonObject state) {
        String moves = state.has("moves") ? state.get("moves").getAsString() : "";
        String status = state.get("status").getAsString();
        String winner = state.has("winner") ? state.get("winner").getAsString() : null;

        String[] moveArray = moves.isEmpty() ? new String[0] : moves.split(" ");
        int currentMoveCount = moveArray.length;

        System.out.println("Moves: " + (moves.isEmpty() ? "none" : moves) + " | Status: " + status);
        System.out.println("Move count: " + currentMoveCount + " (last processed: " + lastProcessedMoveCount + ")");

        // Only update if this is a new state
        if (currentMoveCount > lastProcessedMoveCount) {
            updateGameState(moveArray);
            lastProcessedMoveCount = currentMoveCount;
            waitingForMoveConfirmation = false;  // Move was confirmed
        }

        handleStatusChange(status, winner);
    }

    private void handleChatEvent(JsonObject event) {
        String username = event.has("username") ? event.get("username").getAsString() : "Unknown";
        String text = event.has("text") ? event.get("text").getAsString() : "";
        String room = event.has("room") ? event.get("room").getAsString() : "player";

        String formatted = String.format("[%s] %s: %s", room, username, text);
        chatMessages.add(text);
        formattedChatMessages.add(formatted);
        System.out.println(formatted);

        if (currentGame != null) {
            currentGame.onChatMessage(username, text, room);
        }
    }

    // ========== HTTP Helpers ==========

    private HttpURLConnection httpConnection(String endpoint, String method) throws Exception {
        URL url = new URL(API_BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + oauthToken);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private JsonObject httpGet(String endpoint) throws Exception {
        HttpURLConnection conn = httpConnection(endpoint, "GET");
        if (conn.getResponseCode() == 200) {
            return gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
        }
        return null;
    }

    private JsonObject httpPost(String endpoint, String params) throws Exception {
        HttpURLConnection conn = httpConnection(endpoint, "POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes("utf-8"));
        }

        int code = conn.getResponseCode();
        if (code == 200 || code == 201) {
            return gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
        }
        return null;
    }
}