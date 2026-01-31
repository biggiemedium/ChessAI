package dev.chess.cheat.Simulation.Impl;

import dev.chess.cheat.Simulation.Piece;

public class King extends Piece {

    public King(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        // King moves one square in any direction
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff > 1 || colDiff > 1 || (rowDiff == 0 && colDiff == 0)) {
            return false;
        }

        return isValidDestination(board[toRow][toCol]);
    }

    @Override
    public char getSymbol() {
        return isWhite ? 'K' : 'k';
    }

    @Override
    public String toString() {
        return isWhite ? "♔" : "♚";
    }
}