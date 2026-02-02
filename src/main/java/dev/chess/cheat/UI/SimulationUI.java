package dev.chess.cheat.UI;

import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Simulation.Runner.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SimulationUI {
    private final Stage stage;
    private Algorithm whiteAlgorithm;
    private Algorithm blackAlgorithm;

    private TextArea resultsArea;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Button startButton;

    private Spinner<Integer> whiteDepthSpinner;
    private Spinner<Integer> blackDepthSpinner;
    private Spinner<Integer> maxMovesSpinner;
    private Spinner<Integer> moveDelaySpinner;
    private CheckBox logFileCheckBox;
    private CheckBox unlimitedMovesCheckBox;
    private CheckBox showBoardCheckBox;

    private BoardViewer boardViewer;

    public SimulationUI(Stage stage) {
        this.stage = stage;
        this.boardViewer = new BoardViewer();
    }

    public void setAlgorithms(Algorithm white, Algorithm black) {
        this.whiteAlgorithm = white;
        this.blackAlgorithm = black;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Chess Engine Simulator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #ffffff;");

        HBox algorithmBox = createAlgorithmDisplay();
        GridPane settingsGrid = createSettingsGrid();
        HBox controlBox = createControlBox();

        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(300);
        resultsArea.setFont(Font.font("Monospaced", 12));
        resultsArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        statusLabel = new Label("Ready - Single Game Mode");
        statusLabel.setStyle("-fx-text-fill: #ffffff;");

        VBox.setVgrow(resultsArea, Priority.ALWAYS);

        root.getChildren().addAll(title, algorithmBox, settingsGrid, controlBox,
                progressBar, statusLabel, resultsArea);

        return new Scene(root, 750, 720);
    }

    private HBox createAlgorithmDisplay() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 5;");

        Label whiteLabel = new Label("White: " + (whiteAlgorithm != null ? whiteAlgorithm.getName() : "None"));
        whiteLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label vs = new Label("VS");
        vs.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label blackLabel = new Label("Black: " + (blackAlgorithm != null ? blackAlgorithm.getName() : "None"));
        blackLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        box.getChildren().addAll(whiteLabel, vs, blackLabel);
        return box;
    }

    private GridPane createSettingsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 5;");

        Label whiteDepthLabel = createLabel("White Depth:");
        whiteDepthSpinner = new Spinner<>(1, 10, 3);
        whiteDepthSpinner.setEditable(true);
        whiteDepthSpinner.setPrefWidth(100);

        Label blackDepthLabel = createLabel("Black Depth:");
        blackDepthSpinner = new Spinner<>(1, 10, 3);
        blackDepthSpinner.setEditable(true);
        blackDepthSpinner.setPrefWidth(100);

        Label maxMovesLabel = createLabel("Max Moves:");
        maxMovesSpinner = new Spinner<>(50, 1000, 200);
        maxMovesSpinner.setEditable(true);
        maxMovesSpinner.setPrefWidth(100);

        Label moveDelayLabel = createLabel("Move Delay (ms):");
        moveDelaySpinner = new Spinner<>(0, 5000, 500);
        moveDelaySpinner.setEditable(true);
        moveDelaySpinner.setPrefWidth(100);

        Label unlimitedLabel = createLabel("Unlimited Moves:");
        unlimitedMovesCheckBox = new CheckBox();
        unlimitedMovesCheckBox.setOnAction(e -> {
            maxMovesSpinner.setDisable(unlimitedMovesCheckBox.isSelected());
        });

        Label logLabel = createLabel("Log to File:");
        logFileCheckBox = new CheckBox();
        logFileCheckBox.setSelected(true);

        Label showBoardLabel = createLabel("Show Board:");
        showBoardCheckBox = new CheckBox();
        showBoardCheckBox.setSelected(true);
        showBoardCheckBox.setOnAction(e -> {
            if (showBoardCheckBox.isSelected()) {
                boardViewer.show();
            } else {
                boardViewer.hide();
            }
        });

        grid.add(whiteDepthLabel, 0, 0);
        grid.add(whiteDepthSpinner, 1, 0);
        grid.add(blackDepthLabel, 2, 0);
        grid.add(blackDepthSpinner, 3, 0);

        grid.add(maxMovesLabel, 0, 1);
        grid.add(maxMovesSpinner, 1, 1);
        grid.add(moveDelayLabel, 2, 1);
        grid.add(moveDelaySpinner, 3, 1);

        grid.add(unlimitedLabel, 0, 2);
        grid.add(unlimitedMovesCheckBox, 1, 2);
        grid.add(logLabel, 2, 2);
        grid.add(logFileCheckBox, 3, 2);

        grid.add(showBoardLabel, 0, 3);
        grid.add(showBoardCheckBox, 1, 3);

        return grid;
    }

    private HBox createControlBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        startButton = new Button("Start Game");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        startButton.setOnAction(e -> runSimulation());

        Button clearButton = new Button("Clear Results");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        clearButton.setOnAction(e -> resultsArea.clear());

        box.getChildren().addAll(startButton, clearButton);
        return box;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #ffffff;");
        return label;
    }

    private void runSimulation() {
        if (whiteAlgorithm == null || blackAlgorithm == null) {
            showAlert("Error", "Algorithms not set!");
            return;
        }

        int whiteDepth = whiteDepthSpinner.getValue();
        int blackDepth = blackDepthSpinner.getValue();
        int maxMoves = unlimitedMovesCheckBox.isSelected() ? Integer.MAX_VALUE : maxMovesSpinner.getValue();
        int moveDelay = moveDelaySpinner.getValue();
        boolean logToFile = logFileCheckBox.isSelected();
        boolean showBoard = showBoardCheckBox.isSelected();

        if (showBoard) {
            boardViewer.show();
        }

        startButton.setDisable(true);
        resultsArea.clear();
        progressBar.setProgress(0);

        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    statusLabel.setText("Running game...");
                    progressBar.setProgress(0.5);
                });

                Simulator simulator = new Simulator(maxMoves);
                simulator.setBoardViewer(showBoard ? boardViewer : null);
                simulator.setMoveDelay(moveDelay);

                SimulationStats stats = simulator.runGames(
                        whiteAlgorithm, blackAlgorithm, 1, whiteDepth, blackDepth, logToFile
                );

                GameStats game = stats.games.get(0);

                Platform.runLater(() -> {
                    resultsArea.appendText("=".repeat(70) + "\n");
                    resultsArea.appendText("GAME COMPLETE\n");
                    resultsArea.appendText("=".repeat(70) + "\n");
                    resultsArea.appendText(String.format("Result: %s\n", game.outcome));
                    resultsArea.appendText(String.format("Total Moves: %d\n", game.moves.size()));
                    resultsArea.appendText(String.format("Total Time: %dms\n", game.totalTimeMs));
                    resultsArea.appendText(String.format("Total Nodes: %d\n", game.totalNodes));
                    resultsArea.appendText(String.format("Peak Memory: %dKB\n", game.peakMemoryBytes / 1024));
                    resultsArea.appendText(String.format("Avg Time/Move: %dms\n",
                            game.moves.isEmpty() ? 0 : game.totalTimeMs / game.moves.size()));
                    resultsArea.appendText(String.format("Avg Nodes/Move: %d\n",
                            game.moves.isEmpty() ? 0 : game.totalNodes / game.moves.size()));
                    resultsArea.appendText("=".repeat(70) + "\n");

                    statusLabel.setText("Game complete! Ready for next game.");
                    progressBar.setProgress(1.0);
                    startButton.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Simulation failed: " + e.getMessage());
                    startButton.setDisable(false);
                    statusLabel.setText("Error occurred");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}