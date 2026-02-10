package dev.chess.ai.Engine.Move;

import dev.chess.ai.Simulation.Piece;

public class Move {

    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private Piece capturedPiece;
    private double score;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, null, 0);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece capturedPiece) {
        this(fromRow, fromCol, toRow, toCol, capturedPiece, 0);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece capturedPiece, double score) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = capturedPiece;
        this.score = score;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public void setCapturedPiece(Piece capturedPiece) { this.capturedPiece = capturedPiece; }

    /**
     * Convert move to UCI notation (Universal Chess Notation)
     * (e.g., "e2e4")
     */
    public String toUCI() {
        char fromFile = (char) ('a' + fromCol);
        char toFile = (char) ('a' + toCol);
        int fromRank = 8 - fromRow;
        int toRank = 8 - toRow;
        return "" + fromFile + fromRank + toFile + toRank;
    }

    @Override
    public String toString() {
        return toUCI() + (capturedPiece != null ? " (capture)" : "");
    }

}
