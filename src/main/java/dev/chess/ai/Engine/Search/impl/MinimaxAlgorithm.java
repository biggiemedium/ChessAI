package dev.chess.ai.Engine.Search.impl;

import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Search.Algorithm;
import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Simulation.Board;

import java.util.List;

/**
 * Basic Minimax algorithm without pruning
 *
 * https://www.youtube.com/watch?v=l-hh51ncgDI
 */
public class MinimaxAlgorithm extends Algorithm {

    public MinimaxAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        super(evaluator, moveGenerator);
    }

    @Override
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        resetNodeCounter();

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhite);
        if (moves.isEmpty()) {
            return null;
        }

        Move bestMove = null;
        double bestScore = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for(Move move : moves) {
            board.movePiece(move);

            double score = minimax(
                    board,
                    depth - 1,
                    !isWhite
            );

            board.undoMove(move);

            if (isWhite) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }


        return bestMove;
    }

    /**
     * Recursive minimax search
     */
    private double minimax(Board board, int depth, boolean isWhiteTurn) {
        nodesSearched++;

        if (depth == 0) {
            return evaluator.evaluate(board);
        }

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhiteTurn);
        if (moves.isEmpty()) {
            if (moveGenerator.isKingInCheck(board, isWhiteTurn)) {
                // Checkmate
                return isWhiteTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            // Stalemate
            return 0;
        }

        double bestScore = isWhiteTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for(Move move : moves) {

            board.movePiece(move);
            double score = minimax(
                    board,
                    depth - 1,
                    !isWhiteTurn
            );
            board.undoMove(move);

            if (isWhiteTurn) {
                bestScore = Math.max(bestScore, score);
            } else {
                bestScore = Math.min(bestScore, score);
            }

        }
        return bestScore;
    }

    @Override
    public String getName() {
        return "Minimax";
    }
}
