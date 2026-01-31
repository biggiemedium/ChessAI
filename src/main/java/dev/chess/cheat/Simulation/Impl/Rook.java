package dev.chess.cheat.Simulation.Impl;

import dev.chess.cheat.Simulation.Piece;

public class Rook extends Piece {

    public Rook(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        // Rook moves horizontally or vertically only
        boolean isHorizontal = fromRow == toRow && fromCol != toCol;
        boolean isVertical = fromCol == toCol && fromRow != toRow;

        if (!isHorizontal && !isVertical) {
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
        return isWhite ? 'R' : 'r';
    }

    @Override
    public String toString() {
        return isWhite ? "♖" : "♜";
    }
}