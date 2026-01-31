package dev.chess.cheat.Network.Impl;

import dev.chess.cheat.Network.*;
import dev.chess.cheat.Network.Model.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Lichess.org API client (FULL BOT SUPPORT)
 * Supports creating games, making moves, and streaming
 */
public class LichessClient implements ChessClient {

    private static final String BASE_URL = "https://lichess.org/api";
    private final HttpClient httpClient;
    private final Gson gson;
    private String apiToken;

    public LichessClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.gson = new Gson();
    }

    @Override
    public boolean authenticate(String token) {
        this.apiToken = token;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/account"))
                    .header("Authorization", "Bearer " + apiToken)
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
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/account/playing"))
                    .header("Authorization", "Bearer " + apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                List<Game> games = new ArrayList<>();

                if (json.has("nowPlaying")) {
                    json.getAsJsonArray("nowPlaying").forEach(gameElement -> {
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
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/game/" + gameId))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json")
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
        try {
            String url = BASE_URL + "/bot/game/" + gameId + "/move/" + move;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Game createGame(GameConfig config) {
        try {
            String body = String.format(
                    "rated=%s&clock.limit=%d&clock.increment=%d&color=%s",
                    config.isRated(),
                    config.getTimeLimit(),
                    config.getIncrement(),
                    config.getColor()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/challenge/ai"))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                return parseGame(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void streamGameState(String gameId, GameStateListener listener) {
        try {
            String url = BASE_URL + "/bot/game/stream/" + gameId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiToken)
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        response.body().forEach(line -> {
                            if (!line.trim().isEmpty()) {
                                JsonObject json = gson.fromJson(line, JsonObject.class);
                                listener.onGameStateUpdate(parseGameState(json));
                            }
                        });
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean resign(String gameId) {
        try {
            String url = BASE_URL + "/bot/game/" + gameId + "/resign";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supportsBotPlay() {
        return true;
    }

    private Game parseGame(JsonObject json) {
        Game game = new Game();

        if (json.has("id")) {
            game.setId(json.get("id").getAsString());
        } else if (json.has("gameId")) {
            game.setId(json.get("gameId").getAsString());
        }

        game.setPlatform("Lichess");

        if (json.has("fen")) {
            game.setFen(json.get("fen").getAsString());
        }

        if (json.has("speed")) {
            game.setTimeControl(json.get("speed").getAsString());
        }

        return game;
    }

    private GameState parseGameState(JsonObject json) {
        GameState state = new GameState();

        if (json.has("type")) {
            state.setType(json.get("type").getAsString());
        }

        if (json.has("moves")) {
            state.setMoves(json.get("moves").getAsString());
        }

        if (json.has("status")) {
            state.setStatus(json.get("status").getAsString());
        }

        return state;
    }
}