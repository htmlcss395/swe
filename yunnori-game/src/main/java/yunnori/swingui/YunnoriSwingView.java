package yunnori.swingui;

import yunnori.core.Board;
import yunnori.core.BoardType;
import yunnori.core.GameLogicController;
import yunnori.core.Piece;
import yunnori.core.Team;
import yunnori.core.YunnoriRoll; // If used directly

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YunnoriSwingView extends JFrame implements ActionListener {

    private BoardPanel boardPanel;
    private JTextArea messageArea;
    private JButton rollButton;
    private JPanel controlPanel;
    private JButton quitButton;
    private JButton restartButton;

    private GameLogicController gameController;

    private Piece uiSelectedPieceForMove;
    private List<Piece> uiPiecesAtClickedStack = new ArrayList<>();

    public YunnoriSwingView(int numTeams, int numPieces, boolean isTestMode, BoardType boardType) {
        super("Yunnori Game - Swing UI");

        this.gameController = new GameLogicController();
        this.gameController.setupGame(numTeams, numPieces, boardType, isTestMode);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1780, 1050);
        setLayout(new BorderLayout());

        messageArea = new JTextArea(70, 50);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.EAST);

        controlPanel = new JPanel();
        rollButton = new JButton("Roll");
        rollButton.setFont(new Font("Gulim", Font.BOLD, 20));
        rollButton.setPreferredSize(new Dimension(150, 50));
        rollButton.addActionListener(this);
        controlPanel.add(rollButton);

        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Gulim", Font.BOLD, 16));
        restartButton.setPreferredSize(new Dimension(120, 40));
        restartButton.addActionListener(this);
        controlPanel.add(restartButton);

        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Gulim", Font.BOLD, 16));
        quitButton.setPreferredSize(new Dimension(120, 40));
        quitButton.addActionListener(this);
        controlPanel.add(quitButton);

        add(controlPanel, BorderLayout.NORTH);

        boardPanel = new BoardPanel(gameController.getBoard(), gameController.getTeams(), this);
        boardPanel.setPreferredSize(new Dimension(1150, 1100));
        add(boardPanel, BorderLayout.CENTER);

        setVisible(true);

        List<String> initialMessages = new ArrayList<>();
        initialMessages.add("Game setup complete.");
        initialMessages.add(gameController.startGameAndGetMessages());
        updateStatus(initialMessages);

        updateUIBasedOnGameState();
    }

    private void updateStatus(String message) {
        if (message != null && !message.trim().isEmpty()) {
            messageArea.append(message.trim() + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        }
    }

    private void updateStatus(List<String> messages) {
        if (messages != null) {
            for (String message : messages) {
                updateStatus(message);
            }
        }
    }

    private void updateUIBasedOnGameState() {
        GameLogicController.GameState gameState = gameController.getCurrentGameState();

        rollButton.setEnabled(gameState == GameLogicController.GameState.WAITING_FOR_ROLL ||
                gameState == GameLogicController.GameState.WAITING_FOR_REORDER ||
                gameState == GameLogicController.GameState.AWAITING_TEST_ROLL_INPUT);
        restartButton.setEnabled(gameState == GameLogicController.GameState.GAME_OVER);

        if (gameState == GameLogicController.GameState.GAME_OVER) {
            updateStatus(gameController.getGameOverMessages());
        }

        if (gameState == GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION ||
                gameState == GameLogicController.GameState.WAITING_FOR_STACK_SELECTION) {
            List<Piece> playablePieces = gameController.getPlayablePiecesForCurrentRoll();
            List<Integer> highlightPositions = new ArrayList<>();
            for (Piece p : playablePieces) {
                highlightPositions.add(p.getCurrentPositionIndex());
            }
            boardPanel.setHighlight(this.uiSelectedPieceForMove, highlightPositions);
        } else {
            boardPanel.setHighlight(null, null);
            this.uiSelectedPieceForMove = null;
        }

        boardPanel.repaint();

        // Trigger dialogs if controller is in a state awaiting specific UI input
        // Placed after main UI updates to ensure UI is responsive before modal dialog
        if (gameState == GameLogicController.GameState.AWAITING_TEST_ROLL_INPUT) {
            promptForTestRollAndSubmit();
        } else if (gameState == GameLogicController.GameState.WAITING_FOR_REORDER) {
            promptForReorderAndSubmit();
        } else if (gameState == GameLogicController.GameState.AWAITING_GROUPING_CHOICE) {
            promptForGroupingAndSubmit();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == quitButton) {
            System.exit(0);
        } else if (e.getSource() == restartButton) {
            if (gameController.getCurrentGameState() == GameLogicController.GameState.GAME_OVER) {
                updateStatus(gameController.restartGame());
                this.boardPanel.board = gameController.getBoard(); // Update BoardPanel's board reference
                this.boardPanel.teams = gameController.getTeams(); // Update teams reference
                this.uiSelectedPieceForMove = null;
                this.uiPiecesAtClickedStack.clear();
            }
        } else if (gameController.getCurrentGameState() == GameLogicController.GameState.GAME_OVER) {
            updateStatus("Game is over! Click Restart or Quit.");
            // updateUIBasedOnGameState() will handle button states
        } else if (e.getSource() == rollButton) {
            String rollMessages = gameController.handleRollButtonPressed();
            updateStatus(rollMessages);
        }
        updateUIBasedOnGameState();
    }

    public void pieceClicked(Piece piece) {
        this.uiSelectedPieceForMove = piece;
        updateStatus(gameController.handlePieceClicked(piece));

        // Clear uiSelectedPieceForMove only if the piece was successfully processed
        // and game is not waiting for another decision for *this* piece (like grouping)
        GameLogicController.GameState currentStateAfterClick = gameController.getCurrentGameState();
        if (currentStateAfterClick != GameLogicController.GameState.AWAITING_GROUPING_CHOICE &&
                currentStateAfterClick != GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION && // if move failed, might stay here
                currentStateAfterClick != GameLogicController.GameState.WAITING_FOR_STACK_SELECTION) { // if stack choice failed
            this.uiSelectedPieceForMove = null;
        }
        updateUIBasedOnGameState();
    }

    public void pieceStackClicked(List<Piece> piecesAtStack, int clickX, int clickY) {
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
            updateStatus("No playable piece/group in that stack for " + gameController.getCurrentPlayer() + " with "
                    + (currentRolls.isEmpty() ? "no roll" : currentRolls.get(0).name()) + ".");
        } else if (playablePiecesOrGroupsAtStack.size() == 1) {
            Piece autoSelectedPiece = playablePiecesOrGroupsAtStack.get(0);
            // Useless message
            // updateStatus("Auto-selecting the only playable piece/group: " + autoSelectedPiece);
            List<String> messages = gameController.handlePieceClicked(autoSelectedPiece);
            updateStatus(messages);
            updateUIBasedOnGameState(); // Crucial to refresh after controller action
            return; // Explicitly return as action is handled
        } else {
            this.uiPiecesAtClickedStack = playablePiecesOrGroupsAtStack;
            promptForStackSelectionAndSubmit(); // This will call pieceClicked internally
        }
        // updateUIBasedOnGameState(); // pieceClicked or promptForStackSelectionAndSubmit will call it
    }

    public void boardClicked(int clickX, int clickY) {
        if (gameController.getCurrentGameState() == GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION
                && uiSelectedPieceForMove != null) {
            updateStatus("Selection cancelled by clicking board.");
            uiSelectedPieceForMove = null; // Clear UI selection
        } else if (gameController.getCurrentGameState() == GameLogicController.GameState.WAITING_FOR_PIECE_SELECTION) {
            updateStatus("Click on one of your pieces to move it.");
        } else if (gameController.getCurrentGameState() == GameLogicController.GameState.WAITING_FOR_ROLL) {
            updateStatus("Click the 'Roll' button.");
        }
        updateUIBasedOnGameState();
    }

    private void promptForTestRollAndSubmit() {
        String[] options = { "Do", "Gae", "Geol", "Yut", "Mo", "Back_Do" };
        String input = (String) JOptionPane.showInputDialog(
                this, "Enter test roll:", "Test Roll Input",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        String message;
        if (input != null) {
            YunnoriRoll testRoll = YunnoriRoll.fromName(input);
            message = gameController.submitTestRoll(testRoll);
        } else {
            message = gameController.cancelTestRolling();
        }
        updateStatus(message);
        updateUIBasedOnGameState(); // Controller state has changed
    }

    private void promptForReorderAndSubmit() {
        List<YunnoriRoll> currentRollOrderList = gameController.getRollsToProcess();
        String currentOrderString = currentRollOrderList.stream().map(Enum::name).collect(Collectors.joining(" "));
        String message = "Enter desired order for rolls (" + currentOrderString + "):";
        String input = JOptionPane.showInputDialog(this, message, "Reorder Rolls", JOptionPane.QUESTION_MESSAGE);

        String statusMessage = "";
        if (input != null) {
            List<YunnoriRoll> reordered = new ArrayList<>();
            List<YunnoriRoll> tempOriginal = new ArrayList<>(currentRollOrderList);
            boolean valid = true;
            String[] rollNames = input.trim().split("\\s+");
            if (rollNames.length != currentRollOrderList.size()) {
                statusMessage = "Invalid number of rolls for reordering. Using original order.";
                valid = false;
            } else {
                for (String name : rollNames) {
                    YunnoriRoll roll = YunnoriRoll.fromName(name);
                    if (roll == null || !tempOriginal.remove(roll)) {
                        statusMessage = "Invalid roll name or roll not available: " + name + ". Using original order.";
                        valid = false;
                        break;
                    }
                    reordered.add(roll);
                }
                if (valid && !tempOriginal.isEmpty()) {
                    statusMessage = "Error processing reorder. Some rolls unmatched. Using original order.";
                    valid = false;
                }
            }
            if (valid) {
                statusMessage = gameController.submitReorderedRolls(reordered);
            } else {
                updateStatus(statusMessage); // Show specific error
                statusMessage = gameController.cancelReorder(); // Proceed with original order
            }
        } else {
            statusMessage = gameController.cancelReorder();
        }
        updateStatus(statusMessage);
        updateUIBasedOnGameState();
    }

    private void promptForStackSelectionAndSubmit() {
        List<String> options = new ArrayList<>();
        for (Piece p : uiPiecesAtClickedStack) {
            options.add(p.toString());
        }
        String[] optionsArray = options.toArray(new String[0]);
        String selectionString = (String) JOptionPane.showInputDialog(
                this, "Select which piece/group to move:", "Piece Selection",
                JOptionPane.QUESTION_MESSAGE, null, optionsArray, optionsArray[0]);
        Piece selectedFromDialog = null;
        if (selectionString != null) {
            for (Piece p : uiPiecesAtClickedStack) {
                if (p.toString().equals(selectionString)) {
                    selectedFromDialog = p;
                    break;
                }
            }
        }
        uiPiecesAtClickedStack.clear(); // Clear after use

        if (selectedFromDialog != null) {
            pieceClicked(selectedFromDialog); // This will call controller and then updateUI
        } else {
            updateStatus(selectionString == null ? "Piece selection from stack cancelled."
                    : "Error: Could not find selected piece from stack dialog.");
            // If cancelled, the game state in controller might still be WAITING_FOR_PIECE_SELECTION
            // or WAITING_FOR_STACK_SELECTION. updateUI will refresh based on that.
            updateUIBasedOnGameState();
        }
    }

    private void promptForGroupingAndSubmit() {
        Piece pieceThatMoved = gameController.getPieceToPotentiallyGroupWith();
        List<Piece> friendlies = gameController.getFriendlyPiecesAtTargetForGrouping();

        if (pieceThatMoved == null || friendlies.isEmpty()) {
            // This state should ideally not be reached if controller guards it, but handle defensively.
            updateStatus("Grouping data missing, defaulting to no group.");
            updateStatus(gameController.submitGroupingChoice(false)); // Tell controller to proceed without grouping
            updateUIBasedOnGameState();
            return;
        }

        String existingPieceNames = friendlies.stream().map(Piece::toString).collect(Collectors.joining(", "));
        int choice = JOptionPane.showConfirmDialog(this,
                "You landed on your own piece(s): " + existingPieceNames +
                        ".\nDo you want " + pieceThatMoved.toString() + " to lead/join this stack?",
                "Group Pieces?", JOptionPane.YES_NO_OPTION);

        updateStatus(gameController.submitGroupingChoice(choice == JOptionPane.YES_OPTION));
        updateUIBasedOnGameState();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                int dialogFontSize = 24;
                Font dialogFont = new Font("SansSerif", Font.PLAIN, dialogFontSize);
                Font dialogButtonFont = new Font("SansSerif", Font.BOLD, dialogFontSize - 2);
                UIManager.put("OptionPane.messageFont", dialogFont);
                UIManager.put("OptionPane.buttonFont", dialogButtonFont);
                UIManager.put("TextField.font", dialogFont);
                UIManager.put("Label.font", dialogFont);
                UIManager.put("ComboBox.font", dialogFont);

                String[] boardTypes = { "Rectangle", "Pentagon", "Hexagon" };
                int boardTypeIndex = JOptionPane.showOptionDialog(null, "Select the type of Yutnori board:",
                        "Board Type",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, boardTypes, boardTypes[0]);
                if (boardTypeIndex == JOptionPane.CLOSED_OPTION)
                    System.exit(0);
                BoardType boardType = switch (boardTypeIndex) {
                    case 1 -> BoardType.PENTAGON;
                    case 2 -> BoardType.HEXAGON;
                    default -> BoardType.RECTANGLE;
                };

                String teamsInput = JOptionPane.showInputDialog(null, "Enter number of teams (2-4):", "Game Setup",
                        JOptionPane.QUESTION_MESSAGE);
                if (teamsInput == null)
                    System.exit(0);
                int numTeams = Integer.parseInt(teamsInput);
                if (numTeams < 2 || numTeams > 4)
                    throw new IllegalArgumentException("Invalid number of teams (2-4)");

                String piecesInput = JOptionPane.showInputDialog(null, "Enter number of pieces per team (2-5):",
                        "Game Setup", JOptionPane.QUESTION_MESSAGE);
                if (piecesInput == null)
                    System.exit(0);
                int numPieces = Integer.parseInt(piecesInput);
                if (numPieces < 2 || numPieces > 5)
                    throw new IllegalArgumentException("Invalid number of pieces per team (2-5)");

                int testModeOption = JOptionPane.showConfirmDialog(null, "Enable test mode (manual roll)?",
                        "Game Setup", JOptionPane.YES_NO_OPTION);
                if (testModeOption == JOptionPane.CLOSED_OPTION)
                    System.exit(0);
                boolean isTestMode = (testModeOption == JOptionPane.YES_OPTION);

                new YunnoriSwingView(numTeams, numPieces, isTestMode, boardType);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Invalid input:\n" + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "An unexpected error occurred:\n" + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}