package src.yunnori;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
// ... other imports ...
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class YunnoriGUI extends JFrame implements ActionListener {

    // GUI Components
    private BoardPanel boardPanel;
    private JTextArea messageArea;
    private JButton rollButton;
    private JPanel controlPanel;
    private JButton quitButton;
    private JButton restartButton;

    // Game Logic and State (fields remain the same)
    private Board board;
    private List<Team> teams;
    private YunnoriRoller roller;
    private int numTeams;
    private int numPieces;
    private boolean isTestMode = false;
    private int currentPlayerIndex = 0;
    private List<YunnoriRoll> earnedRollsThisPhase = new ArrayList<>();
    private List<YunnoriRoll> rollsToProcess = new ArrayList<>();
    private Piece selectedPiece = null;

    // private List<Integer> possibleMoveTargets = null;
    private enum GameState {
        WAITING_FOR_ROLL, WAITING_FOR_PIECE_SELECTION, WAITING_FOR_REORDER,
        WAITING_FOR_STACK_SELECTION, GAME_OVER
    }

    private GameState currentState;
    private boolean extraTurnEarnedInThisPhase = false;
    private boolean catchOccurredInThisPhase = false;
    private List<Piece> piecesAtClickedStack = new ArrayList<>();

    // Constructor
    public YunnoriGUI(int numTeams, int numPieces, boolean isTestMode) {
        super("Yunnori Game");
        this.numTeams = numTeams; // Store for restart
        this.numPieces = numPieces; // Store for restart
        this.isTestMode = isTestMode; // Store for restart

        // Initialize game components (Board, Teams, Roller) - will be done in
        // initializeGameLogic()
        // board = new Board(); // Moved to initializeGameLogic
        // teams = new ArrayList<>(); // Moved
        // roller = new YunnoriRoller(isTestMode); // Moved

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1780, 1200);
        setLayout(new BorderLayout());

        // Board Panel will be created after game logic is initialized
        // boardPanel = new BoardPanel(board, teams, this); // Moved
        // add(boardPanel, BorderLayout.CENTER); // Moved

        messageArea = new JTextArea(70, 50);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.EAST);

        controlPanel = new JPanel(); // Use FlowLayout by default
        rollButton = new JButton("Roll");
        rollButton.setFont(new Font("Arial", Font.BOLD, 20));
        rollButton.setPreferredSize(new Dimension(150, 50));
        rollButton.addActionListener(this);
        controlPanel.add(rollButton);

        // Initialize and add Quit/Restart buttons
        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setPreferredSize(new Dimension(120, 40));
        restartButton.addActionListener(this);
        restartButton.setEnabled(false); // Initially disabled
        controlPanel.add(restartButton);

        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.BOLD, 16));
        quitButton.setPreferredSize(new Dimension(120, 40));
        quitButton.addActionListener(this);
        quitButton.setEnabled(true); // Always enabled
        controlPanel.add(quitButton);

        add(controlPanel, BorderLayout.NORTH);

        // Initialize and start the game
        initializeGameLogicAndBoardPanel(); // Creates board, teams, roller, and boardPanel
        setVisible(true); // Make window visible AFTER all components are added
        startGame(); // Start the first turn logic
    }

    // New method to initialize/re-initialize game logic components and BoardPanel
    private void initializeGameLogicAndBoardPanel() {
        board = new Board();
        teams = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team(i, numPieces));
        }
        roller = new YunnoriRoller(isTestMode);

        // If boardPanel already exists (for restart), remove it
        if (boardPanel != null) {
            remove(boardPanel);
        }
        boardPanel = new BoardPanel(board, teams, this);
        boardPanel.setPreferredSize(new Dimension(1000, 1100));
        add(boardPanel, BorderLayout.CENTER); // Add (or re-add) to the frame
        revalidate(); // Important after adding/removing components
        repaint(); // Ensure the new board panel is drawn
    }

    public void startGame() {
        startNewTurnPhase();
    }

    private void startNewTurnPhase() {
        updateStatus("--- Starting turn for " + teams.get(currentPlayerIndex) + " ---");
        currentState = GameState.WAITING_FOR_ROLL;
        updateStatus(teams.get(currentPlayerIndex) + "'s turn. Roll!");
        rollButton.setEnabled(true);
        restartButton.setEnabled(false); // Disable during active game
        quitButton.setEnabled(true); // Redundancy

        extraTurnEarnedInThisPhase = false;
        catchOccurredInThisPhase = false;
        earnedRollsThisPhase.clear();
        rollsToProcess.clear();
        selectedPiece = null;
        // possibleMoveTargets = null; // No longer needed
        piecesAtClickedStack.clear();
        boardPanel.setHighlight(null, null);
        boardPanel.repaint();
    }

    // Called when all rolls are used or game ends
    private void concludeTurnSegment() {
        if (currentState == GameState.GAME_OVER)
            return;

        if (teams.get(currentPlayerIndex).isWinner()) {
            handleGameOver(teams.get(currentPlayerIndex));
            return;
        }

        if (catchOccurredInThisPhase) {
            updateStatus(teams.get(currentPlayerIndex) + " earned an extra turn!");
            startNewTurnPhase();
        } else {
            updateStatus("End of " + teams.get(currentPlayerIndex) + "'s turn.");
            currentPlayerIndex = (currentPlayerIndex + 1) % numTeams;
            startNewTurnPhase();
        }
    }

    private void handleGameOver(Team winningTeam) {
        currentState = GameState.GAME_OVER;
        updateStatus("\n**************************************");
        updateStatus(winningTeam + " wins the game!");
        updateStatus("**************************************");
        rollButton.setEnabled(false);
        restartButton.setEnabled(true); // Enable on game over
        quitButton.setEnabled(true); // Redundancy but anyway...
        boardPanel.setHighlight(null, null);
        boardPanel.repaint();
    }

    private void executeMove(Piece pieceToMove, YunnoriRoll rollUsed) {
        int targetPosition = board.calculateTargetPosition(pieceToMove, rollUsed.getSteps());
        int oldPosition = pieceToMove.getCurrentPositionIndex();
        pieceToMove.moveTo(targetPosition);

        boolean caughtThisMove = false;
        if (pieceToMove.getCurrentPositionIndex() > 0 && pieceToMove.getCurrentPositionIndex() < 31) {
            List<Piece> caughtOpponentPieces = board.findOpponentPiecesAt(pieceToMove.getCurrentPositionIndex(),
                    teams.get(currentPlayerIndex), teams);
            if (!caughtOpponentPieces.isEmpty()) {
                StringBuilder sb = new StringBuilder(teams.get(currentPlayerIndex) + " caught ");
                for (Piece p : caughtOpponentPieces) {
                    sb.append(teams.get(p.getTeamId()).toString()).append(" ").append(p.toString()).append(" ");
                }
                sb.append("at position ").append(pieceToMove.getCurrentPositionIndex()).append("!");
                updateStatus(sb.toString());
                board.resetPiecesToStart(caughtOpponentPieces);
                caughtThisMove = true;
            }
        }
        boolean finished = pieceToMove.isFinished();
        if (finished) {
            updateStatus(teams.get(currentPlayerIndex) + " " + pieceToMove.toString() + " finished!");
        } else if (!caughtThisMove) {
            updateStatus(teams.get(currentPlayerIndex) + " " + pieceToMove.toString() + " moved from " + oldPosition
                    + " to " + pieceToMove.getCurrentPositionIndex() + " with " + rollUsed.name() + ".");
        }
        if (caughtThisMove) {
            catchOccurredInThisPhase = true;
        }
        boardPanel.repaint();

        if (teams.get(currentPlayerIndex).isWinner()) {
            handleGameOver(teams.get(currentPlayerIndex)); // Call central game over handler
            return;
        }

        if (rollsToProcess.isEmpty()) {
            concludeTurnSegment();
        } else {
            checkPlayableMoves(); // Will set state to WAITING_FOR_PIECE_SELECTION
        }
    }

    // checkPlayableMoves method (remains the same)
    private void checkPlayableMoves() {
        List<Piece> playablePieces = teams.get(currentPlayerIndex).getPlayablePieces();
        List<Piece> piecesWithValidMove = new ArrayList<>();
        if (!rollsToProcess.isEmpty()) {
            YunnoriRoll currentRoll = rollsToProcess.get(0);
            for (Piece piece : playablePieces) {
                if (board.isValidMoveStart(piece, currentRoll.getSteps())) {
                    piecesWithValidMove.add(piece);
                }
            }
        }

        if (piecesWithValidMove.isEmpty() && !rollsToProcess.isEmpty()) {
            YunnoriRoll skippedRoll = rollsToProcess.remove(0);
            updateStatus(teams.get(currentPlayerIndex) + " has no piece that can move with " + skippedRoll
                    + ". Skipping roll.");
            if (rollsToProcess.isEmpty()) {
                concludeTurnSegment();
            } else {
                updateStatus("Rolls left: " + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                        + ". Checking next...");
                checkPlayableMoves();
            }
        } else if (!piecesWithValidMove.isEmpty()) {
            currentState = GameState.WAITING_FOR_PIECE_SELECTION;
            updateStatus("Rolls to process: "
                    + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", ")) + ". Select a piece.");
            List<Integer> highlightPositions = new ArrayList<>();
            for (Piece p : piecesWithValidMove) {
                highlightPositions.add(p.getCurrentPositionIndex());
            }
            boardPanel.setHighlight(null, highlightPositions);
            boardPanel.repaint();
        } else {
            concludeTurnSegment();
        }
    }

    // pieceClicked method (remains the same - auto moves)
    public void pieceClicked(Piece piece) {
        if (currentState == GameState.WAITING_FOR_PIECE_SELECTION
                || currentState == GameState.WAITING_FOR_STACK_SELECTION) {
            if (piece.getTeamId() == currentPlayerIndex && !rollsToProcess.isEmpty()) {
                YunnoriRoll currentRoll = rollsToProcess.get(0);
                if (board.isValidMoveStart(piece, currentRoll.getSteps())) {
                    updateStatus("Piece " + piece + " selected. Moving automatically with " + currentRoll.name() + ".");
                    rollsToProcess.remove(0);
                    executeMove(piece, currentRoll);
                    boardPanel.setHighlight(null, null);
                } else {
                    updateStatus("Piece " + piece + " cannot move with " + currentRoll + ". Choose another piece.");
                    if (currentState == GameState.WAITING_FOR_STACK_SELECTION) {
                        currentState = GameState.WAITING_FOR_PIECE_SELECTION;
                        checkPlayableMoves();
                    }
                }
            } else {
                updateStatus("That's not your piece!");
                if (currentState == GameState.WAITING_FOR_STACK_SELECTION) {
                    currentState = GameState.WAITING_FOR_PIECE_SELECTION;
                    checkPlayableMoves();
                }
            }
        } else {
            updateStatus("Wait for your turn to select a piece, or roll first.");
        }
        boardPanel.repaint();
    }

    // pieceStackClicked method (remains the same)
    public void pieceStackClicked(List<Piece> piecesAtStack, int clickX, int clickY) {
        if (currentState == GameState.WAITING_FOR_PIECE_SELECTION) {
            List<Piece> playablePiecesAtStack = new ArrayList<>();
            if (!rollsToProcess.isEmpty()) {
                YunnoriRoll currentRoll = rollsToProcess.get(0);
                for (Piece p : piecesAtStack) {
                    if (!p.isFinished() && p.getTeamId() == currentPlayerIndex
                            && board.isValidMoveStart(p, currentRoll.getSteps())) {
                        playablePiecesAtStack.add(p);
                    }
                }
            }
            if (playablePiecesAtStack.isEmpty()) {
                updateStatus("No playable piece in that stack for " + teams.get(currentPlayerIndex) + " with "
                        + (rollsToProcess.isEmpty() ? "no roll" : rollsToProcess.get(0).name()) + ".");
            } else if (playablePiecesAtStack.size() == 1) {
                updateStatus("Auto-selecting the only playable piece in the stack.");
                pieceClicked(playablePiecesAtStack.get(0));
            } else {
                this.piecesAtClickedStack = playablePiecesAtStack;
                currentState = GameState.WAITING_FOR_STACK_SELECTION;
                updateStatus("Multiple playable pieces at this point. Choose one:");
                promptForStackSelection();
            }
        } else {
            updateStatus("Wait for your turn to select a piece.");
        }
        boardPanel.repaint();
    }

    // promptForStackSelection method (remains the same)
    private void promptForStackSelection() {
        List<String> options = new ArrayList<>();
        for (Piece p : piecesAtClickedStack) {
            options.add("Piece " + (p.getId() + 1) + " (Team " + (p.getTeamId() + 1) + ")");
        }
        String[] optionsArray = options.toArray(new String[0]);
        String selection = (String) JOptionPane.showInputDialog(
                this, "Select which piece to move:", "Piece Selection",
                JOptionPane.QUESTION_MESSAGE, null, optionsArray, optionsArray[0]);
        List<Piece> stackSelectionList = new ArrayList<>(this.piecesAtClickedStack);
        this.piecesAtClickedStack.clear();

        if (selection != null) {
            Piece selectedFromStack = null;
            for (Piece p : stackSelectionList) {
                String pieceString = "Piece " + (p.getId() + 1) + " (Team " + (p.getTeamId() + 1) + ")";
                if (pieceString.equals(selection)) {
                    selectedFromStack = p;
                    break;
                }
            }
            if (selectedFromStack != null) {
                pieceClicked(selectedFromStack);
            } else {
                updateStatus("Error: Could not find selected piece from stack.");
                resetTurnState();
            }
        } else {
            updateStatus("Piece selection cancelled.");
            currentState = GameState.WAITING_FOR_PIECE_SELECTION;
            updateStatus("Rolls left: " + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                    + ". Select a piece.");
            checkPlayableMoves();
        }
    }

    // boardClicked is no longer used for confirming moves
    public void boardClicked(int clickX, int clickY) {
        if (currentState == GameState.WAITING_FOR_PIECE_SELECTION && selectedPiece != null) {
            updateStatus("Selection cancelled by clicking board.");
            selectedPiece = null;
            boardPanel.setHighlight(null, null);
            checkPlayableMoves();
            boardPanel.repaint();
        } else if (currentState == GameState.WAITING_FOR_PIECE_SELECTION) {
            updateStatus("Click on one of your pieces to move it.");
        } else if (currentState == GameState.WAITING_FOR_ROLL) {
            updateStatus("Click the 'Roll' button.");
        }
    }

    // resetTurnState (used for error recovery, not full game restart)
    private void resetTurnState() {
        earnedRollsThisPhase.clear();
        rollsToProcess.clear();
        selectedPiece = null;
        // possibleMoveTargets = null; // Removed
        piecesAtClickedStack.clear();
        extraTurnEarnedInThisPhase = false;
        catchOccurredInThisPhase = false;
        currentState = GameState.WAITING_FOR_ROLL;
        boardPanel.setHighlight(null, null);
        boardPanel.repaint();
        updateStatus(teams.get(currentPlayerIndex) + "'s turn. Roll!");
        rollButton.setEnabled(true);
    }

    private void updateStatus(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == quitButton) {
            System.exit(0);
        } else if (e.getSource() == restartButton) {
            // Restart is allowed only if game is over
            if (currentState == GameState.GAME_OVER) {
                updateStatus("\n--- Restarting Game ---");
                initializeGameLogicAndBoardPanel();
                startGame();
            }
        } else if (currentState == GameState.GAME_OVER) {
            updateStatus("Game is over! Click Restart or Quit.");
            return;
        } else if (e.getSource() == rollButton) {
            if (currentState == GameState.WAITING_FOR_ROLL || currentState == GameState.WAITING_FOR_REORDER) {
                rollButton.setEnabled(false);
                if (currentState == GameState.WAITING_FOR_REORDER) {
                    updateStatus("Reordering cancelled by rolling again.");
                    rollsToProcess.clear();
                }
                // Reset flags for *this specific rolling phase*
                extraTurnEarnedInThisPhase = false;
                // catchOccurredInThisPhase pertains to moves, should persist if this is an
                // extra roll after a catch.
                // However, startNewTurnPhase resets catchOccurredInThisPhase too, which is
                // correct for a truly new turn segment.
                earnedRollsThisPhase.clear();

                startRollingPhase();
            } else {
                updateStatus("It's not time to roll yet. Current state: " + currentState);
            }
        }
    }

    // startRollingPhase method (remains the same)
    private void startRollingPhase() {
        updateStatus(teams.get(currentPlayerIndex) + " is rolling...");
        // ... (rest of the method from previous version) ...
        if (isTestMode) {
            boolean continueTestRolling = true;
            while (continueTestRolling) {
                YunnoriRoll testRoll = promptForTestRoll();
                if (testRoll != null) {
                    roller.setTestRoll(testRoll);
                    YunnoriRoll roll = roller.roll();
                    updateStatus(teams.get(currentPlayerIndex) + " rolled (test): " + roll);
                    earnedRollsThisPhase.add(roll);
                    if (roll == YunnoriRoll.YUT || roll == YunnoriRoll.MO) {
                        extraTurnEarnedInThisPhase = true;
                        updateStatus("Earned an extra roll opportunity! Enter the next test roll.");
                    } else {
                        continueTestRolling = false;
                    }
                } else {
                    updateStatus("Test roll input cancelled.");
                    earnedRollsThisPhase.clear();
                    resetTurnState(); // Go back to waiting for roll for current player
                    return;
                }
            }
            processAfterRollingPhase();
        } else { // Real mode
            boolean continueRolling = true;
            while (continueRolling) {
                YunnoriRoll roll = roller.roll();
                updateStatus(teams.get(currentPlayerIndex) + " rolled: " + roll);
                earnedRollsThisPhase.add(roll);
                if (roll == YunnoriRoll.YUT || roll == YunnoriRoll.MO) {
                    extraTurnEarnedInThisPhase = true;
                    updateStatus("Earned an extra roll!");
                } else {
                    continueRolling = false;
                }
            }
            processAfterRollingPhase();
        }
    }

    // promptForTestRoll method (remains the same)
    private YunnoriRoll promptForTestRoll() {
        String[] options = { "Do", "Gae", "Geol", "Yut", "Mo", "Back_Do" };
        String input = (String) JOptionPane.showInputDialog(
                this, "Enter test roll:", "Test Roll Input",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (input != null) {
            return YunnoriRoll.fromName(input);
        }
        return null;
    }

    // processAfterRollingPhase method (remains the same)
    private void processAfterRollingPhase() {
        rollsToProcess.addAll(earnedRollsThisPhase);
        earnedRollsThisPhase.clear();
        boolean canReorder = rollsToProcess.stream().anyMatch(r -> r == YunnoriRoll.YUT || r == YunnoriRoll.MO);
        if (canReorder && rollsToProcess.size() > 1) {
            currentState = GameState.WAITING_FOR_REORDER;
            updateStatus("You rolled: " + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                    + ". Reorder rolls?");
            promptForReorder();
        } else if (!rollsToProcess.isEmpty()) {
            checkPlayableMoves(); // This will set state to WAITING_FOR_PIECE_SELECTION
        } else {
            updateStatus("No rolls earned or processed. Turn segment ends.");
            concludeTurnSegment();
        }
    }

    // promptForReorder method (remains the same)
    private void promptForReorder() {
        String currentOrder = rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(" "));
        String message = "Enter desired order for rolls (" + currentOrder + "):";
        String input = JOptionPane.showInputDialog(this, message, "Reorder Rolls", JOptionPane.QUESTION_MESSAGE);
        if (input != null) {
            List<YunnoriRoll> reordered = new ArrayList<>();
            List<YunnoriRoll> tempOriginal = new ArrayList<>(rollsToProcess);
            boolean valid = true;
            String[] rollNames = input.trim().split("\\s+");
            if (rollNames.length != rollsToProcess.size()) {
                updateStatus("Invalid number of rolls entered for reordering. Using original order.");
                valid = false;
            } else {
                for (String name : rollNames) {
                    YunnoriRoll roll = YunnoriRoll.fromName(name);
                    if (roll == null || !tempOriginal.remove(roll)) {
                        updateStatus("Invalid roll name or roll not available: " + name + ". Using original order.");
                        valid = false;
                        break;
                    }
                    reordered.add(roll);
                }
                if (!tempOriginal.isEmpty()) {
                    updateStatus("Error processing reorder input. Some rolls were not matched.");
                    valid = false;
                }
            }
            if (valid) {
                rollsToProcess.clear();
                rollsToProcess.addAll(reordered);
                updateStatus("Rolls will be processed in this order: "
                        + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", ")));
            } else {
                updateStatus("Reordering failed. Using original order.");
            }
        } else {
            updateStatus("Reordering cancelled. Using original order.");
        }
        checkPlayableMoves();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
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

                    try {
                        new YunnoriGUI(numTeams, numPieces, isTestMode);
                    } catch (Exception e) {
                        System.err.println("\n--- An error occurred during GUI setup or game initialization ---");
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "An unexpected error occurred:\n" + e.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid input:\n" + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.err.println("\n--- Input validation error ---");
                    e.printStackTrace();
                }
            }
        });
    }
}