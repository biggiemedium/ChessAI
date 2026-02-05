package dev.chess.cheat.Simulation.Impl;

import dev.chess.cheat.Simulation.Piece;

/**
 * https://www.chess.com/terms/chess-rook
 */
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

        if (isHorizontal) {
            while (currentCol != toCol) {
                if (board[currentRow][currentCol] != null) {
                    return false;
                }
                currentCol += colDirection;
            }
        } else { // vertical
            while (currentRow != toRow) {
                if (board[currentRow][currentCol] != null) {
                    return false;
                }
                currentRow += rowDirection;
            }
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