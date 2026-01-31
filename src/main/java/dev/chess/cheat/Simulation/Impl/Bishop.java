package dev.chess.cheat.Simulation.Impl;

import dev.chess.cheat.Simulation.Piece;

/**
 * Bishop may only move diagonally
 *
 * https://www.chess.com/terms/chess-bishop
 */
public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        // Bishop moves diagonally only
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Must move diagonally (equal row and column distance)
        if (rowDiff != colDiff || rowDiff == 0) {
            return false;
        }

        // Check if path is clear
        int rowDirection = (toRow - fromRow) > 0 ? 1 : -1;
        int colDirection = (toCol - fromCol) > 0 ? 1 : -1;

        int currentRow = fromRow + rowDirection;
        int currentCol = fromCol + colDirection;

        while (currentRow != toRow && currentCol != toCol) {
            if (board[currentRow][currentCol] != null) {
                return false; // Path blocked
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }

        // Check destination square using helper method
        return isValidDestination(board[toRow][toCol]);
    }

    @Override
    public char getSymbol() {
        return isWhite ? 'B' : 'b';
    }

    @Override
    public String toString() {
        return isWhite ? "♗" : "♝";
    }
}