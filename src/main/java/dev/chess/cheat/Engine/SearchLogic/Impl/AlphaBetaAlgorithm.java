package dev.chess.cheat.Engine.SearchLogic.Impl;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;

public class AlphaBetaAlgorithm extends Algorithm {

    public AlphaBetaAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
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
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        for (Move move : moves) {
            board.movePiece(move);

            double score = alphaBeta(
                    board,
                    depth - 1,
                    alpha,
                    beta,
                    !isWhite
            );

            board.undoMove(move);

            if (isWhite) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore);
            }
        }

        return bestMove;
    }

    private double alphaBeta(Board board, int depth, double alpha, double beta, boolean isWhiteTurn) {
        nodesSearched++;

        if (depth == 0) {
            return evaluator.evaluate(board);
        }

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhiteTurn);
        if (moves.isEmpty()) {
            if (moveGenerator.isKingInCheck(board, isWhiteTurn)) {
                return isWhiteTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            return 0;
        }

        if (isWhiteTurn) {
            double maxScore = Double.NEGATIVE_INFINITY;
            for (Move move : moves) {
                board.movePiece(move);
                double score = alphaBeta(board, depth - 1, alpha, beta, false);
                board.undoMove(move);

                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);

                if (beta <= alpha) {
                    break;
                }
            }
            return maxScore;
        } else {
            double minScore = Double.POSITIVE_INFINITY;
            for (Move move : moves) {
                board.movePiece(move);
                double score = alphaBeta(board, depth - 1, alpha, beta, true);
                board.undoMove(move);

                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);

                if (beta <= alpha) {
                    break;
                }
            }
            return minScore;
        }
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
