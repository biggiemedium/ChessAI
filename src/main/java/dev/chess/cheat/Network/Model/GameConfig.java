package dev.chess.cheat.Network.Model;

/**
 * Configuration for creating a new game
 */
public class GameConfig {
    private boolean rated;
    private int timeLimit; // seconds
    private int increment; // seconds
    private String color; // "white", "black", or "random"
    private int level; // AI level (1-8 for Lichess)

    public GameConfig() {
        this.rated = false;
        this.timeLimit = 600; // 10 minutes default
        this.increment = 0;
        this.color = "random";
        this.level = 1;
    }

    // Builder pattern
    public GameConfig rated(boolean rated) {
        this.rated = rated;
        return this;
    }

    public GameConfig timeLimit(int seconds) {
        this.timeLimit = seconds;
        return this;
    }

    public GameConfig increment(int seconds) {
        this.increment = seconds;
        return this;
    }

    public GameConfig color(String color) {
        this.color = color;
        return this;
    }

    public GameConfig level(int level) {
        this.level = level;
        return this;
    }

    // Getters
    public boolean isRated() { return rated; }
    public int getTimeLimit() { return timeLimit; }
    public int getIncrement() { return increment; }
    public String getColor() { return color; }
    public int getLevel() { return level; }
}