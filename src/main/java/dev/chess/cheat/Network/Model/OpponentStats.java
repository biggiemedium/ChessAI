package dev.chess.cheat.Network.Model;

public class OpponentStats {

    private String username;
    private String id;

    private int games;
    private int rating;

    // API CALLS
    private String BASE_API = "https://lichess.org/api/";
    private String USER_STATE = BASE_API + "user/{username}";
}
