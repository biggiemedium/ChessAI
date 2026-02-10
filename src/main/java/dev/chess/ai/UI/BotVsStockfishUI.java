package dev.chess.ai.UI;

import dev.chess.ai.Engine.ChessEngine;
import dev.chess.ai.Engine.External.StockfishEngine;
import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Search.Algorithm;
import dev.chess.ai.Engine.Search.AlgorithmFactory;
import dev.chess.ai.Engine.Evaluation.MasterEvaluator;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Game;
import dev.chess.ai.Simulation.GameStatus;
import dev.chess.ai.Simulation.Impl.*;
import dev.chess.ai.Simulation.Piece;
import dev.chess.ai.UI.Viewer.ConsoleViewer;
import dev.chess.ai.Util.Interface.SceneMaker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotVsStockfishUI implements SceneMaker, Game.GameUpdateListener {
    private final Stage stage;
    private final AlgorithmFactory algorithmFactory;
    private final MoveGenerator moveGenerator;
    private final ConsoleViewer console;
    private Game game;
    private ChessEngine myEngine;
    private StockfishEngine stockfish;
    private boolean myBotIsWhite = true;
    private boolean gameInProgress = false;
    private GridPane boardGrid;
    private Label statusLabel;
    private TextArea moveListArea;
    private ComboBox<String> myAlgorithmCombo;
    private Spinner<Integer> myDepthSpinner;
    private Spinner<Integer> stockfishDepthSpinner;
    private ToggleGroup colorToggleGroup;
    private Button startButton;
    private Button stopButton;
    private Button newGameButton;
    private final Map<Character, Image> pieceImages;
    private Move highlightedMove = null;
    private Label myBotScoreLabel;
    private Label stockfishScoreLabel;

    public BotVsStockfishUI(Stage stage) {
        this.stage = stage;
        this.algorithmFactory = new AlgorithmFactory();
        this.moveGenerator = new MoveGenerator();
        this.pieceImages = new HashMap<>();
        this.console = new ConsoleViewer();
        this.stockfish = new StockfishEngine();
        loadPieceImages();
        initializeGame();
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
            console.log("Piece images loaded successfully");
        } catch (Exception e) {
            console.log("Failed to load piece images: " + e.getMessage());
        }
    }

    private void initializeGame() {
        Algorithm algorithm = algorithmFactory.createAlgorithm("Alpha-Beta", new MasterEvaluator(), moveGenerator);
        this.myEngine = new ChessEngine(algorithm);
        this.game = new Game(new Board(), myEngine);
        this.game.addUpdateListener(this);
        console.log("Game initialized");
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");
        root.setPadding(new Insets(20));

        Label title = new Label("Your Bot vs Stockfish");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #ffffff;");

        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.getChildren().add(title);

        root.setTop(topBox);

        VBox boardContainer = new VBox(10);
        boardContainer.setAlignment(Pos.CENTER);

        this.boardGrid = createChessBoard();
        updateBoardDisplay();

        this.statusLabel = new Label("Configure settings and click 'Start Game'");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statusLabel.setStyle("-fx-text-fill: #ffaa00;");

        boardContainer.getChildren().addAll(boardGrid, statusLabel);
        root.setCenter(boardContainer);

        VBox rightPanel = createRightPanel();

        VBox consoleWrapper = new VBox(10);
        consoleWrapper.setPadding(new Insets(10, 0, 0, 20));
        consoleWrapper.getChildren().addAll(rightPanel, new Separator(), console.getNode());
        VBox.setVgrow(console.getNode(), Priority.ALWAYS);

        root.setRight(consoleWrapper);

        return new Scene(root, 1400, 800);
    }

    private GridPane createChessBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #3a3a3a; -fx-padding: 10;");

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createSquare(row, col);
                grid.add(square, col, row);
            }
        }

        return grid;
    }

    private StackPane createSquare(int row, int col) {
        StackPane square = new StackPane();
        square.setPrefSize(80, 80);

        boolean isLightSquare = (row + col) % 2 == 0;
        Color squareColor = isLightSquare ? Color.web("#f0d9b5") : Color.web("#b58863");

        Rectangle bg = new Rectangle(80, 80, squareColor);
        square.getChildren().add(bg);

        return square;
    }

    private void updateBoardDisplay() {
        Board board = game.getBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = (StackPane) getNodeFromGridPane(boardGrid, col, row);

                if (square == null) continue;

                square.getChildren().clear();

                boolean isLightSquare = (row + col) % 2 == 0;
                Color squareColor = isLightSquare ? Color.web("#f0d9b5") : Color.web("#b58863");

                Rectangle bg = new Rectangle(80, 80, squareColor);
                square.getChildren().add(bg);

                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    Image image = pieceImages.get(piece.getSymbol());
                    if (image != null) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(64);
                        imageView.setFitHeight(64);
                        imageView.setPreserveRatio(true);
                        square.getChildren().add(imageView);
                    }
                }

                // Highlight last move
                if (highlightedMove != null) {
                    if (highlightedMove.getToRow() == row && highlightedMove.getToCol() == col) {
                        Circle indicator = new Circle();
                        Piece targetPiece = board.getPiece(row, col);
                        if (targetPiece != null) {
                            indicator.setRadius(35);
                            indicator.setFill(Color.TRANSPARENT);
                            indicator.setStroke(Color.rgb(255, 255, 0, 0.7));
                            indicator.setStrokeWidth(6);
                        } else {
                            indicator.setRadius(12);
                            indicator.setFill(Color.rgb(255, 255, 0, 0.7));
                        }
                        square.getChildren().add(indicator);
                    } else if (highlightedMove.getFromRow() == row && highlightedMove.getFromCol() == col) {
                        Circle indicator = new Circle();
                        indicator.setRadius(8);
                        indicator.setFill(Color.rgb(255, 255, 0, 0.5));
                        square.getChildren().add(indicator);
                    }
                }
            }
        }
    }

    private javafx.scene.Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);
            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                return node;
            }
        }
        return null;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 0, 20));
        panel.setPrefWidth(300);

        Label settingsLabel = new Label("Match Settings");
        settingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        settingsLabel.setStyle("-fx-text-fill: #ffffff;");

        // Score display
        HBox scoreBox = new HBox(20);
        scoreBox.setAlignment(Pos.CENTER);

        this.myBotScoreLabel = new Label("Your Bot: 0");
        myBotScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        myBotScoreLabel.setStyle("-fx-text-fill: #00ff00;");

        this.stockfishScoreLabel = new Label("Stockfish: 0");
        stockfishScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        stockfishScoreLabel.setStyle("-fx-text-fill: #ff0000;");

        scoreBox.getChildren().addAll(myBotScoreLabel, stockfishScoreLabel);

        Label colorLabel = new Label("Your Bot plays as:");
        colorLabel.setStyle("-fx-text-fill: #ffffff;");

        this.colorToggleGroup = new ToggleGroup();
        RadioButton whiteRadio = new RadioButton("White");
        whiteRadio.setToggleGroup(colorToggleGroup);
        whiteRadio.setSelected(true);
        whiteRadio.setStyle("-fx-text-fill: #ffffff;");
        whiteRadio.setUserData(true);

        RadioButton blackRadio = new RadioButton("Black");
        blackRadio.setToggleGroup(colorToggleGroup);
        blackRadio.setStyle("-fx-text-fill: #ffffff;");
        blackRadio.setUserData(false);

        HBox colorBox = new HBox(10, whiteRadio, blackRadio);

        Label myAlgoLabel = new Label("Your Bot Algorithm:");
        myAlgoLabel.setStyle("-fx-text-fill: #00ff00;");

        this.myAlgorithmCombo = new ComboBox<>();
        myAlgorithmCombo.getItems().addAll(algorithmFactory.getAlgorithmNames());
        myAlgorithmCombo.setValue("Alpha-Beta");
        myAlgorithmCombo.setPrefWidth(200);

        Label myDepthLabel = new Label("Your Bot Depth:");
        myDepthLabel.setStyle("-fx-text-fill: #00ff00;");

        this.myDepthSpinner = new Spinner<>(1, 10, 5);
        myDepthSpinner.setEditable(true);
        myDepthSpinner.setPrefWidth(100);

        Label stockfishDepthLabel = new Label("Stockfish Depth:");
        stockfishDepthLabel.setStyle("-fx-text-fill: #ff0000;");

        this.stockfishDepthSpinner = new Spinner<>(1, 30, 15);
        stockfishDepthSpinner.setEditable(true);
        stockfishDepthSpinner.setPrefWidth(100);

        this.startButton = new Button("Start Game");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        startButton.setOnAction(e -> startGame());

        this.stopButton = new Button("Stop");
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopGame());

        this.newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        newGameButton.setOnAction(e -> resetGame());

        HBox buttonBox = new HBox(10, startButton, stopButton, newGameButton);

        Label historyLabel = new Label("Move History");
        historyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        historyLabel.setStyle("-fx-text-fill: #ffffff;");

        this.moveListArea = new TextArea();
        moveListArea.setEditable(false);
        moveListArea.setPrefHeight(200);
        moveListArea.setFont(Font.font("Monospaced", 12));
        moveListArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00;");

        panel.getChildren().addAll(
                settingsLabel,
                scoreBox,
                new Separator(),
                colorLabel,
                colorBox,
                new Separator(),
                myAlgoLabel,
                myAlgorithmCombo,
                myDepthLabel,
                myDepthSpinner,
                new Separator(),
                stockfishDepthLabel,
                stockfishDepthSpinner,
                buttonBox,
                new Separator(),
                historyLabel,
                moveListArea
        );

        return panel;
    }

    private void startGame() {
        console.log("=== STARTING GAME ===");

        // Get settings
        Toggle selected = colorToggleGroup.getSelectedToggle();
        if (selected != null) {
            myBotIsWhite = (Boolean) selected.getUserData();
        }

        // Configure my engine
        String selectedAlgorithm = myAlgorithmCombo.getValue();
        Algorithm newAlgorithm = algorithmFactory.createAlgorithm(selectedAlgorithm, new MasterEvaluator(), moveGenerator);
        myEngine.setAlgorithm(newAlgorithm);

        // Start Stockfish
        try {
            console.log("Starting Stockfish...");
            stockfish.start();
            stockfish.setOption("Threads", "4");
            stockfish.setOption("Hash", "256");
            stockfish.newGame();
            console.log("Stockfish started!");
        } catch (IOException e) {
            console.log("ERROR: Failed to start Stockfish: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Stockfish Error");
            alert.setHeaderText("Failed to start Stockfish");
            alert.setContentText("Make sure stockfish.exe is at:\nC:\\Users\\James Kemp\\Desktop\\stockfish\\stockfish.exe");
            alert.showAndWait();
            return;
        }

        // Reset game
        game.reset();
        highlightedMove = null;
        updateBoardDisplay();
        moveListArea.clear();

        // Update UI
        startButton.setDisable(true);
        stopButton.setDisable(false);
        myAlgorithmCombo.setDisable(true);
        myDepthSpinner.setDisable(true);
        stockfishDepthSpinner.setDisable(true);
        colorToggleGroup.getToggles().forEach(t -> ((RadioButton)t).setDisable(true));

        gameInProgress = true;

        console.log("Your Bot: " + (myBotIsWhite ? "WHITE" : "BLACK"));
        console.log("Stockfish: " + (myBotIsWhite ? "BLACK" : "WHITE"));

        statusLabel.setText("Game in progress...");
        statusLabel.setStyle("-fx-text-fill: #00ff00;");

        // Start the game loop
        playNextMove();
    }

    private void stopGame() {
        console.log("=== GAME STOPPED ===");
        gameInProgress = false;
        stockfish.stop();

        startButton.setDisable(false);
        stopButton.setDisable(true);
        myAlgorithmCombo.setDisable(false);
        myDepthSpinner.setDisable(false);
        stockfishDepthSpinner.setDisable(false);
        colorToggleGroup.getToggles().forEach(t -> ((RadioButton)t).setDisable(false));

        statusLabel.setText("Game stopped");
        statusLabel.setStyle("-fx-text-fill: #ffaa00;");
    }

    private void resetGame() {
        if (gameInProgress) {
            stopGame();
        }
        game.reset();
        highlightedMove = null;
        updateBoardDisplay();
        moveListArea.clear();
        statusLabel.setText("Configure settings and click 'Start Game'");
        statusLabel.setStyle("-fx-text-fill: #ffaa00;");
        console.log("Game reset");
    }

    private void playNextMove() {
        if (!gameInProgress) {
            return;
        }

        if (game.isGameOver()) {
            displayGameOver();
            return;
        }

        boolean currentTurnIsWhite = game.isWhiteTurn();
        boolean myTurn = (currentTurnIsWhite == myBotIsWhite);

        new Thread(() -> {
            try {
                // Small delay to see the board
                Thread.sleep(500);

                if (myTurn) {
                    // My bot's turn
                    Platform.runLater(() -> {
                        statusLabel.setText("Your Bot is thinking...");
                        statusLabel.setStyle("-fx-text-fill: #00ff00;");
                    });

                    console.log("=== YOUR BOT'S TURN ===");
                    int depth = myDepthSpinner.getValue();
                    Move move = myEngine.findBestMove(game.getBoard(), currentTurnIsWhite, depth);

                    if (move != null) {
                        console.log("Your bot chose: " + game.moveToUCI(move));
                        executeMove(move);
                    } else {
                        console.log("ERROR: Your bot returned null move!");
                        Platform.runLater(() -> stopGame());
                    }

                } else {
                    // Stockfish's turn
                    Platform.runLater(() -> {
                        statusLabel.setText("Stockfish is thinking...");
                        statusLabel.setStyle("-fx-text-fill: #ff0000;");
                    });

                    console.log("=== STOCKFISH'S TURN ===");

                    // Get move history in UCI format
                    List<String> uciMoves = convertHistoryToUCI();
                    int depth = stockfishDepthSpinner.getValue();

                    String uciMove = stockfish.getBestMoveByDepth(uciMoves, depth);

                    if (uciMove != null) {
                        console.log("Stockfish chose: " + uciMove);
                        Move move = parseUCIMove(uciMove);
                        if (move != null) {
                            executeMove(move);
                        } else {
                            console.log("ERROR: Failed to parse UCI move: " + uciMove);
                            Platform.runLater(() -> stopGame());
                        }
                    } else {
                        console.log("ERROR: Stockfish returned null move!");
                        Platform.runLater(() -> stopGame());
                    }
                }

            } catch (Exception e) {
                console.log("ERROR during move: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> stopGame());
            }
        }).start();
    }

    private void executeMove(Move move) {
        Platform.runLater(() -> {
            game.getBoard().movePiece(move);

            // Handle promotion
            Piece movedPiece = game.getBoard().getPiece(move.getToRow(), move.getToCol());
            Character promotion = null;

            if (movedPiece instanceof Pawn) {
                if ((movedPiece.isWhite() && move.getToRow() == 0) ||
                        (!movedPiece.isWhite() && move.getToRow() == 7)) {
                    game.getBoard().setPiece(move.getToRow(), move.getToCol(), new Queen(movedPiece.isWhite()));
                    promotion = 'Q';
                }
            }

            game.addMoveToHistory(move, promotion);
            highlightedMove = move;

            updateMoveHistory();
            updateBoardDisplay();

            // Continue game
            new Thread(() -> {
                try {
                    Thread.sleep(800); // Pause to see the move
                    playNextMove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private Move parseUCIMove(String uci) {
        if (uci == null || uci.length() < 4) {
            return null;
        }

        try {
            int fromCol = uci.charAt(0) - 'a';
            int fromRow = 8 - (uci.charAt(1) - '0');
            int toCol = uci.charAt(2) - 'a';
            int toRow = 8 - (uci.charAt(3) - '0');

            Piece captured = game.getBoard().getPiece(toRow, toCol);

            return new Move(fromRow, fromCol, toRow, toCol, captured);

        } catch (Exception e) {
            console.log("Error parsing UCI: " + uci);
            return null;
        }
    }

    private List<String> convertHistoryToUCI() {
        List<String> uciMoves = new ArrayList<>();
        List<Move> history = game.getMoveHistory();
        List<Character> promotions = game.getPromotions();

        for (int i = 0; i < history.size(); i++) {
            Move move = history.get(i);
            String uci = game.moveToUCI(move);

            if (i < promotions.size() && promotions.get(i) != null && !uci.endsWith("q")) {
                uci += promotions.get(i).toString().toLowerCase();
            }

            uciMoves.add(uci);
        }

        return uciMoves;
    }

    private void updateMoveHistory() {
        StringBuilder sb = new StringBuilder();
        List<Move> moves = game.getMoveHistory();
        List<Character> promotions = game.getPromotions();

        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                sb.append((i / 2 + 1)).append(". ");
            }

            Move move = moves.get(i);
            String uci = game.moveToUCI(move);

            if (i < promotions.size() && promotions.get(i) != null) {
                uci += Character.toLowerCase(promotions.get(i));
            }

            sb.append(uci);

            if (i % 2 == 0) {
                sb.append(" ");
            } else {
                sb.append("\n");
            }
        }

        moveListArea.setText(sb.toString());
    }

    private void displayGameOver() {
        GameStatus status = game.getStatus();
        String winner;

        switch (status) {
            case WHITE_WINS:
                winner = myBotIsWhite ? "Your Bot wins!" : "Stockfish wins!";
                break;
            case BLACK_WINS:
                winner = myBotIsWhite ? "Stockfish wins!" : "Your Bot wins!";
                break;
            case STALEMATE:
                winner = "Stalemate - Draw!";
                break;
            case DRAW:
                winner = "Draw!";
                break;
            default:
                winner = "Game Over";
        }

        statusLabel.setText(winner);
        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
        console.log("GAME OVER: " + winner);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(winner + "\n\nMoves: " + game.getMoveCount());
            alert.showAndWait();

            stopGame();
        });
    }

    @Override
    public void onGameUpdated(Game game) {
        Platform.runLater(this::updateBoardDisplay);
    }
}