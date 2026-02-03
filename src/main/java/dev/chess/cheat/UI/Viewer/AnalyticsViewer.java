package dev.chess.cheat.UI.Viewer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsViewer {

    private final Stage stage;

    private Label whiteTotalNodesLabel;
    private Label whiteAvgNodesLabel;
    private Label whiteMinNodesLabel;
    private Label whiteMaxNodesLabel;
    private Label whiteTotalTimeLabel;
    private Label whiteAvgTimeLabel;
    private Label whiteMinTimeLabel;
    private Label whiteMaxTimeLabel;
    private Label whiteNodesPerSecLabel;

    private Label blackTotalNodesLabel;
    private Label blackAvgNodesLabel;
    private Label blackMinNodesLabel;
    private Label blackMaxNodesLabel;
    private Label blackTotalTimeLabel;
    private Label blackAvgTimeLabel;
    private Label blackMinTimeLabel;
    private Label blackMaxTimeLabel;
    private Label blackNodesPerSecLabel;

    private Label totalMovesLabel;
    private Label gameStatusLabel;

    private final List<Integer> whiteNodesList = new ArrayList<>();
    private final List<Long> whiteTimeList = new ArrayList<>();
    private final List<Integer> blackNodesList = new ArrayList<>();
    private final List<Long> blackTimeList = new ArrayList<>();

    public AnalyticsViewer() {
        this.stage = new Stage();
        setupStage();
    }

    private void setupStage() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Text title = new Text("Game Analytics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);

        gameStatusLabel = createValueLabel("Status: Waiting...");
        gameStatusLabel.setStyle("-fx-text-fill: #ffaa00; -fx-font-size: 14px; -fx-font-weight: bold;");

        totalMovesLabel = createValueLabel("Total Moves: 0");
        totalMovesLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");

        Separator sep1 = new Separator();

        GridPane whiteGrid = createPlayerGrid("WHITE", true);

        Separator sep2 = new Separator();

        GridPane blackGrid = createPlayerGrid("BLACK", false);

        root.getChildren().addAll(title, gameStatusLabel, totalMovesLabel, sep1, whiteGrid, sep2, blackGrid);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");

        Scene scene = new Scene(scrollPane, 600, 700);
        stage.setScene(scene);
        stage.setTitle("Analytics");
    }

    private GridPane createPlayerGrid(String playerName, boolean isWhite) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 5;");

        Text playerTitle = new Text(playerName + " STATISTICS");
        playerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        playerTitle.setFill(Color.WHITE);
        GridPane.setColumnSpan(playerTitle, 2);
        grid.add(playerTitle, 0, 0);

        int row = 1;

        grid.add(createHeaderLabel("NODES ANALYSIS"), 0, row++);
        GridPane.setColumnSpan(grid.getChildren().get(grid.getChildren().size() - 1), 2);

        if (isWhite) {
            whiteTotalNodesLabel = createValueLabel("0");
            whiteAvgNodesLabel = createValueLabel("0");
            whiteMinNodesLabel = createValueLabel("0");
            whiteMaxNodesLabel = createValueLabel("0");

            grid.add(createKeyLabel("Total Nodes:"), 0, row);
            grid.add(whiteTotalNodesLabel, 1, row++);
            grid.add(createKeyLabel("Average Nodes:"), 0, row);
            grid.add(whiteAvgNodesLabel, 1, row++);
            grid.add(createKeyLabel("Min Nodes:"), 0, row);
            grid.add(whiteMinNodesLabel, 1, row++);
            grid.add(createKeyLabel("Max Nodes:"), 0, row);
            grid.add(whiteMaxNodesLabel, 1, row++);
        } else {
            blackTotalNodesLabel = createValueLabel("0");
            blackAvgNodesLabel = createValueLabel("0");
            blackMinNodesLabel = createValueLabel("0");
            blackMaxNodesLabel = createValueLabel("0");

            grid.add(createKeyLabel("Total Nodes:"), 0, row);
            grid.add(blackTotalNodesLabel, 1, row++);
            grid.add(createKeyLabel("Average Nodes:"), 0, row);
            grid.add(blackAvgNodesLabel, 1, row++);
            grid.add(createKeyLabel("Min Nodes:"), 0, row);
            grid.add(blackMinNodesLabel, 1, row++);
            grid.add(createKeyLabel("Max Nodes:"), 0, row);
            grid.add(blackMaxNodesLabel, 1, row++);
        }

        grid.add(createHeaderLabel("TIME ANALYSIS"), 0, row++);
        GridPane.setColumnSpan(grid.getChildren().get(grid.getChildren().size() - 1), 2);

        if (isWhite) {
            whiteTotalTimeLabel = createValueLabel("0 ms");
            whiteAvgTimeLabel = createValueLabel("0 ms");
            whiteMinTimeLabel = createValueLabel("0 ms");
            whiteMaxTimeLabel = createValueLabel("0 ms");

            grid.add(createKeyLabel("Total Time:"), 0, row);
            grid.add(whiteTotalTimeLabel, 1, row++);
            grid.add(createKeyLabel("Average Time:"), 0, row);
            grid.add(whiteAvgTimeLabel, 1, row++);
            grid.add(createKeyLabel("Min Time:"), 0, row);
            grid.add(whiteMinTimeLabel, 1, row++);
            grid.add(createKeyLabel("Max Time:"), 0, row);
            grid.add(whiteMaxTimeLabel, 1, row++);
        } else {
            blackTotalTimeLabel = createValueLabel("0 ms");
            blackAvgTimeLabel = createValueLabel("0 ms");
            blackMinTimeLabel = createValueLabel("0 ms");
            blackMaxTimeLabel = createValueLabel("0 ms");

            grid.add(createKeyLabel("Total Time:"), 0, row);
            grid.add(blackTotalTimeLabel, 1, row++);
            grid.add(createKeyLabel("Average Time:"), 0, row);
            grid.add(blackAvgTimeLabel, 1, row++);
            grid.add(createKeyLabel("Min Time:"), 0, row);
            grid.add(blackMinTimeLabel, 1, row++);
            grid.add(createKeyLabel("Max Time:"), 0, row);
            grid.add(blackMaxTimeLabel, 1, row++);
        }

        grid.add(createHeaderLabel("EFFICIENCY"), 0, row++);
        GridPane.setColumnSpan(grid.getChildren().get(grid.getChildren().size() - 1), 2);

        if (isWhite) {
            whiteNodesPerSecLabel = createValueLabel("0 nodes/sec");
            grid.add(createKeyLabel("Nodes per Second:"), 0, row);
            grid.add(whiteNodesPerSecLabel, 1, row++);
        } else {
            blackNodesPerSecLabel = createValueLabel("0 nodes/sec");
            grid.add(createKeyLabel("Nodes per Second:"), 0, row);
            grid.add(blackNodesPerSecLabel, 1, row++);
        }

        return grid;
    }

    private Label createKeyLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
        return label;
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #00aaff; -fx-font-size: 12px; -fx-font-weight: bold;");
        label.setPadding(new Insets(8, 0, 4, 0));
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 12px; -fx-font-family: 'Monospaced';");
        return label;
    }

    public void reset() {
        Platform.runLater(() -> {
            whiteNodesList.clear();
            whiteTimeList.clear();
            blackNodesList.clear();
            blackTimeList.clear();

            gameStatusLabel.setText("Status: Running...");
            totalMovesLabel.setText("Total Moves: 0");

            updateWhiteStats();
            updateBlackStats();
        });
    }

    public void addMoveData(int moveNumber, boolean isWhite, int nodes, long timeMs) {
        if (isWhite) {
            whiteNodesList.add(nodes);
            whiteTimeList.add(timeMs);
        } else {
            blackNodesList.add(nodes);
            blackTimeList.add(timeMs);
        }

        Platform.runLater(() -> {
            totalMovesLabel.setText("Total Moves: " + moveNumber);

            if (isWhite) {
                updateWhiteStats();
            } else {
                updateBlackStats();
            }
        });
    }

    private void updateWhiteStats() {
        if (whiteNodesList.isEmpty()) {
            whiteTotalNodesLabel.setText("0");
            whiteAvgNodesLabel.setText("0");
            whiteMinNodesLabel.setText("0");
            whiteMaxNodesLabel.setText("0");
            whiteTotalTimeLabel.setText("0 ms");
            whiteAvgTimeLabel.setText("0 ms");
            whiteMinTimeLabel.setText("0 ms");
            whiteMaxTimeLabel.setText("0 ms");
            whiteNodesPerSecLabel.setText("0 nodes/sec");
            return;
        }

        long totalNodes = whiteNodesList.stream().mapToLong(Integer::longValue).sum();
        long avgNodes = totalNodes / whiteNodesList.size();
        int minNodes = whiteNodesList.stream().min(Integer::compareTo).orElse(0);
        int maxNodes = whiteNodesList.stream().max(Integer::compareTo).orElse(0);

        long totalTime = whiteTimeList.stream().mapToLong(Long::longValue).sum();
        long avgTime = totalTime / whiteTimeList.size();
        long minTime = whiteTimeList.stream().min(Long::compareTo).orElse(0L);
        long maxTime = whiteTimeList.stream().max(Long::compareTo).orElse(0L);

        long nodesPerSec = totalTime > 0 ? (totalNodes * 1000 / totalTime) : 0;

        whiteTotalNodesLabel.setText(String.format("%,d", totalNodes));
        whiteAvgNodesLabel.setText(String.format("%,d", avgNodes));
        whiteMinNodesLabel.setText(String.format("%,d", minNodes));
        whiteMaxNodesLabel.setText(String.format("%,d", maxNodes));

        whiteTotalTimeLabel.setText(totalTime + " ms");
        whiteAvgTimeLabel.setText(avgTime + " ms");
        whiteMinTimeLabel.setText(minTime + " ms");
        whiteMaxTimeLabel.setText(maxTime + " ms");

        whiteNodesPerSecLabel.setText(String.format("%,d nodes/sec", nodesPerSec));
    }

    private void updateBlackStats() {
        if (blackNodesList.isEmpty()) {
            blackTotalNodesLabel.setText("0");
            blackAvgNodesLabel.setText("0");
            blackMinNodesLabel.setText("0");
            blackMaxNodesLabel.setText("0");
            blackTotalTimeLabel.setText("0 ms");
            blackAvgTimeLabel.setText("0 ms");
            blackMinTimeLabel.setText("0 ms");
            blackMaxTimeLabel.setText("0 ms");
            blackNodesPerSecLabel.setText("0 nodes/sec");
            return;
        }

        long totalNodes = blackNodesList.stream().mapToLong(Integer::longValue).sum();
        long avgNodes = totalNodes / blackNodesList.size();
        int minNodes = blackNodesList.stream().min(Integer::compareTo).orElse(0);
        int maxNodes = blackNodesList.stream().max(Integer::compareTo).orElse(0);

        long totalTime = blackTimeList.stream().mapToLong(Long::longValue).sum();
        long avgTime = totalTime / blackTimeList.size();
        long minTime = blackTimeList.stream().min(Long::compareTo).orElse(0L);
        long maxTime = blackTimeList.stream().max(Long::compareTo).orElse(0L);

        long nodesPerSec = totalTime > 0 ? (totalNodes * 1000 / totalTime) : 0;

        blackTotalNodesLabel.setText(String.format("%,d", totalNodes));
        blackAvgNodesLabel.setText(String.format("%,d", avgNodes));
        blackMinNodesLabel.setText(String.format("%,d", minNodes));
        blackMaxNodesLabel.setText(String.format("%,d", maxNodes));

        blackTotalTimeLabel.setText(totalTime + " ms");
        blackAvgTimeLabel.setText(avgTime + " ms");
        blackMinTimeLabel.setText(minTime + " ms");
        blackMaxTimeLabel.setText(maxTime + " ms");

        blackNodesPerSecLabel.setText(String.format("%,d nodes/sec", nodesPerSec));
    }

    public void setGameStatus(String status) {
        Platform.runLater(() -> {
            gameStatusLabel.setText("Status: " + status);
        });
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    public Stage getStage() {
        return stage;
    }
}
