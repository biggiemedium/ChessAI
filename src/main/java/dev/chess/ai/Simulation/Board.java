package dev.chess.ai.Simulation;

import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Simulation.Impl.*;

import java.util.Random;

/**
 * Chess boards use an 8x8 grid
 *
 * https://www.chess.com/article/view/chess-board-dimensions
 */
public class Board {

    private Piece[][] pieces;
    private long zobristHash;
    private static final long[][][] pieceKeys = new long[64][12][2]; // [square][pieceType][color]

    // King Cache -> moved from MoveGenerator bc I don't want an instance of it here
    private int whiteKingRow = 7, whiteKingCol = 4;
    private int blackKingRow = 0, blackKingCol = 4;

    public Board() {
        this.pieces = new Piece[8][8];
        initializeBoard();
        initializeZobristHash();
        initializeKingPositions();
    }

    static {
        Random rand = new Random(12345);
        for (int sq = 0; sq < 64; sq++) {
            for (int piece = 0; piece < 12; piece++) {
                for (int color = 0; color < 2; color++) {
                    pieceKeys[sq][piece][color] = rand.nextLong();
                }
            }
        }
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

    private void initializeZobristHash() {
        zobristHash = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null) {
                    int square = row * 8 + col;
                    int pieceIndex = getPieceIndex(piece);
                    int colorIndex = piece.isWhite() ? 0 : 1;
                    zobristHash ^= pieceKeys[square][pieceIndex][colorIndex];
                }
            }
        }
    }

    /**
     * Map a piece to an index (0-11)
     * 0=Pawn, 1=Knight, 2=Bishop, 3=Rook, 4=Queen, 5=King (repeated for each color)
     */
    private int getPieceIndex(Piece piece) {
        char symbol = Character.toLowerCase(piece.getSymbol());
        switch (symbol) {
            case 'p': return 0;
            case 'n': return 1;
            case 'b': return 2;
            case 'r': return 3;
            case 'q': return 4;
            case 'k': return 5;
            default: return 0;
        }
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

    public void movePiece(Move move) {
        Piece moving = getPiece(move.getFromRow(), move.getFromCol());
        Piece captured = getPiece(move.getToRow(), move.getToCol());

        if (moving == null) return;
        int fromSquare = move.getFromRow() * 8 + move.getFromCol();
        int toSquare = move.getToRow() * 8 + move.getToCol();
        int pieceIndex = getPieceIndex(moving);
        int colorIndex = moving.isWhite() ? 0 : 1;

        this.zobristHash ^= pieceKeys[fromSquare][pieceIndex][colorIndex];
        if (captured != null) {
            int capturedIndex = getPieceIndex(captured);
            int capturedColorIndex = captured.isWhite() ? 0 : 1;
            zobristHash ^= pieceKeys[toSquare][capturedIndex][capturedColorIndex];
        }
        this.zobristHash ^= pieceKeys[toSquare][pieceIndex][colorIndex];

        setPiece(move.getToRow(), move.getToCol(), moving);
        setPiece(move.getFromRow(), move.getFromCol(), null);

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
        Piece moving = getPiece(move.getToRow(), move.getToCol());
        Piece captured = move.getCapturedPiece();

        if (moving == null) return;

        int fromSquare = move.getFromRow() * 8 + move.getFromCol();
        int toSquare = move.getToRow() * 8 + move.getToCol();
        int pieceIndex = getPieceIndex(moving);
        int colorIndex = moving.isWhite() ? 0 : 1;

        this.zobristHash ^= pieceKeys[toSquare][pieceIndex][colorIndex];
        if (captured != null) {
            int capturedIndex = getPieceIndex(captured);
            int capturedColorIndex = captured.isWhite() ? 0 : 1;
            zobristHash ^= pieceKeys[toSquare][capturedIndex][capturedColorIndex];
        }
        this.zobristHash ^= pieceKeys[fromSquare][pieceIndex][colorIndex];

        setPiece(move.getFromRow(), move.getFromCol(), moving);
        setPiece(move.getToRow(), move.getToCol(), move.getCapturedPiece());

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
        this.zobristHash = 0;
        this.whiteKingRow = this.whiteKingCol = -1;
        this.blackKingRow = this.blackKingCol = -1;
    }

    public void reset() {
        clear();
        initializeBoard();
        initializeZobristHash();
        initializeKingPositions();
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

    public long getZobristHash() {
        return zobristHash;
    }
}