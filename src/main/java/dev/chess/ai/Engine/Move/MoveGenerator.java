package dev.chess.ai.Engine.Move;

import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Impl.King;
import dev.chess.ai.Simulation.Impl.Knight;
import dev.chess.ai.Simulation.Impl.Pawn;
import dev.chess.ai.Simulation.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 2.0
 * @since 2/10/2026
 */
public class MoveGenerator {

    /**
     * Cache king positions in Board to speed up isKingInCheck
     *
     * We were doing enormous checks for nothing
     */
    private int whiteKingRow, whiteKingCol;
    private int blackKingRow, blackKingCol;

    public MoveGenerator(Board board) {
        updateKingPositions(board);
    }

    /**
     * Updates cached values of the kings position
     *
     * @param board
     */
    public void updateKingPositions(Board board) {
        Piece[][] grid = board.getPieces();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = grid[row][col];
                if (p instanceof King) {
                    if (p.isWhite()) {
                        whiteKingRow = row;
                        whiteKingCol = col;
                    } else {
                        blackKingRow = row;
                        blackKingCol = col;
                    }
                }
            }
        }
    }

    /**
     * Generate all legal moves for a given color
     *
     * @param board   the current board state
     * @param isWhite true for white, false for black
     * @return list of all legal moves
     */
    public List<Move> generateAllMoves(Board board, boolean isWhite) {
        List<Move> legalMoves = new ArrayList<>();

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {

                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece == null || piece.isWhite() != isWhite) {
                    continue;
                }

                generatePieceMoves(board, fromRow, fromCol, piece, legalMoves, isWhite);

            }
        }

        return legalMoves;
    }

    /**
     * Generate all pseudo-legal moves for a piece at a position
     * (doesn't check if king is left in check)
     */
    public void generatePieceMoves(Board board, int fromRow, int fromCol, Piece piece, List<Move> legalMoves, boolean isWhite) {
        Piece[][] grid = board.getPieces();

        // Knight check first
        if (piece instanceof Knight) {
            int[][] knightMoves = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
            for (int[] offset : knightMoves) {
                int toRow = fromRow + offset[0];
                int toCol = fromCol + offset[1];
                if (board.isValidPosition(toRow, toCol) &&
                        piece.isValidMove(fromRow, fromCol, toRow, toCol, grid)) {

                    Move move = new Move(fromRow, fromCol, toRow, toCol, grid[toRow][toCol]);
                    if (isLegalMove(board, move, isWhite)) {
                        legalMoves.add(move);
                    }
                }
            }
            return;
        }

        // if not knight -> still check all squares
        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (fromRow == toRow && fromCol == toCol) continue;

                if (!piece.isValidMove(fromRow, fromCol, toRow, toCol, grid)) {
                    continue;
                }

                Move move = new Move(fromRow, fromCol, toRow, toCol, grid[toRow][toCol]);
                if (isLegalMove(board, move, isWhite)) {
                    legalMoves.add(move);
                }
            }
        }
    }

    /**
     * Check if the king of the given color is in check
     */
    public boolean isKingInCheck(Board board, boolean isWhite) {
        int kingRow = isWhite ? whiteKingRow : blackKingRow;
        int kingCol = isWhite ? whiteKingCol : blackKingCol;

        return isSquareAttacked(board, kingRow, kingCol, isWhite);
    }

    private boolean isSquareAttacked(Board board, int targetRow, int targetCol, boolean isWhite) {
        Piece[][] grid = board.getPieces();

        // Check knight attacks -> 8 possible position
        int[][] knightOffsets = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] offset : knightOffsets) {
            int row = targetRow + offset[0];
            int col = targetCol + offset[1];
            if (board.isValidPosition(row, col)) {
                Piece p = grid[row][col];
                if (p instanceof Knight && p.isWhite() != isWhite) {
                    return true;
                }
            }
        }

        // Check pawn attacks -> 2 possible positions per color
        int pawnDir = isWhite ? -1 : 1;
        for (int colOffset : new int[]{-1, 1}) {
            int row = targetRow + pawnDir;
            int col = targetCol + colOffset;
            if (board.isValidPosition(row, col)) {
                Piece p = grid[row][col];
                if (p instanceof Pawn && p.isWhite() != isWhite) {
                    return true;
                }
            }
        }

        // Check king attacks -> 8 possible positions
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int row = targetRow + dr;
                int col = targetCol + dc;
                if (board.isValidPosition(row, col)) {
                    Piece p = grid[row][col];
                    if (p instanceof King && p.isWhite() != isWhite) {
                        return true;
                    }
                }
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece enemy = grid[row][col];
                if (enemy == null || enemy.isWhite() == isWhite) {
                    continue;
                }
                // we already checked this -> skip
                if (enemy instanceof Knight || enemy instanceof Pawn || enemy instanceof King) {
                    continue;
                }
                if (enemy.isValidMove(row, col, targetRow, targetCol, grid)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the position is checkmate
     */
    public boolean isCheckmate(Board board, boolean isWhite) {
        return isKingInCheck(board, isWhite) && generateAllMoves(board, isWhite).isEmpty();
    }

    /**
     * Check if the position is stalemate
     */
    public boolean isStalemate(Board board, boolean isWhite) {
        return !isKingInCheck(board, isWhite) && generateAllMoves(board, isWhite).isEmpty();
    }

    /**
     * Check if a move is legal (doesn't leave own king in check)
     */
    public boolean isLegalMove(Board board, Move move, boolean isWhite) {
        // Store original positions
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Piece moving = board.getPiece(fromRow, fromCol);
        Piece captured = board.getPiece(toRow, toCol); // Get from board -> NOT FROM MOVE

        // King positioning updates -> cache ts
        int oldKingRow = isWhite ? whiteKingRow : blackKingRow;
        int oldKingCol = isWhite ? whiteKingCol : blackKingCol;

        // Make move
        board.setPiece(toRow, toCol, moving);
        board.setPiece(fromRow, fromCol, null);

        if (moving instanceof King) {
            if (isWhite) {
                whiteKingRow = toRow;
                whiteKingCol = toCol;
            } else {
                blackKingRow = toRow;
                blackKingCol = toCol;
            }
        }

        boolean kingInCheck = isKingInCheck(board, isWhite);

        // Undo move -> RESTORE IN REVERSE ORDER OR IT FUCKS IT
        board.setPiece(fromRow, fromCol, moving);
        board.setPiece(toRow, toCol, captured);

        if (moving instanceof King) {
            if (isWhite) {
                whiteKingRow = oldKingRow;
                whiteKingCol = oldKingCol;
            } else {
                blackKingRow = oldKingRow;
                blackKingCol = oldKingCol;
            }
        }


        return !kingInCheck;
    }

    /**
     * Generate all capture positions. Used for {@link dev.chess.ai.Engine.Quiescence.QuiescenceSearch}
     *
     * @param board
     * @param isWhite
     * @return
     */
    public List<Move> generateCaptureMoves(Board board, boolean isWhite) {
        List<Move> captures = new ArrayList<>();
        Piece[][] grid = board.getPieces();

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {

                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece == null || piece.isWhite() != isWhite) {
                    continue;
                }

                // optimal
                if (piece instanceof Knight) {
                    int[][] knightMoves = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
                    for (int[] offset : knightMoves) {
                        int toRow = fromRow + offset[0];
                        int toCol = fromCol + offset[1];
                        if (!board.isValidPosition(toRow, toCol)) continue;

                        Piece target = grid[toRow][toCol];
                        if (target != null && target.isWhite() != isWhite &&
                                piece.isValidMove(fromRow, fromCol, toRow, toCol, grid)) {

                            Move move = new Move(fromRow, fromCol, toRow, toCol, target);
                            if (isLegalMove(board, move, isWhite)) {
                                captures.add(move);
                            }
                        }
                    }
                    continue;
                }

                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {

                        Piece target = board.getPiece(toRow, toCol);
                        if (target == null || target.isWhite() == isWhite) {
                            continue;
                        }

                        if (!piece.isValidMove(fromRow, fromCol, toRow, toCol, board.getPieces())) {
                            continue;
                        }

                        Move move = new Move(fromRow, fromCol, toRow, toCol, target);

                        if (isLegalMove(board, move, isWhite)) {
                            captures.add(move);
                        }
                    }
                }
            }
        }

        return captures;
    }

}
