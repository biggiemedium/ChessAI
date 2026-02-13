package dev.chess.ai.Util.Board;

import dev.chess.ai.Simulation.Piece;
import dev.chess.ai.Util.Math.PiecePosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to Cache piece positions of our Pieces on a board
 * This will prevent us checking for each square in the board to find our pieces
 *
 * Turns O(64) -> to O(n), where n is amount of pieces the color we are checking has
 */
public class PieceCache {

    private final List<PiecePosition> whitePositions = new ArrayList<>();
    private final List<PiecePosition> blackPositions = new ArrayList<>();

    public void add(int row, int col, Piece piece) {
        getList(piece.isWhite()).add(new PiecePosition(row, col, piece));
    }

    public void remove(int row, int col, boolean isWhite) {
        getList(isWhite).removeIf(pp -> pp.getRow() == row && pp.getCol() == col);
    }

    public void update(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        List<PiecePosition> list = getList(piece.isWhite());

        for (PiecePosition pp : list) {
            if (pp.getRow() == fromRow && pp.getCol() == fromCol) {
                pp.setRow(toRow);
                pp.setCol(toCol);
                return;
            }
        }
    }

    public List<PiecePosition> getList(boolean isWhite) {
        return isWhite ? whitePositions : blackPositions;
    }

    public void clear() {
        this.whitePositions.clear();
        this.blackPositions.clear();
    }

    // init
    public void rebuild(Piece[][] pieces) {
        this.clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null) {
                    add(row, col, piece);
                }
            }
        }
    }
}
