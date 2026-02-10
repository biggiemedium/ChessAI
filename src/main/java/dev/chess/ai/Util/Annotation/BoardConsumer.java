package dev.chess.ai.Util.Annotation;

@FunctionalInterface
public interface BoardConsumer {
    void accept(int row, int col);
}
