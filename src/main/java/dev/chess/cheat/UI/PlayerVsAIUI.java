package dev.chess.cheat.UI;

import dev.chess.cheat.Engine.ChessEngine;
import dev.chess.cheat.Engine.Move.Move;
import dev.chess.cheat.Engine.Move.MoveGenerator;
import dev.chess.cheat.Engine.Search.Algorithm;
import dev.chess.cheat.Engine.Search.AlgorithmFactory;
import dev.chess.cheat.Engine.Evaluation.MasterEvaluator;
import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Game;
import dev.chess.cheat.Simulation.GameStatus;
import dev.chess.cheat.Simulation.Impl.*;
import dev.chess.cheat.Simulation.Piece;
import dev.chess.cheat.UI.Viewer.ConsoleViewer;
import dev.chess.cheat.Util.Interface.SceneMaker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerVsAIUI implements SceneMaker, Game.GameUpdateListener {
    private final Stage stage;
    private final AlgorithmFactory algorithmFactory;
    private final MoveGenerator moveGenerator;
    private final ConsoleViewer console;
    private Game game;
    private ChessEngine engine;
    private boolean playerIsWhite = true;
    private boolean aiThinking = false;
    private boolean currentTurnIsWhite = true;
    private GridPane boardGrid;
    private Label statusLabel;
    private TextArea moveListArea;
    private ComboBox<String> aiAlgorithmCombo;
    private Spinner<Integer> aiDepthSpinner;
    private ToggleGroup colorToggleGroup;
    private Button newGameButton;
    private Button undoButton;
    private final Map<Character, Image> pieceImages;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<Move> legalMovesFromSelected;
    private Move highlightedAIMove = null;

    public PlayerVsAIUI(Stage stage) {
        this.stage = stage;
        this.algorithmFactory = new AlgorithmFactory();
        this.moveGenerator = new MoveGenerator();
        this.pieceImages = new HashMap<>();
        this.console = new ConsoleViewer();
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
        this.engine = new ChessEngine(algorithm);
        this.game = new Game(new Board(), engine);
        this.game.addUpdateListener(this);
        console.log("Game initialized - Player is WHITE, AI is BLACK");
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");
        root.setPadding(new Insets(20));

        Label title = new Label("Player vs AI");
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

        this.statusLabel = new Label("White to move");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statusLabel.setStyle("-fx-text-fill: #00ff00;");

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
        square.setUserData(new int[]{row, col});

        boolean isLightSquare = (row + col) % 2 == 0;
        Color squareColor = isLightSquare ? Color.web("#f0d9b5") : Color.web("#b58863");

        Rectangle bg = new Rectangle(80, 80, squareColor);
        square.getChildren().add(bg);

        square.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !aiThinking) {
                int[] coords = (int[]) square.getUserData();
                handleSquareClick(coords[0], coords[1]);
            }
        });

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

                // Highlight selected square
                if (row == selectedRow && col == selectedCol) {
                    squareColor = Color.web("#7fc97f");
                }

                Rectangle bg = new Rectangle(80, 80, squareColor);
                square.getChildren().add(bg);

                // Add piece image
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

                // Add move indicators for player's legal moves (black circles)
                if (legalMovesFromSelected != null) {
                    for (Move move : legalMovesFromSelected) {
                        if (move.getToRow() == row && move.getToCol() == col) {
                            Circle indicator = new Circle();

                            Piece targetPiece = board.getPiece(row, col);
                            if (targetPiece != null) {
                                // Capture move - ring around edge
                                indicator.setRadius(35);
                                indicator.setFill(Color.TRANSPARENT);
                                indicator.setStroke(Color.rgb(0, 0, 0, 0.5));
                                indicator.setStrokeWidth(6);
                            } else {
                                // Normal move - small filled circle
                                indicator.setRadius(12);
                                indicator.setFill(Color.rgb(0, 0, 0, 0.5));
                            }

                            square.getChildren().add(indicator);
                            break;
                        }
                    }
                }

                // Add move indicators for AI's move (white circles)
                if (highlightedAIMove != null) {
                    if (highlightedAIMove.getToRow() == row && highlightedAIMove.getToCol() == col) {
                        Circle indicator = new Circle();

                        Piece targetPiece = board.getPiece(row, col);
                        if (targetPiece != null) {
                            // Capture move - white ring
                            indicator.setRadius(35);
                            indicator.setFill(Color.TRANSPARENT);
                            indicator.setStroke(Color.rgb(255, 255, 255, 0.7));
                            indicator.setStrokeWidth(6);
                        } else {
                            // Normal move - white filled circle
                            indicator.setRadius(12);
                            indicator.setFill(Color.rgb(255, 255, 255, 0.7));
                        }

                        square.getChildren().add(indicator);
                    } else if (highlightedAIMove.getFromRow() == row && highlightedAIMove.getFromCol() == col) {
                        // Show a smaller white circle on the source square
                        Circle indicator = new Circle();
                        indicator.setRadius(8);
                        indicator.setFill(Color.rgb(255, 255, 255, 0.5));
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

        Label settingsLabel = new Label("Game Settings");
        settingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        settingsLabel.setStyle("-fx-text-fill: #ffffff;");

        Label colorLabel = new Label("Play as:");
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

        Label algoLabel = new Label("AI Algorithm:");
        algoLabel.setStyle("-fx-text-fill: #ffffff;");

        this.aiAlgorithmCombo = new ComboBox<>();
        aiAlgorithmCombo.getItems().addAll(algorithmFactory.getAlgorithmNames());
        aiAlgorithmCombo.setValue("Alpha-Beta");
        aiAlgorithmCombo.setPrefWidth(200);

        Label depthLabel = new Label("AI Depth:");
        depthLabel.setStyle("-fx-text-fill: #ffffff;");

        this.aiDepthSpinner = new Spinner<>(1, 10, 3);
        aiDepthSpinner.setEditable(true);
        aiDepthSpinner.setPrefWidth(100);

        this.newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        newGameButton.setOnAction(e -> startNewGame());

        this.undoButton = new Button("Undo Move");
        undoButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        undoButton.setOnAction(e -> undoLastMove());

        HBox buttonBox = new HBox(10, newGameButton, undoButton);

        Label historyLabel = new Label("Move History");
        historyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        historyLabel.setStyle("-fx-text-fill: #ffffff;");

        this.moveListArea = new TextArea();
        moveListArea.setEditable(false);
        moveListArea.setPrefHeight(200);
        moveListArea.setFont(Font.font("Monospaced", 12));
        moveListArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00;");

        panel.getChildren().addAll(settingsLabel, colorLabel, colorBox, algoLabel, aiAlgorithmCombo, depthLabel, aiDepthSpinner, buttonBox, new Separator(), historyLabel, moveListArea);

        return panel;
    }

    private void handleSquareClick(int row, int col) {
        console.log("=== CLICK [" + row + "," + col + "] ===");

        Piece clickedPiece = game.getBoard().getPiece(row, col);
        console.log("Piece: " + (clickedPiece == null ? "empty" : clickedPiece.getSymbol() + "(" + (clickedPiece.isWhite() ? "W" : "B") + ")"));
        console.log("Turn: " + (currentTurnIsWhite ? "WHITE" : "BLACK") + ", Player: " + (playerIsWhite ? "WHITE" : "BLACK"));

        if (currentTurnIsWhite != playerIsWhite) {
            console.log("Not player's turn - ignored");
            statusLabel.setText("Wait for AI to move...");
            return;
        }

        if (selectedRow == -1) {
            if (clickedPiece != null && clickedPiece.isWhite() == playerIsWhite) {
                selectedRow = row;
                selectedCol = col;
                console.log("Selected piece at [" + row + "," + col + "]");

                legalMovesFromSelected = moveGenerator.generateAllMoves(game.getBoard(), playerIsWhite)
                        .stream()
                        .filter(m -> m.getFromRow() == row && m.getFromCol() == col)
                        .toList();

                console.log("Legal moves: " + legalMovesFromSelected.size());
                updateBoardDisplay();
            } else {
                console.log("Cannot select - wrong color or empty");
            }
        } else {
            Move selectedMove = null;

            if (legalMovesFromSelected != null) {
                for (Move move : legalMovesFromSelected) {
                    if (move.getToRow() == row && move.getToCol() == col) {
                        selectedMove = move;
                        break;
                    }
                }
            }

            if (selectedMove != null) {
                console.log("Executing player move: [" + selectedRow + "," + selectedCol + "] -> [" + row + "," + col + "]");
                executePlayerMove(selectedMove);
            } else {
                if (clickedPiece != null && clickedPiece.isWhite() == playerIsWhite) {
                    selectedRow = row;
                    selectedCol = col;
                    console.log("Reselected piece at [" + row + "," + col + "]");

                    legalMovesFromSelected = moveGenerator.generateAllMoves(game.getBoard(), playerIsWhite)
                            .stream()
                            .filter(m -> m.getFromRow() == row && m.getFromCol() == col)
                            .toList();

                    console.log("Legal moves: " + legalMovesFromSelected.size());
                    updateBoardDisplay();
                    return;
                } else {
                    console.log("Invalid move - deselecting");
                }
            }

            selectedRow = -1;
            selectedCol = -1;
            legalMovesFromSelected = null;
            updateBoardDisplay();
        }
    }

    private void executePlayerMove(Move move) {
        console.log("=== PLAYER MOVE ===");
        console.log("Move: [" + move.getFromRow() + "," + move.getFromCol() + "] -> [" + move.getToRow() + "," + move.getToCol() + "]");
        console.log("Turn before: " + (currentTurnIsWhite ? "WHITE" : "BLACK"));

        highlightedAIMove = null;

        game.getBoard().movePiece(move);

        // Determine promotion
        Piece movedPiece = game.getBoard().getPiece(move.getToRow(), move.getToCol());
        Character promotion = null;

        if (movedPiece instanceof Pawn) {
            if ((movedPiece.isWhite() && move.getToRow() == 0) ||
                    (!movedPiece.isWhite() && move.getToRow() == 7)) {
                console.log("Pawn promotion to Queen");
                game.getBoard().setPiece(move.getToRow(), move.getToCol(), new Queen(movedPiece.isWhite()));
                promotion = 'Q';
            }
        }

        game.addMoveToHistory(move, promotion);

        currentTurnIsWhite = game.isWhiteTurn();  // Sync with game state

        console.log("Turn after: " + (currentTurnIsWhite ? "WHITE" : "BLACK"));
        console.log("Move count: " + game.getMoveCount());

        updateMoveHistory();
        updateBoardDisplay();

        if (game.isGameOver()) {
            console.log("Game over detected");
            displayGameOver();
            return;
        }

        console.log("Triggering AI move");
        makeAIMove();
    }

    private void makeAIMove() {
        aiThinking = true;
        statusLabel.setText("AI is thinking...");
        statusLabel.setStyle("-fx-text-fill: #ffaa00;");

        boolean aiIsWhite = !playerIsWhite;
        console.log("=== AI MOVE (color=" + (aiIsWhite ? "WHITE" : "BLACK") + ") ===");

        new Thread(() -> {
            try {
                int depth = aiDepthSpinner.getValue();

                // DEBUG: Check legal moves BEFORE calling AI
                MoveGenerator debugGen = new MoveGenerator();
                List<Move> legalMoves = debugGen.generateAllMoves(game.getBoard(), aiIsWhite);
                console.log("DEBUG: AI has " + legalMoves.size() + " legal moves available");

                if (legalMoves.isEmpty()) {
                    console.log("DEBUG: No legal moves! Game should be over!");
                    Platform.runLater(() -> {
                        if (debugGen.isKingInCheck(game.getBoard(), aiIsWhite)) {
                            console.log("DEBUG: AI is in CHECKMATE");
                        } else {
                            console.log("DEBUG: AI is in STALEMATE");
                        }
                        aiThinking = false;
                    });
                    return;
                }

                // Add thinking delay BEFORE calculating move (simulates "thinking")
                Thread.sleep(500);

                console.log("DEBUG: Calling engine.findBestMove() with depth=" + depth);
                Move aiMove = engine.findBestMove(game.getBoard(), aiIsWhite, depth);
                console.log("DEBUG: Engine returned: " + (aiMove == null ? "NULL" : aiMove.toString()));

                if (aiMove != null) {
                    final Move finalMove = aiMove;

                    console.log("AI chose: [" + finalMove.getFromRow() + "," + finalMove.getFromCol() + "] -> [" + finalMove.getToRow() + "," + finalMove.getToCol() + "]");

                    // Show the move with white circles for 1.5 seconds before executing
                    Platform.runLater(() -> {
                        highlightedAIMove = finalMove;
                        updateBoardDisplay();
                    });

                    // Wait so user can see what the AI is about to do
                    Thread.sleep(1500);

                    Platform.runLater(() -> {
                        console.log("Executing AI move");
                        console.log("Turn before: " + (currentTurnIsWhite ? "WHITE" : "BLACK"));

                        game.getBoard().movePiece(finalMove);

                        Piece movedPiece = game.getBoard().getPiece(finalMove.getToRow(), finalMove.getToCol());
                        Character promotion = null;


                        if (movedPiece instanceof Pawn) {
                            if ((movedPiece.isWhite() && finalMove.getToRow() == 0) ||
                                    (!movedPiece.isWhite() && finalMove.getToRow() == 7)) {
                                console.log("AI pawn promotion to Queen");
                                game.getBoard().setPiece(finalMove.getToRow(), finalMove.getToCol(), new Queen(movedPiece.isWhite()));
                                promotion = 'Q';
                            }
                        }

                        game.addMoveToHistory(finalMove, promotion);

                        currentTurnIsWhite = game.isWhiteTurn();

                        console.log("Turn after: " + (currentTurnIsWhite ? "WHITE" : "BLACK"));
                        console.log("Move count: " + game.getMoveCount());

                        // Keep showing white circles for another 800ms after move
                        new Thread(() -> {
                            try {
                                Thread.sleep(800);
                                Platform.runLater(() -> {
                                    highlightedAIMove = null;
                                    updateBoardDisplay();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                        updateMoveHistory();
                        updateBoardDisplay();

                        if (game.isGameOver()) {
                            console.log("Game over after AI move");
                            displayGameOver();
                        } else {
                            statusLabel.setText((currentTurnIsWhite ? "White" : "Black") + " to move");
                            statusLabel.setStyle("-fx-text-fill: #00ff00;");
                        }

                        aiThinking = false;
                    });
                } else {
                    console.log("ERROR: AI returned null move!");
                    console.log("DEBUG: This should NOT happen if there are legal moves!");
                    console.log("DEBUG: Check your " + engine.getCurrentAlgorithm().getName() + " implementation");

                    if (!legalMoves.isEmpty()) {
                        console.log("DEBUG: Using fallback - picking random legal move");
                        Move fallbackMove = legalMoves.get((int)(Math.random() * legalMoves.size()));

                        Platform.runLater(() -> {
                            game.getBoard().movePiece(fallbackMove);
                            game.getMoveHistory().add(fallbackMove);
                            game.getPromotions().add(null);
                            currentTurnIsWhite = !currentTurnIsWhite;
                            updateMoveHistory();
                            updateBoardDisplay();
                            aiThinking = false;
                        });
                    } else {
                        Platform.runLater(() -> {
                            aiThinking = false;
                        });
                    }
                }

            } catch (Exception e) {
                console.log("AI ERROR: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("AI error!");
                    statusLabel.setStyle("-fx-text-fill: #ff0000;");
                    aiThinking = false;
                });
            }
        }).start();
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
        String message = switch (status) {
            case WHITE_WINS -> "Checkmate! White wins!";
            case BLACK_WINS -> "Checkmate! Black wins!";
            case STALEMATE -> "Stalemate! Draw!";
            case DRAW -> "Draw!";
            default -> "Game Over";
        };

        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
        console.log("GAME OVER: " + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startNewGame() {
        console.log("=== NEW GAME ===");

        Toggle selected = colorToggleGroup.getSelectedToggle();
        if (selected != null) {
            playerIsWhite = (Boolean) selected.getUserData();
        }
        console.log("Player color: " + (playerIsWhite ? "WHITE" : "BLACK"));

        String selectedAlgorithm = aiAlgorithmCombo.getValue();
        Algorithm newAlgorithm = algorithmFactory.createAlgorithm(selectedAlgorithm, new MasterEvaluator(), moveGenerator);
        engine.setAlgorithm(newAlgorithm);

        game.reset();
        selectedRow = -1;
        selectedCol = -1;
        legalMovesFromSelected = null;
        aiThinking = false;
        currentTurnIsWhite = true;
        highlightedAIMove = null;

        console.log("Game reset - turn is WHITE");

        updateBoardDisplay();
        moveListArea.clear();
        statusLabel.setText("White to move");
        statusLabel.setStyle("-fx-text-fill: #00ff00;");

        if (!playerIsWhite) {
            console.log("Player is BLACK - AI moves first");
            makeAIMove();
        }
    }

    private void undoLastMove() {
        console.log("=== UNDO REQUESTED ===");
        List<Move> moves = game.getMoveHistory();
        console.log("Current move count: " + moves.size());

        if (moves.size() < 2) {
            console.log("Not enough moves to undo");
            return;
        }

        console.log("Removing last 2 moves");
        moves.remove(moves.size() - 1);
        moves.remove(moves.size() - 1);

        List<Character> promotions = game.getPromotions();
        if (promotions.size() >= 2) {
            promotions.remove(promotions.size() - 1);
            promotions.remove(promotions.size() - 1);
        }

        console.log("Resetting board and replaying " + moves.size() + " moves");
        game.reset();

        if (moves.size() > 0) {
            String[] uciMoves = new String[moves.size()];
            for (int i = 0; i < moves.size(); i++) {
                uciMoves[i] = game.moveToUCI(moves.get(i));
                console.log("Replaying move " + (i+1) + ": " + uciMoves[i]);
            }
            game.updateFromMoves(uciMoves);
        }

        currentTurnIsWhite = (moves.size() % 2 == 0);
        highlightedAIMove = null;

        console.log("After undo - turn: " + (currentTurnIsWhite ? "WHITE" : "BLACK"));
        console.log("Move count: " + game.getMoveHistory().size());

        updateBoardDisplay();
        updateMoveHistory();
        statusLabel.setText((currentTurnIsWhite ? "White" : "Black") + " to move");
    }

    @Override
    public void onGameUpdated(Game game) {
        Platform.runLater(this::updateBoardDisplay);
    }
}