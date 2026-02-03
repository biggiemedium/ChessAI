package dev.chess.cheat.Network;

import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Simulation.Game;

import java.util.List;

public abstract class ChessClient {

    protected Game currentGame;
    protected String currentGameId;
    protected boolean connected;
    protected String ourUsername;
    protected boolean weAreWhite;

    public ChessClient() {
    }

    // ========== Connection Management ==========

    /**
     * Establish connection to the chess platform
     * @return true if connection successful
     */
    public abstract boolean establishConnection();

    /**
     * Close connection to the chess platform
     * @return true if closed successfully
     */
    public abstract boolean closeConnection();

    /**
     * Check if connection has timed out
     * @return true if timed out
     */
    public abstract boolean isTimedOut();

    /**
     * Check if currently connected
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    // ========== Game State Management ==========

    /**
     * Get the current active game
     * @return the current game, or null if none active
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Get the current game ID
     * @return the game ID, or null if none active
     */
    public String getCurrentGameId() {
        return currentGameId;
    }

    /**
     * Check if it's our turn to move
     * @return true if it's our turn
     */
    public boolean isOurTurn() {
        if (currentGame == null) return false;
        return currentGame.isWhiteTurn() == weAreWhite;
    }

    /**
     * Check what color we are playing
     * @return true if we are white, false if black
     */
    public boolean areWeWhite() {
        return weAreWhite;
    }

    /**
     * Get our username on the platform
     * @return username
     */
    public String getOurUsername() {
        return ourUsername;
    }

    // ========== Game Actions ==========

    /**
     * Start streaming/monitoring a game
     * @param gameId the game to stream
     * @param engine the chess engine to use
     * @return true if streaming started successfully
     */
    public abstract boolean streamGame(String gameId, ChessEngine engine);

    /**
     * Stop streaming the current game
     */
    public abstract void stopStreaming();

    /**
     * Make a move in the current game
     *
     * @param move the move in UCI format (e.g., "e2e4")
     * @return true if move was accepted
     */
    public abstract boolean makeMove(String move);

    /**
     * Calculate the best move using the engine and execute it
     */
    public abstract void calculateAndMakeMove();

    /**
     * Update the game state from move notation
     * @param moves array of moves in the platform's format
     */
    protected abstract void updateGameState(String[] moves);

    /**
     * Handle game status changes (started, finished, etc.)
     * @param status the new status
     * @param winner the winner if game is over (can be null)
     */
    protected abstract void handleStatusChange(String status, String winner);

    public abstract boolean sendMessage(String message);

    public abstract List<String> getGameChat();

    // ========== Challenge/Game Creation ==========

    /**
     * Challenge an AI opponent
     * @param level AI difficulty level
     * @param timeMinutes time control in minutes
     * @param increment increment in seconds
     * @return game ID if successful, null otherwise
     */
    public abstract String challengeAI(int level, int timeMinutes, int increment);

    public abstract String challengePlayer(String gameId);

    // ========== Utility Methods ==========

    /**
     * Convert internal move representation to UCI format
     * @param move the move object
     * @return UCI string (e.g., "e2e4")
     */
    protected abstract String moveToUCI(dev.chess.cheat.Engine.Move move);

    /**
     * Reset the current game state
     */
    protected void resetGameState() {
        currentGame = null;
        currentGameId = null;
        weAreWhite = false;
    }
}
