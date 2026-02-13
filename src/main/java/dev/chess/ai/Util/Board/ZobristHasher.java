package dev.chess.ai.Util.Board;

import dev.chess.ai.Simulation.Piece;

import java.util.Random;

/**
 * Moved from {@link dev.chess.ai.Simulation.Board} for simplicity
 *
 * Used to get an almost unique index number for any chess position, with a very important requirement
 * that two similar positions generate entirely different indices. These index numbers are used for faster
 * and more space-efficient Hash tables or databases
 * e.g. transposition tables and opening books.
 *
 * https://www.chessprogramming.org/Zobrist_Hashing
 */
public class ZobristHasher {

    private static final long[][][] PIECE_KEYS = new long[64][12][2]; // [square][pieceType][color]

    static {
        Random rand = new Random(12345);
        for (int sq = 0; sq < 64; sq++) {
            for (int piece = 0; piece < 12; piece++) {
                for (int color = 0; color < 2; color++) {
                    PIECE_KEYS[sq][piece][color] = rand.nextLong();
                }
            }
        }
    }

    public static long computeHash(Piece[][] pieces) {
        long hash = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null) {
                    hash ^= getPieceKey(row, col, piece);
                }
            }
        }
        return hash;
    }

    public static long getPieceKey(int row, int col, Piece piece) {
        int square = row * 8 + col;
        int pieceIndex = getPieceIndex(piece);
        int colorIndex = piece.isWhite() ? 0 : 1;
        return PIECE_KEYS[square][pieceIndex][colorIndex];
    }

    private static int getPieceIndex(Piece piece) {
        char symbol = Character.toLowerCase(piece.getSymbol());
        switch (symbol) {
            case 'p': return 0;
            case 'n': return 1;
            case 'b': return 2;
            case 'r': return 3;
            case 'q': return 4;
            case 'k': return 5;
            default: return 0;
        }
    }
}
