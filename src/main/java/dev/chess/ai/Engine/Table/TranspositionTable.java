package dev.chess.ai.Engine.Table;

import dev.chess.ai.Engine.Move.Move;

import java.util.Arrays;

public class TranspositionTable {

    private TranspositionTableEntry[] table;
    private final int size;

    public TranspositionTable(int size) {
        this.size = size;
        this.table = new TranspositionTableEntry[size];
    }


    /**
     * Returns the TranspositionTableEntry for the given zobristHash if it exists
     * Currently, this uses a single-index lookup (no collision resolution implemented yet).
     *
     * https://en.wikipedia.org/wiki/Linear_probing
     * https://www.geeksforgeeks.org/dsa/implementing-hash-table-open-addressing-linear-probing-cpp/
     * https://www.geeksforgeeks.org/dsa/quadratic-probing-in-hashing/
     *
     * @param zobristHash
     * @return the table entry if found, null otherwise
     */
    public TranspositionTableEntry probe(long zobristHash) {
        int index = (int) (Math.abs(zobristHash) % size);
        TranspositionTableEntry entry = table[index];
        if (entry != null && entry.zobristHash == zobristHash) {
            return entry;
        }
        return null;
    }

    /**
     * Stores a new transposition entry, replacing the existing one if
     *  1. The slot is empty
     *  2.The new depth is >= existing depth (deeper search)
     *
     * @param zobristHash
     * @param score
     * @param depth
     * @param flag
     * @param bestMove
     * @param age
     */
    public void store(long zobristHash, int score, int depth, byte flag, Move bestMove, byte age) {
        int index = (int) (Math.abs(zobristHash) % size);
        TranspositionTableEntry existing = table[index];

        if (existing == null || depth >= existing.depth) {
            table[index] = new TranspositionTableEntry(zobristHash, score, depth, flag, bestMove, age);
        }
    }

    public void clear() {
        Arrays.fill(table, null);
    }

}
