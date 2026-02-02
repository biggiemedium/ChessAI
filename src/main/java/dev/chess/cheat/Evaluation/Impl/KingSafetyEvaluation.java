package dev.chess.cheat.Evaluation.Impl;

import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

/**
 * Evaluates the safety of the king
 *
 * https://www.chessprogramming.org/King_Safety
 *
 */
public class KingSafetyEvaluation implements Evaluator {

    public KingSafetyEvaluation() {

    }

    @Override
    public double evaluate(Board board) {
        return 0;
    }


}
