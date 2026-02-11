package dev.chess.ai.Simulation;

public abstract class Piece {

    protected final boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    /**
     * Validates if a move is legal for this piece
     *
     * @param fromRow starting row (0-7)
     * @param fromCol starting column (0-7)
     * @param toRow destination row (0-7)
     * @param toCol destination column (0-7)
     * @param board current board state
     * @return true if move is valid
     */
    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board);

    /**
     * @return single character representation (uppercase for white, lowercase for black)
     */
    public abstract char getSymbol();

    /**
     * @return true if piece is white, false if black
     */
    public final boolean isWhite() {
        return isWhite;
    }

    /**
     * Check if destination square is valid
     *
     * @return true if destination is empty or has opponent piece
     */
    protected final boolean isValidDestination(Piece destination) {
        return destination == null || destination.isWhite != this.isWhite;
    }
}