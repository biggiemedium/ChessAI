package dev.chess.cheat.Network.Model;

/**
 * Callback interface for game state updates
 */
public interface GameStateListener {
    void onGameStateUpdate(GameState state);
    void onGameEnd(String result);
    void onError(String error);
}