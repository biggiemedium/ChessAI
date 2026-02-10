package dev.chess.ai.Simulation.Runner;


import dev.chess.ai.Simulation.GameStatus;

import java.util.ArrayList;
import java.util.List;

public class SimulationStats {

    public final String whiteAlgorithm;
    public final String blackAlgorithm;
    public final List<GameStats> games;

    public SimulationStats(String whiteAlgorithm, String blackAlgorithm) {
        this.whiteAlgorithm = whiteAlgorithm;
        this.blackAlgorithm = blackAlgorithm;
        this.games = new ArrayList<>();
    }

    public void addGame(GameStats game) {
        games.add(game);
    }

    public int getWhiteWins() {
        return (int) games.stream().filter(g -> g.outcome == GameStatus.WHITE_WINS).count();
    }

    public int getBlackWins() {
        return (int) games.stream().filter(g -> g.outcome == GameStatus.BLACK_WINS).count();
    }

    public int getDraws() {
        return (int) games.stream().filter(g -> g.outcome == GameStatus.DRAW || g.outcome == GameStatus.STALEMATE).count();
    }

    public long getTotalNodes() {
        return games.stream().mapToLong(g -> g.totalNodes).sum();
    }

    public double getAvgNodesPerGame() {
        return games.isEmpty() ? 0 : (double) getTotalNodes() / games.size();
    }

    public double getAvgTimePerGame() {
        return games.stream().mapToLong(g -> g.totalTimeMs).average().orElse(0);
    }

    public double getAvgMemoryPerGame() {
        return games.stream().mapToLong(g -> g.peakMemoryBytes).average().orElse(0);
    }

    @Override
    public String toString() {
        return String.format("%s vs %s | Games: %d | W: %d | B: %d | D: %d | Avg Nodes: %.0f | Avg Time: %.0fms | Avg Mem: %.0fKB",
                whiteAlgorithm, blackAlgorithm, games.size(),
                getWhiteWins(), getBlackWins(), getDraws(),
                getAvgNodesPerGame(), getAvgTimePerGame(), getAvgMemoryPerGame() / 1024);
    }

}
