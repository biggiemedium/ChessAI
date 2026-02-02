package dev.chess.cheat.Engine.SearchLogic.Impl;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;

/**
 * Basic Minimax algorithm without pruning
 *
 * Good for learning -> *slow for deep searches*
 */
public class MinimaxAlgorithm extends Algorithm {

    public MinimaxAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        super(evaluator, moveGenerator);
    }

    @Override
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        resetNodeCounter();
        List<Move> moves = moveGenerator.generateAllMoves(board, isWhite);

        if (moves.isEmpty()) {
            return null;
        }

        Move bestMove = null;
        double bestScore = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        // TODO: For each move
        // TODO: Make the move
        // TODO: Evaluate with minimax
        // TODO: Undo the move
        // TODO: Track best move

        return bestMove;
    }

    /**
     * Recursive minimax search
     */
    private double minimax(Board board, int depth, boolean isMaximizing) {
        nodesSearched++;

        // TODO: Base case - evaluate position at depth 0

        // TODO: Maximizing player (white)
        // TODO: Minimizing player (black)

        return 0;
    }

    @Override
    public String getName() {
        return "Minimax";
    }
}
