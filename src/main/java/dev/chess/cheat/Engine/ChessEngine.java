package dev.chess.cheat.Engine;

import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

public class ChessEngine {

    private final Evaluator evaluator;
    private final MoveGenerator moveGenerator;

    public ChessEngine(Evaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        this.moveGenerator = moveGenerator;
    }

    public Move findBestMove(Board board, boolean isWhite, int depth) {
        return null;
    }

    private double minimax(Board board, int depth, boolean isMaximizing) {
        return 0;
    }

}
