package dev.chess.cheat.Network;

import dev.chess.cheat.Network.Impl.*;

/**
 * Factory for creating chess platform clients
 */
public class ClientFactory {

    public enum Platform {
        CHESS_COM,
        LICHESS
    }

    public static ChessClient createClient(Platform platform) {
        switch (platform) {
            case CHESS_COM:
                return new ChessComClient();
            case LICHESS:
                return new LichessClient();
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }
}