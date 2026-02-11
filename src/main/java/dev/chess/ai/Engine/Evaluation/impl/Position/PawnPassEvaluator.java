package dev.chess.ai.Engine.Evaluation.impl.Position;

import dev.chess.ai.Engine.Evaluation.Evaluator;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Impl.Pawn;
import dev.chess.ai.Simulation.Piece;

// Determines if a pawn can passed uncontested to become a queen.
// Only do this in late game to improve calculations
public class PawnPassEvaluator implements Evaluator {

    private static final double PASSED_PAWN_BASE_VALUE = 50.0;
    private static final int TOTAL_PIECES_THRESHOLD = 16;
    private static final double PROTECTED_PAWN_BONUS = 1.3;  // 30% bonus
    private static final double CENTER_FILE_BONUS = 1.2;     // 20% bonus
    private static final int D_FILE = 3;
    private static final int E_FILE = 4;

    public PawnPassEvaluator() {

    }

    @Override
    public double evaluate(Board board) {
        if (!shouldStartSearching(board)) {
            return 0;
        }

        double score = 0;

        Piece[][] grid = board.getPieces();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];

                if (piece instanceof Pawn) {
                    double pawnScore = evaluatePawn(grid, row, col, piece.isWhite());
                    score += piece.isWhite() ? pawnScore : -pawnScore;
                }
            }
        }

        return score;
    }

    private double evaluatePawn(Piece[][] grid, int row, int col, boolean isWhite) {
        if (!isPassedPawn(grid, row, col, isWhite)) {
            return 0;
        }

        int distanceToPromotion = isWhite ? row : (7 - row);

        // Gradient by how close they are to getting to the end of the board
        double distanceMultiplier = Math.pow(2.0, (7 - distanceToPromotion) / 2.0);

        double score = PASSED_PAWN_BASE_VALUE * distanceMultiplier;

        if (isProtectedByPawn(grid, row, col, isWhite)) {
            score *= PROTECTED_PAWN_BONUS;
        }

        if (col >= D_FILE && col <= E_FILE) {
            score *= CENTER_FILE_BONUS;
        }

        return score;
    }

    /**
     * Check if a pawn has no enemy pawns blocking its path for promotion
     *
     * @param grid
     * @param row
     * @param col
     * @param isWhite
     * @return
     */
    // O(3 × R) where R = rows to check (worst case 7) = O(21) = O(1) constant
    //
    private boolean isPassedPawn(Piece[][] grid, int row, int col, boolean isWhite) {
        int direction = isWhite ? -1 : 1;
        int endRow = isWhite ? 0 : 7;

        for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
            int checkCol = col + fileOffset;
            if (checkCol < 0 || checkCol >= 8) {
                continue;
            }

            for (int checkRow = row + direction;
                 isWhite ? (checkRow >= endRow) : (checkRow <= endRow);
                 checkRow += direction) {

                Piece piece = grid[checkRow][checkCol];
                if (piece instanceof Pawn && piece.isWhite() != isWhite) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if pawn is protected by a friendly pawn on adjacent file
     * @param grid
     * @param row
     * @param col
     * @param isWhite
     * @return
     */
    private boolean isProtectedByPawn(Piece[][] grid, int row, int col, boolean isWhite) {
        int protectorRow = isWhite ? row + 1 : row - 1;
        if (protectorRow < 0 || protectorRow >= 8) {
            return false;
        }

        for (int colOffset : new int[]{-1, 1}) {
            int protectorCol = col + colOffset;

            if (protectorCol >= 0 && protectorCol < 8) {
                Piece piece = grid[protectorRow][protectorCol];
                if (piece instanceof Pawn && piece.isWhite() == isWhite) {
                    return true;
                }
            }
        }

        return false;
    }

    // Don't search at the start of the game. this is stupid and wastes computation power
    // Time: O(64) -> scans entire board to count pieces
    // TODO: This can be optimized somehow... I think...
    private boolean shouldStartSearching(Board board) {
        int count = 0;
        Piece[][] grid = board.getPieces();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (grid[row][col] != null) {
                    count++;
                }
            }
        }

        // Only evaluate when we are in endgame
        return count <= TOTAL_PIECES_THRESHOLD;
    }

    /**
     *
     * @return true if a pawn can pass uncontested or false
     */
    private boolean searchForPassing() {
        return false;
    }
}
