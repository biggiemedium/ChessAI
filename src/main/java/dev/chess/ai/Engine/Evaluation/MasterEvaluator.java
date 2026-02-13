package dev.chess.ai.Engine.Evaluation;

import dev.chess.ai.Engine.Evaluation.impl.Material.MaterialEvaluator;
import dev.chess.ai.Engine.Evaluation.impl.Position.PawnPassEvaluator;
import dev.chess.ai.Engine.Evaluation.impl.Position.PieceSquareTables;
import dev.chess.ai.Simulation.Board;

import java.util.ArrayList;
import java.util.List;

public class MasterEvaluator implements Evaluator {

    private final List<Evaluator> evaluators;

    public MasterEvaluator() {
        this.evaluators = new ArrayList<>();

        addEvaluator(new MaterialEvaluator());
        //addEvaluator(new PawnPassEvaluator()); // this breaks shit idk why
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