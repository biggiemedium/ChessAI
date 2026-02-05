package dev.chess.cheat.Engine.Search.Impl;

import dev.chess.cheat.Engine.Move.Move;
import dev.chess.cheat.Engine.Move.MoveGenerator;
import dev.chess.cheat.Engine.Quiescence.QuiescenceSearch;
import dev.chess.cheat.Engine.Search.Algorithm;
import dev.chess.cheat.Engine.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;

/**
 * https://www.youtube.com/watch?v=l-hh51ncgDI
 */
public class AlphaBetaAlgorithm extends Algorithm {

    protected QuiescenceSearch quiescenceSearch;

    // I get the vibe this could be done with a tree
    // but that sounds like it would use an insane amount of RAM
    public AlphaBetaAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        super(evaluator, moveGenerator);
        this.quiescenceSearch = new QuiescenceSearch(evaluator, moveGenerator);
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

            // Maximizing player (White)
            if (isWhite) { // Starting at -infinity (max eval)
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else { // Minimizing player (Black)
                // starting at infinity (minimum eval)
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

        if(depth == 0) {
            // return evaluator.evaluate(board);
            return quiescenceSearch.searchCaptures(board, alpha, beta, isWhiteTurn);
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
