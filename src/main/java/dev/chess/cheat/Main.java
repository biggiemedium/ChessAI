package dev.chess.cheat;

import dev.chess.cheat.UI.Development.SimulationUI;
import dev.chess.cheat.UI.LiChessUI;
import dev.chess.cheat.UI.TitleScreenUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Chess.com uses an anticheat engine that checks things such as winstreak, ratio, etc
 *
 * https://cse.buffalo.edu/~regan/papers/pdf/RBZ14aaai.pdf
 * https://cse.buffalo.edu/~regan/papers/pdf/HRdF10.pdf
 */
public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Chess Simulation");
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        //showSimulationUI();
        showLiChessUI();
        primaryStage.show();
    }

    private void showLiChessUI() {
        LiChessUI ui = new LiChessUI(primaryStage);
        primaryStage.setScene(ui.createScene());
    }

    private void setTitleScreenUI() {
        TitleScreenUI ui = new TitleScreenUI(primaryStage);
        primaryStage.setScene(ui.createScene());
    }

    private void showSimulationUI() {
        SimulationUI ui = new SimulationUI(primaryStage);
        primaryStage.setScene(ui.createScene());
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}