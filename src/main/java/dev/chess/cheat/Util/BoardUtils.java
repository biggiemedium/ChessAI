package dev.chess.cheat.Util;

import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Util.Annotation.BoardConsumer;

public class BoardUtils {

    public static void loopThroughBoard(Board board, BoardConsumer action) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                action.accept(row, col);
            }
        }
    }

}
