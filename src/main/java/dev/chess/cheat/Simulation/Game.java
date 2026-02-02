package dev.chess.cheat.Simulation;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an ongoing chess game
 * Manages game state, move history, turn tracking, and game outcome
 */
public class Game {

    private final Board board;
    private final MoveGenerator moveGenerator;
    private boolean isWhiteTurn;
    private final List<Move> moveHistory;
    private GameStatus status;
    private Algorithm whiteAlgorithm;
    private Algorithm blackAlgorithm;

    public enum GameStatus {
        IN_PROGRESS,
        WHITE_WINS,
        BLACK_WINS,
        STALEMATE,
        DRAW
    }

    public Game() {
        this.board = new Board();
        this.moveGenerator = new MoveGenerator();
        this.isWhiteTurn = true;
        this.moveHistory = new ArrayList<>();
        this.status = GameStatus.IN_PROGRESS;
    }

    /**
     * Make a move using row/column coordinates
     *
     * @return true if move was successful
     */
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (status != GameStatus.IN_PROGRESS) {
            return false;
        }

        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null || piece.isWhite() != isWhiteTurn) {
            return false;
        }

        Piece captured = board.getPiece(toRow, toCol);
        Move move = new Move(fromRow, fromCol, toRow, toCol, captured);

        if (!moveGenerator.isLegalMove(board, move, isWhiteTurn)) {
            return false;
        }

        // Execute move
        board.movePiece(move);
        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;

        // Update game status
        updateGameStatus();

        return true;
    }

    /**
     * Make a move using algebraic notation (e.g., "e2" to "e4")
     */
    public boolean makeMove(String from, String to) {
        int fromRow = Board.algebraicToRow(from);
        int fromCol = Board.algebraicToCol(from);
        int toRow = Board.algebraicToRow(to);
        int toCol = Board.algebraicToCol(to);

        return makeMove(fromRow, fromCol, toRow, toCol);
    }

    /**
     * Make a move using a Move object
     */
    public boolean makeMove(Move move) {
        return makeMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
    }

    /**
     * Undo the last move
     */
    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        board.undoMove(lastMove);
        isWhiteTurn = !isWhiteTurn;
        status = GameStatus.IN_PROGRESS;

        return true;
    }

    /**
     * Get all legal moves for the current player
     */
    public List<Move> getLegalMoves() {
        return moveGenerator.generateAllMoves(board, isWhiteTurn);
    }

    /**
     * Get all legal moves for a specific piece
     */
    public List<Move> getLegalMovesForPiece(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece == null || piece.isWhite() != isWhiteTurn) {
            return new ArrayList<>();
        }

        List<Move> pieceMoves = moveGenerator.generatePieceMoves(board, row, col);
        List<Move> legalMoves = new ArrayList<>();

        for (Move move : pieceMoves) {
            if (moveGenerator.isLegalMove(board, move, isWhiteTurn)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    /**
     * Let the engine make a move for the current player
     */
    public Move makeEngineMove(Algorithm algorithm, int depth) {
        if (status != GameStatus.IN_PROGRESS) {
            return null;
        }

        Move bestMove = algorithm.findBestMove(board, isWhiteTurn, depth);
        if (bestMove != null) {
            makeMove(bestMove);
        }

        return bestMove;
    }

    /**
     * Set algorithms for both players (for engine vs engine games)
     */
    public void setEngines(Algorithm whiteAlgorithm, Algorithm blackAlgorithm) {
        this.whiteAlgorithm = whiteAlgorithm;
        this.blackAlgorithm = blackAlgorithm;
    }

    /**
     * Play one engine move (useful for engine vs engine)
     */
    public Move playEngineMove(int depth) {
        Algorithm currentAlgorithm = isWhiteTurn ? whiteAlgorithm : blackAlgorithm;
        if (currentAlgorithm == null) {
            return null;
        }
        return makeEngineMove(currentAlgorithm, depth);
    }

    /**
     * Update the game status based on current position
     */
    private void updateGameStatus() {
        if (moveGenerator.isCheckmate(board, isWhiteTurn)) {
            status = isWhiteTurn ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
        } else if (moveGenerator.isStalemate(board, isWhiteTurn)) {
            status = GameStatus.STALEMATE;
        } else if (isDrawByRepetition() || isDrawByFiftyMoveRule()) {
            status = GameStatus.DRAW;
        }
    }

    /**
     * Check if current position is in check
     */
    public boolean isInCheck() {
        return moveGenerator.isKingInCheck(board, isWhiteTurn);
    }

    /**
     * Simple draw by repetition check (can be enhanced)
     */
    private boolean isDrawByRepetition() {
        // TODO: Implement proper position repetition tracking
        return false;
    }

    /**
     * Check for fifty-move rule
     */
    private boolean isDrawByFiftyMoveRule() {
        // TODO: Implement fifty-move rule tracking
        return false;
    }

    /**
     * Reset the game to starting position
     */
    public void reset() {
        board.reset();
        moveHistory.clear();
        isWhiteTurn = true;
        status = GameStatus.IN_PROGRESS;
    }

    /**
     * Get move count
     */
    public int getMoveCount() {
        return moveHistory.size();
    }

    /**
     * Get full move number (increments after black's move)
     */
    public int getFullMoveNumber() {
        return (moveHistory.size() / 2) + 1;
    }

    /**
     * Get the last move played
     */
    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }

    /**
     * Get complete move history
     */
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    /**
     * Display the move history in UCI (Universal Chess Interface) notation
     */
    public String getMoveHistoryUCI() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moveHistory.size(); i++) {
            if (i % 2 == 0) {
                sb.append(String.format("%d. ", (i / 2) + 1));
            }
            sb.append(moveHistory.get(i).toUCI()).append(" ");
        }
        return sb.toString().trim();
    }

    // Getters
    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public MoveGenerator getMoveGenerator() {
        return moveGenerator;
    }

    public String getCurrentPlayer() {
        return isWhiteTurn ? "White" : "Black";
    }

    public boolean isGameOver() {
        return status != GameStatus.IN_PROGRESS;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Chess Game ===\n");
        sb.append("Turn: ").append(getCurrentPlayer()).append("\n");
        sb.append("Move: ").append(getFullMoveNumber()).append("\n");
        sb.append("Status: ").append(status).append("\n");
        if (isInCheck()) {
            sb.append("CHECK!\n");
        }
        sb.append("Legal moves: ").append(getLegalMoves().size()).append("\n");
        sb.append("Move history: ").append(getMoveHistoryUCI()).append("\n");
        return sb.toString();
    }
}