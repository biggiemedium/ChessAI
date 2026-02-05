package dev.chess.cheat.Network.Impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.chess.cheat.Network.ChessClient;
import dev.chess.cheat.Util.Annotation.Value;
import dev.chess.cheat.Util.Interface.ILiChessEvents;
import dev.chess.cheat.Util.PropertyLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Crash out, Crash out, Crash out, Crash out, Crash out, Crash out, Crash out
 *
 * TODO:
 * Challenge Player -> Challenge players not just AI | /api/challenge/{username}
 * List Challenges -> Show people who challenge you | /api/challenge | For details about challenge -> /api/challenge/{challengeId}/show
 * <p>
 * BOT:
 * Claim Vicotory if opponent disconnects | /api/bot/game/{gameId}/claim-victory
 * Claim Draw if opponent disconnects for x time | /api/bot/game/{gameId}/claim-draw
 * Send Chat messages | /api/bot/game/{gameId}/chat
 * Event based system | /api/stream/event -> (GameStartEvent, GameFinishEvent, ChallengeEvent, ChallengeCancelEvent, ChallengeAcceptEvent)
 */
public class LiChessClient {

    private static final String API_BASE = "https://lichess.org/api";

    @Value(key = "LICHESS_API_KEY")
    private String oauthToken;

    private Gson gson;
    private ILiChessEvents eventListener;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Connection state
    private String ourUsername;
    private String currentGameId;
    private String currentGameColor; // "white" or "black"
    private boolean isOurTurn;
    private volatile boolean streaming = false;

    private final Map<String, Boolean> activeGameStreams = new ConcurrentHashMap<>();

    public LiChessClient(String oauthToken) {
        this.oauthToken = oauthToken;
        this.gson = new Gson();
    }

    public LiChessClient(String oauthToken, Gson gson) {
        this.oauthToken = oauthToken;
        this.gson = gson;
    }

