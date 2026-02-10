package dev.chess.ai.Engine.Evaluation;

import dev.chess.ai.Simulation.Board;

public interface Evaluator {
    /**
     * Evaluates the board position
     *
     * @return positive score favors white, negative favors black
     */
    double evaluate(Board board);
}
