package dev.chess.cheat.Util;

import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Impl.*;
import dev.chess.cheat.Simulation.Piece;
import dev.chess.cheat.Util.Annotation.BoardConsumer;

public class BoardUtils {

    public static void loopThroughBoard(Board board, BoardConsumer action) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                action.accept(row, col);
            }
        }
    }

    /**
     * Convert the board into FEN notation.
     * Assumes board.getPiece(row, col) exists and pieces have isWhite() and type.
     */
    public static String toFEN(Board board, boolean isWhiteTurn) {
        StringBuilder fen = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(getFENChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 7) {
                fen.append('/');
            }
        }

        // Active color
        fen.append(' ').append(isWhiteTurn ? 'w' : 'b');

        // Castling rights placeholder
        fen.append(" KQkq"); // Update if your Board tracks castling

        // En passant target square placeholder
        fen.append(" -");

        // Halfmove clock placeholder
        fen.append(" 0");

        // Fullmove number placeholder
        fen.append(" 1");

        return fen.toString();
    }

    /**
     * Convert a Piece to its FEN character
     */
    private static char getFENChar(Piece piece) {
        char c;
        if (piece instanceof Pawn) c = 'p';
        else if (piece instanceof Knight) c = 'n';
        else if (piece instanceof Bishop) c = 'b';
        else if (piece instanceof Rook) c = 'r';
        else if (piece instanceof Queen) c = 'q';
        else if (piece instanceof King) c = 'k';
        else c = '?';

        return piece.isWhite() ? Character.toUpperCase(c) : c;
    }
}
