package dev.chess.cheat.UI.Viewer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ConsoleViewer {

    private final Stage stage;
    private final TextArea console;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ConsoleViewer() {
        this.stage = new Stage();
        this.console = new TextArea();

        console.setEditable(false);
        console.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00; " +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        VBox root = new VBox(console);
        root.setPadding(new Insets(10));
        VBox.setVgrow(console, Priority.ALWAYS);

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("LiChess Bot Console");
    }

    public void log(String message) {
        Platform.runLater(() -> {
            String time = LocalTime.now().format(TIME_FMT);
            console.appendText("[" + time + "] " + message + "\n");
        });
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }
}
