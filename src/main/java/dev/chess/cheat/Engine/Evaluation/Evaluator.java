package dev.chess.cheat.Engine.Evaluation;

import dev.chess.cheat.Simulation.Board;

public interface Evaluator {
    /**
     * Evaluates the board position
     *
     * @return positive score favors white, negative favors black
     */
    double evaluate(Board board);
}
