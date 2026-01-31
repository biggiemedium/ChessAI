package dev.chess.cheat.Simulation;

import dev.chess.cheat.Simulation.Impl.*;

/**
 * Chess boards use an 8x8 grid
 *
 * https://www.chess.com/article/view/chess-board-dimensions
 */
public class Board {

    private Piece[][] pieces;

    public Board() {
        this.pieces = new Piece[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        // Black pieces (row 0 and 1)
        pieces[0][0] = new Rook(false);
        pieces[0][1] = new Knight(false);
        pieces[0][2] = new Bishop(false);
        pieces[0][3] = new Queen(false);
        pieces[0][4] = new King(false);
        pieces[0][5] = new Bishop(false);
        pieces[0][6] = new Knight(false);
        pieces[0][7] = new Rook(false);

        for (int col = 0; col < 8; col++) {
            pieces[1][col] = new Pawn(false);
        }

        // White pieces (row 6 and 7)
        for (int col = 0; col < 8; col++) {
            pieces[6][col] = new Pawn(true);
        }

        pieces[7][0] = new Rook(true);
        pieces[7][1] = new Knight(true);
        pieces[7][2] = new Bishop(true);
        pieces[7][3] = new Queen(true);
        pieces[7][4] = new King(true);
        pieces[7][5] = new Bishop(true);
        pieces[7][6] = new Knight(true);
        pieces[7][7] = new Rook(true);
    }

    public Piece getPiece(int row, int col) {
        if (!isValidPosition(row, col)) {
            return null;
        }
        return pieces[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col)) {
            pieces[row][col] = piece;
        }
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidPosition(fromRow, fromCol) || !isValidPosition(toRow, toCol)) {
            return false;
        }

        Piece piece = pieces[fromRow][fromCol];
        if (piece == null) {
            return false;
        }

        if (!piece.isValidMove(fromRow, fromCol, toRow, toCol, pieces)) {
            return false;
        }

        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;
        return true;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                pieces[row][col] = null;
            }
        }
    }

    public void reset() {
        clear();
        initializeBoard();
    }

    public Piece[][] getPieces() {
        return pieces;
    }

    public static int algebraicToRow(String notation) {
        return 8 - Character.getNumericValue(notation.charAt(1));
    }

    public static int algebraicToCol(String notation) {
        return notation.charAt(0) - 'a';
    }
}