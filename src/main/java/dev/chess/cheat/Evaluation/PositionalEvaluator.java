package dev.chess.cheat.Evaluation;

import dev.chess.cheat.Simulation.Board;

public class PositionalEvaluator implements Evaluator {

    private final MaterialEvaluator materialEvaluator;

    public PositionalEvaluator() {
        this.materialEvaluator = new MaterialEvaluator();
    }

    @Override
    public double evaluate(Board board) {
        double score = 0;

        // TODO: Add material evaluation
        score += materialEvaluator.evaluate(board);

        // TODO: Add piece-square table bonuses
        score += evaluatePieceSquareTables(board);

        // TODO: Add bonuses for:
        // - Pawn structure (doubled pawns, isolated pawns, passed pawns)
        // - King safety (pawn shield, open files near king)
        // - Piece mobility (number of legal moves)
        // - Control of center squares

        return score;
    }

    private double evaluatePieceSquareTables(Board board) {
        double score = 0;

        // TODO: Loop through all pieces
        // TODO: Add piece-square table value for each piece
        // TODO: Use PieceSquareTables.getValue()

        return score;
    }

    private double evaluatePawnStructure(Board board) {
        // TODO: Penalize doubled pawns
        // TODO: Penalize isolated pawns
        // TODO: Bonus for passed pawns
        return 0;
    }

    private double evaluateKingSafety(Board board) {
        // TODO: Bonus for pawns in front of king
        // TODO: Penalty for open files near king
        return 0;
    }

    private double evaluateMobility(Board board) {
        // TODO: Count legal moves for each side
        // TODO: More mobility = better position
        return 0;
    }

}
