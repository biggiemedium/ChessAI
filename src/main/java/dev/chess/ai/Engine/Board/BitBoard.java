package dev.chess.ai.Engine.Board;

/**
 * https://www.reddit.com/r/chessprogramming/comments/vywozv/eli5_bitboards_potentially_magic_bitboards/
 *
 */
public class BitBoard {

    /**
     * NOTES FOR CODING:
     * "~" negates the bits (1 to 0 and vice versa)
     * "|" requires either corresponding bit to be 1 for the resulting bit to be 1
     * "&" requires both the bits to be 1 in order for the result to be 1
     * "^" Compares corresponding bits and sets the result bit to 1 if the bits are different; otherwise, it's 0
     * "<<" Shift left
     * ">>" Shift right
     *
     * A bitboard is a 64-bit long where each bit represents one square.
     * Bit 0 = A1, Bit 1 = B1, ..., Bit 7 = H1
     * Bit 8 = A2, ..., Bit 63 = H8
     */

    private long board;

    public BitBoard() {
        this.board = 0L; // 00000000
    }

    public void setBit(int square) {
        this.board |= (1L << square);
    }

    public void clear() {
        board = 0L;
    }

    public long getBoard() {
        return board;
    }

    public void setBoard(long board) {
        this.board = board;
    }
}
