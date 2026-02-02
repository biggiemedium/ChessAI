package dev.chess.cheat.Engine;

import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;

public class ChessEngine {

    private Algorithm currentAlgorithm;

    public ChessEngine(Algorithm algorithm) {
        this.currentAlgorithm = algorithm;
    }

    /**
     * Find the best move using the current algorithm.
     */
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        long startTime = System.currentTimeMillis();

        Move bestMove = currentAlgorithm.findBestMove(board, isWhite, depth);

        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;

        System.out.println("Algorithm: " + currentAlgorithm.getName());
        System.out.println("Nodes searched: " + currentAlgorithm.getNodesSearched());
        System.out.println("Time: " + elapsed + "ms");
        System.out.println("Nodes/sec: " +
                (currentAlgorithm.getNodesSearched() * 1000 / Math.max(elapsed, 1)));

        return bestMove;
    }

    /**
     * Swap the search algorithm.
     */
    public void setAlgorithm(Algorithm algorithm) {
        this.currentAlgorithm = algorithm;
    }

    public Algorithm getCurrentAlgorithm() {
        return currentAlgorithm;
    }

}
