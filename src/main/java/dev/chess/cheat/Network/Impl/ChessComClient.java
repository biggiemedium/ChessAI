package dev.chess.cheat.Network.Impl;

import dev.chess.cheat.Network.*;
import dev.chess.cheat.Network.Model.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Chess.com API client (READ-ONLY)
 * Note: Chess.com does not support creating games or making moves via API
 */
public class ChessComClient implements ChessClient {

    private static final String BASE_URL = "https://api.chess.com/pub";
    private final HttpClient httpClient;
    private final Gson gson;
    private String username;

    public ChessComClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.gson = new Gson();
    }

    @Override
    public boolean authenticate(String username) {
        this.username = username;
        // Chess.com public API doesn't require auth for read operations
        // Just validate username exists
        try {
            String url = BASE_URL + "/player/" + username;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ChessBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Game> getActiveGames() {
        if (username == null) {
            throw new IllegalStateException("Must authenticate first");
        }

        try {
            String url = BASE_URL + "/player/" + username + "/games";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ChessBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                List<Game> games = new ArrayList<>();

                if (json.has("games")) {
                    json.getAsJsonArray("games").forEach(gameElement -> {
                        games.add(parseGame(gameElement.getAsJsonObject()));
                    });
                }

                return games;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @Override
    public Game getGame(String gameId) {
        // Chess.com uses URLs as game IDs in their API
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gameId))
                    .header("User-Agent", "ChessBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseGame(gson.fromJson(response.body(), JsonObject.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean makeMove(String gameId, String move) {
        throw new UnsupportedOperationException("Chess.com API does not support making moves");
    }

    @Override
    public Game createGame(GameConfig config) {
        throw new UnsupportedOperationException("Chess.com API does not support creating games");
    }

    @Override
    public void streamGameState(String gameId, GameStateListener listener) {
        throw new UnsupportedOperationException("Chess.com API does not support streaming");
    }

    @Override
    public boolean resign(String gameId) {
        throw new UnsupportedOperationException("Chess.com API does not support resignations");
    }

    @Override
    public boolean supportsBotPlay() {
        return false;
    }

    private Game parseGame(JsonObject json) {
        Game game = new Game();
        game.setId(json.has("url") ? json.get("url").getAsString() : "");
        game.setPlatform("Chess.com");

        if (json.has("fen")) {
            game.setFen(json.get("fen").getAsString());
        }

        if (json.has("pgn")) {
            game.setPgn(json.get("pgn").getAsString());
        }

        if (json.has("time_class")) {
            game.setTimeControl(json.get("time_class").getAsString());
        }

        return game;
    }
}