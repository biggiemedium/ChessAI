package dev.chess.cheat.Evaluation;

import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Piece;

public class MaterialEvaluator implements Evaluator {

    // Standard piece values (centipawns)
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    @Override
    public double evaluate(Board board) {
        double score = 0;

        // TODO: Loop through all squares on the board
        // TODO: For each piece, add its value if white, subtract if black
        // TODO: Use getPieceValue() helper method

        return score;
    }

    /**
     * Get the material value of a piece
     */
    private int getPieceValue(Piece piece) {
        if (piece == null) return 0;

        // TODO: Check piece type using getSymbol()
        // TODO: Return appropriate value (PAWN_VALUE, KNIGHT_VALUE, etc.)

        char symbol = Character.toLowerCase(piece.getSymbol());
        switch (symbol) {
            case 'p': return PAWN_VALUE;
            case 'n': return KNIGHT_VALUE;
            case 'b': return BISHOP_VALUE;
            case 'r': return ROOK_VALUE;
            case 'q': return QUEEN_VALUE;
            case 'k': return KING_VALUE;
            default: return 0;
        }
    }


}
