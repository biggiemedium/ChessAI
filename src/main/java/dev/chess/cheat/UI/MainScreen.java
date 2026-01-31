package dev.chess.cheat.UI;

import dev.chess.cheat.Network.Impl.ChessComClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main login screen with platform selection
 */
public class MainScreen {

    private final Stage stage;
    private final Scene scene;
    private final VBox root;

    private double xOffset = 0;
    private double yOffset = 0;

    private TextField usernameField;
    private PasswordField apiKeyField;
    private ToggleGroup platformGroup;
    private Label statusLabel;

    public MainScreen(Stage stage) {
        this.stage = stage;
        this.root = new VBox();
        this.scene = new Scene(root, 450, 550);

        setupUI();
        makeDraggable();
    }

    private void setupUI() {
        root.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setSpacing(0);

        // Custom title bar
        root.getChildren().add(createTitleBar());

        // Main content
        VBox content = new VBox(25);
        content.setPadding(new Insets(30, 40, 40, 40));
        content.setAlignment(Pos.TOP_CENTER);

        content.getChildren().addAll(
                createHeader(),
                createPlatformSelector(),
                createCredentialsSection(),
                createConnectButton(),
                createStatusLabel()
        );

        root.getChildren().add(content);
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setPadding(new Insets(10, 15, 10, 20));
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setStyle("-fx-background-color: #252525; -fx-background-radius: 15 15 0 0;");
        titleBar.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeBtn = createTitleBarButton("−");
        minimizeBtn.setOnAction(e -> stage.setIconified(true));

        Button closeBtn = createTitleBarButton("✕");
        closeBtn.setOnAction(e -> System.exit(0));
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");

        titleBar.getChildren().addAll(spacer, minimizeBtn, closeBtn);

        return titleBar;
    }

