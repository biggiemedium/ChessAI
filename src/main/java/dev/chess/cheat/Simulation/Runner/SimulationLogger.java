package dev.chess.cheat.Simulation.Runner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimulationLogger {

    private final String logPath;
    private PrintWriter writer;

    public SimulationLogger(String logDirectory) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        this.logPath = logDirectory + "/simulation_" + timestamp + ".log";

        try {
            Files.createDirectories(Paths.get(logDirectory));
            writer = new PrintWriter(new FileWriter(logPath));
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
            writer = null;
        }
    }

    public void logHeader(String white, String black, int games, int whiteDepth, int blackDepth) {
        if (writer == null) return;
        writer.println("=".repeat(80));
        writer.println("SIMULATION: " + white + " (depth " + whiteDepth + ") vs " + black + " (depth " + blackDepth + ")");
        writer.println("Games: " + games + " | Time: " + LocalDateTime.now());
        writer.println("=".repeat(80));
        writer.flush();
    }

    public void logGame(GameStats stats) {
        if (writer == null) return;
        writer.println("\nGame " + stats.gameNumber + " | " + stats.outcome);
        writer.println("Moves: " + stats.moves.size() + " | Time: " + stats.totalTimeMs + "ms | Nodes: " + stats.totalNodes);

        for (int i = 0; i < stats.moves.size(); i++) {
            GameStats.MoveStats move = stats.moves.get(i);
            writer.println(String.format("  %d. %s | %dms | %d nodes | %dKB",
                    i + 1, move.move.toUCI(), move.timeMs, move.nodes, move.memoryBytes / 1024));
        }
        writer.flush();
    }

    public void logSummary(SimulationStats stats) {
        if (writer == null) return;
        writer.println("\n" + "=".repeat(80));
        writer.println("SUMMARY");
        writer.println("-".repeat(80));
        writer.println("Total Games: " + stats.games.size());
        writer.println("White Wins: " + stats.getWhiteWins());
        writer.println("Black Wins: " + stats.getBlackWins());
        writer.println("Draws: " + stats.getDraws());
        writer.println("Total Nodes: " + stats.getTotalNodes());
        writer.println("Avg Nodes/Game: " + String.format("%.0f", stats.getAvgNodesPerGame()));
        writer.println("Avg Time/Game: " + String.format("%.0f", stats.getAvgTimePerGame()) + "ms");
        writer.println("Avg Memory/Game: " + String.format("%.0f", stats.getAvgMemoryPerGame() / 1024) + "KB");
        writer.println("=".repeat(80));
        writer.flush();
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
