package dev.chess.cheat;

import dev.chess.cheat.Engine.MoveGenerator;
import dev.chess.cheat.Engine.SearchLogic.Impl.MinimaxAlgorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Evaluation.Impl.MaterialEvaluator;
import dev.chess.cheat.UI.ChessBoardScreen;
import dev.chess.cheat.UI.MainScreen;
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
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Chess Bot");
        primaryStage.setResizable(false);

        showChessBoard();
        primaryStage.show();
    }

    private void showChessBoard() {
        ChessBoardScreen chessBoardScreen = new ChessBoardScreen(primaryStage);

        Evaluator evaluator = new MaterialEvaluator();
        MoveGenerator moveGen = new MoveGenerator();
        MinimaxAlgorithm minimax = new MinimaxAlgorithm(evaluator, moveGen);

        chessBoardScreen.setAIEngine(minimax, evaluator, 4);

        primaryStage.setScene(chessBoardScreen.getScene());
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}