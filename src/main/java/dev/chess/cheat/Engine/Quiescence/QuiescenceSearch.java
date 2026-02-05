package dev.chess.cheat.Engine.Quiescence;

import dev.chess.cheat.Engine.Evaluation.Evaluator;
import dev.chess.cheat.Engine.Move.Move;
import dev.chess.cheat.Engine.Move.MoveGenerator;
import dev.chess.cheat.Simulation.Board;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * At the end of the main search perform a more limited quiescence search should happen.
 * The purpose of this search is to only evaluate "quiet" positions, or positions where there are no winning tactical moves to be made
 *
 * Example:
 * What if you were to search one move deeper and find that the next move is PxQ?
 * You didn't win a pawn, you actually lost a queen
 *
 * https://www.chessprogramming.org/Quiescence_Search
 */
public class QuiescenceSearch {

    private final MoveGenerator moveGenerator;
    private final Evaluator evaluator;

    public QuiescenceSearch(Evaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        this.moveGenerator = moveGenerator;
    }

    // Lets do some testing...
    // https://chess.stackexchange.com/questions/42613/how-to-improve-performance-of-quiescence-search

    /**
     * Start a search if we are to capture a piece
     *
     * Yes we may capture a piece but will they counter and take our piece (PxQ example above)
     * Use {@link dev.chess.cheat.Engine.Evaluation.Impl.MaterialEvaluator} to assess pieces are willing to sacrificing
     * if sacrificing is something we are okay with doing
     *
     * @param board
     * @param alpha
     * @param beta
     * @return
     */
    public double searchCaptures(Board board, double alpha, double beta, boolean isWhiteTurn) {
        double standardPat = this.evaluator.evaluate(board);

        // opponent wont allow this -> Position is too good
        if (standardPat >= beta) {
            return beta;
        }

        // Delta pruning
        // https://talkchess.com/viewtopic.php?t=80325
        // we can model this as an equation with
        // if(statc_eval + Δ < α) return α
        final double QUEEN_VALUE = 900;
        double bigDelta = QUEEN_VALUE;
        if (standardPat + bigDelta > alpha) {
            alpha = standardPat;
        }

        List<Move> captures = moveGenerator.generateCaptureMoves(board, isWhiteTurn);
        for(Move move : captures) {

            board.movePiece(move);

            // Were going to use a score similar to our search engine
            // This is technically alpha beta pruning search but with depth extension
            double score = (-searchCaptures(board, -alpha, -beta, !isWhiteTurn));

            board.undoMove(move);

            // This is a good move but we anticipate that our opponent
            // won't let us take the piece
            if(score >= beta) {
                return beta;
            }

            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    // Skip obviously bad captures (e.g., QxP when pawn is defended by pawn)
    private boolean isLosingCapture() {
        return false;
    }

}
