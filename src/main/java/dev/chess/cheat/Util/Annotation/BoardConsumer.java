package dev.chess.cheat.Util.Annotation;

@FunctionalInterface
public interface BoardConsumer {
    void accept(int row, int col);
}
