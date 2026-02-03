package dev.chess.cheat.UI;

import com.google.gson.JsonObject;
import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Impl.AlphaBetaAlgorithm;
import dev.chess.cheat.Evaluation.MasterEvaluator;
import dev.chess.cheat.Network.Impl.LiChessClient;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.UI.Viewer.ConsoleViewer;
import dev.chess.cheat.Util.Interface.ILiChessEvents;
import dev.chess.cheat.Util.Interface.SceneMaker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class LiChessUI implements SceneMaker, ILiChessEvents {

    private final Stage stage;

    private final ConsoleViewer console;

    // Simulation setup
    private LiChessClient client;
    private ChessEngine engine;

    // UI Components
    private Label connectionStatus;
    private Label usernameDisplay;

    public LiChessUI(Stage stage) {
        this.stage = stage;
        this.console = new ConsoleViewer();
    }


    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();

        // Header bar (top)
        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e1e1e;");

        this.connectionStatus = new Label("Disconnected");
        this.connectionStatus.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Display our username if were connected -> else: don't display
        this.usernameDisplay = new Label(this.client.getOurUsername() == null ? "" : this.client.getOurUsername());
        this.usernameDisplay.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: right");

        header.getChildren().add(connectionStatus);
        header.getChildren().add(usernameDisplay);

        // Attach header to top
        root.setTop(header);

        // Placeholder center content
        VBox center = new VBox();
        center.setPadding(new Insets(10));
        root.setCenter(center);

        return new Scene(root, 600, 500);
    }

    @Override
    public void onGameStart(String gameId, JsonObject gameData) {
        Platform.runLater(() -> {

        });
    }

    @Override
    public void onGameFinish(String gameId, JsonObject gameData) {
        Platform.runLater(() -> {

        });
    }

    @Override
    public void onChallengeReceived(String challengeId, JsonObject challengeData) {

    }

    @Override
    public void onChallengeCanceled(String challengeId, JsonObject challengeData) {

    }

    @Override
    public void onChallengeAccepted(String challengeId, JsonObject challengeData) {

    }
}