    /**
     * Establish connection and verify credentials
     * API: GET /api/account
     *
     * Get public information about the logged in user, including:
     * - id, username, url
     * - perfs: Performance ratings for all game types (bullet, blitz, rapid, etc.)
     * - count: Game statistics (all, rated, wins, losses, draws, etc.)
     * - createdAt, seenAt: Account timestamps
     * - playTime: Total play time statistics
     * - profile: User profile (bio, location, flag, ratings from other organizations)
     * - title: Player title if titled (GM, IM, FM, etc.) or BOT
     * - patron, patronColor: Patron status
     * - flair: User flair
     * - streaming: Whether user is currently streaming
     * - followable, following, blocking: Social features (OAuth only)
     * - disabled, tosViolation: Account status flags
     *
     * @return true if connection successful and user authenticated
     */
    public boolean establishConnection() {
        try {
            if (oauthToken == null || oauthToken.trim().isEmpty()) {
                System.err.println("ERROR: OAuth token is null or empty!");
                System.err.println("Check your properties file and ensure LICHESS_API_KEY is set.");
                return false;
            }

            // Trim whitespace
            oauthToken = oauthToken.trim();

            JsonObject account = httpGet("/account");
            if (account == null) return false;

            this.ourUsername = account.get("username").getAsString();

            if (eventListener != null) {
                eventListener.onConnected(ourUsername);
            }
            return true;
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Get full account information
     * API: GET /api/account
     *
     * Returns the complete account object with all profile data, ratings, statistics, etc.
     * Same endpoint as establishConnection() but returns the full JSON object.
     *
     * @return JsonObject with complete account data, or null on error
     */
    public JsonObject getAccountInfo() {
        try {
            return httpGet("/account");
        } catch (Exception e) {
            notifyError(e);
            return null;
        }
    }


    public void closeConnection() {
        streaming = false;
        executor.shutdownNow();
    }

    /**
     * Start listening for global events -> game starts, challenges, etc...
     * API: GET /api/stream/event
     * <p>
     * Stream incoming events in real time as ndjson.
     * An empty line is sent every 7 seconds for keep alive.
     * <p>
     * Event types:
     * - gameStart: Start of a game
     * - gameFinish: Completion of a game
     * - challenge: A player sends you a challenge or you challenge someone
     * - challengeCanceled: A player cancels their challenge to you
     * - challengeDeclined: The opponent declines your challenge
     */
    public void startGlobalEventStream() {
        if (streaming) {
            System.err.println("Event stream already running");
            return;
        }

        streaming = true;
        executor.submit(() -> {
            try {
                HttpURLConnection conn = httpConnection("/stream/event", "GET");
                conn.setReadTimeout(0); // Infinite timeout for streaming

                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                    String line;
                    while (streaming && (line = reader.readLine()) != null) {
                        if (line.isBlank()) continue;

                        JsonObject event = gson.fromJson(line, JsonObject.class);
                        handleGlobalEvent(event);
                    }
                }
            } catch (Exception e) {
                if (streaming) { // Only notify if not intentionally stopped
                    notifyError(e);
                }
            } finally {
                streaming = false;
            }
        });
    }

    /**
     * Handle incoming global events from /api/stream/event
     */
    private void handleGlobalEvent(JsonObject event) {
        if (!event.has("type")) return;

        String type = event.get("type").getAsString();

        switch (type) {
            case "gameStart" -> {
                if (event.has("game")) {
                    JsonObject game = event.getAsJsonObject("game");

                    // Extract game ID
                    String gameId = game.has("gameId") ?
                            game.get("gameId").getAsString() :
                            game.get("id").getAsString();

                    this.currentGameId = gameId;

                    // Extract our color
                    if (game.has("color")) {
                        this.currentGameColor = game.get("color").getAsString();
                        System.out.println("Game started - Playing as: " + currentGameColor);
                    }

                    // Extract turn information
                    if (game.has("isMyTurn")) {
                        this.isOurTurn = game.get("isMyTurn").getAsBoolean();
                    }

                    // Log opponent info
                    if (game.has("opponent")) {
                        JsonObject opponent = game.getAsJsonObject("opponent");
                        String oppName = opponent.has("username") ?
                                opponent.get("username").getAsString() : "AI";
                        int oppRating = opponent.has("rating") ?
                                opponent.get("rating").getAsInt() : 0;
                        System.out.println("Opponent: " + oppName + " (" + oppRating + ")");
                    }

                    // Notify listener
                    if (eventListener != null) {
                        eventListener.onGameStarted(gameId);
                    }
                }
            }

            case "gameFinish" -> {
                if (event.has("game")) {
                    JsonObject game = event.getAsJsonObject("game");

                    String gameId = game.has("gameId") ?
                            game.get("gameId").getAsString() :
                            game.get("id").getAsString();

                    // Log game result
                    if (game.has("winner")) {
                        String winner = game.get("winner").getAsString();
                        System.out.println("Game finished - Winner: " + winner);
                    }

                    // Notify listener
                    if (eventListener != null) {
                        eventListener.onGameFinished(gameId);
                    }

                    // Clear current game state
                    if (gameId.equals(currentGameId)) {
                        currentGameId = null;
                        currentGameColor = null;
                        isOurTurn = false;
                    }
                }
            }

            case "challenge" -> {
                if (event.has("challenge")) {
                    JsonObject challenge = event.getAsJsonObject("challenge");

                    // Log challenge details
                    String challengeId = challenge.has("id") ?
                            challenge.get("id").getAsString() : "unknown";
                    String challengerName = "unknown";

                    if (challenge.has("challenger")) {
                        JsonObject challenger = challenge.getAsJsonObject("challenger");
                        if (challenger.has("name")) {
                            challengerName = challenger.get("name").getAsString();
                        }
                    }

                    System.out.println("Challenge received from " + challengerName +
                            " (ID: " + challengeId + ")");

                    // Notify listener
                    if (eventListener != null) {
                        eventListener.onChallengeReceived(challenge);
                    }
                }
            }

            case "challengeCanceled" -> {
                if (event.has("challenge")) {
                    JsonObject challenge = event.getAsJsonObject("challenge");
                    String challengeId = challenge.has("id") ?
                            challenge.get("id").getAsString() : "unknown";

                    System.out.println("Challenge canceled: " + challengeId);

                    if (eventListener != null) {
                        eventListener.onChallengeCanceled(challengeId);
                    }
                }
            }

            case "challengeDeclined" -> {
                if (event.has("challenge")) {
                    JsonObject challenge = event.getAsJsonObject("challenge");
                    String challengeId = challenge.has("id") ?
                            challenge.get("id").getAsString() : "unknown";

                    System.out.println("Challenge declined: " + challengeId);

                    if (eventListener != null) {
                        eventListener.onChallengeDeclined(challengeId);
                    }
                }
            }

            default -> {
                System.out.println("Unknown event type: " + type);
            }
        }
    }

    /**
     * Stream the state of a game being played with the Bot API
     * API: GET /api/bot/game/stream/{gameId}
     *
     * Stream the state of a game as ndjson. Use this endpoint to get updates about the game in real-time.
     * Each line is a JSON object containing a type field. Possible values are:
     *
     * - gameFull: Full game data. All values are immutable, except for the state field.
     *   Contains: id, variant, speed, perf, rated, createdAt, white, black, initialFen, clock, state
     *
     * - gameState: Current state of the game. Immutable values not included.
     *   Contains: type, moves (UCI format), wtime, btime, winc, binc, status, winner (if finished)
     *   Optional: bdraw, wdraw, btakeback, wtakeback (draw/takeback offers)
     *   Optional: expiration (for games where first move hasn't been made)
     *
     * - chatLine: Chat message sent by a user in the room "player" or "spectator"
     *
     * - opponentGone: Whether the opponent has left the game, and how long before you can claim win/draw
     *
     * The first line is always of type gameFull.
     *
     * Note: This method starts a background thread. Use the provided callback to handle events.
     *
     * @param gameId The game ID
     * @param callback Callback interface to handle game events
     * @return true if stream started successfully
     */
    public boolean streamBotGame(String gameId, GameStreamCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        activeGameStreams.put(gameId, true);

        executor.submit(() -> {
            try {
                HttpURLConnection conn = httpConnection("/bot/game/stream/" + gameId, "GET");
                conn.setReadTimeout(0); // Infinite timeout for streaming

                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                    String line;
                    while (activeGameStreams.getOrDefault(gameId, true) &&
                            (line = reader.readLine()) != null) {
                        if (line.isBlank()) continue;

                        JsonObject event = gson.fromJson(line, JsonObject.class);

                        if (!event.has("type")) continue;

                        String type = event.get("type").getAsString();

                        switch (type) {
                            case "gameFull" -> callback.onGameFull(gameId, event);
                            case "gameState" -> callback.onGameState(gameId, event);
                            case "chatLine" -> callback.onChatLine(gameId, event);
                            case "opponentGone" -> callback.onOpponentGone(gameId, event);
                            default -> System.out.println("Unknown game event type: " + type);
                        }
                    }
                }
            } catch (Exception e) {
                callback.onError(gameId, e);
            } finally {
                activeGameStreams.remove(gameId);
            }
        });

        return true;
    }

