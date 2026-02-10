package dev.chess.ai.Engine.Evaluation.impl.KingSaftey;

import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Simulation.Board;

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
