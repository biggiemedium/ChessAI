package dev.chess.cheat.Network.Model;

import java.util.HashMap;
import java.util.Map;

public class OpponentStats {

    private String username;
    private String id;
    private int games;
    private int rating;
    private boolean isBot;
    private String title;
    private boolean online;

    private int wins;
    private int losses;
    private int draws;

    private Integer blitzRating;
    private Integer rapidRating;
    private Integer bulletRating;
    private Integer classicalRating;

    private final String oauthToken;

    public OpponentStats(String username) {
        this(username, null);
    }

    public OpponentStats(String username, String oauthToken) {
        this.username = username;
        this.oauthToken = oauthToken;
    }

    /**
     * Get all stats as a map
     * @return Map containing all opponent data
     */
    public Map<String, Object> getStatsMap() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("username", username);
        stats.put("id", id);
        stats.put("isBot", isBot);
        stats.put("title", title);
        stats.put("online", online);
        stats.put("games", games);
        stats.put("wins", wins);
        stats.put("losses", losses);
        stats.put("draws", draws);
        stats.put("winRate", games > 0 ? (wins * 100.0) / games : 0.0);
        stats.put("rating", rating);
        stats.put("blitzRating", blitzRating);
        stats.put("rapidRating", rapidRating);
        stats.put("bulletRating", bulletRating);
        stats.put("classicalRating", classicalRating);

        // Add AI level indicator if it's an AI
        if (isBot && username != null && username.startsWith("AI Level")) {
            stats.put("isAI", true);
        } else {
            stats.put("isAI", false);
        }

        return stats;
    }

    /**
     * Get bot-specific stats as a map
     * @return Map containing bot data, or null if not a bot
     */
    public Map<String, Object> getBotStatsMap() {
        if (!isBot) return null;

        Map<String, Object> botStats = new HashMap<>();
        botStats.put("username", username);
        botStats.put("active", online);
        botStats.put("strength", rating);
        botStats.put("blitzRating", blitzRating);
        botStats.put("rapidRating", rapidRating);
        botStats.put("bulletRating", bulletRating);

        // Only include game stats if they exist (not for AI)
        if (games > 0) {
            botStats.put("games", games);
            botStats.put("record", wins + "W-" + losses + "L-" + draws + "D");
            botStats.put("winRate", games > 0 ? (wins * 100.0) / games : 0.0);
        }

        return botStats;
    }

    // Getters
    public String getUsername() { return username; }
    public String getId() { return id; }
    public int getGames() { return games; }
    public int getRating() { return rating; }
    public boolean isBot() { return isBot; }
    public String getTitle() { return title; }
    public boolean isOnline() { return online; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getDraws() { return draws; }
    public Integer getBlitzRating() { return blitzRating; }
    public Integer getRapidRating() { return rapidRating; }
    public Integer getBulletRating() { return bulletRating; }
    public Integer getClassicalRating() { return classicalRating; }
    public double getWinRate() { return games > 0 ? (wins * 100.0) / games : 0.0; }

    // Setters (package-private for LiChessClient)
    public void setUsername(String username) { this.username = username; }
    public void setId(String id) { this.id = id; }
    public void setGames(int games) { this.games = games; }
    public void setRating(int rating) { this.rating = rating; }
    public void setBot(boolean isBot) { this.isBot = isBot; }
    public void setTitle(String title) { this.title = title; }
    public void setOnline(boolean online) { this.online = online; }
    public void setWins(int wins) { this.wins = wins; }
    public void setLosses(int losses) { this.losses = losses; }
    public void setDraws(int draws) { this.draws = draws; }
    public void setBlitzRating(Integer blitzRating) { this.blitzRating = blitzRating; }
    public void setRapidRating(Integer rapidRating) { this.rapidRating = rapidRating; }
    public void setBulletRating(Integer bulletRating) { this.bulletRating = bulletRating; }
    public void setClassicalRating(Integer classicalRating) { this.classicalRating = classicalRating; }
}