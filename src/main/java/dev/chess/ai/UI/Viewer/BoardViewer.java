package dev.chess.ai.UI.Viewer;

import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Piece;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class BoardViewer {
    private static final int SQUARE_SIZE = 80;
    private static final Color LIGHT_SQUARE = Color.rgb(240, 217, 181);
    private static final Color DARK_SQUARE = Color.rgb(181, 136, 99);

    private final Stage stage;
    private final GridPane boardGrid;
    private final Map<Character, Image> pieceImages;
    private Text statusText;

    public BoardViewer() {
        this.stage = new Stage();
        this.boardGrid = new GridPane();
        this.pieceImages = new HashMap<>();

        loadPieceImages();
        setupStage();
    }

    private void loadPieceImages() {
        try {
            pieceImages.put('P', new Image(getClass().getResourceAsStream("/images/wp.png")));
            pieceImages.put('R', new Image(getClass().getResourceAsStream("/images/wr.png")));
            pieceImages.put('N', new Image(getClass().getResourceAsStream("/images/wn.png")));
            pieceImages.put('B', new Image(getClass().getResourceAsStream("/images/wb.png")));
            pieceImages.put('Q', new Image(getClass().getResourceAsStream("/images/wq.png")));
            pieceImages.put('K', new Image(getClass().getResourceAsStream("/images/wk.png")));

            pieceImages.put('p', new Image(getClass().getResourceAsStream("/images/bp.png")));
            pieceImages.put('r', new Image(getClass().getResourceAsStream("/images/br.png")));
            pieceImages.put('n', new Image(getClass().getResourceAsStream("/images/bn.png")));
            pieceImages.put('b', new Image(getClass().getResourceAsStream("/images/bb.png")));
            pieceImages.put('q', new Image(getClass().getResourceAsStream("/images/bq.png")));
            pieceImages.put('k', new Image(getClass().getResourceAsStream("/images/bk.png")));
        } catch (Exception e) {
            System.err.println("Failed to load piece images: " + e.getMessage());
        }
    }

    private void setupStage() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Text title = new Text("Live Board View");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);

        statusText = new Text("Waiting for game...");
        statusText.setFont(Font.font("Arial", 14));
        statusText.setFill(Color.LIGHTGREEN);

        boardGrid.setHgap(0);
        boardGrid.setVgap(0);

        createEmptyBoard();

        root.getChildren().addAll(title, statusText, boardGrid);

        Scene scene = new Scene(root, SQUARE_SIZE * 8 + 40, SQUARE_SIZE * 8 + 100);
        stage.setScene(scene);
        stage.setTitle("Chess Board Viewer");
    }

    private void createEmptyBoard() {
        boardGrid.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createSquare(row, col, null);
                boardGrid.add(square, col, row);
            }
        }
    }

    private StackPane createSquare(int row, int col, Piece piece) {
        StackPane square = new StackPane();
        square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        boolean isLight = (row + col) % 2 == 0;
        square.setBackground(new Background(new BackgroundFill(
                isLight ? LIGHT_SQUARE : DARK_SQUARE,
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        if (piece != null) {
            char symbol = piece.getSymbol();
            Image image = pieceImages.get(symbol);
            if (image != null) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(SQUARE_SIZE * 0.8);
                imageView.setFitHeight(SQUARE_SIZE * 0.8);
                imageView.setPreserveRatio(true);
                square.getChildren().add(imageView);
            }
        }

        return square;
    }

    public void updateBoard(Board board, String status) {
        javafx.application.Platform.runLater(() -> {
            boardGrid.getChildren().clear();

            Piece[][] pieces = board.getPieces();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    StackPane square = createSquare(row, col, pieces[row][col]);
                    boardGrid.add(square, col, row);
                }
            }

            statusText.setText(status);
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
