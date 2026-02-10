package dev.chess.ai.Simulation.Impl;

import dev.chess.ai.Simulation.Piece;

/**
 * https://www.chess.com/terms/chess-queen
 */
public class Queen extends Piece {

    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        // Queen moves like rook (straight) or bishop (diagonal)
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        boolean isStraight = (fromRow == toRow || fromCol == toCol) && (rowDiff + colDiff > 0);
        boolean isDiagonal = rowDiff == colDiff && rowDiff > 0;

        if (!isStraight && !isDiagonal) {
            return false;
        }

        // Check if path is clear
        int rowDirection = Integer.compare(toRow - fromRow, 0);
        int colDirection = Integer.compare(toCol - fromCol, 0);

        int currentRow = fromRow + rowDirection;
        int currentCol = fromCol + colDirection;

        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != null) {
                return false; // Path blocked
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }

        return isValidDestination(board[toRow][toCol]);
    }

    @Override
    public char getSymbol() {
        return isWhite ? 'Q' : 'q';
    }

    @Override
    public String toString() {
        return isWhite ? "♕" : "♛";
    }
}