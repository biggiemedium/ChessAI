package dev.chess.cheat;

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
        primaryStage.initStyle(StageStyle.UNDECORATED); // Remove default window frame
        primaryStage.setTitle("Chess Bot");
        primaryStage.setResizable(false);

        showMainScreen();
        primaryStage.show();
    }

    private void showMainScreen() {
        MainScreen mainScreen = new MainScreen(primaryStage);
        primaryStage.setScene(mainScreen.getScene());
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}