    /**
     * Make a move in a game being played with the Bot API (without draw offer)
     * API: POST /api/bot/game/{gameId}/move/{move}
     *
     * @param gameId The game ID
     * @param move The move to play in UCI format (e.g., "e2e4", "e7e8q" for promotion)
     * @return true if successful
     */
    public boolean makeBotMove(String gameId, String move) {
        return makeBotMove(gameId, move, false);
    }

    /**
     * Make a move in a game being played with the Bot API
     * API: POST /api/bot/game/{gameId}/move/{move}
     *
     * The move can also contain a draw offer/agreement.
     *
     * @param gameId The game ID
     * @param move The move to play in UCI format (e.g., "e2e4", "e7e8q" for promotion)
     * @param offeringDraw Whether to offer (or agree to) a draw
     * @return true if successful
     */
    public boolean makeBotMove(String gameId, String move, boolean offeringDraw) {
        try {
            if (move == null || move.isEmpty()) {
                throw new IllegalArgumentException("Move cannot be empty");
            }

            String endpoint = "/bot/game/" + gameId + "/move/" + move;

            // Add offeringDraw as query parameter if true
            if (offeringDraw) {
                endpoint += "?offeringDraw=true";
            }

            JsonObject response = httpPost(endpoint, "");

            if (response != null && response.has("error")) {
                String error = response.get("error").getAsString();
                if (error.contains("Not your turn") || error.contains("game already over")) {
                    System.out.println("Move rejected - game ended: " + error);
                    return false; // Handle without throwing
                }
            }

            return response != null && response.has("ok") && response.get("ok").getAsBoolean();
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * List all current challenges (incoming and outgoing)
     * API: GET /api/challenge
     *
     * Returns an object with two arrays:
     * - "in": Incoming challenges (targeted at you)
     * - "out": Outgoing challenges (created by you)
     *
     * Each challenge contains:
     * - id: Challenge ID
     * - url: Challenge URL
     * - status: "created", "offline", "canceled", "declined", or "accepted"
     * - challenger: User who created the challenge
     * - destUser: Target user (if specified)
     * - variant: Chess variant being played
     * - rated: Whether the game is rated
     * - speed: Time control speed
     * - timeControl: Time control settings
     * - color: Color preference ("white", "black", "random")
     * - perf: Performance category
     * - direction: "in" or "out"
     *
     * @return JsonObject with "in" and "out" arrays, or null on error
     */
    public JsonObject listChallenges() {
        try {
            return httpGet("/challenge");
        } catch (Exception e) {
            notifyError(e);
            return null;
        }
    }

    /**
     * Get details about a specific challenge
     * API: GET /api/challenge/{challengeId}/show
     *
     * Returns detailed information about a challenge:
     * - id: Challenge ID
     * - url: Challenge URL
     * - status: "created", "offline", "canceled", "declined", or "accepted"
     * - challenger: User who created the challenge (with id, name, rating, etc.)
     * - destUser: Target user if specified (null for open challenges)
     * - variant: Chess variant (key, name, short)
     * - rated: Whether the game is rated
     * - speed: Time control speed ("ultraBullet", "bullet", "blitz", "rapid", "classical", "correspondence")
     * - timeControl: Time control settings (type, limit, increment)
     * - color: Color preference ("white", "black", "random")
     * - finalColor: Actual assigned color ("white" or "black")
     * - perf: Performance category (icon, name)
     * - direction: "in" (incoming) or "out" (outgoing)
     * - initialFen: Starting FEN if variant is "fromPosition"
     * - rematchOf: ID of game this is a rematch of (if applicable)
     *
     * @param challengeId The challenge ID
     * @return JsonObject with challenge details, or null on error
     */
    public JsonObject getChallengeInfo(String challengeId) {
        try {
            return httpGet("/challenge/" + challengeId + "/show");
        } catch (Exception e) {
            notifyError(e);
            return null;
        }
    }

    /**
     * Accept an incoming challenge with no color preference
     * API: POST /api/challenge/{challengeId}/accept
     *
     * @param challengeId The challenge ID
     * @return true if successful
     */
    public boolean acceptChallenge(String challengeId) {
        return acceptChallenge(challengeId, null);
    }

    /**
     * Accept an incoming challenge
     * API: POST /api/challenge/{challengeId}/accept
     *
     * You should receive a gameStart event on the incoming events stream.
     *
     * @param challengeId The challenge ID
     * @param color Accept challenge as this color (only valid if this is an open challenge)
     *              Valid values: "white", "black", or null for no preference
     * @return true if successful
     */
    public boolean acceptChallenge(String challengeId, String color) {
        try {
            String endpoint = "/challenge/" + challengeId + "/accept";

            // Add color as query parameter if specified
            if (color != null && !color.isEmpty()) {
                endpoint += "?color=" + urlEncode(color);
            }

            JsonObject response = httpPost(endpoint, "");
            return response != null && response.has("ok") && response.get("ok").getAsBoolean();
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Decline an incoming challenge with no specific reason
     * API: POST /api/challenge/{challengeId}/decline
     *
     * @param challengeId The challenge ID
     * @return true if successful
     */
    public boolean declineChallenge(String challengeId) {
        return declineChallenge(challengeId, null);
    }

    /**
     * Decline an incoming challenge
     * API: POST /api/challenge/{challengeId}/decline
     *
     * @param challengeId The challenge ID
     * @param reason Reason for declining (optional, can be null)
     *               Valid reasons: "generic", "later", "tooFast", "tooSlow",
     *               "timeControl", "rated", "casual", "standard", "variant",
     *               "noBot", "onlyBot"
     * @return true if successful
     */
    public boolean declineChallenge(String challengeId, String reason) {
        try {
            String params = "";
            if (reason != null && !reason.isEmpty()) {
                params = "reason=" + urlEncode(reason);
            }

            JsonObject response = httpPost("/challenge/" + challengeId + "/decline", params);
            return response != null && response.has("ok") && response.get("ok").getAsBoolean();
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Cancel a challenge you sent (without opponent token)
     * API: POST /api/challenge/{challengeId}/cancel
     *
     * @param challengeId The challenge ID
     * @return true if successful
     */
    public boolean cancelChallenge(String challengeId) {
        return cancelChallenge(challengeId, null);
    }

    /**
     * Cancel a challenge you sent, or abort the game if accepted but not yet played
     * API: POST /api/challenge/{challengeId}/cancel
     *
     * Note: The ID of a game is the same as the ID of the challenge that created it.
     * Works for user challenges and open challenges alike.
     *
     * @param challengeId The challenge ID (or game ID if challenge was accepted)
     * @param opponentToken Optional challenge:write token of the opponent.
     *                      If set, the game can be canceled even if both players have moved
     * @return true if successful
     */
    public boolean cancelChallenge(String challengeId, String opponentToken) {
        try {
            String endpoint = "/challenge/" + challengeId + "/cancel";

            // Add opponentToken as query parameter if specified
            if (opponentToken != null && !opponentToken.isEmpty()) {
                endpoint += "?opponentToken=" + urlEncode(opponentToken);
            }

            JsonObject response = httpPost(endpoint, "");
            return response != null && response.has("ok") && response.get("ok").getAsBoolean();
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Challenge the AI for correspondence game (no clock)
     * API: POST /api/challenge/ai
     *
     * @param level AI difficulty (1-8)
     * @param daysPerMove Days per move (1, 2, 3, 5, 7, 10, or 14)
     * @return JsonObject with game details, or null on error
     */
    public JsonObject challengeAICorrespondence(int level, int daysPerMove) {
        return challengeAI(level, null, null, null, null, null, daysPerMove);
    }

    /**
     * Challenge the AI with standard variant (timed game)
     * API: POST /api/challenge/ai
     *
     * @param level AI difficulty (1-8)
     * @param clockLimitMinutes Initial time in minutes
     * @param clockIncrementSeconds Increment per move in seconds
     * @param color Color preference ("white", "black", "random", or null)
     * @return JsonObject with game details, or null on error
     */
    public JsonObject challengeAI(int level, int clockLimitMinutes, int clockIncrementSeconds, String color) {
        return challengeAI(level, clockLimitMinutes * 60, clockIncrementSeconds, color, null, null, null);
    }

    /**
     * Challenge the AI with basic settings (timed game)
     * API: POST /api/challenge/ai
     *
     * @param level AI difficulty (1-8)
     * @param clockLimitMinutes Initial time in minutes
     * @param clockIncrementSeconds Increment per move in seconds
     * @return JsonObject with game details, or null on error
     */
    public JsonObject challengeAI(int level, int clockLimitMinutes, int clockIncrementSeconds) {
        return challengeAI(level, clockLimitMinutes * 60, clockIncrementSeconds, null, null, null, null);
    }

    /**
     * Challenge the AI (Stockfish)
     * API: POST /api/challenge/ai
     *
     * Start a game with Lichess AI. You will be notified on the event stream that a new game has started.
     *
     * @param level AI difficulty (1-8, required)
     * @param clockLimit Clock initial time in seconds (0-10800). If 0 or null, creates correspondence game
     * @param clockIncrement Clock increment in seconds (0-60). If 0 or null, creates correspondence game
     * @param color Color preference ("white", "black", "random", or null for random)
     * @param variant Variant name ("standard", "chess960", "crazyhouse", "antichess", "atomic",
     *                "horde", "kingOfTheHill", "racingKings", "threeCheck", "fromPosition", or null for standard)
     * @param fen Custom initial position in X-FEN format (only for "standard", "fromPosition", or valid "chess960" positions)
     * @param days Days per move for correspondence games (1, 2, 3, 5, 7, 10, or 14). Clock settings must be omitted
     * @return JsonObject with game details (id, fullId, variant, speed, perf, rated, fen, status, player, etc.), or null on error
     */
    public JsonObject challengeAI(int level, Integer clockLimit, Integer clockIncrement,
                                  String color, String variant, String fen, Integer days) {
        try {
            if (level < 1 || level > 8) {
                throw new IllegalArgumentException("AI level must be between 1 and 8");
            }

            StringBuilder params = new StringBuilder();
            params.append("level=").append(level);

            boolean isCorrespondence = (days != null);

            if (isCorrespondence) {
                // Correspondence game -> only include days (NO clock setting defined)
                params.append("&days=").append(days);
            } else {
                int limit = (clockLimit != null) ? clockLimit : 300;  // 5 min
                int increment = (clockIncrement != null) ? clockIncrement : 0;  // 0

                if (limit < 0 || limit > 10800) {
                    throw new IllegalArgumentException("Clock limit must be between 0 and 10800 seconds");
                }
                if (increment < 0 || increment > 60) {
                    throw new IllegalArgumentException("Clock increment must be between 0 and 60 seconds");
                }

                params.append("&clock.limit=").append(limit);
                params.append("&clock.increment=").append(increment);
            }

            if (color != null && !color.isEmpty()) {
                params.append("&color=").append(urlEncode(color));
            }

            if (variant != null && !variant.isEmpty()) {
                params.append("&variant=").append(urlEncode(variant));
            }

            if (fen != null && !fen.isEmpty()) {
                params.append("&fen=").append(urlEncode(fen));
            }

            return httpPost("/challenge/ai", params.toString());
        } catch (Exception e) {
            notifyError(e);
            return null;
        }
    }

    /**
     * Start the clocks of a game immediately
     * API: POST /api/challenge/{gameId}/start-clocks
     *
     * For AI games, omit token2 parameter.
     *
     * @param gameId The game ID
     * @param token1 OAuth token of first player
     * @param token2 OAuth token of second player (null for AI games)
     * @return true if successful
     */
    public boolean startGameClocks(String gameId, String token1, String token2) {
        try {
            StringBuilder endpoint = new StringBuilder("/challenge/" + gameId + "/start-clocks");
            endpoint.append("?token1=").append(urlEncode(token1));

            if (token2 != null && !token2.isEmpty()) {
                endpoint.append("&token2=").append(urlEncode(token2));
            }

            JsonObject response = httpPost(endpoint.toString(), "");
            return response != null && response.has("ok") && response.get("ok").getAsBoolean();
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Claim victory in the current game
     *
     * @return true if successful, false if no game is active or claim failed
     */
    public boolean claimVictoryInCurrentGame() {
        if (currentGameId == null) {
            System.err.println("No active game to claim victory in");
            return false;
        }

        return claimVictory(currentGameId);
    }

    /**
     * Claim victory when the opponent has left the game for a while
     * API: POST /api/bot/game/{gameId}/claim-victory
     *
     * This can only be used after the opponent has been gone for a certain amount of time.
     * You will receive an "opponentGone" event in the game stream when this becomes available.
     *
     * @param gameId The game ID
     * @return true if successful
     */
    public boolean claimVictory(String gameId) {
        try {
            if (gameId == null || gameId.isEmpty()) {
                throw new IllegalArgumentException("Game ID cannot be null or empty");
            }

            String endpoint = "/bot/game/" + gameId + "/claim-victory";
            JsonObject response = httpPost(endpoint, "");

            if (response != null && response.has("ok") && response.get("ok").getAsBoolean()) {
                System.out.println("Successfully claimed victory in game: " + gameId);

                // Clear current game state if this was the active game
                if (gameId.equals(currentGameId)) {
                    currentGameId = null;
                    currentGameColor = null;
                    isOurTurn = false;
                }

                return true;
            }

            // Check if there's an error message
            if (response != null && response.has("error")) {
                System.err.println("Cannot claim victory: " + response.get("error").getAsString());
            }

            return false;
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Add seconds to the opponent's clock
     * API: POST /api/round/{gameId}/add-time/{seconds}
     *
     * Can be used to create games with time odds.
     *
     * @param gameId The game ID
     * @param seconds How many seconds to give (5-60)
     * @return true if successful
     */
    public boolean addTimeToOpponent(String gameId, int seconds) {
        try {
            if (seconds < 5 || seconds > 60) {
                throw new IllegalArgumentException("Seconds must be between 5 and 60");
            }

            String endpoint = "/round/" + gameId + "/add-time/" + seconds;
            JsonObject response = httpPost(endpoint, "");
            return response != null && response.has("ok") && response.get("ok").getAsBoolean();
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    /**
     * Resign the current game
     *
     * @return true if successful, false if no game is active or resignation failed
     */
    public boolean resignCurrentGame() {
        if (currentGameId == null) {
            System.err.println("No active game to resign");
            return false;
        }

        return resignGame(currentGameId);
    }

    /**
     * Resign a game being played with the Bot API
     * API: POST /api/bot/game/{gameId}/resign
     *
     * @param gameId The game ID
     * @return true if successful
     */
    public boolean resignGame(String gameId) {
        try {
            if (gameId == null || gameId.isEmpty()) {
                throw new IllegalArgumentException("Game ID cannot be null or empty");
            }

            String endpoint = "/bot/game/" + gameId + "/resign";
            JsonObject response = httpPost(endpoint, "");

            if (response != null && response.has("ok") && response.get("ok").getAsBoolean()) {
                System.out.println("Successfully resigned from game: " + gameId);

                // Clear current game state if this was the active game
                if (gameId.equals(currentGameId)) {
                    currentGameId = null;
                    currentGameColor = null;
                    isOurTurn = false;
                }

                return true;
            }

            return false;
        } catch (Exception e) {
            notifyError(e);
            return false;
        }
    }

    public void setEventListener(ILiChessEvents listener) {
        this.eventListener = listener;
    }

    private void notifyError(Throwable t) {
        if (eventListener != null) {
            eventListener.onError(t);
        }
    }

    public void shutdown() {
        closeConnection();
    }

    private HttpURLConnection httpConnection(String endpoint, String method) throws Exception {
        URL url = new URL(API_BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);

        System.out.println("Connecting to: " + API_BASE + endpoint);
        System.out.println("Token present: " + (oauthToken != null && !oauthToken.isEmpty()));

        conn.setRequestProperty("Authorization", "Bearer " + oauthToken);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private JsonObject httpGet(String endpoint) throws Exception {
        HttpURLConnection conn = httpConnection(endpoint, "GET");
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            return gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
        } else {
            System.err.println("HTTP GET failed: " + responseCode + " for " + endpoint);
            return null;
        }
    }

    private JsonObject httpPost(String endpoint, String params) throws Exception {
        HttpURLConnection conn = httpConnection(endpoint, "POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        if (params != null && !params.isEmpty()) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes("utf-8"));
            }
        }

        int code = conn.getResponseCode();
        if (code == 200 || code == 201) {
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                return gson.fromJson(reader, JsonObject.class);
            }
        } else {
            System.err.println("HTTP POST failed: " + code + " for " + endpoint);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            } catch (Exception ignored) {
            }

            return null;
        }
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    public void stopGameStream(String gameId) {
        activeGameStreams.put(gameId, false);
    }

    public String getOurUsername() {
        return ourUsername;
    }

    public String getCurrentGameId() {
        return currentGameId;
    }

    public String getCurrentGameColor() {
        return currentGameColor;
    }

    public boolean isOurTurn() {
        return isOurTurn;
    }

    public boolean isStreaming() {
        return streaming;
    }

    /**
     * Get the color we're playing in the current game
     * Returns "white", "black", or null if no game active
     */
    public String getOurColorInCurrentGame() {
        // This will be set when the game starts
        // it will be tracked by "GameStartEvent"
        return currentGameColor;
    }

    public interface GameStreamCallback {
        /**
         * Called when full game data is received (always first event)
         * @param gameId The game ID
         * @param gameFull Full game data including id, variant, speed, perf, rated,
         *                 createdAt, white, black, initialFen, clock, and current state
         */
        void onGameFull(String gameId, JsonObject gameFull);

        /**
         * Called when game state updates
         * @param gameId The game ID
         * @param gameState Current state with moves, times, status, etc.
         */
        void onGameState(String gameId, JsonObject gameState);

        /**
         * Called when a chat message is received
         * @param gameId The game ID
         * @param chatLine Chat message data (room, username, text)
         */
        default void onChatLine(String gameId, JsonObject chatLine) {
            // Optional - implement if needed
        }

        /**
         * Called when opponent has left the game
         * @param gameId The game ID
         * @param opponentGone Data about how long before claim is available
         */
        default void onOpponentGone(String gameId, JsonObject opponentGone) {
            // Optional - implement if needed
        }

        /**
         * Called when an error occurs
         * @param gameId The game ID
         * @param error The error
         */
        void onError(String gameId, Throwable error);
    }
}