package dev.chess.cheat.Engine;

import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Impl.King;
import dev.chess.cheat.Simulation.Piece;

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

                if (!pseudoMoves.isEmpty()) {
                    System.out.println("Piece at " + (char)('a' + fromCol) + (8 - fromRow) + " (" + piece.getClass().getSimpleName() + "): " + pseudoMoves.size() + " pseudo-legal moves");
                }

                for (Move move : pseudoMoves) {
                    if (isLegalMove(board, move, isWhite)) {
                        legalMoves.add(move);

                        System.out.println("  Legal: " + (char)('a' + move.getFromCol()) + (8 - move.getFromRow()) + " -> " + (char)('a' + move.getToCol()) + (8 - move.getToRow()));
                    }
                }
            }
        }

        System.out.println("Total legal moves: " + legalMoves.size());

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

        Piece[][] grid = board.getPieces();

        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {

                if (!piece.isValidMove(fromRow, fromCol, toRow, toCol, grid)) {
                    continue;
                }

                Piece captured = grid[toRow][toCol];
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
        Piece moving = board.getPiece(move.getFromRow(), move.getFromCol());
        Piece captured = move.getCapturedPiece();

        // Make move
        board.setPiece(move.getToRow(), move.getToCol(), moving);
        board.setPiece(move.getFromRow(), move.getFromCol(), null);

        boolean kingInCheck = isKingInCheck(board, isWhite);

        // Undo move
        board.setPiece(move.getFromRow(), move.getFromCol(), moving);
        board.setPiece(move.getToRow(), move.getToCol(), captured);

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

}
