package dev.chess.cheat.Util.Interface;

import com.google.gson.JsonObject;

public interface ILiChessEvents {

    /**
     * Called when a game starts
     */
    void onGameStart(String gameId, JsonObject gameData);

    /**
     * Called when a game finishes
     */
    void onGameFinish(String gameId, JsonObject gameData);

    /**
     * Called when a challenge is received
     */
    void onChallengeReceived(String challengeId, JsonObject challengeData);

    /**
     * Called when a challenge is canceled or declined
     */
    void onChallengeCanceled(String challengeId, JsonObject challengeData);

    /**
     * Called when a challenge is accepted
     */
    void onChallengeAccepted(String challengeId, JsonObject challengeData);

}
