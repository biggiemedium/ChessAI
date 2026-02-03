package dev.chess.cheat.UI.Viewer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChartViewer {

    private final Stage stage;
    private LineChart<Number, Number> nodesChart;
    private LineChart<Number, Number> timeChart;
    private XYChart.Series<Number, Number> whiteNodesSeries;
    private XYChart.Series<Number, Number> blackNodesSeries;
    private XYChart.Series<Number, Number> whiteTimeSeries;
    private XYChart.Series<Number, Number> blackTimeSeries;

    public ChartViewer() {
        this.stage = new Stage();
        setupStage();
    }

    private void setupStage() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Text title = new Text("Performance Charts");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(javafx.scene.paint.Color.WHITE);

        NumberAxis nodesXAxis = new NumberAxis();
        nodesXAxis.setLabel("Move Number");
        nodesXAxis.setStyle("-fx-tick-label-fill: white;");

        NumberAxis nodesYAxis = new NumberAxis();
        nodesYAxis.setLabel("Nodes Searched");
        nodesYAxis.setStyle("-fx-tick-label-fill: white;");

        nodesChart = new LineChart<>(nodesXAxis, nodesYAxis);
        nodesChart.setTitle("Nodes Per Move");
        nodesChart.setCreateSymbols(true);
        nodesChart.setPrefHeight(300);
        nodesChart.setStyle("-fx-background-color: #3a3a3a;");
        nodesChart.setLegendVisible(true);

        whiteNodesSeries = new XYChart.Series<>();
        whiteNodesSeries.setName("White Nodes");

        blackNodesSeries = new XYChart.Series<>();
        blackNodesSeries.setName("Black Nodes");

        nodesChart.getData().addAll(whiteNodesSeries, blackNodesSeries);

        NumberAxis timeXAxis = new NumberAxis();
        timeXAxis.setLabel("Move Number");
        timeXAxis.setStyle("-fx-tick-label-fill: white;");

        NumberAxis timeYAxis = new NumberAxis();
        timeYAxis.setLabel("Time (ms)");
        timeYAxis.setStyle("-fx-tick-label-fill: white;");

        timeChart = new LineChart<>(timeXAxis, timeYAxis);
        timeChart.setTitle("Time Per Move");
        timeChart.setCreateSymbols(true);
        timeChart.setPrefHeight(300);
        timeChart.setStyle("-fx-background-color: #3a3a3a;");
        timeChart.setLegendVisible(true);

        whiteTimeSeries = new XYChart.Series<>();
        whiteTimeSeries.setName("White Time");

        blackTimeSeries = new XYChart.Series<>();
        blackTimeSeries.setName("Black Time");

        timeChart.getData().addAll(whiteTimeSeries, blackTimeSeries);

        root.getChildren().addAll(title, nodesChart, timeChart);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.setTitle("Performance Charts");
    }

    public void clear() {
        Platform.runLater(() -> {
            whiteNodesSeries.getData().clear();
            blackNodesSeries.getData().clear();
            whiteTimeSeries.getData().clear();
            blackTimeSeries.getData().clear();
        });
    }

    public void addDataPoint(int moveNumber, boolean isWhite, int nodes, long timeMs) {
        Platform.runLater(() -> {
            if (isWhite) {
                whiteNodesSeries.getData().add(new XYChart.Data<>(moveNumber, nodes));
                whiteTimeSeries.getData().add(new XYChart.Data<>(moveNumber, timeMs));
            } else {
                blackNodesSeries.getData().add(new XYChart.Data<>(moveNumber, nodes));
                blackTimeSeries.getData().add(new XYChart.Data<>(moveNumber, timeMs));
            }
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
