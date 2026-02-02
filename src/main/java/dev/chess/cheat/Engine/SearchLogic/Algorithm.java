package dev.chess.cheat.Engine.SearchLogic;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

public abstract class Algorithm {

    protected final Evaluator evaluator;
    protected final MoveGenerator moveGenerator;
    protected int nodesSearched;

    public Algorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        this.moveGenerator = moveGenerator;
    }

    /**
     * Find the best move for the current position
     *
     * @param board current board state
     * @param isWhite true if white to move
     * @param depth search depth
     * @return the best move found
     */
    public abstract Move findBestMove(Board board, boolean isWhite, int depth);

    /**
     * Get the name of this algorithm
     */
    public abstract String getName();

    /**
     * Get the number of nodes searched in the last search
     */
    public int getNodesSearched() {
        return nodesSearched;
    }

    /**
     * Reset node counter
     */
    protected void resetNodeCounter() {
        nodesSearched = 0;
    }

}
