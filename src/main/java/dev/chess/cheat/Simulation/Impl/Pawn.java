package dev.chess.cheat.Simulation.Impl;

import dev.chess.cheat.Simulation.Piece;

public class Pawn extends Piece {

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        // White pawns move up (decreasing row), black pawns move down (increasing row)
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        // Move forward one square
        if (rowDiff == direction && colDiff == 0) {
            return board[toRow][toCol] == null;
        }

        // Move forward two squares from starting position
        if (rowDiff == 2 * direction && colDiff == 0 && fromRow == startRow) {
            return board[toRow][toCol] == null && board[fromRow + direction][fromCol] == null;
        }

        // Diagonal capture
        if (rowDiff == direction && colDiff == 1) {
            Piece target = board[toRow][toCol];
            return target != null && target.isWhite() != this.isWhite;
        }

        return false;
    }

    @Override
    public char getSymbol() {
        return isWhite ? 'P' : 'p';
    }

    @Override
    public String toString() {
        return isWhite ? "♙" : "♟";
    }
}