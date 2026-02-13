package dev.chess.ai.Simulation;

import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Simulation.Impl.*;
import dev.chess.ai.Util.Board.PieceCache;
import dev.chess.ai.Util.Board.ZobristHasher;
import dev.chess.ai.Util.Math.PiecePosition;

import java.util.List;

/**
 * Chess boards use an 8x8 grid
 * <p>
 * https://www.chess.com/article/view/chess-board-dimensions
 * <p>
 * We are now Caching pieces to
 *
 * @version 2.0
 */
public class Board {

    private Piece[][] pieces;
    private long zobristHash; /// {@link ZobristHasher}

    // King Cache -> moved from MoveGenerator bc I don't want an instance of it here
    private int whiteKingRow = 7, whiteKingCol = 4;
    private int blackKingRow = 0, blackKingCol = 4;

    private PieceCache pieceCache;

    public Board() {
        this.pieces = new Piece[8][8];
        this.pieceCache = new PieceCache();
        initializeBoard();
        this.initialize();
    }

    private void initialize() {
        this.pieceCache.rebuild(pieces);
        initializeKingPositions();
        zobristHash = ZobristHasher.computeHash(pieces);
    }

    private void initializeKingPositions() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = pieces[row][col];
                if (p instanceof King) {
                    if (p.isWhite()) {
                        whiteKingRow = row;
                        whiteKingCol = col;
                    } else {
                        blackKingRow = row;
                        blackKingCol = col;
                    }
                }
            }
        }
    }

    public int[] getKingPosition(boolean isWhite) {
        return isWhite ? new int[]{whiteKingRow, whiteKingCol}
                : new int[]{blackKingRow, blackKingCol};
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
        if (!isValidPosition(row, col)) {
            return;
        }

        Piece oldPiece = pieces[row][col];
        // Update our cached positions
        if (oldPiece != null) {
            zobristHash ^= ZobristHasher.getPieceKey(row, col, oldPiece);
            pieceCache.remove(row, col, oldPiece.isWhite());
        }
        if (piece != null) {
            zobristHash ^= ZobristHasher.getPieceKey(row, col, piece);
            pieceCache.add(row, col, piece);
        }

        pieces[row][col] = piece;

    }

    public void movePiece(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Piece moving = getPiece(move.getFromRow(), move.getFromCol());
        Piece captured = getPiece(move.getToRow(), move.getToCol());

        if (moving == null) return;

        // Hash
        zobristHash ^= ZobristHasher.getPieceKey(fromRow, fromCol, moving);
        if (captured != null) {
            zobristHash ^= ZobristHasher.getPieceKey(toRow, toCol, captured);
        }
        zobristHash ^= ZobristHasher.getPieceKey(toRow, toCol, moving);

        if (captured != null) {
            pieceCache.remove(toRow, toCol, captured.isWhite());
        }

        pieceCache.update(fromRow, fromCol, toRow, toCol, moving);
        pieces[toRow][toCol] = moving;
        pieces[fromRow][fromCol] = null;

        if (moving instanceof King) {
            if (moving.isWhite()) {
                whiteKingRow = move.getToRow();
                whiteKingCol = move.getToCol();
            } else {
                blackKingRow = move.getToRow();
                blackKingCol = move.getToCol();
            }
        }
    }

    public void undoMove(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Piece moving = getPiece(move.getToRow(), move.getToCol());
        Piece captured = move.getCapturedPiece();

        if (moving == null) return;

        zobristHash ^= ZobristHasher.getPieceKey(toRow, toCol, moving);
        if (captured != null) {
            zobristHash ^= ZobristHasher.getPieceKey(toRow, toCol, captured);
        }
        zobristHash ^= ZobristHasher.getPieceKey(fromRow, fromCol, moving);

        pieceCache.update(toRow, toCol, fromRow, fromCol, moving);
        if (captured != null) {
            pieceCache.add(toRow, toCol, captured);
        }

        // update grid directly. don't depend on setPiece
        pieces[fromRow][fromCol] = moving;
        pieces[toRow][toCol] = captured;

        // King pos update
        if (moving instanceof King) {
            if (moving.isWhite()) {
                whiteKingRow = move.getFromRow();
                whiteKingCol = move.getFromCol();
            } else {
                blackKingRow = move.getFromRow();
                blackKingCol = move.getFromCol();
            }
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

        Piece captured = pieces[toRow][toCol];
        if (captured != null) {
            pieceCache.remove(toRow, toCol, captured.isWhite());
        }
        pieceCache.update(fromRow, fromCol, toRow, toCol, piece);

        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;

        return true;
    }

    public void setKingPosition(boolean isWhite, int row, int col) {
        if (isWhite) {
            whiteKingRow = row;
            whiteKingCol = col;
        } else {
            blackKingRow = row;
            blackKingCol = col;
        }
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
        this.pieceCache.clear();
        this.zobristHash = 0;
        this.whiteKingRow = this.whiteKingCol = -1;
        this.blackKingRow = this.blackKingCol = -1;
    }

    public void reset() {
        clear();
        initializeBoard();
        this.pieceCache.rebuild(pieces);
        initializeKingPositions();
        zobristHash = ZobristHasher.computeHash(pieces);
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

    public PieceCache getPieceCache() {
        return pieceCache;
    }

    public long getZobristHash() {
        return zobristHash;
    }
}