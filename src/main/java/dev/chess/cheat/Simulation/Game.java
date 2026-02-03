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
    private boolean isWhiteTurn;
    private GameStatus status;
    private final List<Move> moveHistory;
    private final ChessEngine AI;

    // Connection
    private final ChessClient client;
    private String gameId;

    private final List<GameUpdateListener> listeners;

    // im going to kill myself
    private final List<Character> promotions;

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

    /**
     * Update game state from array of UCI moves
     * @param uciMoves array of moves in UCI notation (e.g., "e2e4", "e7e8q")
     */
    public void updateFromMoves(String[] uciMoves) {
        // Reset board to starting position
        board.reset();
        moveHistory.clear();
        promotions.clear();
        isWhiteTurn = true;

        // Apply each move sequentially
        for (String uciMove : uciMoves) {
            UCIMove parsedMove = parseUCIMove(uciMove);
            if (parsedMove != null) {
                applyMove(parsedMove);
            }
        }

        notifyListeners();
    }

    /**
     * Apply a UCI move to the board
     */
    private void applyMove(UCIMove uciMove) {
        // Get the piece being moved
        Piece piece = board.getPiece(uciMove.fromRow, uciMove.fromCol);
        if (piece == null) {
            System.err.println("No piece at source position");
            return;
        }

        // Store the captured piece
        Piece captured = board.getPiece(uciMove.toRow, uciMove.toCol);

        // Create Move object
        Move move = new Move(uciMove.fromRow, uciMove.fromCol, uciMove.toRow, uciMove.toCol, captured);

        // Make the move on the board
        board.movePiece(move);

        // Handle pawn promotion
        if (uciMove.promotion != null) {
            handlePromotion(uciMove.toRow, uciMove.toCol, uciMove.promotion, piece.isWhite());
            promotions.add(uciMove.promotion);
        } else {
            promotions.add(null);
        }

        // Add to history and toggle turn
        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;
    }

    /**
     * Handle pawn promotion by replacing the piece
     */
    private void handlePromotion(int row, int col, char promotionPiece, boolean isWhite) {
        Piece promoted = null;

        switch (Character.toUpperCase(promotionPiece)) {
            case 'Q':
                promoted = new Queen(isWhite);
                break;
            case 'R':
                promoted = new Rook(isWhite);
                break;
            case 'B':
                promoted = new Bishop(isWhite);
                break;
            case 'N':
                promoted = new Knight(isWhite);
                break;
            default:
                System.err.println("Unknown promotion piece: " + promotionPiece);
                return;
        }

        board.setPiece(row, col, promoted);
    }


    /**
     * Parse UCI move notation
     * UCI format: e2e4, e7e5, e1g1 (castling), e7e8q (promotion)
     */
    private UCIMove parseUCIMove(String uci) {
        if (uci == null || uci.length() < 4) {
            return null;
        }

        try {
            // Parse source square
            int fromCol = uci.charAt(0) - 'a';  // a-h -> 0-7
            int fromRow = 8 - (uci.charAt(1) - '0');  // 1-8 -> 7-0 (inverted for array indexing)

            // Parse destination square
            int toCol = uci.charAt(2) - 'a';
            int toRow = 8 - (uci.charAt(3) - '0');

            // Handle promotion
            Character promotion = null;
            if (uci.length() == 5) {
                promotion = Character.toUpperCase(uci.charAt(4));
            }

            return new UCIMove(fromRow, fromCol, toRow, toCol, promotion);
        } catch (Exception e) {
            System.err.println("Error parsing UCI move: " + uci);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update game status from LiChess status string
     * @param lichessStatus the status from LiChess API
     * @param winner "white", "black", or null
     */
    public void updateStatus(String lichessStatus, String winner) {
        switch (lichessStatus) {
            case "started":
            case "created":
                this.status = GameStatus.IN_PROGRESS;
                break;
            case "mate":
                // Determine winner
                if ("white".equals(winner)) {
                    this.status = GameStatus.WHITE_WINS;
                } else if ("black".equals(winner)) {
                    this.status = GameStatus.BLACK_WINS;
                } else {
                    this.status = GameStatus.IN_PROGRESS;
                }
                break;
            case "resign":
            case "timeout":
            case "outoftime":
                // Winner decided by resignation or timeout
                if ("white".equals(winner)) {
                    this.status = GameStatus.WHITE_WINS;
                } else if ("black".equals(winner)) {
                    this.status = GameStatus.BLACK_WINS;
                } else {
                    this.status = GameStatus.DRAW;
                }
                break;
            case "stalemate":
                this.status = GameStatus.STALEMATE;
                break;
            case "draw":
            case "aborted":
                this.status = GameStatus.DRAW;
                break;
            default:
                this.status = GameStatus.IN_PROGRESS;
        }

        notifyListeners();
    }

    /**
     * Add a listener for game updates (for JavaFX UI)
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
     * Notify all listeners of game update
     */
    private void notifyListeners() {
        for (GameUpdateListener listener : listeners) {
            listener.onGameUpdated(this);
        }
    }


    public void reset() {
        board.reset();
        moveHistory.clear();
        isWhiteTurn = true;
        status = GameStatus.IN_PROGRESS;
    }


    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        isWhiteTurn = whiteTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
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

    public List<GameUpdateListener> getListeners() {
        return listeners;
    }

    /**
     * Helper class to store UCI move data including promotion
     */
    private static class UCIMove {
        final int fromRow;
        final int fromCol;
        final int toRow;
        final int toCol;
        final Character promotion;

        UCIMove(int fromRow, int fromCol, int toRow, int toCol, Character promotion) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.promotion = promotion;
        }
    }

    /**
     * Interface for listening to game updates
     */
    public interface GameUpdateListener {
        void onGameUpdated(Game game);
    }
}
