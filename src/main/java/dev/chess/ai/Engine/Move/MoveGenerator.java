package dev.chess.ai.Engine.Move;

import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Impl.King;
import dev.chess.ai.Simulation.Piece;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

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

                List<Move> pseudoMoves = generatePieceMoves(board, fromRow, fromCol);

                for (Move move : pseudoMoves) {
                    if (isLegalMove(board, move, isWhite)) {
                        legalMoves.add(move);
                    }
                }
            }
        }

        return legalMoves;
    }

    /**
     * Generate all pseudo-legal moves for a piece at a position
     * (doesn't check if king is left in check)
     */
    public List<Move> generatePieceMoves(Board board, int fromRow, int fromCol) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) return moves;

        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (!piece.isValidMove(fromRow, fromCol, toRow, toCol, board.getPieces())) {
                    continue;
                }

                Piece captured = board.getPiece(toRow, toCol);
                moves.add(new Move(fromRow, fromCol, toRow, toCol, captured));
            }
        }

        return moves;
    }

    /**
     * Check if the king of the given color is in check
     */
    public boolean isKingInCheck(Board board, boolean isWhite) {
        int[] kingPos = findKing(board, isWhite);
        if (kingPos == null) return false;

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];
        Piece[][] grid = board.getPieces();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece enemy = grid[row][col];
                if (enemy == null || enemy.isWhite() == isWhite) {
                    continue;
                }

                if (enemy.isValidMove(row, col, kingRow, kingCol, grid)) {
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

        // Make move
        board.setPiece(toRow, toCol, moving);
        board.setPiece(fromRow, fromCol, null);

        boolean kingInCheck = isKingInCheck(board, isWhite);

        // Undo move -> RESTORE IN REVERSE ORDER OR IT FUCKS IT
        board.setPiece(fromRow, fromCol, moving);
        board.setPiece(toRow, toCol, captured);

        return !kingInCheck;
    }

    /**
     * Find the king's position for a given color
     */
    private int[] findKing(Board board, boolean isWhite) {
        Piece[][] grid = board.getPieces();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = grid[row][col];
                if (piece instanceof King && piece.isWhite() == isWhite) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
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

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {

                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece == null || piece.isWhite() != isWhite) {
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
