package dev.chess.cheat.Engine;

import dev.chess.cheat.Simulation.Piece;

public class Move {

    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final Piece capturedPiece;
    private final double score;

    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece capturedPiece, double score) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = capturedPiece;
        this.score = score;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public double getScore() {
        return score;
    }
}
