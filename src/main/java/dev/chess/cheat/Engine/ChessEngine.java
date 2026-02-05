package dev.chess.cheat.Engine;

import dev.chess.cheat.Engine.Move.Move;
import dev.chess.cheat.Engine.Search.Algorithm;
import dev.chess.cheat.Simulation.Board;

/**
 * Dependencies
 *
 * {@link Algorithm}
 * {@link dev.chess.cheat.Engine.Quiescence.QuiescenceSearch} for depth extension
 * {@link dev.chess.cheat.Engine.Move.MoveGenerator} and {@link Move} for move logic
 * {@link dev.chess.cheat.Engine.Ordering.MoveOrdering} for handing when we want to do/hold off on an attack/defence
 */
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
