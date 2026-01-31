package dev.chess.cheat.Network;

import dev.chess.cheat.Network.Model.*;
import dev.chess.cheat.Simulation.Board;
import java.net.http.HttpClient;
import com.google.gson.Gson;

/**
 * Abstract base class for chess platform clients
 * Provides common functionality and enforces contract for child implementations
 */
public abstract class ChessClient {

    protected final HttpClient httpClient;
    protected final Gson gson;
    protected String authToken;
    protected Game currentGame;
    protected Board simulationBoard;

    protected ChessClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.gson = new Gson();
        this.simulationBoard = new Board();
    }

    /**
     * Authenticate with the platform
     * @param credentials API token, username, or other auth data
     * @return true if authentication successful
     */
    public abstract boolean authenticate(String credentials);

    /**
     * Get the current active game
     * @return the current game, or null if none active
     */
    public abstract Game getCurrentGame();

    /**
     * Get a specific game by ID and set as current game
     * @param gameId the game identifier
     * @return the game data
     */
    public abstract Game loadGame(String gameId);

    /**
     * Make a move in the current game
     * @param move the move in UCI notation (e.g., "e2e4")
     * @return true if move was successful
     */
    public abstract boolean makeMove(String move);

    /**
     * Create a new game (if supported by platform)
     * @param config game configuration
     * @return the created game
     */
    public abstract Game createGame(GameConfig config);

    /**
     * Stream game state updates for current game
     * @param listener callback for game updates
     */
    public abstract void streamGameState(GameStateListener listener);

    /**
     * Resign from the current game
     * @return true if resignation successful
     */
    public abstract boolean resign();

    /**
     * Check if platform supports bot play (making moves programmatically)
     * @return true if platform allows automated moves
     */
    public abstract boolean supportsBotPlay();

    /**
     * Sync the current game state to the simulation board
     * @return true if sync successful
     */
    public boolean syncToBoard() {
        if (currentGame == null) {
            return false;
        }

        String fen = currentGame.getFen();
        if (fen != null && !fen.isEmpty()) {
            return loadFenToBoard(fen);
        }

        String pgn = currentGame.getPgn();
        if (pgn != null && !pgn.isEmpty()) {
            return loadPgnToBoard(pgn);
        }

        return false;
    }

    /**
     * Get the simulation board linked to this client
     * @return the Board instance
     */
    public Board getBoard() {
        return simulationBoard;
    }

    /**
     * Set a custom board instance
     * @param board the board to use
     */
    public void setBoard(Board board) {
        this.simulationBoard = board;
    }

    /**
     * Reset the simulation board to starting position
     */
    public void resetBoard() {
        simulationBoard.reset();
    }

    /**
     * Load FEN notation into the simulation board
     * @param fen FEN string
     * @return true if successful
     */
    protected abstract boolean loadFenToBoard(String fen);

    /**
     * Load PGN notation into the simulation board
     * @param pgn PGN string
     * @return true if successful
     */
    protected abstract boolean loadPgnToBoard(String pgn);

    /**
     * Get the current game
     * @return current game or null
     */
    public Game getGame() {
        return currentGame;
    }

    /**
     * Check if client is authenticated
     * @return true if authenticated
     */
    public boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    /**
     * Check if a game is currently loaded
     * @return true if game is active
     */
    public boolean hasActiveGame() {
        return currentGame != null;
    }
}