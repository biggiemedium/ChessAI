package dev.chess.cheat.Engine.Evaluation.Impl;

import dev.chess.cheat.Engine.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Piece;

/**
 * Piece-Square Tables for positional evaluation
 * These tables assign bonuses/penalties for pieces on certain squares
 */
public class PieceSquareTables implements Evaluator {

    public PieceSquareTables() {

    }

    @Override
    public double evaluate(Board board) {
        return evaluatePieceSquareTables(board);
    }

    private double evaluatePieceSquareTables(Board board) {
        double score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    score += this.getValue(board, piece.getSymbol(), row, col, piece.isWhite());
                }
            }
        }
        return score;
    }

    /**
     * Get piece-square table value for a piece at a position
     *
     * @param pieceType 'p', 'n', 'b', 'r', 'q', 'k'
     * @param row board row (0-7)
     * @param col board column (0-7)
     * @param isWhite true if white piece
     * @return positional bonus/penalty
     */
    private int getValue(Board board, char pieceType, int row, int col, boolean isWhite) {
        if (!isWhite) {
            row = 7 - row; // flip the table for black pieces
        }

        switch (Character.toLowerCase(pieceType)) {
            case 'p': return PAWN_TABLE[row][col];
            case 'n': return KNIGHT_TABLE[row][col];
            case 'b': return BISHOP_TABLE[row][col];
            case 'r': return ROOK_TABLE[row][col];
            case 'q': return QUEEN_TABLE[row][col];
            case 'k': {
                if(board.getPieces().length < 10) {
                    return KING_TABLE_ENDGAME[row][col];
                } else {
                    return KING_TABLE[row][col];
                }
            }
            default: return 0;
        }
    }

    public static final int[][] PAWN_TABLE = {
            {  0,   0,   0,   0,   0,   0,   0,   0 },
            { 50,  50,  50,  50,  50,  50,  50,  50 },
            { 10,  10,  20,  30,  30,  20,  10,  10 },
            {  5,   5,  10,  25,  25,  10,   5,   5 },
            {  0,   0,   0,  20,  20,   0,   0,   0 },
            {  5,  -5, -10,   0,   0, -10,  -5,   5 },
            {  5,  10,  10, -20, -20,  10,  10,   5 },
            {  0,   0,   0,   0,   0,   0,   0,   0 }
    };
    public static final int[][] KNIGHT_TABLE = {
            { -50, -40, -30, -30, -30, -30, -40, -50 },
            { -40, -20,   0,   0,   0,   0, -20, -40 },
            { -30,   0,  10,  15,  15,  10,   0, -30 },
            { -30,   5,  15,  20,  20,  15,   5, -30 },
            { -30,   0,  15,  20,  20,  15,   0, -30 },
            { -30,   5,  10,  15,  15,  10,   5, -30 },
            { -40, -20,   0,   5,   5,   0, -20, -40 },
            { -50, -40, -30, -30, -30, -30, -40, -50 }
    };

    private static int[][] BISHOP_TABLE = {
                    {-20,-10,-10,-10,-10,-10,-10,-20} ,
                    {-10,  0,  0,  0,  0,  0,  0,-10},
                    {-10,  0,  5, 10, 10,  5,  0,-10},
                    {-10,  5,  5, 10, 10,  5,  5,-10},
                    {-10,  0, 10, 10, 10, 10,  0,-10},
                    {-10, 10, 10, 10, 10, 10, 10,-10},
                    {-10,  5,  0,  0,  0,  0,  5,-10},
                    {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    private static int[][] ROOK_TABLE = {
                    {0,  0,  0,  0,  0,  0,  0,  0},
                    {5, 10, 10, 10, 10, 10, 10,  5},
                    {-5,  0,  0,  0,  0,  0,  0, -5},
                    {-5,  0,  0,  0,  0,  0,  0, -5},
                    {-5,  0,  0,  0,  0,  0,  0, -5},
                    {-5,  0,  0,  0,  0,  0,  0, -5},
                    {-5,  0,  0,  0,  0,  0,  0, -5},
                    {0,  0,  0,  5,  5,  0,  0,  0},
            };

    private static int[][] QUEEN_TABLE =
            {
                    {-20,-10,-10, -5, -5,-10,-10,-20},
                    {-10,  0,  0,  0,  0,  0,  0,-10},
                    {-10,  0,  5,  5,  5,  5,  0,-10},
                    {-5,  0,  5,  5,  5,  5,  0, -5},
                    {0,  0,  5,  5,  5,  5,  0, -5},
                    {-10,  5,  5,  5,  5,  5,  0,-10},
                    {-10,  0,  5,  0,  0,  0,  0,-10},
                    {-20,-10,-10, -5, -5,-10,-10,-20} ,
            };


    private static int[][] KING_TABLE =
            {
                    {-30,-40,-40,-50,-50,-40,-40,-30},
                    {-30,-40,-40,-50,-50,-40,-40,-30},
                    {-30,-40,-40,-50,-50,-40,-40,-30},
                    {-30,-40,-40,-50,-50,-40,-40,-30},
                    {-20,-30,-30,-40,-40,-30,-30,-20},
                    {-10,-20,-20,-20,-20,-20,-20,-10},
                    {20, 20,  0,  0,  0,  0, 20, 20},
                    {20, 30, 10,  0,  0, 10, 30, 20},
            };


    private static int[][] KING_TABLE_ENDGAME =
            {
                    {-50,-40,-30,-20,-20,-30,-40,-50},
                    {-30,-20,-10,  0,  0,-10,-20,-30},
                    {-30,-10, 20, 30, 30, 20,-10,-30},
                    {-30,-10, 30, 40, 40, 30,-10,-30},
                    {-30,-10, 30, 40, 40, 30,-10,-30},
                    {-30,-10, 20, 30, 30, 20,-10,-30},
                    {-30,-30,  0,  0,  0,  0,-30,-30},
                    {-50,-30,-30,-30,-30,-30,-30,-50},
            };

}
