package dev.chess.cheat.UI;

import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Engine.SearchLogic.AlgorithmFactory;
import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Evaluation.MasterEvaluator;
import dev.chess.cheat.Network.Impl.LiChessClient;
import dev.chess.cheat.UI.Viewer.LiChessBoardViewer;
import dev.chess.cheat.UI.Viewer.ConsoleViewer;
import dev.chess.cheat.Util.Interface.SceneMaker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LiChessUI implements SceneMaker {

    private final LiChessBoardViewer boardViewer;
    private final ConsoleViewer console;
    private LiChessClient client;
    private ChessEngine engine;

    private TextField tokenField;
    private ComboBox<String> aiCombo;
    private Spinner<Integer> depthSpinner;
    private Spinner<Integer> timeSpinner;

    private Button connectButton;
    private Button disconnectButton;
    private Button queueButton;
    private Button stopButton;
    private Label statusLabel;

    public LiChessUI(Stage stage) {
        this.boardViewer = new LiChessBoardViewer();
        this.console = new ConsoleViewer();
    }

    @Override
    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = createTitle("LiChess AI Battle Station");
        GridPane connectionPanel = createConnectionPanel();
        GridPane configPanel = createConfigPanel();
        HBox controls = createControls();
        statusLabel = createStatusLabel();

        root.getChildren().addAll(title, connectionPanel, configPanel, controls, statusLabel);
        return new Scene(root, 600, 450);
    }

    private Label createTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setStyle("-fx-text-fill: white;");
        return label;
    }

    private GridPane createConnectionPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 5;");

        Label sectionLabel = new Label("CONNECTION");
        sectionLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 14px;");

        tokenField = new TextField();
        tokenField.setPromptText("Enter API token (optional - leave empty for config)");
        tokenField.setPrefWidth(400);

        grid.add(sectionLabel, 0, 0, 2, 1);
        addRow(grid, 1, "API Token:", tokenField);

        return grid;
    }

    private GridPane createConfigPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 5;");

        Label sectionLabel = new Label("AI CONFIGURATION");
        sectionLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 14px;");

        var factory = new AlgorithmFactory();
        aiCombo = new ComboBox<>();
        aiCombo.getItems().addAll(factory.getAlgorithmNames());
        aiCombo.setValue(factory.getAlgorithmNames().get(0));
        aiCombo.setPrefWidth(200);

        depthSpinner = new Spinner<>(1, 10, 4);
        depthSpinner.setEditable(true);
        depthSpinner.setPrefWidth(100);

        timeSpinner = new Spinner<>(1, 60, 10);
        timeSpinner.setEditable(true);
        timeSpinner.setPrefWidth(100);

        grid.add(sectionLabel, 0, 0, 2, 1);
        addRow(grid, 1, "AI Algorithm:", aiCombo);
        addRow(grid, 2, "Search Depth:", depthSpinner);
        addRow(grid, 3, "Time (min):", timeSpinner);

        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, Control control) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white;");
        grid.add(label, 0, row);
        grid.add(control, 1, row);
    }

    private HBox createControls() {
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        connectButton = createButton("Connect", "#4CAF50");
        connectButton.setOnAction(e -> handleConnect());

        disconnectButton = createButton("Disconnect", "#f44336");
        disconnectButton.setDisable(true);
        disconnectButton.setOnAction(e -> handleDisconnect());

        queueButton = createButton("Queue for Game", "#00BCD4");
        queueButton.setDisable(true);
        queueButton.setOnAction(e -> handleQueue());

        stopButton = createButton("Stop Game", "#FF9800");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> handleStop());

        Button consoleBtn = createButton("Show Console", "#2196F3");
        consoleBtn.setOnAction(e -> console.show());

        Button boardBtn = createButton("Show Board", "#9C27B0");
        boardBtn.setOnAction(e -> boardViewer.show());

        box.getChildren().addAll(connectButton, disconnectButton, queueButton, stopButton, consoleBtn, boardBtn);
        return box;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 10 20;", color));
        return btn;
    }

    private Label createStatusLabel() {
        Label label = new Label("Status: Not connected");
        label.setStyle("-fx-text-fill: #ffaa00; -fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private void handleConnect() {
        String token = tokenField.getText().trim();
        console.log("Connecting to LiChess...");
        statusLabel.setText("Status: Connecting...");

        new Thread(() -> {
            client = token.isEmpty() ? new LiChessClient() : new LiChessClient(token);

            if (client.establishConnection()) {
                console.log("✓ Connected to LiChess");

                // Initialize AI
                var factory = new AlgorithmFactory();
                var algo = factory.createAlgorithm(aiCombo.getValue(), new MasterEvaluator(), new MoveGenerator());
                engine = new ChessEngine(algo);
                console.log("✓ AI initialized: " + aiCombo.getValue());

                Platform.runLater(() -> {
                    statusLabel.setText("Status: Connected ✓");
                    statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
                    connectButton.setDisable(true);
                    disconnectButton.setDisable(false);
                    queueButton.setDisable(false);
                });
            } else {
                console.log("✗ Connection failed");
                Platform.runLater(() -> {
                    statusLabel.setText("Status: Connection failed ✗");
                    statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 14px;");
                });
            }
        }).start();
    }

    private void handleDisconnect() {
        if (client != null) {
            client.closeConnection();
            console.log("Disconnected from LiChess");
        }

        statusLabel.setText("Status: Disconnected");
        statusLabel.setStyle("-fx-text-fill: #ffaa00; -fx-font-weight: bold; -fx-font-size: 14px;");
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        queueButton.setDisable(true);
        stopButton.setDisable(true);
        boardViewer.clearOpponentStats();
    }

    private void handleQueue() {
        queueButton.setDisable(true);
        stopButton.setDisable(false);

        console.log("=".repeat(50));
        console.log("Queueing for game vs AI...");
        console.log("Algorithm: " + aiCombo.getValue());
        console.log("Depth: " + depthSpinner.getValue());
        console.log("Time: " + timeSpinner.getValue() + " minutes");
        console.log("=".repeat(50));

        new Thread(() -> {
            String gameId = client.challengeAI(5, timeSpinner.getValue(), 0);

            if (gameId != null) {
                console.log("✓ Game created: " + gameId);
                Platform.runLater(() -> {
                    statusLabel.setText("Status: Game started - " + gameId);
                    statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
                });

                client.streamGame(gameId, engine);

                Platform.runLater(() -> {
                    boardViewer.show();

                    // Update game info
                    String ourColor = client.areWeWhite() ? "White" : "Black";
                    boardViewer.updateGameInfo(gameId, ourColor, "standard", "rapid");

                    // Fetch and display opponent stats
                    var opponentStats = client.getCurrentOpponentStats();
                    if (opponentStats != null) {
                        boardViewer.updateOpponentStats(opponentStats.getStatsMap());
                        console.log("Opponent: " + opponentStats.getUsername() +
                                " (Rating: " + opponentStats.getRating() + ")" +
                                (opponentStats.isBot() ? " [BOT]" : ""));
                    }

                    var game = client.getCurrentGame();
                    game.addUpdateListener(g -> {
                        Platform.runLater(() -> {
                            int moveCount = g.getMoveHistory().size();
                            String status = g.getStatus().toString();
                            boardViewer.updateBoard(g.getBoard(), "Move " + moveCount + " | " + status);
                            console.log("Move " + moveCount + " | " + status);

                            if (!status.equals("IN_PROGRESS")) {
                                console.log("=".repeat(50));
                                console.log("GAME FINISHED: " + status);
                                console.log("=".repeat(50));
                                stopButton.setDisable(true);
                                queueButton.setDisable(false);
                            }
                        });
                    });
                });
            } else {
                console.log("✗ Failed to create game");
                Platform.runLater(() -> {
                    statusLabel.setText("Status: Failed to queue");
                    statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 14px;");
                    queueButton.setDisable(false);
                    stopButton.setDisable(true);
                });
            }
        }).start();
    }

    private void handleStop() {
        if (client != null) {
            client.stopStreaming();
            console.log("Game stopped");
        }

        statusLabel.setText("Status: Connected - Ready to queue");
        stopButton.setDisable(true);
        queueButton.setDisable(false);
    }
}