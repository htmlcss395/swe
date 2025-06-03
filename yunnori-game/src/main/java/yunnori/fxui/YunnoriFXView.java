package yunnori.fxui;

import yunnori.core.*; // Imports all classes from yunnori.core

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class YunnoriFXView extends Application {

    private GameLogicController gameController;
    private BoardCanvas boardCanvas;
    private TextArea messageArea;
    private Button rollButton;
    private Button restartButton;
    private Button quitButton;
    private HBox controlPanel;

    // Static fields to receive parameters from Launcher
    private static int initialNumTeams;
    private static int initialNumPieces;
    private static boolean initialIsTestMode;
    private static BoardType initialBoardType;

    private Piece uiSelectedPieceForMoveFX = null; // For JavaFX UI selection state
    private List<Piece> uiPiecesAtClickedStackFX = new ArrayList<>();

    public static void setGameParameters(int numTeams, int numPieces, boolean isTestMode, BoardType boardType) {
        initialNumTeams = numTeams;
        initialNumPieces = numPieces;
        initialIsTestMode = isTestMode;
        initialBoardType = boardType;
    }

    @Override
    public void start(Stage primaryStage) {
        gameController = new GameLogicController();
        gameController.setupGame(initialNumTeams, initialNumPieces, initialBoardType, initialIsTestMode);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Control Panel (Top)
        controlPanel = new HBox(10);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(0, 0, 10, 0));

        rollButton = new Button("Roll");
        rollButton.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 18));
        rollButton.setOnAction(e -> handleRollButtonAction());

        restartButton = new Button("Restart");
        restartButton.setOnAction(e -> handleRestartButtonAction());

        quitButton = new Button("Quit");
        quitButton.setOnAction(e -> Platform.exit());

        controlPanel.getChildren().addAll(rollButton, restartButton, quitButton);
        root.setTop(controlPanel);

        // Message Area (Right)
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setPrefColumnCount(40); // Adjust width
        messageArea.setPrefRowCount(30); // Adjust height
        ScrollPane messageScrollPane = new ScrollPane(messageArea);
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setFitToHeight(true);
        root.setRight(messageScrollPane);

        // Board Canvas (Center)
        // Dimensions should match BoardPanel's preferred size for consistency
        boardCanvas = new BoardCanvas(gameController, 1150, 950); // Adjust as needed
        boardCanvas.setOnMouseClicked(this::handleBoardCanvasClick);
        root.setCenter(boardCanvas);

        Scene scene = new Scene(root, 1700, 1000); // Initial scene size
        primaryStage.setTitle("Yunnori Game - JavaFX UI");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> Platform.exit()); // Ensure Platform.exit on window close
        primaryStage.show();

        updateStatus(gameController.startGameAndGetMessages());
        updateUIBasedOnGameState();
    }

    private void updateStatus(String message) {
        if (message != null && !message.trim().isEmpty()) {
            Platform.runLater(() -> { // Ensure UI updates on JavaFX Application Thread
                messageArea.appendText(message.trim() + "\n");
            });
        }
    }

    private void updateStatus(List<String> messages) {
        if (messages != null) {
            Platform.runLater(() -> {
                for (String message : messages) {
                    if (message != null && !message.trim().isEmpty()) {
                        messageArea.appendText(message.trim() + "\n");
                    }
                }
            });
        }
    }

    private void updateUIBasedOnGameState() {
        Platform.runLater(() -> { // Ensure all UI updates are on the JavaFX Application Thread
            GameLogicController.GameState gameState = gameController.getCurrentGameState();

            rollButton.setDisable(!(gameState == GameLogicController.GameState.WAITING_FOR_ROLL ||
                    gameState == GameLogicController.GameState.WAITING_FOR_REORDER ||
                    gameState == GameLogicController.GameState.AWAITING_TEST_ROLL_INPUT));
            restartButton.setDisable(gameState != GameLogicController.GameState.GAME_OVER);

            if (gameState == GameLogicController.GameState.GAME_OVER) {
                updateStatus(gameController.getGameOverMessages());
            }

            List<Integer> highlightPositions = new ArrayList<>();
            if (gameState == GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION ||
                    gameState == GameLogicController.GameState.WAITING_FOR_STACK_SELECTION) {
                List<Piece> playablePieces = gameController.getPlayablePiecesForCurrentRoll();
                for (Piece p : playablePieces) {
                    highlightPositions.add(p.getCurrentPositionIndex());
                }
                boardCanvas.setHighlightData(this.uiSelectedPieceForMoveFX, highlightPositions);
            } else {
                boardCanvas.setHighlightData(null, null);
                this.uiSelectedPieceForMoveFX = null;
            }

            boardCanvas.redraw();

            // Trigger dialogs based on game state
            if (gameState == GameLogicController.GameState.AWAITING_TEST_ROLL_INPUT) {
                promptForTestRollAndSubmitFX();
            } else if (gameState == GameLogicController.GameState.WAITING_FOR_REORDER) {
                promptForReorderAndSubmitFX();
            } else if (gameState == GameLogicController.GameState.AWAITING_GROUPING_CHOICE) {
                promptForGroupingAndSubmitFX();
            }
        });
    }

    private void handleRollButtonAction() {
        String rollResultMessages = gameController.handleRollButtonPressed();
        updateStatus(rollResultMessages);
        updateUIBasedOnGameState(); // Controller state will change (e.g. to AWAITING_TEST_ROLL or WAITING_FOR_REORDER etc)
    }

    private void handleRestartButtonAction() {
        if (gameController.getCurrentGameState() == GameLogicController.GameState.GAME_OVER) {
            updateStatus(gameController.restartGame());
            // BoardCanvas uses gameController directly, so its board/teams refs are implicitly updated
            updateUIBasedOnGameState();
        }
    }

    private void handleBoardCanvasClick(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();
        Board board = gameController.getBoard();
        if (board == null)
            return;

        // Find which board point was clicked (logic similar to BoardPanel)
        int clickedPointIndex = -1;
        for (int i = 0; i < board.getPointCount(); i++) {
            Board.BoardPoint pointCoords = board.getBoardPoint(i);
            if (pointCoords != null) {
                // Approximate click area, similar to Swing version
                double pointDisplaySize = 15 * 3; // boardCanvas.pointSize * 3, make pointSize accessible or const
                if (Math.abs(clickX - pointCoords.x) < pointDisplaySize / 2 &&
                        Math.abs(clickY - pointCoords.y) < pointDisplaySize / 2) {
                    clickedPointIndex = i;
                    break;
                }
            }
        }

        if (clickedPointIndex != -1) {
            List<Piece> piecesAtThisPoint = new ArrayList<>();
            for (Team team : gameController.getTeams()) {
                piecesAtThisPoint.addAll(team.getInteractivePiecesAt(clickedPointIndex));
            }

            if (!piecesAtThisPoint.isEmpty()) {
                handlePieceStackClickedFX(piecesAtThisPoint); // Pass all interactive pieces at point
                return; // Click handled by piece/stack interaction
            }
        }

        // If no piece/stack was clicked, treat as general board click (for deselection)
        if (gameController.getCurrentGameState() == GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION
                && uiSelectedPieceForMoveFX != null) {
            updateStatus("Selection cancelled by clicking board.");
            uiSelectedPieceForMoveFX = null;
        } else if (gameController.getCurrentGameState() == GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION) {
            updateStatus("Click on one of your pieces to move it.");
        } else if (gameController.getCurrentGameState() == GameLogicController.GameState.WAITING_FOR_ROLL) {
            updateStatus("Click the 'Roll' button.");
        }
        updateUIBasedOnGameState();
    }

    private void handlePieceClickedFX(Piece piece) { // Analogous to YunnoriGUI.pieceClicked
        this.uiSelectedPieceForMoveFX = piece; // For potential highlight if grouping needed
        updateStatus(gameController.handlePieceClicked(piece));

        GameLogicController.GameState currentStateAfterClick = gameController.getCurrentGameState();
        if (currentStateAfterClick != GameLogicController.GameState.AWAITING_GROUPING_CHOICE) {
            this.uiSelectedPieceForMoveFX = null;
        }
        updateUIBasedOnGameState();
    }

    private void handlePieceStackClickedFX(List<Piece> piecesAtStack) { // Analogous to YunnoriGUI.pieceStackClicked
        GameLogicController.GameState gameState = gameController.getCurrentGameState();
        if (gameState != GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION &&
                gameState != GameLogicController.GameState.WAITING_FOR_STACK_SELECTION) {
            updateStatus("Not time to select from stack.");
            updateUIBasedOnGameState();
            return;
        }

        List<Piece> playablePiecesOrGroupsAtStack = new ArrayList<>();
        List<YunnoriRoll> currentRolls = gameController.getRollsToProcess();
        if (!currentRolls.isEmpty()) {
            YunnoriRoll currentRoll = currentRolls.get(0);
            for (Piece p : piecesAtStack) {
                if (p.getTeamId() == gameController.getCurrentPlayerIndex() &&
                        gameController.getBoard().isValidMoveStart(p, currentRoll.getSteps())) {
                    playablePiecesOrGroupsAtStack.add(p);
                }
            }
        }

        if (playablePiecesOrGroupsAtStack.isEmpty()) {
            updateStatus("No playable piece/group in stack for " + gameController.getCurrentPlayer() +
                    (currentRolls.isEmpty() ? "." : " with " + currentRolls.get(0).name() + "."));
        } else if (playablePiecesOrGroupsAtStack.size() == 1) {
            Piece autoSelected = playablePiecesOrGroupsAtStack.get(0);
            updateStatus("Auto-selecting: " + autoSelected.toString());
            handlePieceClickedFX(autoSelected); // Call the FX piece click handler
        } else {
            this.uiPiecesAtClickedStackFX = playablePiecesOrGroupsAtStack;
            promptForStackSelectionAndSubmitFX();
        }
        // updateUIBasedOnGameState(); // Called by handlePieceClickedFX or prompt method
    }

    // --- JavaFX Dialog Prompts ---
    private void promptForTestRollAndSubmitFX() {
        List<String> choices = Arrays.asList("Do", "Gae", "Geol", "Yut", "Mo", "Back_Do");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Test Roll Input");
        dialog.setHeaderText("Enter test roll for " + gameController.getCurrentPlayer().toString() + ":");
        dialog.setContentText("Choose roll:");

        Optional<String> result = dialog.showAndWait();
        String message;
        if (result.isPresent()) {
            message = gameController.submitTestRoll(YunnoriRoll.fromName(result.get()));
        } else {
            message = gameController.cancelTestRolling();
        }
        updateStatus(message);
        updateUIBasedOnGameState();
    }

    private void promptForReorderAndSubmitFX() {
        List<YunnoriRoll> currentRollOrderList = gameController.getRollsToProcess(); // <<< DECLARED AND INITIALIZED HERE
        List<YunnoriRoll> currentOrderList = gameController.getRollsToProcess();
        String currentOrderString = currentOrderList.stream().map(Enum::name).collect(Collectors.joining(" "));

        TextInputDialog dialog = new TextInputDialog(currentOrderString);
        dialog.setTitle("Reorder Rolls");
        dialog.setHeaderText("Current order: " + currentOrderString);
        dialog.setContentText("Enter desired order (space separated):");

        Optional<String> result = dialog.showAndWait();
        String statusMessage = "";

        if (result.isPresent() && result.get() != null && !result.get().trim().isEmpty()) {
            String input = result.get().trim();
            List<YunnoriRoll> reordered = new ArrayList<>();
            List<YunnoriRoll> tempOriginal = new ArrayList<>(currentRollOrderList);
            boolean valid = true;
            String[] rollNames = input.split("\\s+");

            if (rollNames.length != currentRollOrderList.size()) {
                statusMessage = "Invalid number of rolls. Using original order.";
                valid = false;
            } else {
                for (String name : rollNames) {
                    YunnoriRoll roll = YunnoriRoll.fromName(name);
                    if (roll == null || !tempOriginal.remove(roll)) {
                        statusMessage = "Invalid roll: " + name + ". Using original order.";
                        valid = false;
                        break;
                    }
                    reordered.add(roll);
                }
                if (valid && !tempOriginal.isEmpty()) {
                    statusMessage = "Error processing reorder. Using original order.";
                    valid = false;
                }
            }
            if (valid)
                statusMessage = gameController.submitReorderedRolls(reordered);
            else {
                updateStatus(statusMessage);
                statusMessage = gameController.cancelReorder();
            }
        } else {
            statusMessage = gameController.cancelReorder();
        }
        updateStatus(statusMessage);
        updateUIBasedOnGameState();
    }

    private void promptForStackSelectionAndSubmitFX() {
        List<String> choices = new ArrayList<>();
        for (Piece p : uiPiecesAtClickedStackFX) {
            choices.add(p.toString());
        }

        if (choices.isEmpty()) { // Should not happen if this method is called
            updateUIBasedOnGameState();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Piece Selection");
        dialog.setHeaderText("Multiple pieces at this point.");
        dialog.setContentText("Select which piece/group to move:");

        Optional<String> result = dialog.showAndWait();
        Piece selectedFromDialog = null;
        if (result.isPresent()) {
            for (Piece p : uiPiecesAtClickedStackFX) {
                if (p.toString().equals(result.get())) {
                    selectedFromDialog = p;
                    break;
                }
            }
        }
        uiPiecesAtClickedStackFX.clear();

        if (selectedFromDialog != null) {
            handlePieceClickedFX(selectedFromDialog);
        } else {
            updateStatus(result.isPresent() ? "Error: Could not find selected piece."
                    : "Piece selection from stack cancelled.");
            updateUIBasedOnGameState();
        }
    }

    private void promptForGroupingAndSubmitFX() {
        Piece pieceThatMoved = gameController.getPieceToPotentiallyGroupWith();
        List<Piece> friendlies = gameController.getFriendlyPiecesAtTargetForGrouping();

        if (pieceThatMoved == null || friendlies.isEmpty()) {
            updateStatus("Grouping data missing, defaulting to no group.");
            updateStatus(gameController.submitGroupingChoice(false));
            updateUIBasedOnGameState();
            return;
        }

        String existingPieceNames = friendlies.stream().map(Piece::toString).collect(Collectors.joining(", "));
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Group Pieces?");
        alert.setHeaderText("Landed on own piece(s): " + existingPieceNames);
        alert.setContentText("Do you want " + pieceThatMoved.toString() + " to lead/join this stack?");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        boolean choseToGroup = result.isPresent() && result.get() == buttonTypeYes;

        updateStatus(gameController.submitGroupingChoice(choseToGroup));
        updateUIBasedOnGameState();
    }

    // Not needed if Launcher.main calls Application.launch(YunnoriJavaFXView.class, args);
    // public static void main(String[] args) {
    //     launch(args);
    // }
}