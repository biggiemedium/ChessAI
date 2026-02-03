package dev.chess.cheat.UI;

import dev.chess.cheat.Util.Interface.SceneMaker;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TitleScreenUI implements SceneMaker {

    private final Stage stage;

    public TitleScreenUI(Stage stage) {
        this.stage = stage;
    }


    @Override
    public Scene createScene() {
        return null;
    }

}
