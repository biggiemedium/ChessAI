package dev.chess.cheat.Network;

import dev.chess.cheat.Network.Model.Game;
import dev.chess.cheat.Network.Model.GameConfig;
import dev.chess.cheat.Network.Model.GameStateListener;

import java.util.List;

/**
 * Interface for chess platform clients
 */
public interface ChessClient {

    /**
     * Authenticate with the platform
     * @param token API token or credentials
     * @return true if authentication successful
     */
    boolean authenticate(String token);

    /**
     * Get current active games for the authenticated user
     * @return list of active games
     */
    List<Game> getActiveGames();

    /**
     * Get a specific game by ID
     * @param gameId the game identifier
     * @return the game data
     */
    Game getGame(String gameId);

    /**
     * Make a move in a game
     * @param gameId the game identifier
     * @param move the move in UCI notation (e.g., "e2e4")
     * @return true if move was successful
     */
    boolean makeMove(String gameId, String move);

    /**
     * Create a new game (if supported by platform)
     * @param config game configuration
     * @return the created game
     */
    Game createGame(GameConfig config);

    /**
     * Stream game state updates
     * @param gameId the game identifier
     * @param listener callback for game updates
     */
    void streamGameState(String gameId, GameStateListener listener);

    /**
     * Resign from a game
     * @param gameId the game identifier
     * @return true if resignation successful
     */
    boolean resign(String gameId);

    /**
     * Check if platform supports bot play
     * @return true if platform allows automated moves
     */
    boolean supportsBotPlay();
}
