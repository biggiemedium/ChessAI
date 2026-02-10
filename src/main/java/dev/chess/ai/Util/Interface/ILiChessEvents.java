package dev.chess.ai.Util.Interface;

import com.google.gson.JsonObject;

public interface ILiChessEvents {

    void onConnected(String username);

    void onGameStarted(String gameId);

    void onGameFinished(String gameId);

    void onError(Throwable t);

    default void onChallengeReceived(JsonObject challenge) {
    }

    default void onChallengeCanceled(String challengeId) {
    }

    default void onChallengeDeclined(String challengeId) {
    }

}
