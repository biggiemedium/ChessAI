package dev.chess.cheat.Simulation;

import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Network.ChessClient;
import dev.chess.cheat.Simulation.Impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Game {

    private final Board board;
    private final ChessEngine AI;
    private final ChessClient client;

    // Game state
    private String gameId;
    private boolean isWhiteTurn;
    private GameStatus status;

    // Move tracking
    private final List<Move> moveHistory;
    private final List<Character> promotions; // Tracks which moves were promotions

    // Event listeners for UI updates
    private final List<GameUpdateListener> listeners;

    // ========== Constructor ==========

    public Game(Board board, List<Move> moveHistory, ChessEngine ai, ChessClient client) {
        this.board = board;
        this.moveHistory = new ArrayList<>(moveHistory);
        this.AI = ai;
        this.client = client;
        this.isWhiteTurn = true;
        this.status = GameStatus.IN_PROGRESS;
        this.listeners = new CopyOnWriteArrayList<>();
        this.promotions = new ArrayList<>();
    }

    // ========== Game State Management ==========

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

    // ========== Move Handling ==========

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

        // Capture piece at destination (if any)
        Piece captured = board.getPiece(toRow, toCol);

        // Create and execute move
        Move move = new Move(fromRow, fromCol, toRow, toCol, captured);
        board.movePiece(move);

        // Handle pawn promotion
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

    // ========== Status Conversion ==========

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

    // ========== Event Listeners ==========

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

    // ========== Chat Handling ==========

    /**
     * Handle incoming chat message from LiChess
     * Can be overridden or extended for custom behavior
     */
    public void onChatMessage(String username, String text, String room) {
        System.out.println("[" + room + "] " + username + ": " + text);
    }

    // ========== Getters ==========

    public Board getBoard() {
        return board;
    }

    public ChessEngine getAI() {
        return AI;
    }

    public ChessClient getClient() {
        return client;
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
        return status != GameStatus.IN_PROGRESS;
    }

    // ========== Listener Interface ==========

    /**
     * Listener interface for game state updates
     * Typically implemented by UI components
     */
    public interface GameUpdateListener {
        void onGameUpdated(Game game);
    }
}
