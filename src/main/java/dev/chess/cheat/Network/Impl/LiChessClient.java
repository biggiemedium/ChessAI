package dev.chess.cheat.Network.Impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Network.ChessClient;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.Util.Annotation.Value;
import dev.chess.cheat.Util.PropertyLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.*;

public class LiChessClient extends ChessClient {

    private static final String API_BASE = "https://lichess.org/api";

    @Value(key = "LICHESS_API_KEY")
    private String oauthToken;

    private boolean connected;
    private Game currentGame;
    private String currentGameId;
    private ExecutorService executor;
    private volatile boolean streaming;
    private final Gson gson;
    private String ourUsername;
    private boolean weAreWhite;

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
        return true;
    }

    @Override
    public boolean isTimedOut() { return false; }

    @Override
    public Game getCurrentGame() { return currentGame; }

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

    public boolean streamGame(String gameId, ChessEngine engine) {
        if (!connected) return false;

        this.currentGameId = gameId;
        this.currentGame = new Game(new Board(), new ArrayList<>(), engine, this);
        this.currentGame.setGameId(gameId);

        streaming = true;
        executor.submit(() -> streamGameState(gameId));
        return true;
    }

    public void stopStreaming() {
        streaming = false;
    }

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
                // Determine our color
                weAreWhite = isOurColor(event, "white");
                System.out.println("We are: " + (weAreWhite ? "WHITE" : "BLACK"));

                if (event.has("state")) {
                    updateGameState(event.getAsJsonObject("state"));
                }
                break;
            case "gameState":
                updateGameState(event);
                break;
        }
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

    private void updateGameState(JsonObject state) {
        String moves = state.has("moves") ? state.get("moves").getAsString() : "";
        String status = state.get("status").getAsString();

        System.out.println("Moves: " + (moves.isEmpty() ? "none" : moves) + " | Status: " + status);

        if (!moves.isEmpty()) {
            currentGame.updateFromMoves(moves.split(" "));
        } else {
            currentGame.reset();
        }

        currentGame.updateStatus(status, state.has("winner") ? state.get("winner").getAsString() : null);

        // Make move if it's our turn
        if ("started".equals(status) && isOurTurn()) {
            executor.submit(this::calculateAndMakeMove);
        }
    }

    private boolean isOurTurn() {
        return currentGame.isWhiteTurn() == weAreWhite;
    }

    private void calculateAndMakeMove() {
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
                makeMove(currentGameId, uci);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean makeMove(String gameId, String move) {
        try {
            HttpURLConnection conn = createConnection("/bot/game/" + gameId + "/move/" + move, "POST");
            int code = conn.getResponseCode();
            System.out.println(code == 200 ? "✓ Move sent" : "✗ Move rejected: " + code);
            return code == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String moveToUCI(dev.chess.cheat.Engine.Move move) {
        char fromFile = (char) ('a' + move.getFromCol());
        char toFile = (char) ('a' + move.getToCol());
        int fromRank = 8 - move.getFromRow();
        int toRank = 8 - move.getToRow();
        return "" + fromFile + fromRank + toFile + toRank;
    }

    // Helper methods
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

    public boolean isConnected() { return connected; }
}