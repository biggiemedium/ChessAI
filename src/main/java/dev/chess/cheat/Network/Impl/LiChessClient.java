package dev.chess.cheat.Network.Impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Network.ChessClient;
import dev.chess.cheat.Network.Model.OpponentStats;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.Util.Annotation.Value;
import dev.chess.cheat.Util.PropertyLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class LiChessClient extends ChessClient {

    private static final String API_BASE = "https://lichess.org/api";

    @Value(key = "LICHESS_API_KEY")
    private String oauthToken;

    private ExecutorService executor;
    private volatile boolean streaming;
    private final Gson gson;
    private OpponentStats opponentStats;

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

    @Override
    public boolean establishConnection() {
        try {
            JsonObject account = apiGet("/account");
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
        connected = false;
        executor.shutdown();
        resetGameState();
        opponentStats = null;
        return true;
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }

    @Override
    public String challengeAI(int level, int timeMinutes, int increment) {
        try {
            String params = String.format("level=%d&clock.limit=%d&clock.increment=%d",
                    level, timeMinutes * 60, increment);
            JsonObject result = apiPost("/challenge/ai", params);
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
    public boolean streamGame(String gameId, ChessEngine engine) {
        if (!connected) return false;

        this.currentGameId = gameId;
        this.currentGame = new Game(new Board(), new ArrayList<>(), engine, this);
        this.currentGame.setGameId(gameId);

        streaming = true;
        executor.submit(() -> streamGameState(gameId));
        return true;
    }

    @Override
    public void stopStreaming() {
        streaming = false;
    }

    @Override
    public boolean makeMove(String move) {
        try {
            HttpURLConnection conn = createConnection("/bot/game/" + currentGameId + "/move/" + move, "POST");
            int code = conn.getResponseCode();
            System.out.println(code == 200 ? "✓ Move sent" : "✗ Move rejected: " + code);
            return code == 200;
        } catch (Exception e) {
            e.printStackTrace();
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

        if ("started".equals(status) && isOurTurn()) {
            executor.submit(this::calculateAndMakeMove);
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

    // ========== Opponent Stats Methods ==========

    /**
     * Fetch opponent stats for a given username
     * @param opponentUsername the opponent's username
     * @return OpponentStats object, or null if fetch failed
     */
    public OpponentStats fetchOpponentStats(String opponentUsername) {
        try {
            System.out.println("Fetching stats for: " + opponentUsername);
            JsonObject userData = getUserData(opponentUsername);
            if (userData == null) {
                System.err.println("Failed to fetch user data for: " + opponentUsername);
                return null;
            }

            System.out.println("User data received: " + userData.toString());
            this.opponentStats = parseOpponentStats(userData);
            System.out.println("Opponent stats parsed successfully");
            return opponentStats;

        } catch (Exception e) {
            System.err.println("Error fetching opponent stats:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get opponent stats as a map
     * @param opponentUsername the opponent's username
     * @return Map containing all opponent data
     */
    public Map<String, Object> getOpponentStatsMap(String opponentUsername) {
        OpponentStats stats = fetchOpponentStats(opponentUsername);
        return stats != null ? stats.getStatsMap() : null;
    }

    /**
     * Get current opponent's stats
     * @return OpponentStats for the current opponent, or null if not available
     */
    public OpponentStats getCurrentOpponentStats() {
        return opponentStats;
    }

    /**
     * Check if current opponent is a bot
     * @return true if opponent is a bot
     */
    public boolean isOpponentBot() {
        return opponentStats != null && opponentStats.isBot();
    }

    /**
     * Get bot-specific stats map for current opponent
     * @return Map containing bot data, or null if not a bot
     */
    public Map<String, Object> getOpponentBotStatsMap() {
        return opponentStats != null ? opponentStats.getBotStatsMap() : null;
    }

    private JsonObject getUserData(String username) throws Exception {
        // Use the public API endpoint (no /api prefix needed, it's already in API_BASE)
        String endpoint = "/user/" + username;
        System.out.println("Fetching from endpoint: " + API_BASE + endpoint);

        HttpURLConnection conn = createConnection(endpoint, "GET");
        int responseCode = conn.getResponseCode();

        System.out.println("Response code: " + responseCode);

        if (responseCode == 200) {
            JsonObject result = gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
            return result;
        } else {
            System.err.println("Failed to fetch user data. Response code: " + responseCode);
            if (conn.getErrorStream() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println(line);
                }
            }
        }

        return null;
    }

    private OpponentStats parseOpponentStats(JsonObject data) {
        String username = data.has("username") ? data.get("username").getAsString() :
                data.has("id") ? data.get("id").getAsString() : "Unknown";

        OpponentStats stats = new OpponentStats(username, oauthToken);

        // Set basic info
        stats.setId(data.has("id") ? data.get("id").getAsString() : username);
        stats.setUsername(username);
        stats.setOnline(data.has("seenAt")); // If seenAt exists, consider online

        // Check if bot
        boolean isBot = data.has("title") && "BOT".equals(data.get("title").getAsString());
        stats.setBot(isBot);
        stats.setTitle(data.has("title") ? data.get("title").getAsString() : null);

        // Parse count data
        if (data.has("count")) {
            JsonObject count = data.getAsJsonObject("count");
            stats.setGames(count.has("all") ? count.get("all").getAsInt() : 0);
            stats.setWins(count.has("win") ? count.get("win").getAsInt() : 0);
            stats.setLosses(count.has("loss") ? count.get("loss").getAsInt() : 0);
            stats.setDraws(count.has("draw") ? count.get("draw").getAsInt() : 0);
        }

        // Parse perfs (ratings) data
        if (data.has("perfs")) {
            JsonObject perfs = data.getAsJsonObject("perfs");

            if (perfs.has("blitz") && perfs.getAsJsonObject("blitz").has("rating")) {
                stats.setBlitzRating(perfs.getAsJsonObject("blitz").get("rating").getAsInt());
            }
            if (perfs.has("rapid") && perfs.getAsJsonObject("rapid").has("rating")) {
                stats.setRapidRating(perfs.getAsJsonObject("rapid").get("rating").getAsInt());
            }
            if (perfs.has("bullet") && perfs.getAsJsonObject("bullet").has("rating")) {
                stats.setBulletRating(perfs.getAsJsonObject("bullet").get("rating").getAsInt());
            }
            if (perfs.has("classical") && perfs.getAsJsonObject("classical").has("rating")) {
                stats.setClassicalRating(perfs.getAsJsonObject("classical").get("rating").getAsInt());
            }

            // Set default rating to first available
            Integer rating = stats.getBlitzRating() != null ? stats.getBlitzRating() :
                    stats.getRapidRating() != null ? stats.getRapidRating() :
                            stats.getBulletRating() != null ? stats.getBulletRating() :
                                    stats.getClassicalRating() != null ? stats.getClassicalRating() : 0;
            stats.setRating(rating);
        }

        return stats;
    }

    // ========== LiChess-Specific Implementation ==========

    private void streamGameState(String gameId) {
        System.out.println("Streaming game: " + gameId);
        try {
            HttpURLConnection conn = createConnection("/bot/game/stream/" + gameId, "GET");
            conn.setReadTimeout(0);

            if (conn.getResponseCode() != 200) {
                System.err.println("Stream failed: " + conn.getResponseCode());
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
            System.out.println("Stream ended");
        }
    }

    private void handleGameEvent(JsonObject event) {
        String type = event.get("type").getAsString();
        System.out.println("Event: " + type);

        switch (type) {
            case "gameFull":
                weAreWhite = isOurColor(event, "white");
                System.out.println("We are: " + (weAreWhite ? "WHITE" : "BLACK"));

                // Fetch opponent stats automatically
                String opponentColor = weAreWhite ? "black" : "white";
                JsonObject opponentPlayer = event.has(opponentColor) ? event.getAsJsonObject(opponentColor) : null;

                if (opponentPlayer != null) {
                    if (opponentPlayer.has("aiLevel")) {
                        // Handle AI opponent
                        int aiLevel = opponentPlayer.get("aiLevel").getAsInt();
                        System.out.println("Opponent: AI Level " + aiLevel);
                        this.opponentStats = createAIOpponentStats(aiLevel);
                    } else if (opponentPlayer.has("id")) {
                        // Handle human opponent
                        String opponentUsername = opponentPlayer.get("id").getAsString();
                        System.out.println("Opponent username: " + opponentUsername);
                        fetchOpponentStats(opponentUsername);
                    }
                }

                if (event.has("state")) {
                    processStateUpdate(event.getAsJsonObject("state"));
                }
                break;
            case "gameState":
                processStateUpdate(event);
                break;
        }
    }

    private OpponentStats createAIOpponentStats(int aiLevel) {
        OpponentStats stats = new OpponentStats("AI Level " + aiLevel, oauthToken);

        stats.setId("ai-level-" + aiLevel);
        stats.setUsername("Stockfish AI");
        stats.setBot(true);
        stats.setTitle("BOT");
        stats.setOnline(true);

        // Estimate rating based on AI level (rough approximation)
        int estimatedRating = 800 + (aiLevel * 250); // Level 1 ≈ 1050, Level 8 ≈ 2800
        stats.setRating(estimatedRating);
        stats.setBlitzRating(estimatedRating);
        stats.setRapidRating(estimatedRating);
        stats.setBulletRating(estimatedRating);
        stats.setClassicalRating(estimatedRating);

        // AI doesn't have game history
        stats.setGames(0);
        stats.setWins(0);
        stats.setLosses(0);
        stats.setDraws(0);

        System.out.println("Created AI opponent stats: Level " + aiLevel + " (~" + estimatedRating + " rating)");
        return stats;
    }

    private void processStateUpdate(JsonObject state) {
        String moves = state.has("moves") ? state.get("moves").getAsString() : "";
        String status = state.get("status").getAsString();
        String winner = state.has("winner") ? state.get("winner").getAsString() : null;

        System.out.println("Moves: " + (moves.isEmpty() ? "none" : moves) + " | Status: " + status);

        updateGameState(moves.isEmpty() ? new String[0] : moves.split(" "));
        handleStatusChange(status, winner);
    }

    private boolean isOurColor(JsonObject event, String color) {
        if (event.has(color)) {
            JsonObject player = event.getAsJsonObject(color);
            if (player.has("id")) {
                return player.get("id").getAsString().equalsIgnoreCase(ourUsername);
            }
        }
        return false;
    }

    private String extractUsername(JsonObject event, String color) {
        if (event.has(color)) {
            JsonObject player = event.getAsJsonObject(color);
            if (player.has("id")) {
                return player.get("id").getAsString();
            }
            // Also check for aiLevel (AI opponents don't have id)
            if (player.has("aiLevel")) {
                int aiLevel = player.get("aiLevel").getAsInt();
                return "AI Level " + aiLevel;
            }
        }
        return null;
    }

    // ========== HTTP Helper Methods ==========

    private HttpURLConnection createConnection(String endpoint, String method) throws Exception {
        URL url = new URL(API_BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + oauthToken);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private JsonObject apiGet(String endpoint) throws Exception {
        HttpURLConnection conn = createConnection(endpoint, "GET");
        if (conn.getResponseCode() == 200) {
            return gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
        }
        return null;
    }

    private JsonObject apiPost(String endpoint, String params) throws Exception {
        HttpURLConnection conn = createConnection(endpoint, "POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes("utf-8"));
        }

        if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201) {
            return gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
        }
        return null;
    }
}