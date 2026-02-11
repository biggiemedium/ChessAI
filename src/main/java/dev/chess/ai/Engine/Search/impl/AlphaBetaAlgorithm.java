package dev.chess.ai.Engine.Search.impl;

import dev.chess.ai.Engine.Evaluation.impl.Material.MaterialEvaluator;
import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Quiescence.QuiescenceSearch;
import dev.chess.ai.Engine.Search.Algorithm;
import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Engine.Table.TranspositionTable;
import dev.chess.ai.Engine.Table.TranspositionTableEntry;
import dev.chess.ai.Simulation.Board;

import java.util.List;

/**
 * https://www.youtube.com/watch?v=l-hh51ncgDI
 */
public class AlphaBetaAlgorithm extends Algorithm {

    protected QuiescenceSearch quiescenceSearch;
    private final MaterialEvaluator materialEvaluator;
    private final TranspositionTable transpositionTable;

    // I get the vibe this could be done with a tree
    // but that sounds like it would use an insane amount of RAM
    public AlphaBetaAlgorithm(Evaluator evaluator, MoveGenerator moveGenerator) {
        super(evaluator, moveGenerator);
        this.quiescenceSearch = new QuiescenceSearch(evaluator, moveGenerator);
        this.materialEvaluator = new MaterialEvaluator();
        this.transpositionTable = new TranspositionTable(1_000_000); // 1 million entries
    }

    @Override
    public Move findBestMove(Board board, boolean isWhite, int depth) {
        resetNodeCounter();

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhite);
        if (moves.isEmpty()) {
            return null; // game over -> checkmate / stalemate already happened
        }

        sortMoves(board, moves);

        Move bestMove = null;
        double bestScore = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        // Root node of our "Tree", don't prune here
        // remember this class is basically a tree data structure without nodes or ADT type class
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

    /**
     *  When alpha >= beta -> we can stop early (pruning)
     */
    private double alphaBeta(Board board, int depth, double alpha, double beta, boolean isWhiteTurn) {
        nodesSearched++;

        long zobristHash = board.getZobristHash();
        TranspositionTableEntry entry = transpositionTable.probe(zobristHash);
        if (entry != null && entry.depth >= depth) {
            if (entry.flag == 0) {
                return entry.score;
            } else if (entry.flag == 1) {
                alpha = Math.max(alpha, entry.score);
            } else if (entry.flag == 2) {
                beta = Math.min(beta, entry.score);
            }

            if (alpha >= beta) {
                return entry.score;
            }
        }

        if(depth == 0) {
            //return evaluator.evaluate(board);
            return quiescenceSearch.searchCaptures(board, alpha, beta, isWhiteTurn); // better search
        }

        List<Move> moves = moveGenerator.generateAllMoves(board, isWhiteTurn);
        if (moves.isEmpty()) {
            if (moveGenerator.isKingInCheck(board, isWhiteTurn)) {
                return isWhiteTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            return 0;
        }

        // Filter for good moves first -> finds our cutoff thresholds earlier
        sortMoves(board, moves);

        double originalAlpha = alpha;
        Move bestMove = null;

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

            byte flag = maxScore <= originalAlpha ? (byte) 2 : maxScore >= beta ? (byte) 1 : (byte) 0;
            transpositionTable.store(zobristHash, (int)maxScore, depth, flag, bestMove, (byte)0);

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
            byte flag = minScore <= originalAlpha ? (byte) 2 :
                    minScore >= beta ? (byte) 1 : (byte) 0;
            transpositionTable.store(zobristHash, (int)minScore, depth, flag, bestMove, (byte)0);

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
     */
    private int getMoveOrderingScore(Board board, Move move) {
        int score = 0;

        // if we found this was our best move last time -> try it first
        TranspositionTableEntry entry = transpositionTable.probe(board.getZobristHash());
        if (entry != null && entry.bestMove != null && entry.bestMove.equals(move)) {
            score += 20000; // Highest priority (Descending ?)
        }

        // Prioritize captures -> use MVV-LVA
        if (move.getCapturedPiece() != null) {
            int victimValue = materialEvaluator.getPieceValue(move.getCapturedPiece());
            int attackerValue = materialEvaluator.getPieceValue(board.getPiece(move.getFromRow(), move.getFromCol()));

            score += 10000 + victimValue * 10 - attackerValue;
        }

        return score;
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
