package dev.chess.cheat.Engine.Evaluation;

import dev.chess.cheat.Engine.Evaluation.Impl.KingSafetyEvaluation;
import dev.chess.cheat.Engine.Evaluation.Impl.MaterialEvaluator;
import dev.chess.cheat.Engine.Evaluation.Impl.PieceSquareTables;
import dev.chess.cheat.Engine.Evaluation.Impl.PositionalEvaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.ArrayList;
import java.util.List;

public class MasterEvaluator implements Evaluator{

    private final List<Evaluator> evaluators;

    public MasterEvaluator() {
        this.evaluators = new ArrayList<>();

        addEvaluator(new MaterialEvaluator());
        addEvaluator(new PositionalEvaluator());
        addEvaluator(new PieceSquareTables());
       // addEvaluator(new KingSafetyEvaluation());
    }

    public MasterEvaluator(List<Evaluator> evaluators) {
        this.evaluators = new ArrayList<>(evaluators);
    }

    public void addEvaluator(Evaluator evaluator) {
        evaluators.add(evaluator);
    }

    @Override
    public double evaluate(Board board) {
        double score = 0;
        for (Evaluator e : evaluators) {
            score += e.evaluate(board);
        }
        return score;
    }
}
