package dev.chess.ai.Simulation;

import dev.chess.ai.Engine.ChessEngine;
import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Simulation.Impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Game {

    private final Board board;
    private final ChessEngine AI;

    // Game state
    private String gameId;
    private boolean isWhiteTurn;
    private GameStatus status;

    // Move tracking
    private final List<Move> moveHistory;
    private final List<Character> promotions;

    // Event listeners for UI updates
    private final List<GameUpdateListener> listeners;

    public Game(Board board, ChessEngine ai) {
        this.board = board;
        this.AI = ai;
        this.moveHistory = new ArrayList<>();
        this.isWhiteTurn = true;
        this.status = GameStatus.IN_PROGRESS;
        this.listeners = new CopyOnWriteArrayList<>();
        this.promotions = new ArrayList<>();
    }

    /**
     * Reset game to starting position
     */
    public void reset() {
        board.reset();
        moveHistory.clear();
        promotions.clear();
        isWhiteTurn = true;
        status = GameStatus.IN_PROGRESS;
        notifyListeners();
    }

    /**
     * Update entire game state from UCI move list (from LiChess)
     * This replays all moves from the starting position
     */
    public void updateFromMoves(String[] uciMoves) {
        reset();

        // Replay each move
        for (String uci : uciMoves) {
            applyUCIMove(uci);
        }

        notifyListeners();
    }

    /**
     * Update game status based on LiChess status and winner
     */
    public void updateStatus(String lichessStatus, String winner) {
        this.status = convertLiChessStatus(lichessStatus, winner);
        notifyListeners();
    }

    /**
     * Parse and apply a single UCI move (e.g., "e2e4" or "e7e8q")
     */
    private void applyUCIMove(String uci) {
        if (uci == null || uci.length() < 4) {
            System.err.println("Invalid UCI move: " + uci);
            return;
        }

        try {
            // Parse UCI notation
            int fromCol = uci.charAt(0) - 'a';           // a-h -> 0-7
            int fromRow = 8 - (uci.charAt(1) - '0');     // 1-8 -> 7-0 (array indexing)
            int toCol = uci.charAt(2) - 'a';
            int toRow = 8 - (uci.charAt(3) - '0');

            // Check for promotion (5th character)
            Character promotion = (uci.length() == 5) ?
                    Character.toUpperCase(uci.charAt(4)) : null;

            // Execute the move
            executeMove(fromRow, fromCol, toRow, toCol, promotion);

        } catch (Exception e) {
            System.err.println("Error parsing UCI move: " + uci);
            e.printStackTrace();
        }
    }

    /**
     * Execute a move on the board
     */
    private void executeMove(int fromRow, int fromCol, int toRow, int toCol, Character promotion) {
        // Get the piece being moved
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) {
            System.err.println("No piece at " + (char)('a' + fromCol) + (8 - fromRow));
            return;
        }

        // Check for castling
        if (piece instanceof King && Math.abs(toCol - fromCol) == 2) {
            executeCastling(fromRow, fromCol, toRow, toCol, piece.isWhite());
            moveHistory.add(new Move(fromRow, fromCol, toRow, toCol, null));
            promotions.add(null);
            isWhiteTurn = !isWhiteTurn;
            return;
        }

        // Check for en passant
        Piece captured = board.getPiece(toRow, toCol);
        if (piece instanceof Pawn && toCol != fromCol && captured == null) {
            // En passant capture -> remove the pawn that was captured
            int capturedPawnRow = fromRow; // Same row as attacking pawn
            captured = board.getPiece(capturedPawnRow, toCol);
            board.setPiece(capturedPawnRow, toCol, null);
        }

        // Create n execute move
        Move move = new Move(fromRow, fromCol, toRow, toCol, captured);
        board.movePiece(move);

        // pawn promotion
        if (promotion != null) {
            promotePawn(toRow, toCol, promotion, piece.isWhite());
            promotions.add(promotion);
        } else {
            promotions.add(null);
        }

        // Update state
        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;
    }

    /**
     * Execute a castling move
     */
    private void executeCastling(int fromRow, int fromCol, int toRow, int toCol, boolean isWhite) {
        // Move the king
        Piece king = board.getPiece(fromRow, fromCol);
        board.setPiece(fromRow, fromCol, null);
        board.setPiece(toRow, toCol, king);

        // Move the rook
        if (toCol > fromCol) {
            // Kingside castling (O-O)
            // Rook moves from h-file (col 7) -> f-file (col 5)
            Piece rook = board.getPiece(fromRow, 7);
            board.setPiece(fromRow, 7, null);
            board.setPiece(fromRow, 5, rook);
        } else {
            // Queenside castling (O-O-O)
            // Rook moves from a-file (col 0) -> d-file (col 3)
            Piece rook = board.getPiece(fromRow, 0);
            board.setPiece(fromRow, 0, null);
            board.setPiece(fromRow, 3, rook);
        }
    }

    /**
     * Promote a pawn to the specified piece
     */
    private void promotePawn(int row, int col, char promotionPiece, boolean isWhite) {
        Piece promoted;

        switch (promotionPiece) {
            case 'Q': promoted = new Queen(isWhite); break;
            case 'R': promoted = new Rook(isWhite); break;
            case 'B': promoted = new Bishop(isWhite); break;
            case 'N': promoted = new Knight(isWhite); break;
            default:
                System.err.println("Invalid promotion piece: " + promotionPiece);
                return;
        }

        board.setPiece(row, col, promoted);
    }

    /**
     * Get AI's best move for current position
     */
    public Move getAIMove(int depth) {
        return AI.findBestMove(board, isWhiteTurn, depth);
    }

    /**
     * Convert Move to UCI notation (e.g., "e2e4")
     */
    public String moveToUCI(Move move) {
        if (move == null) return null;

        char fromCol = (char) ('a' + move.getFromCol());
        char fromRow = (char) ('8' - move.getFromRow());
        char toCol = (char) ('a' + move.getToCol());
        char toRow = (char) ('8' - move.getToRow());

        String uci = "" + fromCol + fromRow + toCol + toRow;

        Piece piece = board.getPiece(move.getToRow(), move.getToCol());
        if (piece instanceof Pawn) {
            if ((piece.isWhite() && move.getToRow() == 0) ||
                    (!piece.isWhite() && move.getToRow() == 7)) {
                uci += "q";
            }
        }

        return uci;
    }


    /**
     * Convert LiChess status string to GameStatus enum
     */
    private GameStatus convertLiChessStatus(String lichessStatus, String winner) {
        switch (lichessStatus) {
            case "started":
            case "created":
                return GameStatus.IN_PROGRESS;

            case "mate":
                return getWinnerStatus(winner);

            case "resign":
            case "timeout":
            case "outoftime":
                return winner != null ? getWinnerStatus(winner) : GameStatus.DRAW;

            case "stalemate":
                return GameStatus.STALEMATE;

            case "draw":
            case "aborted":
                return GameStatus.DRAW;

            default:
                return GameStatus.IN_PROGRESS;
        }
    }

    /**
     * Get game status based on winner
     */
    private GameStatus getWinnerStatus(String winner) {
        if ("white".equals(winner)) {
            return GameStatus.WHITE_WINS;
        } else if ("black".equals(winner)) {
            return GameStatus.BLACK_WINS;
        }
        return GameStatus.DRAW;
    }

    /**
     * Add a listener for game state changes (for UI updates)
     */
    public void addUpdateListener(GameUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     */
    public void removeUpdateListener(GameUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that game state has changed
     */
    private void notifyListeners() {
        for (GameUpdateListener listener : listeners) {
            listener.onGameUpdated(this);
        }
    }

    public void addMoveToHistory(Move move, Character promotion) {
        moveHistory.add(move);
        promotions.add(promotion);
        isWhiteTurn = !isWhiteTurn;
        notifyListeners();
    }

    public Board getBoard() {
        return board;
    }

    public ChessEngine getAI() {
        return AI;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public List<Character> getPromotions() {
        return new ArrayList<>(promotions);
    }

    public int getMoveCount() {
        return moveHistory.size();
    }

    public boolean isGameOver() {
        if (status != GameStatus.IN_PROGRESS) {
            return true;
        }

        MoveGenerator moveGen = new MoveGenerator();
        List<Move> legalMoves = moveGen.generateAllMoves(board, isWhiteTurn);

        if (legalMoves.isEmpty()) {
            if (moveGen.isKingInCheck(board, isWhiteTurn)) {
                status = isWhiteTurn ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
                System.out.println("Checkmate detected - " + (isWhiteTurn ? "Black" : "White") + " wins");
            } else {
                status = GameStatus.STALEMATE;
                System.out.println("Stalemate detected");
            }
            notifyListeners();
            return true;
        }

        return false;
    }

    /**
     * Listener interface for game state updates
     * Typically implemented by UI components
     */
    public interface GameUpdateListener {
        void onGameUpdated(Game game);
    }
}