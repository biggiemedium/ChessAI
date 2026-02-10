package dev.chess.ai.Engine.Evaluation.impl.Material;

import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Piece;

/**
 * Chess piece values indicate the value of the different chess pieces and how they relate to each other.
 * Every piece has different strengths and weaknesses, so they are valued differently
 *
 * Chess Values:
 * https://www.chess.com/terms/chess-piece-value
 *
 * Material Evaluation:
 * https://www.chessprogramming.org/Material
 */
public class MaterialEvaluator implements Evaluator {

    // Standard piece values (centipawns)
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20000;

    @Override
    public double evaluate(Board board) {
        double score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) continue;

                double value = getPieceValue(piece);
                score += piece.isWhite() ? value : -value;
            }
        }

        return score;
    }

    /**
     * Get the material value of a piece
     */
    public int getPieceValue(Piece piece) {
        if (piece == null) return 0;

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
