package dev.chess.cheat.Network;

import dev.chess.cheat.Simulation.Game;

public abstract class ChessClient {

    public ChessClient() {

    }

    public abstract boolean establishConnection();

    public abstract boolean closeConnection();

    public abstract boolean isTimedOut();

    /**
     * Get the current active game
     *
     * @return the current game, or null if none active
     */
    public abstract Game getCurrentGame();
}
