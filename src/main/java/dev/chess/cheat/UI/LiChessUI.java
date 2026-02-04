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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private ToggleGroup gameTypeGroup;
    private RadioButton aiRadio;
    private RadioButton playerRadio;
    private HBox radioButtonBox;
    private Button displayGame;


    // Game state
    private boolean connected = false;
    private boolean inGame = false;

    public LiChessUI(Stage stage) {
        this.stage = stage;
        this.console = new ConsoleViewer();
        this.engine = new ChessEngine(new AlphaBetaAlgorithm(new MasterEvaluator(), new MoveGenerator()));
        console.log("============================");
        console.log(" ");
        console.log("         LICHESS BOT        ");
        console.log(" ");
        console.log("============================");
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
        Label tokenLabel = new Label("Enter your LiChess Token:");
        tokenLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        this.token = new TextField();
        token.setStyle(
                "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-border-color: #4a90e2;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 6 8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #1e1e1e;"
        );

        this.connectButton = new Button("Connect");
        connectButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        );

        // Hover FX
        connectButton.setOnMouseEntered(e -> connectButton.setStyle(
                "-fx-background-color: #357ABD;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));

        connectButton.setOnMouseExited(e -> connectButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        this.connectButton.setOnAction(actionEvent -> connect(token.getText()));

        centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(tokenLabel, token, connectButton);

        // Game box (queue button)
        this.connectionButton = new Button("Queue"); // Queue for game against AI or player
        connectionButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        );

        // Hover FX
        connectionButton.setOnMouseEntered(e -> connectionButton.setStyle(
                "-fx-background-color: #357ABD;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        connectionButton.setOnMouseExited(e -> connectionButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        this.connectionButton.setOnAction(actionEvent -> {
            if (inGame) {
                if (client.getCurrentGameId() != null) {
                    client.resignGame(client.getCurrentGameId());
                    console.log("Resigned from game " + client.getCurrentGameId());
                }
                client.closeConnection();
                inGame = false;
                connectionButton.setText("Queue");
                radioButtonBox.setVisible(!inGame);
                radioButtonBox.setManaged(!inGame);

                displayGame.setVisible(inGame);
                displayGame.setManaged(inGame);
            } else {
                inGame = true;
                client.startGlobalEventStream(); // Start listening for game events
                if (aiRadio.isSelected()) {
                    client.challengeAI(3, 5, 0);
                    console.log("Challenging ai... ");
                } else {
                    console.log("Player challenge not yet implemented");
                    inGame = false;
                    return;
                }
                connectionButton.setText("Disconnect");
                radioButtonBox.setVisible(!inGame);
                radioButtonBox.setManaged(!inGame);
                displayGame.setVisible(inGame);
                displayGame.setManaged(inGame);
            }
        });

        // Game type selection
        this.gameTypeGroup = new ToggleGroup();

        this.aiRadio = new RadioButton("AI");
        this.aiRadio.setToggleGroup(gameTypeGroup);
        this.aiRadio.setSelected(true);
        this.aiRadio.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        this.playerRadio = new RadioButton("Player");
        this.playerRadio.setToggleGroup(gameTypeGroup);
        this.playerRadio.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        // HBox for radio buttons with proper alignment
        this.radioButtonBox = new HBox(15);
        this.radioButtonBox.setAlignment(Pos.CENTER);
        this.radioButtonBox.getChildren().addAll(aiRadio, playerRadio);

        this.displayGame = new Button("Display Game");
        displayGame.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        );

        // Hover FX - Fixed to apply to displayGame button
        displayGame.setOnMouseEntered(e -> displayGame.setStyle(
                "-fx-background-color: #357ABD;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        displayGame.setOnMouseExited(e -> displayGame.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        this.displayGame.setOnAction(actionEvent -> {
            if (!connected || client == null) {
                console.log("Not connected to LiChess.");
                return;
            }

            String gameId = client.getCurrentGameId();
            if (gameId == null || gameId.isEmpty()) {
                console.log("No active game to display.");
                return;
            }

            try {
                URI gameUri = new URI("https://lichess.org/" + gameId);
                java.awt.Desktop.getDesktop().browse(gameUri);
                console.log("Opened game: " + gameId);
            } catch (IOException | URISyntaxException e) {
                console.log("Failed to open game in browser.");
                e.printStackTrace();
            }
        });

        this.gameBox = new VBox(15);
        this.gameBox.setAlignment(Pos.CENTER);
        this.gameBox.getChildren().addAll(connectionButton, radioButtonBox, displayGame);

        // Initial visibility setup
        this.centerBox.setVisible(true);
        this.centerBox.setManaged(true);
        this.gameBox.setVisible(false);
        this.gameBox.setManaged(false);
        this.displayGame.setVisible(false);
        this.displayGame.setManaged(false);
        this.radioButtonBox.setVisible(true);
        this.radioButtonBox.setManaged(true);


        VBox centerWrapper = new VBox();
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setSpacing(10);
        centerWrapper.setPadding(new Insets(20));
        centerWrapper.getChildren().addAll(centerBox, gameBox);

        root.setLeft(centerWrapper);

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
                    this.connected = true;
                    this.connectionStatus.setText("Connected");
                    this.connectionStatus.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 16px; -fx-font-weight: bold;");
                    this.console.log("Successfully connected!");

                    String username = client.getOurUsername();
                    if (username != null && !username.isEmpty()) {
                        usernameDisplay.setText("Username: " + username);
                        usernameDisplay.setVisible(true);
                        console.log("Logged in as: " + username);
                    }

                    this.centerBox.setVisible(false);
                    this.centerBox.setManaged(false);
                    this.gameBox.setVisible(true);
                    this.gameBox.setManaged(true);
                } else {
                    this.connected = false;
                    this.connectionStatus.setText("Connection failed");
                    this.connectionStatus.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
                    this.console.log("Failed to connect. Check your token.");

                    this.centerBox.setVisible(true);
                    this.centerBox.setManaged(true);
                    this.gameBox.setVisible(false);
                    this.gameBox.setManaged(false);
                }
            });
        }).start();

    }


    @Override
    public void onGameStart(String gameId, JsonObject gameData) {
        // Ignore game start events if we're not intentionally in a game
        if (!inGame) {
            console.log("Ignoring existing game: " + gameId);
            return;
        }

        Platform.runLater(() -> {
            this.connectionButton.setText("Disconnect");
            radioButtonBox.setVisible(!inGame);
            radioButtonBox.setManaged(!inGame);
            displayGame.setVisible(inGame);
            displayGame.setManaged(inGame);
            client.streamGame(gameId, this.engine);
        });
    }

    @Override
    public void onGameFinish(String gameId, JsonObject gameData) {
        Platform.runLater(() -> {
            displayGame.setVisible(false);
            this.inGame = false;
            this.connectionButton.setText("Connect");
            radioButtonBox.setVisible(!inGame);
            radioButtonBox.setManaged(!inGame);
            displayGame.setVisible(inGame);
            displayGame.setManaged(inGame);
            console.log("Disconnecting from game: " + gameId);
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