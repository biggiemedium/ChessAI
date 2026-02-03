package dev.chess.cheat.UI.Viewer;

import dev.chess.cheat.Simulation.Board;
import dev.chess.cheat.Simulation.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class LiChessBoardViewer {
    private static final int SQUARE_SIZE = 80;
    private static final Color LIGHT_SQUARE = Color.rgb(240, 217, 181);
    private static final Color DARK_SQUARE = Color.rgb(181, 136, 99);

    private final Stage stage;
    private final GridPane boardGrid;
    private final Map<Character, Image> pieceImages;
    private Text statusText;

    // Info panel
    private VBox infoPanel;
    private VBox gameInfoSection;
    private VBox opponentInfoSection;

    public LiChessBoardViewer() {
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
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #2b2b2b;");

        // Left side - Board
        VBox boardSection = createBoardSection();

        // Right side - Info Panel
        infoPanel = createInfoPanel();

        mainLayout.getChildren().addAll(boardSection, infoPanel);

        Scene scene = new Scene(mainLayout, SQUARE_SIZE * 8 + 350, SQUARE_SIZE * 8 + 100);
        stage.setScene(scene);
        stage.setTitle("LiChess Live Game");
    }

    private VBox createBoardSection() {
        VBox boardSection = new VBox(10);

        Text title = new Text("LiChess Live Game");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);

        statusText = new Text("Waiting for game...");
        statusText.setFont(Font.font("Arial", 14));
        statusText.setFill(Color.LIGHTGREEN);

        boardGrid.setHgap(0);
        boardGrid.setVgap(0);

        createEmptyBoard();

        boardSection.getChildren().addAll(title, statusText, boardGrid);
        return boardSection;
    }

    private VBox createInfoPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 10;");
        panel.setPrefWidth(280);
        panel.setAlignment(Pos.TOP_LEFT);

        // Game info section
        gameInfoSection = new VBox(8);
        Text gameTitle = new Text("GAME INFO");
        gameTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gameTitle.setFill(Color.rgb(100, 200, 255));
        gameInfoSection.getChildren().add(gameTitle);

        // Separator
        Region separator = new Region();
        separator.setPrefHeight(15);
        separator.setStyle("-fx-border-color: #555555; -fx-border-width: 1 0 0 0;");

        // Opponent info section
        opponentInfoSection = new VBox(8);
        Text opponentTitle = new Text("OPPONENT");
        opponentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        opponentTitle.setFill(Color.rgb(255, 107, 107));
        opponentInfoSection.getChildren().add(opponentTitle);

        panel.getChildren().addAll(gameInfoSection, separator, opponentInfoSection);
        return panel;
    }

    private Text createInfoText(String text, int fontSize, boolean bold) {
        Text infoText = new Text(text);
        infoText.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));
        infoText.setFill(Color.WHITE);
        return infoText;
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

    /**
     * Update game information
     * @param gameId the game ID
     * @param ourColor our color ("white" or "black")
     * @param variant game variant
     * @param speed game speed (blitz, rapid, etc.)
     */
    public void updateGameInfo(String gameId, String ourColor, String variant, String speed) {
        javafx.application.Platform.runLater(() -> {
            // Clear previous game info (keep title)
            gameInfoSection.getChildren().removeIf(node -> node instanceof Text && !((Text) node).getText().equals("GAME INFO"));

            if (gameId != null) {
                gameInfoSection.getChildren().add(createInfoText("ID: " + gameId, 11, false));
            }
            if (ourColor != null) {
                Text colorText = createInfoText("Playing as: " + ourColor.toUpperCase(), 12, true);
                colorText.setFill(ourColor.equalsIgnoreCase("white") ? Color.LIGHTGRAY : Color.rgb(100, 100, 100));
                gameInfoSection.getChildren().add(colorText);
            }
            if (variant != null && !variant.equalsIgnoreCase("standard")) {
                gameInfoSection.getChildren().add(createInfoText("Variant: " + variant, 11, false));
            }
            if (speed != null) {
                gameInfoSection.getChildren().add(createInfoText("Speed: " + speed, 11, false));
            }
        });
    }

    /**
     * Update opponent stats display - only shows non-null information
     * @param stats Map containing opponent data
     */
    public void updateOpponentStats(Map<String, Object> stats) {
        javafx.application.Platform.runLater(() -> {
            if (stats == null) {
                clearOpponentStats();
                return;
            }

            // Clear previous opponent info (keep title)
            opponentInfoSection.getChildren().removeIf(node -> node instanceof Text && !((Text) node).getText().equals("OPPONENT"));

            String username = (String) stats.get("username");
            String title = (String) stats.get("title");
            boolean isBot = stats.containsKey("isBot") && (Boolean) stats.get("isBot");
            boolean isAI = stats.containsKey("isAI") && (Boolean) stats.get("isAI");

            // Username/Name
            if (username != null) {
                String displayName = username;
                if (title != null && !isAI) {
                    displayName += " [" + title + "]";
                }

                Text nameText = createInfoText(displayName, 14, true);
                if (isAI) {
                    nameText.setFill(Color.rgb(100, 200, 255)); // Light blue for AI
                } else if (isBot) {
                    nameText.setFill(Color.rgb(255, 193, 7)); // Yellow for bots
                } else {
                    nameText.setFill(Color.LIGHTGREEN);
                }
                opponentInfoSection.getChildren().add(nameText);
            }

            // Type indicator
            if (isAI) {
                Text typeText = createInfoText("Lichess AI", 11, false);
                typeText.setFill(Color.rgb(100, 200, 255));
                opponentInfoSection.getChildren().add(typeText);
            } else if (isBot) {
                Text typeText = createInfoText("Bot Account", 11, false);
                typeText.setFill(Color.rgb(255, 193, 7));
                opponentInfoSection.getChildren().add(typeText);
            }

            // Rating
            Integer rating = (Integer) stats.get("rating");
            if (rating != null && rating > 0) {
                Text ratingText = createInfoText("Rating: " + rating, 13, true);
                ratingText.setFill(Color.ORANGE);
                opponentInfoSection.getChildren().add(ratingText);
            }

            // Online status (only for real players)
            if (!isAI && stats.containsKey("online")) {
                boolean online = (Boolean) stats.get("online");
                Text statusText = createInfoText("Status: " + (online ? "Online" : "Offline"), 11, false);
                statusText.setFill(online ? Color.LIGHTGREEN : Color.LIGHTGRAY);
                opponentInfoSection.getChildren().add(statusText);
            }

            // Game statistics (only if games > 0)
            Integer games = (Integer) stats.get("games");
            if (games != null && games > 0) {
                opponentInfoSection.getChildren().add(createInfoText("Games: " + games, 11, false));

                Integer wins = (Integer) stats.get("wins");
                Integer losses = (Integer) stats.get("losses");
                Integer draws = (Integer) stats.get("draws");
                if (wins != null && losses != null && draws != null) {
                    opponentInfoSection.getChildren().add(
                            createInfoText(String.format("%dW - %dL - %dD", wins, losses, draws), 11, false)
                    );
                }

                Double winRate = (Double) stats.get("winRate");
                if (winRate != null && winRate > 0) {
                    opponentInfoSection.getChildren().add(
                            createInfoText(String.format("Win Rate: %.1f%%", winRate), 11, false)
                    );
                }
            }

            // Add separator if we have ratings to show
            boolean hasRatings = false;
            Integer blitz = (Integer) stats.get("blitzRating");
            Integer rapid = (Integer) stats.get("rapidRating");
            Integer bullet = (Integer) stats.get("bulletRating");
            Integer classical = (Integer) stats.get("classicalRating");

            if ((blitz != null && blitz > 0) || (rapid != null && rapid > 0) ||
                    (bullet != null && bullet > 0) || (classical != null && classical > 0)) {
                hasRatings = true;
            }

            if (hasRatings) {
                Region spacer = new Region();
                spacer.setPrefHeight(8);
                opponentInfoSection.getChildren().add(spacer);

                Text ratingsHeader = new Text("Ratings");
                ratingsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                ratingsHeader.setFill(Color.rgb(255, 107, 107));
                opponentInfoSection.getChildren().add(ratingsHeader);

                if (blitz != null && blitz > 0) {
                    opponentInfoSection.getChildren().add(createInfoText("Blitz: " + blitz, 11, false));
                }
                if (rapid != null && rapid > 0) {
                    opponentInfoSection.getChildren().add(createInfoText("Rapid: " + rapid, 11, false));
                }
                if (bullet != null && bullet > 0) {
                    opponentInfoSection.getChildren().add(createInfoText("Bullet: " + bullet, 11, false));
                }
                if (classical != null && classical > 0) {
                    opponentInfoSection.getChildren().add(createInfoText("Classical: " + classical, 11, false));
                }
            }
        });
    }

    /**
     * Clear opponent stats display
     */
    public void clearOpponentStats() {
        opponentInfoSection.getChildren().removeIf(node -> node instanceof Text && !((Text) node).getText().equals("OPPONENT"));
        opponentInfoSection.getChildren().add(createInfoText("Waiting...", 12, false));
    }

    /**
     * Clear game info display
     */
    public void clearGameInfo() {
        gameInfoSection.getChildren().removeIf(node -> node instanceof Text && !((Text) node).getText().equals("GAME INFO"));
        gameInfoSection.getChildren().add(createInfoText("Waiting for game...", 12, false));
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