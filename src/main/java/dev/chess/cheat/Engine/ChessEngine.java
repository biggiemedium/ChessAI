package dev.chess.cheat.Engine;

import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;

public class ChessEngine {

    private Algorithm currentAlgorithm;
    private final Evaluator evaluator;
    private final MoveGenerator moveGenerator;

    public ChessEngine(Evaluator evaluator, MoveGenerator moveGenerator, Algorithm algorithm) {
        this.evaluator = evaluator;
        this.moveGenerator = moveGenerator;
        this.currentAlgorithm = algorithm;
    }

    /**
     * Find the best move using the current algorithm
     */
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        long startTime = System.currentTimeMillis();

        Move bestMove = currentAlgorithm.findBestMove(board, isWhite, depth);

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;

        System.out.println("Algorithm: " + currentAlgorithm.getName());
        System.out.println("Nodes searched: " + currentAlgorithm.getNodesSearched());
        System.out.println("Time: " + timeElapsed + "ms");
        System.out.println("Nodes/sec: " + (currentAlgorithm.getNodesSearched() * 1000 / Math.max(timeElapsed, 1)));

        return bestMove;
    }

    /**
     * Change the search algorithm
     */
    public void setAlgorithm(Algorithm algorithm) {
        this.currentAlgorithm = algorithm;
    }

    /**
     * Get the current algorithm
     */
    public Algorithm getCurrentAlgorithm() {
        return currentAlgorithm;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public MoveGenerator getMoveGenerator() {
        return moveGenerator;
    }

}
