package dev.chess.cheat.UI;

import com.google.gson.JsonObject;
import com.sun.javafx.scene.control.InputField;
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
import javafx.scene.layout.*;
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

    // Connection components
    private VBox centerBox;
    private Button connectButton;
    private TextField token;

    // In Game components
    private VBox gameBox;
    private Button connectionButton; // Toggle Connect/disconnect

    // Game state
    private boolean connected = false;

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
        this.usernameDisplay = new Label("");
        this.usernameDisplay.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        this.usernameDisplay.setVisible(false);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(connectionStatus, spacer, usernameDisplay);
        root.setTop(header);

        // Console display
        VBox consoleWrapper = new VBox(console.getNode());
        consoleWrapper.setPadding(new Insets(0, 10, 0, 0));
        VBox.setVgrow(console.getNode(), Priority.ALWAYS);
        root.setRight(consoleWrapper);

        // Connect state
        this.token = new TextField("");

        this.connectButton = new Button("Connect");
        this.connectButton.setOnAction(actionEvent -> connect(token.getText()));

        centerBox = new VBox(10); // store it in the field
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(new Label("Enter your LiChess Token:"), token, connectButton);
        root.setLeft(centerBox); // keep it in the scene

        // Game state
        this.connectionButton = new Button("Queue"); // queue for game against AI or player

        gameBox = new VBox(10);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(connectionButton);

        root.setLeft(gameBox);

        root.setStyle("-fx-background-color: #282424;");
        return new Scene(root, 900, 500);
    }

    public void connect(String token) {
        if (token == null || token.isEmpty()) {
            console.log("Please enter a token!");
            return;
        }

        this.client = new LiChessClient(token);
        this.client.setEventListener(this);

        console.log("Connecting to LiChess...");
        new Thread(() -> {
            boolean success = client.establishConnection();

            Platform.runLater(() -> {
                if (success) {
                    connected = true;
                    connectionStatus.setText("Connected");
                    connectionStatus.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 16px; -fx-font-weight: bold;");
                    console.log("Successfully connected!");

                    String username = client.getOurUsername();
                    if (username != null && !username.isEmpty()) {
                        usernameDisplay.setText("Username: " + username);
                        usernameDisplay.setVisible(true);
                        console.log("Logged in as: " + username);
                    }

                    centerBox.setVisible(false);
                    centerBox.setManaged(false);

                    gameBox.setVisible(true);
                    gameBox.setManaged(true);
                } else {
                    connected = false;
                    connectionStatus.setText("Connection failed");
                    connectionStatus.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
                    console.log("Failed to connect. Check your token.");

                    centerBox.setVisible(true);
                    centerBox.setManaged(true);
                    gameBox.setVisible(false);
                    gameBox.setManaged(false);
                }
            });
        }).start();

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