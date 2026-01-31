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

    /**
     * Sets up the standard chess starting position
     */
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

    /**
     * Get piece at position
     */
    public Piece getPiece(int row, int col) {
        if (!isValidPosition(row, col)) {
            return null;
        }
        return pieces[row][col];
    }

    /**
     * Move a piece from one position to another
     * @return true if move was successful
     */
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

        // Execute move
        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;
        return true;
    }

    /**
     * Check if position is within board bounds
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    /**
     * Clear the board
     */
    public void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                pieces[row][col] = null;
            }
        }
    }

    /**
     * Reset board to starting position
     */
    public void reset() {
        clear();
        initializeBoard();
    }

    /**
     * Get the raw board array (for piece validation)
     */
    public Piece[][] getPieces() {
        return pieces;
    }

    /**
     * Display the board in console
     */
    public void display() {
        System.out.println("  a b c d e f g h");
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " ");
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece == null) {
                    System.out.print("· ");
                } else {
                    System.out.print(piece.toString() + " ");
                }
            }
            System.out.println(8 - row);
        }
        System.out.println("  a b c d e f g h");
    }

    /**
     * Convert algebraic notation to row (e.g., "e4" -> row 4)
     */
    public static int algebraicToRow(String notation) {
        return 8 - Character.getNumericValue(notation.charAt(1));
    }

    /**
     * Convert algebraic notation to column (e.g., "e4" -> col 4)
     */
    public static int algebraicToCol(String notation) {
        return notation.charAt(0) - 'a';
    }
}