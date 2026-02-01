package dev.chess.cheat.Simulation.Impl;

import dev.chess.cheat.Simulation.Piece;

public class Knight extends Piece {

    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        // Knight moves in L-shape -> 2 squares in one direction, 1 in perpendicular
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        boolean isValidLShape = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);

        if (!isValidLShape) {
            return false;
        }

        // Knight jumps over pieces, only check destination
        return isValidDestination(board[toRow][toCol]);
    }

    @Override
    public char getSymbol() {
        return isWhite ? 'N' : 'n';
    }

    @Override
    public String toString() {
        return isWhite ? "♘" : "♞";
    }
}