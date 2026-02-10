package dev.chess.ai.Engine.Quiescence;

import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Engine.Evaluation.impl.Material.MaterialEvaluator;
import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Piece;

import java.util.List;

/**
 * At the end of the main search perform a more limited quiescence search should happen.
 * The purpose of this search is to only evaluate "quiet" positions, or positions where there are no winning tactical moves to be made
 * <p>
 * Example:
 * What if you were to search one move deeper and find that the next move is PxQ?
 * You didn't win a pawn, you actually lost a queen
 * <p>
 * https://www.chessprogramming.org/Quiescence_Search
 */
public class QuiescenceSearch {

    private final MoveGenerator moveGenerator;
    private final Evaluator evaluator;
    private final MaterialEvaluator materialEvaluator;

    // Maximum depth for quiescence search to prevent infinite recursion
    private static final int MAX_QUIESCENCE_DEPTH = 10;

    public QuiescenceSearch(Evaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        this.moveGenerator = moveGenerator;
        this.materialEvaluator = new MaterialEvaluator();
    }

    /**
     * Entry point for quiescence search
     */
    public double searchCaptures(Board board, double alpha, double beta, boolean isWhiteTurn) {
        return searchCaptures(board, alpha, beta, isWhiteTurn, MAX_QUIESCENCE_DEPTH);
    }

    // Lets do some testing...
    // https://chess.stackexchange.com/questions/42613/how-to-improve-performance-of-quiescence-search

    /**
     * Start a search if we are to capture a piece
     * <p>
     * Yes we may capture a piece but will they counter and take our piece (PxQ example above)
     * Use {@link MaterialEvaluator} to assess pieces are willing to sacrificing
     * if sacrificing is something we are okay with doing
     * <p>
     * Worst-case: O(N^d) where N is number of captures per position and d is quiescence depth.
     *
     * @param board
     * @param alpha
     * @param beta
     * @return
     */
    private double searchCaptures(Board board, double alpha, double beta, boolean isWhiteTurn, int depth) {

        // fix explosions
        if (depth <= 0) {
            return this.evaluator.evaluate(board);
        }

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

        for (Move move : captures) {

            // skip bad captures
            // "If I take this -> and it’s obviously defended by something cheaper -> don’t even try"
            if (isLosingCapture(board, move, isWhiteTurn)) {
                continue;
            }

            board.movePiece(move);

            // Were going to use a score similar to our search engine
            // This is technically alpha beta pruning search but with depth extension
            double score = searchCaptures(board, alpha, beta, !isWhiteTurn, depth - 1);

            board.undoMove(move);

            // This is a good move but we anticipate that our opponent
            // won't let us take the piece
            if (score >= beta) {
                return beta;
            }

            // Update alpha
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    /**
     * The value of the piece that has the highest value on our board
     * {@link MaterialEvaluator#QUEEN_VALUE}
     *
     * @return Big Delta
     */
    private double getBigDelta() {
        //final double QUEEN_VALUE = 900;
        //double bigDelta = QUEEN_VALUE;
        return MaterialEvaluator.QUEEN_VALUE;
    }

    /**
     *
     * Example: QxP defended by pawn -> we lose 900-100 = 800 material -> skip this capture
     *
     * @param board the current board state
     * @param move the capture move to evaluate
     * @param byWhite true if white is making the capture
     * @return true if the capture loses material, false otherwise
     * @return
     */
    // Skip obviously bad captures (e.g., QxP when pawn is defended by pawn)
    /// improved method -> calling {@link Piece#isValidMove} turns our program to O(64 x isValidMove) which is insane
    private boolean isLosingCapture(Board board, Move move, boolean byWhite) {
        if (move.getCapturedPiece() == null) {
            return false; // not a capture piece
        }

        Piece attacker = board.getPiece(move.getFromRow(), move.getFromCol());
        int attackValue = this.materialEvaluator.getPieceValue(attacker);
        int sacrificeValue = this.materialEvaluator.getPieceValue(move.getCapturedPiece());

        // Only check if attacker is MORE valuable than victim
        if (attackValue <= sacrificeValue) {
            return false; // Equal or favorable trade -> keep
        }
        int cheapestDefenderValue = findCheapestDefender(
                board,
                move.getToRow(),
                move.getToCol(),
                byWhite
        );

        if (cheapestDefenderValue  == -1) return false; // No defender -> safe

        // Losing if: (value of our attacker) > (value we capture + value of their recapture piece)
        return attackValue > sacrificeValue + cheapestDefenderValue ;
    }


    /**
     * Find the cheapest piece defending a target square
     *
     * @param board the current board state
     * @param targetRow the row of the square being attacked
     * @param targetCol the column of the square being attacked
     * @param attackerIsWhite true if the attacker is white (defender is black)
     * @return the material value of the cheapest defender, or -1 if no defender exists
     */
    private int findCheapestDefender(Board board, int targetRow, int targetCol, boolean attackerIsWhite) {
        Piece[][] grid = board.getPieces();
        int cheapest = Integer.MAX_VALUE;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = grid[row][col];
                if (p == null || p.isWhite() == attackerIsWhite) continue;

                if (attacksSquare(p, row, col, targetRow, targetCol, grid)) {
                    int value = materialEvaluator.getPieceValue(p);
                    if (value < cheapest) {
                        cheapest = value;
                    }
                }
            }
        }

        return cheapest == Integer.MAX_VALUE ? -1 : cheapest;
    }

