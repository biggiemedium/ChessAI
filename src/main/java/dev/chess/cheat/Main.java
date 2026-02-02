package dev.chess.cheat;

import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Impl.MinimaxAlgorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Evaluation.Impl.MaterialEvaluator;
import dev.chess.cheat.UI.SimulationUI;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

        showSimulationUI();
        primaryStage.show();
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