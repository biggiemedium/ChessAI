package dev.chess.ai.Engine.Search;

import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Search.impl.*;
import dev.chess.ai.Engine.Evaluation.Evaluator;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmFactory {

    private final List<AlgorithmTemplate> templates;

    public AlgorithmFactory() {
        this.templates = new ArrayList<>();
        registerAlgorithms();
    }

    private void registerAlgorithms() {
        templates.add(new AlgorithmTemplate("Random", RandomAlgorithm.class));
        templates.add(new AlgorithmTemplate("Minimax", MinimaxAlgorithm.class));
        templates.add(new AlgorithmTemplate("Alpha-Beta", AlphaBetaAlgorithm.class));
    }

    public Algorithm createAlgorithm(String name, Evaluator evaluator, MoveGenerator moveGenerator) {
        for (AlgorithmTemplate template : templates) {
            if (template.name.equals(name)) {
                return template.create(evaluator, moveGenerator);
            }
        }
        throw new IllegalArgumentException("Unknown algorithm: " + name);
    }

    public List<String> getAlgorithmNames() {
        List<String> names = new ArrayList<>();
        for (AlgorithmTemplate template : templates) {
            names.add(template.name);
        }
        return names;
    }

    public int getAlgorithmCount() {
        return templates.size();
    }

    private static class AlgorithmTemplate {
        final String name;
        final Class<? extends Algorithm> algorithmClass;

        AlgorithmTemplate(String name, Class<? extends Algorithm> algorithmClass) {
            this.name = name;
            this.algorithmClass = algorithmClass;
        }

        Algorithm create(Evaluator evaluator, MoveGenerator moveGenerator) {
            try {
                return algorithmClass.getDeclaredConstructor(Evaluator.class, MoveGenerator.class)
                        .newInstance(evaluator, moveGenerator);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create algorithm: " + name, e);
            }
        }
    }
}
