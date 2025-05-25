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
    private BoardType boardType;

    // Game Logic and State
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

    private enum GameState {
        WAITING_FOR_ROLL, WAITING_FOR_PIECE_SELECTION, WAITING_FOR_REORDER,
        WAITING_FOR_STACK_SELECTION, GAME_OVER
    }

    private GameState currentState;
    private boolean extraTurnEarnedInThisPhase = false;
    private boolean catchOccurredInThisPhase = false;
    private List<Piece> piecesAtClickedStack = new ArrayList<>();

    // Constructor
    public YunnoriGUI(int numTeams, int numPieces, boolean isTestMode, BoardType boardType) {
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
        this.initializeGameLogicAndBoardPanel(boardType);

        // Initialize and start the game
        initializeGameLogicAndBoardPanel(boardType);
        setVisible(true); // Make window visible AFTER all components are added
        startGame(); // Start the first turn logic
    }

    // New method to initialize/re-initialize game logic components and BoardPanel
    private void initializeGameLogicAndBoardPanel(BoardType boardType) {
        // board = new Board();
        board = new Board(boardType);
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
        boardPanel.setPreferredSize(new Dimension(1150, 1100));
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
        // pieceToMove is guaranteed to be a leader or an individual piece due to
        // getPlayablePieces & piece selection logic

        int targetPosition = board.calculateTargetPosition(pieceToMove, rollUsed.getSteps());
        int oldPosition = pieceToMove.getCurrentPositionIndex();

        // Temporarily store pieces pieceToMove is currently carrying.
        // This is because pieceToMove might merge with another group, and its current
        // identity as a leader changes.
        // The addToStack logic in Piece.java is designed to handle merging.
        // No need to detach here if addToStack correctly absorbs.

        pieceToMove.moveTo(targetPosition); // Moves pieceToMove and its entire stack (if any)

        String moveMsg = teams.get(currentPlayerIndex) + " " + pieceToMove.toString() + " moved from " + oldPosition
                + " to " + pieceToMove.getCurrentPositionIndex() + " with " + rollUsed.name() + ".";
        updateStatus(moveMsg);

        // --- Grouping Logic ---
        // Only group if not at start/finish and not already finished
        if (pieceToMove.getCurrentPositionIndex() > 0 && pieceToMove.getCurrentPositionIndex() < 31
                && !pieceToMove.isFinished()) {
            Team currentTeam = teams.get(currentPlayerIndex);
            List<Piece> friendlyPiecesAtTarget = new ArrayList<>();
            // Find other friendly leaders or individuals at the target position
            for (Piece p : currentTeam.getInteractivePiecesAt(targetPosition)) {
                if (p != pieceToMove) { // Don't try to group with itself
                    friendlyPiecesAtTarget.add(p);
                }
            }

            if (!friendlyPiecesAtTarget.isEmpty()) {
                String existingPieceNames = friendlyPiecesAtTarget.stream()
                        .map(Piece::toString) // Piece.toString() now shows group size
                        .collect(Collectors.joining(", "));
                int choice = JOptionPane.showConfirmDialog(this,
                        "You landed on your own piece(s): " + existingPieceNames +
                                ".\nDo you want " + pieceToMove.toString() + " to lead/join this stack?",
                        "Group Pieces?", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    // pieceToMove will attempt to become the leader of the pieces it landed on.
                    // If pieceToMove was already a leader, its existing stack is part of it.
                    // If any friendlyPiecesAtTarget were leaders, their stacks are merged under
                    // pieceToMove by addToStack.
                    for (Piece existingPiece : friendlyPiecesAtTarget) {
                        pieceToMove.addToStack(existingPiece); // This handles merging logic
                    }
                    updateStatus(pieceToMove.toString() + " now leads the stack at position " + targetPosition + ".");
                }
            }
        }
        // --- End Grouping Logic ---

        boolean caughtThisMove = false;
        // Check for catches only if the piece is on a catchable part of the board
        if (pieceToMove.getCurrentPositionIndex() > 0 && pieceToMove.getCurrentPositionIndex() < 31) {
            // findOpponentPiecesAt returns leaders or individual opponent pieces
            List<Piece> caughtOpponentLeadersOrIndividuals = board.findOpponentPiecesAt(
                    pieceToMove.getCurrentPositionIndex(),
                    teams.get(currentPlayerIndex), teams);

            if (!caughtOpponentLeadersOrIndividuals.isEmpty()) {
                StringBuilder sb = new StringBuilder(pieceToMove.toString() + " caught: "); // Current player's piece
                List<Piece> allPiecesActuallyReset = new ArrayList<>();

                for (Piece opponentLeaderOrInd : caughtOpponentLeadersOrIndividuals) {
                    sb.append(opponentLeaderOrInd.toString()).append(" (Team ")
                            .append(opponentLeaderOrInd.getTeamId() + 1).append(") ");
                    allPiecesActuallyReset.add(opponentLeaderOrInd); // The leader/individual itself
                    // If it was a leader, its reset() will handle its stack.
                }
                sb.append("at position ").append(pieceToMove.getCurrentPositionIndex()).append("!");
                updateStatus(sb.toString());

                board.resetPiecesToStart(allPiecesActuallyReset); // Resetting leaders will reset their stacks
                caughtThisMove = true;
            }
        }

        boolean finished = pieceToMove.isFinished(); // This checks the leader
        if (finished) {
            updateStatus(teams.get(currentPlayerIndex) + " " + pieceToMove.toString() + " finished!");
        }
        // No need for the else if (!caughtThisMove) for basic move message, as it's
        // printed earlier.

        if (caughtThisMove) {
            catchOccurredInThisPhase = true;
        }
        boardPanel.repaint();

        if (teams.get(currentPlayerIndex).isWinner()) {
            handleGameOver(teams.get(currentPlayerIndex));
            return;
        }

        if (rollsToProcess.isEmpty()) {
            concludeTurnSegment();
        } else {
            checkPlayableMoves();
        }
    }

    private void checkPlayableMoves() {
        // getPlayablePieces now returns only leaders or individual un-stacked pieces.
        List<Piece> playableLeadersOrIndividuals = teams.get(currentPlayerIndex).getPlayablePieces();
        List<Piece> piecesWithValidMove = new ArrayList<>();

        if (!rollsToProcess.isEmpty()) {
            YunnoriRoll currentRoll = rollsToProcess.get(0);
            for (Piece piece : playableLeadersOrIndividuals) {
                // isValidMoveStart uses piece.canMove() which checks !isStacked() and
                // !isFinished()
                if (board.isValidMoveStart(piece, currentRoll.getSteps())) {
                    piecesWithValidMove.add(piece);
                }
            }
        }

        if (piecesWithValidMove.isEmpty() && !rollsToProcess.isEmpty()) {
            YunnoriRoll skippedRoll = rollsToProcess.remove(0);
            updateStatus(teams.get(currentPlayerIndex) + " has no piece/group that can move with " + skippedRoll
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
                    + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                    + ". Select a piece/group.");
            List<Integer> highlightPositions = new ArrayList<>();
            for (Piece p : piecesWithValidMove) { // These are leaders/individuals
                highlightPositions.add(p.getCurrentPositionIndex());
            }
            boardPanel.setHighlight(null, highlightPositions); // Highlight positions of movable leaders/individuals
            boardPanel.repaint();
        } else { // No rolls left or no playable moves with remaining rolls
            concludeTurnSegment();
        }
    }

    // pieceClicked is called when a piece is chosen (either directly or from a
    // stack prompt)
    public void pieceClicked(Piece piece) { // 'piece' here will be a leader or an individual
        if (currentState == GameState.WAITING_FOR_PIECE_SELECTION
                || currentState == GameState.WAITING_FOR_STACK_SELECTION) {

            if (piece.getTeamId() == currentPlayerIndex && !rollsToProcess.isEmpty()) {
                YunnoriRoll currentRoll = rollsToProcess.get(0);
                // piece.canMove() is checked by isValidMoveStart
                if (board.isValidMoveStart(piece, currentRoll.getSteps())) {
                    updateStatus("Piece/group " + piece + " selected. Moving with " + currentRoll.name() + ".");
                    rollsToProcess.remove(0);
                    executeMove(piece, currentRoll); // 'piece' is the leader/individual
                    boardPanel.setHighlight(null, null);
                } else {
                    updateStatus("Piece/group " + piece + " cannot move with " + currentRoll + ". Choose another.");
                    // If selection came from stack prompt, revert to general piece selection
                    if (currentState == GameState.WAITING_FOR_STACK_SELECTION) {
                        currentState = GameState.WAITING_FOR_PIECE_SELECTION;
                        checkPlayableMoves(); // Re-evaluate based on available pieces
                    }
                }
            } else {
                updateStatus("That's not your piece/group or no rolls available!");
                if (currentState == GameState.WAITING_FOR_STACK_SELECTION) {
                    currentState = GameState.WAITING_FOR_PIECE_SELECTION;
                    checkPlayableMoves();
                }
            }
        } else {
            updateStatus("Wait for your turn to select a piece/group, or roll first.");
        }
        boardPanel.repaint();
    }

    public void pieceStackClicked(List<Piece> piecesAtStack, int clickX, int clickY) {
        // piecesAtStack from BoardPanel now contains leaders or individual pieces at
        // the clicked visual point.
        // These are already filtered by Team.getInteractivePiecesAt().
        if (currentState == GameState.WAITING_FOR_PIECE_SELECTION) {
            List<Piece> playablePiecesOrGroupsAtStack = new ArrayList<>();
            if (!rollsToProcess.isEmpty()) {
                YunnoriRoll currentRoll = rollsToProcess.get(0);
                for (Piece p : piecesAtStack) { // p is a leader or individual
                    if (p.getTeamId() == currentPlayerIndex && board.isValidMoveStart(p, currentRoll.getSteps())) {
                        playablePiecesOrGroupsAtStack.add(p);
                    }
                }
            }

            if (playablePiecesOrGroupsAtStack.isEmpty()) {
                updateStatus("No playable piece/group in that stack for " + teams.get(currentPlayerIndex) + " with "
                        + (rollsToProcess.isEmpty() ? "no roll" : rollsToProcess.get(0).name()) + ".");
            } else if (playablePiecesOrGroupsAtStack.size() == 1) {
                updateStatus("Auto-selecting the only playable piece/group: " + playablePiecesOrGroupsAtStack.get(0));
                pieceClicked(playablePiecesOrGroupsAtStack.get(0)); // Pass the leader/individual
            } else {
                // Multiple leaders/individuals of the current player are at the same visual
                // spot and are playable
                this.piecesAtClickedStack = playablePiecesOrGroupsAtStack;
                currentState = GameState.WAITING_FOR_STACK_SELECTION;
                updateStatus("Multiple playable pieces/groups at this point. Choose one:");
                promptForStackSelection(); // JOptionPane will use Piece.toString() which shows group size
            }
        } else {
            updateStatus("Wait for your turn to select a piece/group.");
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
                initializeGameLogicAndBoardPanel(this.boardType);
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

    private void initializeGameLogicAndBoardPanel() {
        initializeGameLogicAndBoardPanel(this.boardType);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // --- START: Add these UIManager settings for larger dialogs ---
                    int dialogFontSize = 24; // Adjust this value as needed
                    Font dialogFont = new Font("SansSerif", Font.PLAIN, dialogFontSize);
                    Font dialogButtonFont = new Font("SansSerif", Font.BOLD, dialogFontSize - 2); // Slightly smaller
                    UIManager.put("OptionPane.messageFont", dialogFont);
                    UIManager.put("OptionPane.buttonFont", dialogButtonFont);
                    UIManager.put("TextField.font", dialogFont); // For input dialogs
                    UIManager.put("Label.font", dialogFont); // For labels within option panes
                    UIManager.put("ComboBox.font", dialogFont);

                    String[] boardTypes = { "square", "pentagon", "hexagon" };
                    int boardTypeIndex = JOptionPane.showOptionDialog(
                            null,
                            "Select the type of Yutnori board:",
                            "Board Type",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            boardTypes,
                            boardTypes[0]);
                    if (boardTypeIndex == JOptionPane.CLOSED_OPTION)
                        System.exit(0);

                    BoardType boardType;
                    switch (boardTypeIndex) {
                        case 1:
                            boardType = BoardType.PENTAGON;
                            break;
                        case 2:
                            boardType = BoardType.HEXAGON;
                            break;
                        default:
                            boardType = BoardType.RECTANGLE;
                            break;
                    }

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
                        new YunnoriGUI(numTeams, numPieces, isTestMode, boardType); // boardType 전달!
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
