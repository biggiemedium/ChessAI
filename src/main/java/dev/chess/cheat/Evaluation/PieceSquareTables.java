package dev.chess.cheat.Evaluation;

/**
 * Piece-Square Tables for positional evaluation
 * These tables assign bonuses/penalties for pieces on certain squares
 */
public class PieceSquareTables {

    // Pawn table -> encourage center control and advancement
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

    // Knight table -> encourage center control
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

    // TODO: Add tables for BISHOP, ROOK, QUEEN, KING_MIDDLE_GAME, KING_END_GAME

    /**
     * Get piece-square table value for a piece at a position
     *
     * @param pieceType 'p', 'n', 'b', 'r', 'q', 'k'
     * @param row board row (0-7)
     * @param col board column (0-7)
     * @param isWhite true if white piece
     * @return positional bonus/penalty
     */
    public static int getValue(char pieceType, int row, int col, boolean isWhite) {
        // TODO: Flip row for black pieces (they start at top)
        // TODO: Return appropriate table value based on piece type
        return 0;
    }

}
