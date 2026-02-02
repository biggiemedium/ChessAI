package dev.chess.cheat.UI;

import dev.chess.cheat.Engine.Move;
import dev.chess.cheat.Engine.SearchLogic.Algorithm;
import dev.chess.cheat.Evaluation.Evaluator;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.Simulation.Piece;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessBoardScreen {

    private final Stage stage;
    private final Scene scene;
    private final BorderPane root;
    private GridPane chessBoard;
    private Game game;
    private HBox titleBar;
    private Label boardTitleLabel;

    private double xOffset = 0;
    private double yOffset = 0;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private StackPane selectedSquare = null;

    private static final Map<String, Image> imageCache = new HashMap<>();

    private ImageView draggedPiece = null;
    private double dragStartX = 0;
    private double dragStartY = 0;

    private static final Color LIGHT_SQUARE = Color.web("#F0D9B5");
    private static final Color DARK_SQUARE = Color.web("#B58863");
    private static final Color SELECTED_SQUARE = Color.web("#BACA44");
    private static final Color VALID_MOVE_SQUARE = Color.web("#829769");
    private static final int SQUARE_SIZE = 70;

    private List<int[]> validMoveSquares = new ArrayList<>();

    private VBox aiPanel;
    private Label aiStatusLabel;
    private Label aiMoveLabel;
    private Label aiScoreLabel;
    private Label aiNodesLabel;
    private Label aiDepthLabel;
    private Label aiTimeLabel;
    private VBox moveHistoryBox;
    private VBox topMovesBox;
    private Button aiMoveButton;
    private Button autoAnalyzeButton;
    private Algorithm aiEngine;
    private Evaluator evaluator;
    private int aiDepth = 4;
    private boolean autoAnalyze = false;

    public ChessBoardScreen(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.scene = new Scene(root, 1050, 720);
        this.game = new Game();

        setupUI();
        makeTitleBarDraggable();
    }

    private void setupUI() {
        root.setStyle("-fx-background-color: #1e1e1e;");

        titleBar = createTitleBar();
        root.setTop(titleBar);

        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        boardTitleLabel = new Label("♚ Chess Board - " + game.getCurrentPlayer() + "'s Turn");
        boardTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        boardTitleLabel.setTextFill(Color.WHITE);

        chessBoard = createChessBoard();
        centerContent.getChildren().addAll(boardTitleLabel, chessBoard);
        root.setCenter(centerContent);

        aiPanel = createAIPanel();
        root.setRight(aiPanel);
    }

    private VBox createAIPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #252525; -fx-border-color: #3a3a3a; -fx-border-width: 0 0 0 2;");
        panel.setPrefWidth(300);

        Label panelTitle = new Label("⚙ AI Engine");
        panelTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        panelTitle.setTextFill(Color.web("#4CAF50"));

        aiStatusLabel = createInfoLabel("Status: Ready");
        aiMoveLabel = createInfoLabel("Best Move: -");
        aiScoreLabel = createInfoLabel("Evaluation: 0.0");
        aiNodesLabel = createInfoLabel("Nodes: 0");
        aiDepthLabel = createInfoLabel("Depth: " + aiDepth);
        aiTimeLabel = createInfoLabel("Time: 0ms");

        aiMoveButton = new Button("Execute AI Move");
        aiMoveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        aiMoveButton.setPrefWidth(260);
        aiMoveButton.setPrefHeight(35);
        aiMoveButton.setOnAction(e -> executeAIMove());

        autoAnalyzeButton = new Button("Auto-Analyze: OFF");
        autoAnalyzeButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        autoAnalyzeButton.setPrefWidth(260);
        autoAnalyzeButton.setPrefHeight(35);
        autoAnalyzeButton.setOnAction(e -> toggleAutoAnalyze());

        Button resetButton = new Button("Reset Game");
        resetButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        resetButton.setPrefWidth(260);
        resetButton.setPrefHeight(35);
        resetButton.setOnAction(e -> resetGame());

        Label topMovesTitle = new Label("Top Moves");
        topMovesTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        topMovesTitle.setTextFill(Color.web("#9e9e9e"));

        topMovesBox = new VBox(5);
        topMovesBox.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10;");
        topMovesBox.setPrefHeight(120);

        Label historyTitle = new Label("Move History");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        historyTitle.setTextFill(Color.web("#9e9e9e"));

        moveHistoryBox = new VBox(5);
        moveHistoryBox.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10;");

        ScrollPane historyScroll = new ScrollPane(moveHistoryBox);
        historyScroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        historyScroll.setFitToWidth(true);
        historyScroll.setPrefHeight(150);

        panel.getChildren().addAll(
                panelTitle,
                createSeparator(),
                aiStatusLabel,
                aiMoveLabel,
                aiScoreLabel,
                aiNodesLabel,
                aiDepthLabel,
                aiTimeLabel,
                createSeparator(),
                aiMoveButton,
                autoAnalyzeButton,
                resetButton,
                createSeparator(),
                topMovesTitle,
                topMovesBox,
                createSeparator(),
                historyTitle,
                historyScroll
        );

        return panel;
    }

    private void toggleAutoAnalyze() {
        autoAnalyze = !autoAnalyze;
        autoAnalyzeButton.setText("Auto-Analyze: " + (autoAnalyze ? "ON" : "OFF"));
        autoAnalyzeButton.setStyle("-fx-background-color: " + (autoAnalyze ? "#4CAF50" : "#FF9800") +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");

        if (autoAnalyze) {
            analyzePosition();
        }
    }

    private void analyzePosition() {
        if (aiEngine == null || game.isGameOver()) return;

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            List<Move> allMoves = game.getLegalMoves();

            for (Move move : allMoves) {
                game.getBoard().movePiece(move);
                double score = -evaluator.evaluate(game.getBoard());
                game.getBoard().undoMove(move);
                move.setScore(score);
            }

            allMoves.sort((m1, m2) -> Double.compare(m2.getScore(), m1.getScore()));
            long elapsed = System.currentTimeMillis() - startTime;

            Platform.runLater(() -> {
                updateTopMoves(allMoves.subList(0, Math.min(5, allMoves.size())));
                if (!allMoves.isEmpty()) {
                    aiMoveLabel.setText("Best Move: " + allMoves.get(0).toUCI());
                    aiScoreLabel.setText("Evaluation: " + String.format("%.2f", allMoves.get(0).getScore()));
                }
                aiTimeLabel.setText("Time: " + elapsed + "ms");
            });
        }).start();
    }

    private void updateTopMoves(List<Move> topMoves) {
        topMovesBox.getChildren().clear();

        for (int i = 0; i < topMoves.size(); i++) {
            Move move = topMoves.get(i);
            String moveText = (i + 1) + ". " + move.toUCI() + " (" + String.format("%.2f", move.getScore()) + ")";

            Label moveLabel = new Label(moveText);
            moveLabel.setTextFill(Color.web("#e0e0e0"));
            moveLabel.setFont(Font.font("Consolas", 12));
            topMovesBox.getChildren().add(moveLabel);
        }
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web("#e0e0e0"));
        return label;
    }

    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #3a3a3a;");
        return separator;
    }

    public void setAIEngine(Algorithm engine, Evaluator evaluator, int depth) {
        this.aiEngine = engine;
        this.evaluator = evaluator;
        this.aiDepth = depth;
        aiDepthLabel.setText("Depth: " + depth);
        aiStatusLabel.setText("AI: " + engine.getName());
        analyzePosition();
    }

    private void executeAIMove() {
        if (aiEngine == null || game.isGameOver()) return;

        aiMoveButton.setDisable(true);
        aiStatusLabel.setText("AI: Calculating...");

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            Move bestMove = aiEngine.findBestMove(game.getBoard(), game.isWhiteTurn(), aiDepth);
            int nodesSearched = aiEngine.getNodesSearched();
            long elapsed = System.currentTimeMillis() - startTime;

            Platform.runLater(() -> {
                if (bestMove != null) {
                    game.makeMove(bestMove);

                    aiMoveLabel.setText("Best Move: " + bestMove.toUCI());
                    aiScoreLabel.setText("Evaluation: " + String.format("%.2f", bestMove.getScore()));
                    aiNodesLabel.setText("Nodes: " + nodesSearched);
                    aiTimeLabel.setText("Time: " + elapsed + "ms");
                    aiStatusLabel.setText("AI: Move executed");

                    updateMoveHistory();
                    refreshBoard();

                    if (autoAnalyze) {
                        analyzePosition();
                    }
                }

                aiMoveButton.setDisable(false);
            });
        }).start();
    }

    private void updateMoveHistory() {
        moveHistoryBox.getChildren().clear();
        List<Move> history = game.getMoveHistory();

        for (int i = 0; i < history.size(); i++) {
            Move move = history.get(i);
            String moveText = ((i / 2) + 1) + ". " + move.toUCI();

            Label moveLabel = new Label(moveText);
            moveLabel.setTextFill(i % 2 == 0 ? Color.web("#e0e0e0") : Color.web("#a0a0a0"));
            moveLabel.setFont(Font.font("Consolas", 12));

            moveHistoryBox.getChildren().add(moveLabel);
        }
    }

    private void resetGame() {
        game.reset();
        clearSelection();
        refreshBoard();

        aiStatusLabel.setText("AI: Game Reset");
        aiMoveLabel.setText("Best Move: -");
        aiScoreLabel.setText("Evaluation: 0.0");
        aiNodesLabel.setText("Nodes: 0");
        aiTimeLabel.setText("Time: 0ms");
        moveHistoryBox.getChildren().clear();
        topMovesBox.getChildren().clear();

        if (autoAnalyze) {
            analyzePosition();
        }
    }

    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 15, 10, 20));
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle("-fx-background-color: #252525;");
        bar.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeBtn = createTitleBarButton("−");
        minimizeBtn.setOnAction(e -> stage.setIconified(true));

        Button closeBtn = createTitleBarButton("X");
        closeBtn.setOnAction(e -> System.exit(0));
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;"));

        bar.getChildren().addAll(spacer, minimizeBtn, closeBtn);
        return bar;
    }

    private Button createTitleBarButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9e9e9e; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setPrefSize(30, 30);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9e9e9e; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;"));
        return btn;
    }

    private GridPane createChessBoard() {
        GridPane boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < 8; row++) {
            Label rankLabel = new Label(String.valueOf(8 - row));
            rankLabel.setTextFill(Color.web("#888888"));
            rankLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            rankLabel.setPrefSize(20, SQUARE_SIZE);
            rankLabel.setAlignment(Pos.CENTER);
            boardGrid.add(rankLabel, 0, row);
        }

        for (int col = 0; col < 8; col++) {
            Label fileLabel = new Label(String.valueOf((char) ('a' + col)));
            fileLabel.setTextFill(Color.web("#888888"));
            fileLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            fileLabel.setPrefSize(SQUARE_SIZE, 20);
            fileLabel.setAlignment(Pos.CENTER);
            boardGrid.add(fileLabel, col + 1, 8);
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createSquare(row, col);
                boardGrid.add(square, col + 1, row);
            }
        }

        return boardGrid;
    }

    private StackPane createSquare(int row, int col) {
        StackPane square = new StackPane();
        square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        boolean isLight = (row + col) % 2 == 0;
        Color squareColor = isLight ? LIGHT_SQUARE : DARK_SQUARE;
        square.setStyle("-fx-background-color: " + toHexString(squareColor) + ";");

        Piece piece = game.getBoard().getPiece(row, col);
        if (piece != null) {
            ImageView pieceImage = loadPieceImage(piece);
            if (pieceImage != null) {
                square.getChildren().add(pieceImage);
                setupPieceDragHandlers(pieceImage, square, row, col);
            }
        }

        square.setUserData(new int[]{row, col});

        square.setOnMouseEntered(e -> {
            if (selectedSquare != square && !isSquareHighlighted(row, col)) {
                square.setStyle("-fx-background-color: " + toHexString(squareColor.brighter()) + "; -fx-cursor: hand;");
            }
        });

        square.setOnMouseExited(e -> {
            if (selectedSquare != square && !isSquareHighlighted(row, col)) {
                square.setStyle("-fx-background-color: " + toHexString(squareColor) + ";");
            }
        });

        square.setOnMouseClicked(e -> {
            if (e.getTarget() == square) {
                handleSquareClick(square, row, col);
            }
        });

        return square;
    }

    private boolean isSquareHighlighted(int row, int col) {
        for (int[] pos : validMoveSquares) {
            if (pos[0] == row && pos[1] == col) return true;
        }
        return false;
    }

    private void setupPieceDragHandlers(ImageView pieceImage, StackPane square, int row, int col) {
        pieceImage.setOnMousePressed(e -> {
            Piece piece = game.getBoard().getPiece(row, col);
            if (piece == null || piece.isWhite() != game.isWhiteTurn()) return;

            clearSelection();
            draggedPiece = pieceImage;
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            selectedRow = row;
            selectedCol = col;
            selectedSquare = square;
            square.setStyle("-fx-background-color: " + toHexString(SELECTED_SQUARE) + ";");
            pieceImage.setOpacity(0.7);
            highlightValidMoves(row, col);
            e.consume();
        });

        pieceImage.setOnMouseDragged(e -> {
            if (draggedPiece == pieceImage) {
                pieceImage.setTranslateX(e.getSceneX() - dragStartX);
                pieceImage.setTranslateY(e.getSceneY() - dragStartY);
                e.consume();
            }
        });

        pieceImage.setOnMouseReleased(e -> {
            if (draggedPiece == pieceImage) {
                pieceImage.setOpacity(1.0);
                pieceImage.setTranslateX(0);
                pieceImage.setTranslateY(0);

                int targetRow = findRowFromY(e.getSceneY());
                int targetCol = findColFromX(e.getSceneX());

                if (targetRow != -1 && targetCol != -1) {
                    attemptMove(selectedRow, selectedCol, targetRow, targetCol);
                } else {
                    clearSelection();
                }
                draggedPiece = null;
                e.consume();
            }
        });

        pieceImage.setOnMouseClicked(e -> {
            if (draggedPiece == null) {
                handleSquareClick(square, row, col);
            }
            e.consume();
        });
    }

    private int findRowFromY(double sceneY) {
        for (int row = 0; row < 8; row++) {
            double minY = 100 + row * SQUARE_SIZE;
            double maxY = minY + SQUARE_SIZE;
            if (sceneY >= minY && sceneY < maxY) return row;
        }
        return -1;
    }

    private int findColFromX(double sceneX) {
        for (int col = 0; col < 8; col++) {
            double minX = 30 + (col + 1) * SQUARE_SIZE;
            double maxX = minX + SQUARE_SIZE;
            if (sceneX >= minX && sceneX < maxX) return col;
        }
        return -1;
    }

    private void handleSquareClick(StackPane square, int row, int col) {
        if (selectedRow == -1) {
            Piece piece = game.getBoard().getPiece(row, col);
            if (piece != null && piece.isWhite() == game.isWhiteTurn()) {
                selectedRow = row;
                selectedCol = col;
                selectedSquare = square;
                square.setStyle("-fx-background-color: " + toHexString(SELECTED_SQUARE) + ";");
                highlightValidMoves(row, col);
            }
        } else {
            Piece clickedPiece = game.getBoard().getPiece(row, col);
            if (clickedPiece != null && clickedPiece.isWhite() == game.isWhiteTurn() && (row != selectedRow || col != selectedCol)) {
                clearSelection();
                selectedRow = row;
                selectedCol = col;
                selectedSquare = square;
                square.setStyle("-fx-background-color: " + toHexString(SELECTED_SQUARE) + ";");
                highlightValidMoves(row, col);
            } else {
                attemptMove(selectedRow, selectedCol, row, col);
            }
        }
    }

    private void highlightValidMoves(int fromRow, int fromCol) {
        validMoveSquares.clear();
        List<Move> moves = game.getLegalMovesForPiece(fromRow, fromCol);

        for (Move move : moves) {
            int toRow = move.getToRow();
            int toCol = move.getToCol();
            validMoveSquares.add(new int[]{toRow, toCol});

            StackPane targetSquare = findSquare(toRow, toCol);
            if (targetSquare != null) {
                targetSquare.setStyle("-fx-background-color: " + toHexString(VALID_MOVE_SQUARE) + ";");
            }
        }
    }

    private StackPane findSquare(int row, int col) {
        for (javafx.scene.Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane) {
                Object userData = node.getUserData();
                if (userData instanceof int[]) {
                    int[] pos = (int[]) userData;
                    if (pos[0] == row && pos[1] == col) return (StackPane) node;
                }
            }
        }
        return null;
    }

    private void attemptMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (game.makeMove(fromRow, fromCol, toRow, toCol)) {
            updateMoveHistory();
            refreshBoard();
            checkGameStatus();

            if (autoAnalyze) {
                analyzePosition();
            }
        } else {
            clearSelection();
        }
    }

    private void checkGameStatus() {
        if (game.isGameOver()) {
            aiStatusLabel.setText("Game: " + game.getStatus());
        }
    }

    private void clearSelection() {
        if (selectedSquare != null) {
            int[] pos = (int[]) selectedSquare.getUserData();
            boolean isLight = (pos[0] + pos[1]) % 2 == 0;
            selectedSquare.setStyle("-fx-background-color: " + toHexString(isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");
        }

        for (int[] pos : validMoveSquares) {
            StackPane square = findSquare(pos[0], pos[1]);
            if (square != null) {
                boolean isLight = (pos[0] + pos[1]) % 2 == 0;
                square.setStyle("-fx-background-color: " + toHexString(isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");
            }
        }
        validMoveSquares.clear();
        selectedRow = -1;
        selectedCol = -1;
        selectedSquare = null;
    }

    private void refreshBoard() {
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        boardTitleLabel.setText("♚ Chess Board - " + game.getCurrentPlayer() + "'s Turn");

        chessBoard = createChessBoard();
        centerContent.getChildren().addAll(boardTitleLabel, chessBoard);
        root.setCenter(centerContent);

        selectedRow = -1;
        selectedCol = -1;
        selectedSquare = null;
    }

    private ImageView loadPieceImage(Piece piece) {
        String pieceName = getPieceImageName(piece);
        if (imageCache.containsKey(pieceName)) {
            return createImageView(imageCache.get(pieceName));
        }

        String[] paths = {"/images/" + pieceName + ".png", "/resources/images/" + pieceName + ".png"};
        for (String path : paths) {
            try {
                Image image = new Image(getClass().getResourceAsStream(path));
                imageCache.put(pieceName, image);
                return createImageView(image);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(SQUARE_SIZE * 0.8);
        imageView.setFitHeight(SQUARE_SIZE * 0.8);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setMouseTransparent(false);
        return imageView;
    }

    private String getPieceImageName(Piece piece) {
        String color = piece.isWhite() ? "w" : "b";
        char type = Character.toLowerCase(piece.getSymbol());
        return color + type;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void makeTitleBarDraggable() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public Scene getScene() {
        return scene;
    }

    public Game getGame() {
        return game;
    }
}