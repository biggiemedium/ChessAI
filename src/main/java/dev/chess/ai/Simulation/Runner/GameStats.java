package dev.chess.ai.Simulation.Runner;

import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Simulation.GameStatus;

import java.util.ArrayList;
import java.util.List;

public class GameStats {

    public final int gameNumber;
    public final List<MoveStats> moves;
    public GameStatus outcome;
    public long totalTimeMs;
    public long totalNodes;
    public long peakMemoryBytes;

    public GameStats(int gameNumber) {
        this.gameNumber = gameNumber;
        this.moves = new ArrayList<>();
    }

    public void addMove(Move move, long timeMs, int nodes, long memoryBytes) {
        moves.add(new MoveStats(move, timeMs, nodes, memoryBytes));
        totalTimeMs += timeMs;
        totalNodes += nodes;
        if (memoryBytes > peakMemoryBytes) {
            peakMemoryBytes = memoryBytes;
        }
    }

    public static class MoveStats {
        public final Move move;
        public final long timeMs;
        public final int nodes;
        public final long memoryBytes;

        public MoveStats(Move move, long timeMs, int nodes, long memoryBytes) {
            this.move = move;
            this.timeMs = timeMs;
            this.nodes = nodes;
            this.memoryBytes = memoryBytes;
        }
    }

}
