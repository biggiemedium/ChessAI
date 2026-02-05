package dev.chess.cheat.Engine.Quiescence;

import dev.chess.cheat.Engine.Evaluation.Evaluator;
import dev.chess.cheat.Engine.Evaluation.Impl.MaterialEvaluator;
import dev.chess.cheat.Engine.Move.Move;
import dev.chess.cheat.Engine.Move.MoveGenerator;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Piece;

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
    private final MaterialEvaluator materialEvaluator;

    public QuiescenceSearch(Evaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        this.moveGenerator = moveGenerator;
        this.materialEvaluator = new MaterialEvaluator();
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
     * Worst-case: O(N^d) where N is number of captures per position and d is quiescence depth.
     *
     * @param board
     * @param alpha
     * @param beta
     * @return
     */
    public double searchCaptures(Board board, double alpha, double beta, boolean isWhiteTurn) {
        double standardPat = this.evaluator.evaluate(board);

        // Position is already too good for us -> no need to explore
        if (standardPat >= beta) {
            return beta;
        }

        if (standardPat > alpha) {
            alpha = standardPat;
        }

        // Delta pruning -> only continue if a capture could improve alpha
        // https://talkchess.com/viewtopic.php?t=80325
        // we can model this as an equation with
        // if(statc_eval + Δ < α) return α
        if (standardPat + getBigDelta() + 200 < alpha) {
            return alpha;
        }

        List<Move> captures = moveGenerator.generateCaptureMoves(board, isWhiteTurn);
        // Sorting for effectiveness
        captures.sort((m1, m2) -> {
            return Integer.compare(
                    (materialEvaluator.getPieceValue(m2.getCapturedPiece()) * 10
                            - materialEvaluator.getPieceValue(board.getPiece(m2.getFromRow(), m2.getFromCol()))),
                    (materialEvaluator.getPieceValue(m1.getCapturedPiece()) * 10
                            - materialEvaluator.getPieceValue(board.getPiece(m1.getFromRow(), m1.getFromCol())))
            );
        });

        for(Move move : captures) {

            // skip bad captures
            if(isLosingCapture(board, move, isWhiteTurn)) {
                continue;
            }

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

    /**
     * The value of the piece that has the highest value on our board
     * {@link dev.chess.cheat.Engine.Evaluation.Impl.MaterialEvaluator#QUEEN_VALUE}
     * @return Big Delta
     */
    private double getBigDelta() {
        //final double QUEEN_VALUE = 900;
        //double bigDelta = QUEEN_VALUE;
        return MaterialEvaluator.QUEEN_VALUE;
    }

    // Skip obviously bad captures (e.g., QxP when pawn is defended by pawn)
    private boolean isLosingCapture(Board board, Move move, boolean byWhite) {
        if(move.getCapturedPiece() == null) {
            return false; // not a capture piece
        }

        Piece attacker = board.getPiece(move.getFromRow(), move.getFromCol());
        int attackValue = this.materialEvaluator.getPieceValue(attacker);
        int sacrificeValue = this.materialEvaluator.getPieceValue(move.getCapturedPiece());

        // Only check if attacker is MORE valuable than victim
        if(attackValue <= sacrificeValue) {
            return false; // Equal or favorable trade -> keep
        }

        if(attackValue > sacrificeValue + 200) {
            //board.movePiece(move);

            // store as local for now
            // wait nvm we need this
            Piece[][] grid = board.getPieces();
            for(int row = 0; row < 8; row++) {
                for(int col = 0; col < 8; col++) {
                    Piece piece = grid[row][col];
                    if (piece == null || piece.isWhite() == byWhite) { // can't attack ourselves
                        continue;
                    }
                    if (row == move.getToRow() && col == move.getToCol()) { // can't defend ourselves
                        continue;
                    }
                    // Check if an enemy can attack the target square
                    if (piece.isValidMove(row, col, move.getToRow(), move.getToCol(), grid)) {
                        return true;
                    }

                }
            }
            //board.undoMove(move);
        }

        return false;
    }

}
