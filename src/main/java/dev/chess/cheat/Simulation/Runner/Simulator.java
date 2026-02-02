package dev.chess.cheat.Simulation.Runner;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.UI.BoardViewer;

public class Simulator {

    private final int maxMoves;
    private BoardViewer boardViewer;
    private int moveDelayMs = 500;
    private StatsCallback statsCallback;

    public interface StatsCallback {
        void onMoveComplete(int moveNumber, boolean isWhite, int nodes, long timeMs);
    }

    public Simulator(int maxMoves) {
        this.maxMoves = maxMoves;
    }

    public void setBoardViewer(BoardViewer viewer) {
        this.boardViewer = viewer;
    }

    public void setMoveDelay(int delayMs) {
        this.moveDelayMs = delayMs;
    }

    public void setStatsCallback(StatsCallback callback) {
        this.statsCallback = callback;
    }

    public SimulationStats runGames(Algorithm white, Algorithm black, int numGames,
                                    int whiteDepth, int blackDepth, boolean logToFile) {
        SimulationStats stats = new SimulationStats(white.getName(), black.getName());
        SimulationLogger logger = logToFile ? new SimulationLogger("simulation_logs") : null;

        System.out.println("=".repeat(80));
        System.out.println("STARTING SIMULATION");
        System.out.println("White: " + white.getName() + " (depth " + whiteDepth + ")");
        System.out.println("Black: " + black.getName() + " (depth " + blackDepth + ")");
        System.out.println("Games: " + numGames);
        System.out.println("Max Moves: " + (maxMoves == Integer.MAX_VALUE ? "Unlimited" : maxMoves));
        System.out.println("=".repeat(80));

        if (logger != null) {
            logger.logHeader(white.getName(), black.getName(), numGames, whiteDepth, blackDepth);
        }

        for (int i = 1; i <= numGames; i++) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("GAME " + i + " of " + numGames);
            System.out.println("=".repeat(80));

            GameStats gameStats = playGame(white, black, whiteDepth, blackDepth, i);
            stats.addGame(gameStats);

            System.out.println("\nGAME " + i + " RESULT: " + gameStats.outcome);
            System.out.println("Total Moves: " + gameStats.moves.size());
            System.out.println("Total Time: " + gameStats.totalTimeMs + "ms");
            System.out.println("Total Nodes: " + gameStats.totalNodes);
            System.out.println("Peak Memory: " + (gameStats.peakMemoryBytes / 1024) + "KB");

            if (logger != null) {
                logger.logGame(gameStats);
            }
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("SIMULATION COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("White Wins: " + stats.getWhiteWins());
        System.out.println("Black Wins: " + stats.getBlackWins());
        System.out.println("Draws: " + stats.getDraws());
        System.out.println("Avg Nodes/Game: " + String.format("%.0f", stats.getAvgNodesPerGame()));
        System.out.println("Avg Time/Game: " + String.format("%.0f", stats.getAvgTimePerGame()) + "ms");
        System.out.println("=".repeat(80));

        if (logger != null) {
            logger.logSummary(stats);
            logger.close();
        }

        return stats;
    }

    private GameStats playGame(Algorithm white, Algorithm black, int whiteDepth, int blackDepth, int gameNum) {
        Game game = new Game();
        game.setEngines(white, black);
        GameStats stats = new GameStats(gameNum);

        System.out.println("\nStarting position:");
        if (boardViewer != null) {
            boardViewer.updateBoard(game.getBoard(), "Game " + gameNum + " - Starting Position");
            sleep(1000);
        }

        int moveCount = 0;
        while (!game.isGameOver() && moveCount < maxMoves) {
            boolean isWhiteTurn = game.isWhiteTurn();
            Algorithm current = isWhiteTurn ? white : black;
            int depth = isWhiteTurn ? whiteDepth : blackDepth;
            String player = isWhiteTurn ? "White" : "Black";

            System.out.println("\n--- Move " + (moveCount + 1) + " ---");
            System.out.println(player + " to move (depth " + depth + ")");

            Runtime runtime = Runtime.getRuntime();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();
            long startTime = System.nanoTime();

            Move move = game.makeEngineMove(current, depth);

            long timeMs = (System.nanoTime() - startTime) / 1_000_000;
            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            long memUsed = memAfter - memBefore;

            if (move == null) {
                System.out.println("ERROR: No move found!");
                break;
            }

            int nodes = current.getNodesSearched();

            System.out.println(player + " plays: " + move.toUCI());
            System.out.println("  Time: " + timeMs + "ms");
            System.out.println("  Nodes: " + String.format("%,d", nodes));
            System.out.println("  Memory: " + (memUsed / 1024) + "KB");
            System.out.println("  Nodes/sec: " + (timeMs > 0 ? String.format("%,d", (nodes * 1000 / timeMs)) : "N/A"));

            if (game.isInCheck()) {
                System.out.println("  ** CHECK! **");
            }

            stats.addMove(move, timeMs, nodes, memUsed);
            moveCount++;

            if (statsCallback != null) {
                statsCallback.onMoveComplete(moveCount, !isWhiteTurn, nodes, timeMs);
            }

            if (boardViewer != null) {
                String status = String.format("Game %d - Move %d: %s played %s | %s to move",
                        gameNum, moveCount, player, move.toUCI(), game.isWhiteTurn() ? "White" : "Black");

                if (game.isInCheck()) {
                    status += " [CHECK!]";
                }

                boardViewer.updateBoard(game.getBoard(), status);
                sleep(moveDelayMs);
            }
        }

        stats.outcome = game.getStatus();

        System.out.println("\n" + "-".repeat(80));
        System.out.println("GAME OVER: " + stats.outcome);
        System.out.println("-".repeat(80));

        if (boardViewer != null) {
            boardViewer.updateBoard(game.getBoard(), "Game " + gameNum + " Complete: " + stats.outcome);
            sleep(2000);
        }

        return stats;
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}