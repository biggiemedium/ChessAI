package dev.chess.cheat.Network.Impl;

import dev.chess.cheat.Network.*;
import dev.chess.cheat.Network.Model.*;
import dev.chess.cheat.Simulation.Piece;
import dev.chess.cheat.Simulation.Impl.*;
import java.net.URI;
import java.net.http.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
 * Chess.com API client implementation
 *
 * Chess.com does not provide an API
 * We must match their websocket or anticheat will flag our client
 */
public class ChessComClient extends ChessClient {

    private static final String BASE_URL = "https://api.chess.com/pub";
    private String username;

    public ChessComClient() {
        super();
    }

    @Override
    public boolean authenticate(String username) {
        this.username = username;
        this.authToken = username;

        try {
            String url = BASE_URL + "/player/" + username;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Chess/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Game getCurrentGame() {
        if (!isAuthenticated()) {
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

                if (json.has("games")) {
                    JsonArray games = json.getAsJsonArray("games");
                    if (games.size() > 0) {
                        currentGame = parseGame(games.get(0).getAsJsonObject());
                        syncToBoard();
                        return currentGame;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get current game: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Game loadGame(String gameId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gameId))
                    .header("User-Agent", "ChessBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                currentGame = parseGame(gson.fromJson(response.body(), JsonObject.class));
                syncToBoard();
                return currentGame;
            }
        } catch (Exception e) {
            System.err.println("Failed to load game: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean makeMove(String move) {
        throw new UnsupportedOperationException(
                "Chess.com API does not support making moves. Consider using Lichess for bot play."
        );
    }

    @Override
    public Game createGame(GameConfig config) {
        throw new UnsupportedOperationException(
                "Chess.com API does not support creating games. Consider using Lichess for bot play."
        );
    }

    @Override
    public void streamGameState(GameStateListener listener) {
        throw new UnsupportedOperationException(
                "Chess.com API does not support streaming. Consider using Lichess for bot play."
        );
    }

    @Override
    public boolean resign() {
        throw new UnsupportedOperationException(
                "Chess.com API does not support resignations. Consider using Lichess for bot play."
        );
    }

    @Override
    public boolean supportsBotPlay() {
        return false;
    }

    @Override
    protected boolean loadFenToBoard(String fen) {
        try {
            String[] parts = fen.split(" ");
            String position = parts[0];

            simulationBoard.clear();

            String[] ranks = position.split("/");
            for (int row = 0; row < 8; row++) {
                int col = 0;
                for (char c : ranks[row].toCharArray()) {
                    if (Character.isDigit(c)) {
                        col += Character.getNumericValue(c);
                    } else {
                        Piece piece = createPieceFromFen(c);
                        if (piece != null) {
                            simulationBoard.setPiece(row, col, piece);
                        }
                        col++;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Failed to load FEN: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean loadPgnToBoard(String pgn) {
        // TODO: Implement full PGN parser
        // For now -> reset to starting position
        simulationBoard.reset();
        return true;
    }

    private Game parseGame(JsonObject json) {
        Game game = new Game();
        game.setPlatform("Chess.com");

        if (json.has("url")) {
            game.setId(json.get("url").getAsString());
        }

        if (json.has("fen")) {
            game.setFen(json.get("fen").getAsString());
        }

        if (json.has("pgn")) {
            game.setPgn(json.get("pgn").getAsString());
        }

        if (json.has("time_class")) {
            game.setTimeControl(json.get("time_class").getAsString());
        }

        if (json.has("white")) {
            JsonObject white = json.getAsJsonObject("white");
            if (white.has("username")) {
                game.setWhitePlayer(white.get("username").getAsString());
            }
        }

        if (json.has("black")) {
            JsonObject black = json.getAsJsonObject("black");
            if (black.has("username")) {
                game.setBlackPlayer(black.get("username").getAsString());
            }
        }

        return game;
    }

    private Piece createPieceFromFen(char c) {
        boolean isWhite = Character.isUpperCase(c);
        char piece = Character.toLowerCase(c);

        switch (piece) {
            case 'p': return new Pawn(isWhite);
            case 'r': return new Rook(isWhite);
            case 'n': return new Knight(isWhite);
            case 'b': return new Bishop(isWhite);
            case 'q': return new Queen(isWhite);
            case 'k': return new King(isWhite);
            default: return null;
        }
    }
}