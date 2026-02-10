package dev.chess.ai.Engine.Search.impl;

import dev.chess.ai.Engine.Evaluation.impl.Material.MaterialEvaluator;
import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Quiescence.QuiescenceSearch;
import dev.chess.ai.Engine.Search.Algorithm;
import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Simulation.Board;

import java.util.List;

/**
 * https://www.youtube.com/watch?v=l-hh51ncgDI
 */
public class AlphaBetaAlgorithm extends Algorithm {

    protected QuiescenceSearch quiescenceSearch;
    private final MaterialEvaluator materialEvaluator;

    // I get the vibe this could be done with a tree
    // but that sounds like it would use an insane amount of RAM
    public AlphaBetaAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        super(evaluator, moveGenerator);
        this.quiescenceSearch = new QuiescenceSearch(evaluator, moveGenerator);
        this.materialEvaluator = new MaterialEvaluator();
    }

    @Override
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        resetNodeCounter();

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhite);
        if (moves.isEmpty()) {
            return null;
        }

        sortMoves(board, moves);

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
            //return evaluator.evaluate(board);
            return quiescenceSearch.searchCaptures(board, alpha, beta, isWhiteTurn);
        }

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhiteTurn);
        if (moves.isEmpty()) {
            if (moveGenerator.isKingInCheck(board, isWhiteTurn)) {
                return isWhiteTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            return 0;
        }

        sortMoves(board, moves);

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

    /**
     * Sort moves to improve alpha-beta pruning efficiency
     * Prioritizes captures using MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
     *
     * @param board current board state
     * @param moves list of moves to sort (modified in place)
     */
    private void sortMoves(Board board, List<Move> moves) {
        moves.sort((m1, m2) -> {
            int score1 = getMoveOrderingScore(board, m1);
            int score2 = getMoveOrderingScore(board, m2);

            return Integer.compare(score2, score1); // Higher score first
        });
    }

    /**
     * Calculate a heuristic score for move ordering
     * Higher scores are searched first to maximize alpha-beta cutoffs
     *
     * @param board current board state
     * @param move the move to score
     * @return heuristic score for ordering
     */
    private int getMoveOrderingScore(Board board, Move move) {
        int score = 0;

        // Prioritize captures -> use MVV-LVA
        if (move.getCapturedPiece() != null) {
            int victimValue = materialEvaluator.getPieceValue(move.getCapturedPiece());
            int attackerValue = materialEvaluator.getPieceValue(
                    board.getPiece(move.getFromRow(), move.getFromCol())
            );

            // Most Valuable Victim - Least Valuable Attacker
            // Multiply victim by 10 to prioritize capture value over attacker value
            score += victimValue * 10 - attackerValue;
        }

        // TODO: Add bonus for checks, promotions, castling, etc.

        return score;
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
