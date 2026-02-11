package dev.chess.ai.Simulation.Runner;

import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Search.Algorithm;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.GameStatus;
import dev.chess.ai.Simulation.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRunner {

    private final Board board;
    private final MoveGenerator moveGenerator;
    private boolean isWhiteTurn;
    private final List<Move> moveHistory;
    private GameStatus status;
    private Algorithm whiteAlgorithm;
    private Algorithm blackAlgorithm;
    private final Map<String, Integer> positionCount;
    private int movesSinceCaptureOrPawn;

    public GameRunner() {
        this.board = new Board();
        this.moveGenerator = new MoveGenerator(board);
        this.isWhiteTurn = true;
        this.moveHistory = new ArrayList<>();
        this.status = GameStatus.IN_PROGRESS;
        this.positionCount = new HashMap<>();
        this.movesSinceCaptureOrPawn = 0;
    }

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

        boolean isPawnMove = piece.getSymbol() == 'P' || piece.getSymbol() == 'p';
        boolean isCapture = captured != null;

        board.movePiece(move);
        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;

        if (isPawnMove || isCapture) {
            movesSinceCaptureOrPawn = 0;
            positionCount.clear();
        } else {
            movesSinceCaptureOrPawn++;
            String positionKey = getBoardHash();
            positionCount.put(positionKey, positionCount.getOrDefault(positionKey, 0) + 1);
        }

        updateGameStatus();

        return true;
    }

    public boolean makeMove(String from, String to) {
        int fromRow = Board.algebraicToRow(from);
        int fromCol = Board.algebraicToCol(from);
        int toRow = Board.algebraicToRow(to);
        int toCol = Board.algebraicToCol(to);

        return makeMove(fromRow, fromCol, toRow, toCol);
    }

    public boolean makeMove(Move move) {
        return makeMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
    }

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

    public List<Move> getLegalMoves() {
        return moveGenerator.generateAllMoves(board, isWhiteTurn);
    }

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

    public void setEngines(Algorithm whiteAlgorithm, Algorithm blackAlgorithm) {
        this.whiteAlgorithm = whiteAlgorithm;
        this.blackAlgorithm = blackAlgorithm;
    }

    public Move playEngineMove(int depth) {
        Algorithm currentAlgorithm = isWhiteTurn ? whiteAlgorithm : blackAlgorithm;
        if (currentAlgorithm == null) {
            return null;
        }
        return makeEngineMove(currentAlgorithm, depth);
    }

    private void updateGameStatus() {
        if (moveGenerator.isCheckmate(board, isWhiteTurn)) {
            status = isWhiteTurn ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
        } else if (moveGenerator.isStalemate(board, isWhiteTurn)) {
            status = GameStatus.STALEMATE;
        } else if (isDrawByRepetition() || isDrawByFiftyMoveRule()) {
            status = GameStatus.DRAW;
        }
    }

    public boolean isInCheck() {
        return moveGenerator.isKingInCheck(board, isWhiteTurn);
    }

    private boolean isDrawByRepetition() {
        String currentPosition = getBoardHash();
        return positionCount.getOrDefault(currentPosition, 0) >= 3;
    }

    private boolean isDrawByFiftyMoveRule() {
        return movesSinceCaptureOrPawn >= 100;
    }

    private String getBoardHash() {
        StringBuilder hash = new StringBuilder();
        Piece[][] pieces = board.getPieces();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece == null) {
                    hash.append('.');
                } else {
                    hash.append(piece.getSymbol());
                }
            }
        }
        hash.append(isWhiteTurn ? 'W' : 'B');
        return hash.toString();
    }

    public void reset() {
        board.reset();
        moveHistory.clear();
        isWhiteTurn = true;
        status = GameStatus.IN_PROGRESS;
        positionCount.clear();
        movesSinceCaptureOrPawn = 0;
    }

    public int getMoveCount() {
        return moveHistory.size();
    }

    public int getFullMoveNumber() {
        return (moveHistory.size() / 2) + 1;
    }

    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

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