    private Button createTitleBarButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9e9e9e; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setPrefSize(30, 30);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9e9e9e; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;"));
        return btn;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("♚ Chess Bot");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Intelligent Chess Assistant");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#888888"));

        header.getChildren().addAll(titleLabel, subtitleLabel);

        return header;
    }

    private VBox createPlatformSelector() {
        VBox platformBox = new VBox(15);
        platformBox.setAlignment(Pos.CENTER);

        Label platformLabel = new Label("Select Platform");
        platformLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        platformLabel.setTextFill(Color.web("#cccccc"));

        platformGroup = new ToggleGroup();

        HBox platformButtons = new HBox(15);
        platformButtons.setAlignment(Pos.CENTER);

        ToggleButton chessComBtn = createPlatformButton("Chess.com", "🌐");
        ToggleButton lichessBtn = createPlatformButton("Lichess", "♞");

        chessComBtn.setToggleGroup(platformGroup);
        lichessBtn.setToggleGroup(platformGroup);

        chessComBtn.setSelected(true); // Default selection

        platformButtons.getChildren().addAll(chessComBtn, lichessBtn);
        platformBox.getChildren().addAll(platformLabel, platformButtons);

        return platformBox;
    }

    private ToggleButton createPlatformButton(String text, String icon) {
        ToggleButton btn = new ToggleButton();

        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));
        iconLabel.setTextFill(Color.web("#888888"));

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        textLabel.setTextFill(Color.web("#888888"));

        content.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(content);

        btn.setPrefSize(120, 80);
        btn.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10; -fx-border-color: #3a3a3a; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;");

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10; -fx-border-color: #4a9eff; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;");
                iconLabel.setTextFill(Color.web("#4a9eff"));
                textLabel.setTextFill(Color.web("#4a9eff"));
            } else {
                btn.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10; -fx-border-color: #3a3a3a; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;");
                iconLabel.setTextFill(Color.web("#888888"));
                textLabel.setTextFill(Color.web("#888888"));
            }
        });

        return btn;
    }

    private VBox createCredentialsSection() {
        VBox credBox = new VBox(12);
        credBox.setAlignment(Pos.CENTER);

        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Segoe UI", 12));
        usernameLabel.setTextFill(Color.web("#cccccc"));

        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #666666; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #3a3a3a; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 0 15 0 15;"
        );

        usernameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                usernameField.setStyle(
                        "-fx-background-color: #2a2a2a; " +
                                "-fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #666666; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #4a9eff; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-width: 2; " +
                                "-fx-padding: 0 15 0 15;"
                );
            } else {
                usernameField.setStyle(
                        "-fx-background-color: #2a2a2a; " +
                                "-fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #666666; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #3a3a3a; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-width: 1; " +
                                "-fx-padding: 0 15 0 15;"
                );
            }
        });

        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // API Key field
        VBox apiBox = new VBox(5);
        Label apiLabel = new Label("API Key (Optional for Chess.com)");
        apiLabel.setFont(Font.font("Segoe UI", 12));
        apiLabel.setTextFill(Color.web("#cccccc"));

        apiKeyField = new PasswordField();
        apiKeyField.setPromptText("Enter API key");
        apiKeyField.setPrefHeight(40);
        apiKeyField.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #666666; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #3a3a3a; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 0 15 0 15;"
        );

        apiKeyField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                apiKeyField.setStyle(
                        "-fx-background-color: #2a2a2a; " +
                                "-fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #666666; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #4a9eff; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-width: 2; " +
                                "-fx-padding: 0 15 0 15;"
                );
            } else {
                apiKeyField.setStyle(
                        "-fx-background-color: #2a2a2a; " +
                                "-fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #666666; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #3a3a3a; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-width: 1; " +
                                "-fx-padding: 0 15 0 15;"
                );
            }
        });

        apiBox.getChildren().addAll(apiLabel, apiKeyField);

        credBox.getChildren().addAll(usernameBox, apiBox);
        credBox.setPrefWidth(370);

        return credBox;
    }

    private Button createConnectButton() {
        Button connectBtn = new Button("Connect");
        connectBtn.setPrefSize(370, 45);
        connectBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        connectBtn.setStyle(
                "-fx-background-color: #4a9eff; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        connectBtn.setOnMouseEntered(e -> connectBtn.setStyle(
                "-fx-background-color: #3a8eef; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        ));

        connectBtn.setOnMouseExited(e -> connectBtn.setStyle(
                "-fx-background-color: #4a9eff; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        ));

        connectBtn.setOnAction(e -> handleConnect());

        return connectBtn;
    }

    private Label createStatusLabel() {
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#888888"));
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(370);
        statusLabel.setWrapText(true);

        return statusLabel;
    }

    private void makeDraggable() {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private void handleConnect() {
        String username = usernameField.getText().trim();
        String apiKey = apiKeyField.getText().trim();

        ToggleButton selected = (ToggleButton) platformGroup.getSelectedToggle();
        String platform = ((Label)((VBox)selected.getGraphic()).getChildren().get(1)).getText();

        if (username.isEmpty()) {
            updateStatus("Please enter a username", "#e74c3c");
            return;
        }

        updateStatus("Connecting to " + platform + "...", "#f39c12");

        Thread connectionThread = new Thread(() -> {
            try {
                if (platform.equals("Chess.com")) {
                    ChessComClient client = new ChessComClient();
                    boolean success = client.authenticate(username);

                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            updateStatus("Connected successfully! ✓", "#27ae60");
                            // TODO: Open game screen
                        } else {
                            updateStatus("Connection failed. Check username.", "#e74c3c");
                        }
                    });
                } else {
                    // TODO: Implement Lichess connection
                    javafx.application.Platform.runLater(() -> {
                        updateStatus("Lichess support coming soon!", "#f39c12");
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    updateStatus("Error: " + e.getMessage(), "#e74c3c");
                });
            }
        });

        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void updateStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web(color));
    }

    public Scene getScene() {
        return scene;
    }
}