    private boolean attacksSquare(Piece piece, int row, int col, int targetRow, int targetCol, Piece[][] grid) {
        switch (Character.toLowerCase(piece.getSymbol())) {
            case 'p': {
                return pawnAttacksSquare(piece, row, col, targetRow, targetCol);
            }
            case 'n': {
                return knightAttacksSquare(row, col, targetRow, targetCol);
            }
            case 'b': {
                return bishopAttacksSquare(row, col, targetRow, targetCol, grid);
            }
            case 'r': {
                return rookAttacksSquare(row, col, targetRow, targetCol, grid);
            }
            case 'q': {
                return queenAttacksSquare(row, col, targetRow, targetCol, grid);
            }
            case 'k': {
                return Math.max(
                        Math.abs(targetRow - row),
                        Math.abs(targetCol - col)
                ) == 1;
            }
        }
        return false;
    }

    private boolean pawnAttacksSquare(Piece pawn, int fromRow, int fromCol, int toRow, int toCol) {
        int direction = pawn.isWhite() ? -1 : 1;
        return toRow - fromRow == direction && Math.abs(toCol - fromCol) == 1;
    }

    private boolean knightAttacksSquare(int fr, int fc, int tr, int tc) {
        int dr = Math.abs(tr - fr);
        int dc = Math.abs(tc - fc);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    private boolean bishopAttacksSquare(int fromRow, int fromCol, int toRow, int toCol, Piece[][] grid) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff != colDiff || rowDiff == 0) return false;

        // is path clear ? true -> 1 | false -> -1
        int rowDir = (toRow - fromRow) > 0 ? 1 : -1;
        int colDir = (toCol - fromCol) > 0 ? 1 : -1;

        int r = fromRow + rowDir;
        int c = fromCol + colDir;

        while (r != toRow) {
            if (grid[r][c] != null) return false; // Path blocked
            r += rowDir;
            c += colDir;
        }
        return true;
    }

    private boolean rookAttacksSquare(int fromRow, int fromCol, int toRow, int toCol, Piece[][] grid) {
        if (fromRow != toRow && fromCol != toCol) return false;

        if (fromRow == toRow) {
            int start = Math.min(fromCol, toCol) + 1;
            int end = Math.max(fromCol, toCol);
            for (int c = start; c < end; c++) {
                if (grid[fromRow][c] != null) {
                    return false;
                }
            }
        } else {
            int start = Math.min(fromRow, toRow) + 1;
            int end = Math.max(fromRow, toRow);
            for (int r = start; r < end; r++) {
                if (grid[r][fromCol] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean queenAttacksSquare(int fromRow, int fromCol, int toRow, int toCol, Piece[][] grid) {
        return bishopAttacksSquare(fromRow, fromCol, toRow, toCol, grid)
                || rookAttacksSquare(fromRow, fromCol, toRow, toCol, grid);
    }
}
