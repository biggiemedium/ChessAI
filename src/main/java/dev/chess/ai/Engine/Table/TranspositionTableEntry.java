package dev.chess.ai.Engine.Table;

import dev.chess.ai.Engine.Move.Move;

/**
 * Transposition tables just let you optimize calculating the best move when you encounter
 * situations where different plays results in the board being the in same end state
 *
 * Essentially once you get to one specific board position you just store the result of
 * your minimax calculation at that position in the transposition table
 *
 * https://www.chessprogramming.org/Transposition_Table
 * https://stackoverflow.com/questions/20009796/transposition-tables
 *
 * Zobrist hashing:
 * https://en.wikipedia.org/wiki/Zobrist_hashing
 */
public class TranspositionTableEntry {

    // https://www.chessprogramming.org/Zobrist_Hashing
    public long zobristHash;

    public int score;

    public int depth;

    public byte flag; // 0=EXACT, 1=LOWER_BOUND(alpha), 2=UPPER_BOUND(beta)

    public Move bestMove;

    public byte age;

    // UTILITY
    private static final byte EXACT = 0;
    private static final byte LOWER_BOUND = 1;
    private static final byte UPPER_BOUND = 2;

    public TranspositionTableEntry(long zobristHash, int score, int depth, byte flag, Move bestMove, byte age) {
        this.zobristHash = zobristHash;
        this.score = score;
        this.depth = depth;
        this.flag = flag;
        this.bestMove = bestMove;
        this.age = age;
    }
}
