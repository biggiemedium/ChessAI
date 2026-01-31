package dev.chess.cheat.Network.Model;

public interface GameStateListener {
    void onGameStateUpdate(GameState state);

    void onGameEnd(String result);

    void onError(String error);
}