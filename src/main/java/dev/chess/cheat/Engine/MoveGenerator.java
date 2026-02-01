package dev.chess.cheat.Engine;

import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Piece;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    /**
     * Generate all legal moves for a given color
     *
     * @param board the current board state
     * @param isWhite true for white, false for black
     * @return list of all legal moves
     */
    public List<Move> generateAllMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        // TODO: Loop through all squares
        // TODO: For each piece of the correct color, generate its moves
        // TODO: Filter out illegal moves (moves that leave king in check)

        return moves;
    }

    /**
     * Generate all pseudo-legal moves for a piece at a position
     * (doesn't check if king is left in check)
     */
    public List<Move> generatePieceMoves(Board board, int row, int col) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);

        if (piece == null) {
            return moves;
        }

        // TODO: Loop through all possible destination squares
        // TODO: Use piece.isValidMove() to check if move is legal
        // TODO: Create Move objects for valid moves

        return moves;
    }

    /**
     * Check if the king of the given color is in check
     */
    public boolean isKingInCheck(Board board, boolean isWhite) {
        // TODO: Find the king's position
        // TODO: Check if any enemy piece can attack the king
        return false;
    }

    /**
     * Check if the position is checkmate
     */
    public boolean isCheckmate(Board board, boolean isWhite) {
        // TODO: Check if king is in check
        // TODO: Check if there are any legal moves
        return false;
    }

    /**
     * Check if the position is stalemate
     */
    public boolean isStalemate(Board board, boolean isWhite) {
        // TODO: Check if king is NOT in check
        // TODO: Check if there are no legal moves
        return false;
    }

    /**
     * Check if a move is legal (doesn't leave own king in check)
     */
    public boolean isLegalMove(Board board, Move move, boolean isWhite) {
        // TODO: Make the move on a copy of the board
        // TODO: Check if king is in check after the move
        // TODO: Undo the move
        return false;
    }

    /**
     * Find the king's position for a given color
     */
    private int[] findKing(Board board, boolean isWhite) {
        // TODO: Loop through board to find the king
        // TODO: Return {row, col} or null if not found
        return null;
    }

}
