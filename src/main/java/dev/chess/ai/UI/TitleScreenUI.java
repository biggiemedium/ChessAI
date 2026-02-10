package dev.chess.ai.UI;

import dev.chess.ai.Util.Interface.SceneMaker;
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
