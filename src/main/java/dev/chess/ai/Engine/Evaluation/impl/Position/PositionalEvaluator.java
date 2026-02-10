package dev.chess.ai.Engine.Evaluation.impl.Position;

import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Util.BoardUtils;

public class PositionalEvaluator implements Evaluator {

    public PositionalEvaluator() {

    }

    @Override
    public double evaluate(Board board) {
        double score = 0;

        score += evaluatePieceSquareTables(board);
        score += evaluatePawnStructure(board);
        score += evaluateKingSafety(board);
        score += evaluateMobility(board);

        return score;
    }

    private double evaluatePieceSquareTables(Board board) {
        double score = 0;

        BoardUtils.loopThroughBoard(board, ((row, col) -> {

        }));
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
