package dev.chess.cheat.Engine.SearchLogic.Impl;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;
import java.util.Random;

/**
 * Algorithm that uses Java.util.Random to dictate moves
 */
public class RandomAlgorithm extends Algorithm {

    private final Random random = new Random();

    public RandomAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        super(evaluator, moveGenerator);
    }

    @Override
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        resetNodeCounter();

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhite);
        if (moves.isEmpty()) {
            return null;
        }

        // literally just doing a random legal move...
        nodesSearched = moves.size();
        return moves.get(random.nextInt(moves.size()));
    }

    @Override
    public String getName() {
        return "Random";
    }
}
