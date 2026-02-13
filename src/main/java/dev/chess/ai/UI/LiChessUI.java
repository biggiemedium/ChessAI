package dev.chess.ai.UI;

import com.google.gson.JsonObject;
import dev.chess.ai.Engine.ChessEngine;
import dev.chess.ai.Engine.Move.Move;
import dev.chess.ai.Engine.Move.MoveGenerator;
import dev.chess.ai.Engine.Search.Algorithm;
import dev.chess.ai.Engine.Search.AlgorithmFactory;
import dev.chess.ai.Engine.Search.impl.AlphaBetaAlgorithm;
import dev.chess.ai.Engine.Evaluation.MasterEvaluator;
import dev.chess.ai.Network.Impl.LiChessClient;
import dev.chess.ai.Simulation.Board;
import dev.chess.ai.Simulation.Game;
import dev.chess.ai.UI.Viewer.ConsoleViewer;
import dev.chess.ai.Util.Board.BoardUtils;
import dev.chess.ai.Util.Interface.ILiChessEvents;
import dev.chess.ai.Util.Interface.SceneMaker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class LiChessUI implements SceneMaker, ILiChessEvents, Game.GameUpdateListener, LiChessClient.GameStreamCallback {

    private final Stage stage;
    private final ConsoleViewer console;

    // Simulation setup
    private LiChessClient client;
    private ChessEngine engine;
    private final AlgorithmFactory algorithmFactory;
    private Game game;
    private String currentGameId;
    private boolean isPlayingWhite; // Track which color we're playing

    // UI Components
    private Label connectionStatus;
    private Label usernameDisplay;

    // Connection components
    private VBox centerBox;
    private Button connectButton;
    private TextField token;

    // In Game components
    private VBox gameBox;
    private Button connectionButton;
    private ToggleGroup gameTypeGroup;
    private RadioButton aiRadio;
    private RadioButton playerRadio;
    private HBox radioButtonBox;
    private Button displayGame;
    private Spinner<Integer> aiLevelSpinner;

    // My AI settings
    private Spinner<Integer> depthSpinner;
    private ComboBox<String> algorithmComboBox;

    // Game state
    private boolean connected = false;
    private boolean inGame = false;
    private volatile boolean waitingForMoveResponse = false;
    private volatile int moveCountWhenStartedCalculating = -1;

    public LiChessUI(Stage stage) {
        this.stage = stage;
        this.console = new ConsoleViewer();

        // Initialize engine and game
        this.algorithmFactory = new AlgorithmFactory();
        this.engine = new ChessEngine(new AlphaBetaAlgorithm(new MasterEvaluator(), new MoveGenerator(new Board())));
        this.game = new Game(new Board(), engine);
        this.game.addUpdateListener(this); // Listen to game updates

        console.log("============================");
        console.log(" ");
        console.log("         LICHESS BOT        ");
        console.log(" ");
        console.log("============================");
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();

        // Header bar (top)
        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e1e1e;");

        this.connectionStatus = new Label("Disconnected");
        this.connectionStatus.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        this.usernameDisplay = new Label("");
        this.usernameDisplay.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        this.usernameDisplay.setVisible(false);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(connectionStatus, spacer, usernameDisplay);
        root.setTop(header);

        // Console display
        VBox consoleWrapper = new VBox(console.getNode());
        consoleWrapper.setPadding(new Insets(0, 10, 0, 0));
        VBox.setVgrow(console.getNode(), Priority.ALWAYS);
        root.setRight(consoleWrapper);

        // Connect state
        Label tokenLabel = new Label("Enter your LiChess Token:");
        tokenLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        this.token = new TextField();
        token.setStyle(
                "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-border-color: #4a90e2;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 6 8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #1e1e1e;"
        );

        this.connectButton = new Button("Connect");
        connectButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        );

        connectButton.setOnMouseEntered(e -> connectButton.setStyle(
                "-fx-background-color: #357ABD;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));

        connectButton.setOnMouseExited(e -> connectButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        this.connectButton.setOnAction(actionEvent -> connect(token.getText()));

        centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(tokenLabel, token, connectButton);

        // ========== Game Settings ========== //

        // AI Settings Group
        Label aiSettingsLabel = new Label("AI Settings");
        aiSettingsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Algorithm ComboBox
        Label algorithmLabel = new Label("Algorithm:");
        algorithmLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        this.algorithmComboBox = new ComboBox<>();
        algorithmComboBox.getItems().addAll(algorithmFactory.getAlgorithmNames());
        algorithmComboBox.setValue("Alpha-Beta"); // Default selection
        algorithmComboBox.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-text-fill: white;"
        );

        // Update algorithm when selection changes
        algorithmComboBox.setOnAction(e -> {
            String selectedAlgorithm = algorithmComboBox.getValue();
            Algorithm newAlgorithm = algorithmFactory.createAlgorithm(
                    selectedAlgorithm,
                    new MasterEvaluator(),
                    new MoveGenerator(game.getBoard())
            );
            engine.setAlgorithm(newAlgorithm);
            console.log("Algorithm changed to: " + selectedAlgorithm);
        });

        HBox algorithmBox = new HBox(10);
        algorithmBox.setAlignment(Pos.CENTER);
        algorithmBox.getChildren().addAll(algorithmLabel, algorithmComboBox);

        // Search Depth Spinner
        Label depthLabel = new Label("Search Depth:");
        depthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        this.depthSpinner = new Spinner<>(1, 10, 3);
        depthSpinner.setEditable(true);
        depthSpinner.setPrefWidth(80);
        depthSpinner.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-text-fill: white;"
        );

        HBox depthBox = new HBox(10);
        depthBox.setAlignment(Pos.CENTER);
        depthBox.getChildren().addAll(depthLabel, depthSpinner);

        // AI Settings container
        VBox aiSettingsBox = new VBox(10);
        aiSettingsBox.setAlignment(Pos.CENTER);
        aiSettingsBox.setStyle(
                "-fx-border-color: #4a90e2;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-padding: 15;"
        );
        aiSettingsBox.getChildren().addAll(aiSettingsLabel, algorithmBox, depthBox);

        // ========== Queue Group ========== //

        // Queue Group Label
        Label queueLabel = new Label("Queue");
        queueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Game type selection
        this.gameTypeGroup = new ToggleGroup();

        this.aiRadio = new RadioButton("AI");
        this.aiRadio.setToggleGroup(gameTypeGroup);
        this.aiRadio.setSelected(true);
        this.aiRadio.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        this.playerRadio = new RadioButton("Player");
        this.playerRadio.setToggleGroup(gameTypeGroup);
        this.playerRadio.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        this.radioButtonBox = new HBox(15);
        this.radioButtonBox.setAlignment(Pos.CENTER);
        this.radioButtonBox.getChildren().addAll(aiRadio, playerRadio);

        // AI Level Spinner (for LiChess AI challenges)
        Label aiLevelLabel = new Label("LiChess AI Level:");
        aiLevelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        this.aiLevelSpinner = new Spinner<>(1, 8, 3);
        aiLevelSpinner.setEditable(true);
        aiLevelSpinner.setPrefWidth(80);
        aiLevelSpinner.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-text-fill: white;"
        );

        HBox aiLevelBox = new HBox(10);
        aiLevelBox.setAlignment(Pos.CENTER);
        aiLevelBox.getChildren().addAll(aiLevelLabel, aiLevelSpinner);

        // Queue button
        this.connectionButton = new Button("Queue");
        connectionButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        );

        connectionButton.setOnMouseEntered(e -> connectionButton.setStyle(
                "-fx-background-color: #357ABD;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        connectionButton.setOnMouseExited(e -> connectionButton.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        this.connectionButton.setOnAction(actionEvent -> handleQueueButton());

        // Queue container
        VBox queueBox = new VBox(10);
        queueBox.setAlignment(Pos.CENTER);
        queueBox.setStyle(
                "-fx-border-color: #4a90e2;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-padding: 15;"
        );
        queueBox.getChildren().addAll(queueLabel, radioButtonBox, aiLevelBox, connectionButton);

        // Display Game button (outside the groups)
        this.displayGame = new Button("Display Game");
        displayGame.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        );

        displayGame.setOnMouseEntered(e -> displayGame.setStyle(
                "-fx-background-color: #357ABD;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        displayGame.setOnMouseExited(e -> displayGame.setStyle(
                "-fx-background-color: #4a90e2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 20;" +
                        "-fx-cursor: hand;"
        ));
        this.displayGame.setOnAction(actionEvent -> openGameInBrowser());

        this.gameBox = new VBox(15);
        this.gameBox.setAlignment(Pos.CENTER);
        this.gameBox.getChildren().addAll(
                aiSettingsBox,
                queueBox,
                displayGame
        );

        // Initial visibility setup
        this.centerBox.setVisible(true);
        this.centerBox.setManaged(true);
        this.gameBox.setVisible(false);
        this.gameBox.setManaged(false);
        this.displayGame.setVisible(false);
        this.displayGame.setManaged(false);

        VBox centerWrapper = new VBox();
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setSpacing(10);
        centerWrapper.setPadding(new Insets(20));
        centerWrapper.getChildren().addAll(centerBox, gameBox);

        root.setLeft(centerWrapper);
        root.setStyle("-fx-background-color: #282424;");

        return new Scene(root, 900, 500);
    }

    // ========== Connection Management ==========

    public void connect(String token) {
        if (token == null || token.isEmpty()) {
            console.log("Please enter a token!");
            return;
        }

        this.client = new LiChessClient(token);
        this.client.setEventListener(this);

        console.log("Connecting to LiChess...");
        new Thread(() -> {
            boolean success = client.establishConnection();

            Platform.runLater(() -> {
                if (success) {
                    this.connected = true;
                    this.connectionStatus.setText("Connected");
                    this.connectionStatus.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 16px; -fx-font-weight: bold;");
                    this.console.log("Successfully connected!");

                    String username = client.getOurUsername();
                    if (username != null && !username.isEmpty()) {
                        usernameDisplay.setText("Username: " + username);
                        usernameDisplay.setVisible(true);
                        console.log("Logged in as: " + username);
                    }

                    client.startGlobalEventStream();

                    this.centerBox.setVisible(false);
                    this.centerBox.setManaged(false);
                    this.gameBox.setVisible(true);
                    this.gameBox.setManaged(true);
                }
            });
        }).start();
    }

    // ========== Game Management ==========

    private void handleQueueButton() {
        if (inGame) {
            // Resign and disconnect
            if (currentGameId != null) {
                console.log("Resigning from game " + currentGameId);
                client.resignGame(currentGameId);
            }

            inGame = false;
            currentGameId = null;

            Platform.runLater(() -> {
                connectionButton.setText("Queue");
                radioButtonBox.setVisible(true);
                radioButtonBox.setManaged(true);
                displayGame.setVisible(false);
                displayGame.setManaged(false);
            });
        } else {
            inGame = true;

            if (aiRadio.isSelected()) {
                int aiLevel = aiLevelSpinner.getValue();
                console.log("Challenging AI (Level " + aiLevel + ", 5+0)...");
                client.challengeAI(aiLevel, 5, 0);
            } else {
                console.log("Player challenge not yet implemented");
                inGame = false;
                return;
            }

            Platform.runLater(() -> {
                connectionButton.setText("Disconnect");
                radioButtonBox.setVisible(false);
                radioButtonBox.setManaged(false);
            });
        }
    }

    private void openGameInBrowser() {
        if (!connected || client == null) {
            console.log("Not connected to LiChess.");
            return;
        }

        if (currentGameId == null || currentGameId.isEmpty()) {
            console.log("No active game to display.");
            return;
        }

        try {
            URI gameUri = new URI("https://lichess.org/" + currentGameId);
            java.awt.Desktop.getDesktop().browse(gameUri);
            console.log("Opened game: " + currentGameId);
        } catch (IOException | URISyntaxException e) {
            console.log("Failed to open game in browser.");
            e.printStackTrace();
        }
    }

    // ========== ILiChessEvents Implementation ==========

    @Override
    public void onConnected(String username) {
        Platform.runLater(() -> {
            this.connected = true;
            connectionStatus.setText("Connected");
            connectionStatus.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 16px; -fx-font-weight: bold;");

            usernameDisplay.setText("Username: " + username);
            usernameDisplay.setVisible(true);

            centerBox.setVisible(false);
            centerBox.setManaged(false);
            gameBox.setVisible(true);
            gameBox.setManaged(true);

            console.log("Connected as " + username);
        });
    }

    @Override
    public void onGameStarted(String gameId) {
        if (!inGame || currentGameId != null) {
            console.log("Ignoring game event for: " + gameId);
            return;
        }

        this.currentGameId = gameId;
        this.game.reset();
        this.game.setGameId(gameId);

        String ourColor = client.getCurrentGameColor();
        if (ourColor != null) {
            this.isPlayingWhite = "white".equals(ourColor);
            console.log("We are playing as: " + ourColor);
        }

        Platform.runLater(() -> {
            connectionButton.setText("Disconnect");
            radioButtonBox.setVisible(false);
            radioButtonBox.setManaged(false);
            displayGame.setVisible(true);
            displayGame.setManaged(true);
            console.log("Game started: " + gameId);
        });

        client.streamBotGame(gameId, this);
    }

    @Override
    public void onGameFinished(String gameId) {
        if (!gameId.equals(currentGameId)) {
            console.log("Ignoring finished game event for: " + gameId);
            return;
        }

        Platform.runLater(() -> {
            console.log("Game finished: " + gameId);

            inGame = false;
            currentGameId = null;

            connectionButton.setText("Queue");
            radioButtonBox.setVisible(true);
            radioButtonBox.setManaged(true);
            displayGame.setVisible(false);
            displayGame.setManaged(false);
        });
    }

    @Override
    public void onChallengeReceived(JsonObject challenge) {
        console.log("Challenge received: " + challenge);
    }

    @Override
    public void onChallengeCanceled(String challengeId) {
        console.log("Challenge canceled: " + challengeId);
    }

    @Override
    public void onChallengeDeclined(String challengeId) {
        console.log("Challenge declined: " + challengeId);
    }

    @Override
    public void onError(Throwable t) {
        Platform.runLater(() -> {
            console.log("LiChess error: " + t.getMessage());
        });
        t.printStackTrace();
    }

    // ========== GameStreamCallback Implementation ==========

    @Override
    public void onGameFull(String gameId, JsonObject gameFull) {
        console.log("Received game full data");

        // Extract our color from gameFull
        if (gameFull.has("white") && gameFull.has("black")) {
            JsonObject white = gameFull.getAsJsonObject("white");
            JsonObject black = gameFull.getAsJsonObject("black");

            String ourUsername = client.getOurUsername();

            if (white.has("id") && white.get("id").getAsString().equals(ourUsername.toLowerCase())) {
                isPlayingWhite = true;
                console.log("We are playing WHITE");
            } else if (black.has("id") && black.get("id").getAsString().equals(ourUsername.toLowerCase())) {
                isPlayingWhite = false;
                console.log("We are playing BLACK");
            }
        }

        // Process initial state
        if (gameFull.has("state")) {
            JsonObject state = gameFull.getAsJsonObject("state");
            processGameState(gameId, state);
        }
    }

    @Override
    public void onGameState(String gameId, JsonObject gameState) {
        console.log("Received game state update for: " + gameId);

        // gameState IS the state directly - no nested "state" field
        // Don't try to extract color info - that's only in gameFull
        processGameState(gameId, gameState);
    }

    private void processGameState(String gameId, JsonObject state) {
        if (!gameId.equals(currentGameId)) return;

        // Track the number of moves BEFORE updating
        int moveCountBefore = game.getMoveCount();

        // Extract moves and update local board
        if (state.has("moves")) {
            String movesStr = state.get("moves").getAsString();

            if (!movesStr.isEmpty()) {
                String[] moves = movesStr.split(" ");
                console.log("Updating board with " + moves.length + " moves");
                game.updateFromMoves(moves);
            } else {
                // No moves yet -> reset board
                game.reset();
            }
        }

        if (state.has("status")) {
            String status = state.get("status").getAsString();

            if (!"started".equals(status)) {
                console.log("Game status: " + status);

                // Update game status
                String winner = state.has("winner") ? state.get("winner").getAsString() : null;
                game.updateStatus(status, winner);

                waitingForMoveResponse = false;
                client.stopGameStream(gameId);

                return; // Exit early -> don't try to make moves
            }
        }

        // Track the number of moves AFTER updating
        int moveCountAfter = game.getMoveCount();

        // Determine whose turn it is
        boolean isWhiteTurn = game.isWhiteTurn();
        boolean isOurTurn = (isWhiteTurn == isPlayingWhite);

        console.log("Turn: " + (isWhiteTurn ? "White" : "Black") +
                " | Our turn: " + isOurTurn +
                " | Moves: " + moveCountAfter);

        // Reset waiting flag when opponent makes a move
        if (moveCountAfter > moveCountBefore && isOurTurn) {
            if (moveCountAfter > moveCountWhenStartedCalculating) {
                console.log("Opponent's move confirmed - ready to move");
                waitingForMoveResponse = false;
            } else {
                console.log("Still waiting for our move to be confirmed");
            }
        }

        if (game.isGameOver()) {
            console.log("Game is over locally: " + game.getStatus());
            waitingForMoveResponse = false;
            client.stopGameStream(gameId);
            return;
        }

        // Make our move if it's our turn and we're not already calculating
        if (isOurTurn && !game.isGameOver() && !waitingForMoveResponse) {
            makeAIMove();
        } else if (waitingForMoveResponse) {
            console.log("Skipping move - already calculating/waiting");
        } else if (game.isGameOver()) {
            console.log("Game is over - not making a move");
        }
    }

    @Override
    public void onChatLine(String gameId, JsonObject chatLine) {
        if (chatLine.has("username") && chatLine.has("text")) {
            String username = chatLine.get("username").getAsString();
            String text = chatLine.get("text").getAsString();
            console.log("[Chat] " + username + ": " + text);
        }
    }

    @Override
    public void onOpponentGone(String gameId, JsonObject opponentGone) {
        console.log("Opponent has left the game");
    }

    @Override
    public void onError(String gameId, Throwable error) {
        if (error.getMessage() != null && error.getMessage().contains("429")) {
            console.log("Rate limited on game " + gameId + " - too many requests");
            // Optionally retry with backoff
        } else {
            console.log("Game stream error: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // ========== AI Move Logic ==========

    private void makeAIMove() {
        // Prevent duplicate move calculations
        if (waitingForMoveResponse) {
            console.log("Already calculating/sending a move, skipping...");
            return;
        }

        if (game.isGameOver()) {
            console.log("Game is over, not calculating move");
            return;
        }

        waitingForMoveResponse = true;
        moveCountWhenStartedCalculating = game.getMoveCount();


        new Thread(() -> {
            try {
                console.log("=== Current Board State ===");
                console.log("Move count: " + game.getMoveCount());
                console.log("White to move: " + game.isWhiteTurn());
                console.log("Board FEN: " + BoardUtils.toFEN(game.getBoard(), game.isWhiteTurn()));

                console.log("Calculating best move...");

                int searchDepth = depthSpinner.getValue();
                console.log("Calculating best move (depth " + searchDepth + ")...");

                Move bestMove = game.getAIMove(searchDepth);

                if (game.isGameOver()) {
                    console.log("Game ended during calculation, not sending move");
                    waitingForMoveResponse = false;
                    return;
                }

                if (bestMove != null) {
                    String uci = game.moveToUCI(bestMove);

                    if (game.getMoveCount() != moveCountWhenStartedCalculating) {
                        console.log("Position changed during calculation (was " +
                                moveCountWhenStartedCalculating + ", now " +
                                game.getMoveCount() + ") - discarding move");
                        waitingForMoveResponse = false;
                        return;
                    }

                    console.log("Best move calculated: " + uci);
                    console.log("From: " + (char)('a' + bestMove.getFromCol()) + (8 - bestMove.getFromRow()));
                    console.log("To: " + (char)('a' + bestMove.getToCol()) + (8 - bestMove.getToRow()));

                    console.log("Playing move: " + uci);

                    if (game.isGameOver()) {
                        console.log("Game ended before sending move");
                        waitingForMoveResponse = false;
                        return;
                    }

                    boolean success = client.makeBotMove(currentGameId, uci);

                    if (success) {
                        console.log("Move sent successfully");
                    } else {
                        console.log("Failed to send move");
                        console.log("=== Legal moves available ===");
                        MoveGenerator tempMoveGen = new MoveGenerator(game.getBoard());
                        List<Move> legalMoves = tempMoveGen.generateAllMoves(
                                game.getBoard(), game.isWhiteTurn()
                        );
                        for (Move m : legalMoves) {
                            console.log("  " + game.moveToUCI(m));
                        }

                        waitingForMoveResponse = false;
                    }
                } else {
                    console.log("No legal moves available");

                    MoveGenerator moveGen = new MoveGenerator(game.getBoard());
                    boolean inCheck = moveGen.isKingInCheck(game.getBoard(), game.isWhiteTurn());

                    if (inCheck) {
                        console.log("We are in checkmate - resigning");
                    } else {
                        console.log("Stalemate position - resigning anyway");
                    }

                    if (currentGameId != null) {
                        console.log("Resigning game: " + currentGameId);
                        client.resignGame(currentGameId);
                    }
                    waitingForMoveResponse = false;
                }
            } catch (Exception e) {
                console.log("Error making move: " + e.getMessage());
                e.printStackTrace();
                waitingForMoveResponse = false;
            }
        }).start();
    }


    // ========== Game Update Listener ==========

    @Override
    public void onGameUpdated(Game game) {
        Platform.runLater(() -> {
            console.log("Local game updated - Moves: " + game.getMoveCount() + " | Status: " + game.getStatus());
        });
    }
}