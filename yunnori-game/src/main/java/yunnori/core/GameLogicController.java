package yunnori.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameLogicController {

    public enum GameState {
        NOT_STARTED,
        WAITING_FOR_ROLL,
        WAITING_FOR_PIECE_SELECTION,
        WAITING_FOR_REORDER,
        WAITING_FOR_STACK_SELECTION, // UI prompts based on this state
        AWAITING_TEST_ROLL_INPUT, // UI prompts for test roll
        AWAITING_GROUPING_CHOICE, // UI prompts for grouping choice
        GAME_OVER
    }

    private Board board;
    private List<Team> teams;
    private YunnoriRoller roller;
    private int numTeams;
    private int numPieces;
    private boolean isTestMode;
    private BoardType boardType;

    private int currentPlayerIndex;
    private List<YunnoriRoll> earnedRollsThisPhase;
    private List<YunnoriRoll> rollsToProcess;

    private GameState currentGameState;
    private boolean catchOccurredInThisPhase;

    // For UI interaction context
    private Piece pieceToPotentiallyGroupWith;
    private List<Piece> friendlyPiecesAtTargetForGrouping;
    private Piece pieceAwaitingGroupingDecision; // Piece that moved and might group
    private YunnoriRoll rollUsedForPausedMove; // Roll used for the move that paused for grouping
    private int oldPositionForPausedMove; // Old position for that paused move

    public GameLogicController() {
        this.earnedRollsThisPhase = new ArrayList<>();
        this.rollsToProcess = new ArrayList<>();
        this.currentGameState = GameState.NOT_STARTED;
    }

    public void setupGame(int numTeams, int numPieces, BoardType boardType, boolean isTestMode) {
        this.numTeams = numTeams;
        this.numPieces = numPieces;
        this.boardType = boardType;
        this.isTestMode = isTestMode;

        this.board = new Board(boardType);
        this.teams = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) {
            this.teams.add(new Team(i, numPieces));
        }
        this.roller = new YunnoriRoller(isTestMode);

        this.currentPlayerIndex = 0;
        this.rollsToProcess.clear();
        this.earnedRollsThisPhase.clear();
        this.catchOccurredInThisPhase = false;
        // currentGameState will be set by startGame/startNewTurnPhase
        System.out.println("GLC DEBUG: Game setup complete.");
    }

    public String startGameAndGetMessages() {
        return startNewTurnPhase();
    }

    // --- Getters for UI ---
    public Board getBoard() {
        return board;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getCurrentPlayer() {
        return (teams != null && !teams.isEmpty()) ? teams.get(currentPlayerIndex) : null;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public List<YunnoriRoll> getRollsToProcess() {
        return new ArrayList<>(rollsToProcess);
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public boolean isTestMode() {
        return isTestMode;
    }

    public BoardType getBoardType() {
        return boardType;
    }

    public Piece getPieceToPotentiallyGroupWith() {
        return pieceToPotentiallyGroupWith;
    }

    public List<Piece> getFriendlyPiecesAtTargetForGrouping() {
        return friendlyPiecesAtTargetForGrouping != null ? new ArrayList<>(friendlyPiecesAtTargetForGrouping)
                : new ArrayList<>();
    }

    public List<Piece> getPlayablePiecesForCurrentRoll() {
        List<Piece> playable = new ArrayList<>();
        if (!rollsToProcess.isEmpty()) { // Relaxed state check here
            YunnoriRoll currentRoll = rollsToProcess.get(0);
            List<Piece> candidates = teams.get(currentPlayerIndex).getPlayablePieces();
            for (Piece p : candidates) {
                if (board.isValidMoveStart(p, currentRoll.getSteps())) {
                    playable.add(p);
                }
            }
        }
        return playable;
    }

    public Team getWinner() {
        if (currentGameState == GameState.GAME_OVER) {
            for (Team team : teams) {
                if (team.isWinner()) {
                    return team;
                }
            }
            System.err.println("GLC ERROR: Game is OVER, but no winning team identified!");
        }
        return null;
    }

    // --- UI Action Handlers (returning String or List<String> for messages) ---

    public String handleRollButtonPressed() {
        StringBuilder messages = new StringBuilder();
        if (currentGameState != GameState.WAITING_FOR_ROLL && currentGameState != GameState.WAITING_FOR_REORDER) {
            return "It's not time to roll yet. Current state: " + currentGameState;
        }

        if (currentGameState == GameState.WAITING_FOR_REORDER) {
            messages.append("Reordering cancelled by rolling again.\n");
            rollsToProcess.clear();
        }
        earnedRollsThisPhase.clear();

        Team player = teams.get(currentPlayerIndex);
        if (isTestMode) {
            currentGameState = GameState.AWAITING_TEST_ROLL_INPUT;
            messages.append(player.toString()).append(" is rolling (test mode). UI should prompt for roll.\n");
        } else {
            messages.append(player.toString()).append(" is rolling...\n");
            boolean continueRolling = true;
            while (continueRolling) {
                YunnoriRoll roll = roller.roll();
                messages.append(player.toString()).append(" rolled: ").append(roll).append("\n");
                earnedRollsThisPhase.add(roll);
                if (roll == YunnoriRoll.YUT || roll == YunnoriRoll.MO) {
                    messages.append("Earned an extra roll!\n");
                } else {
                    continueRolling = false;
                }
            }
            messages.append(processEarnedRollsAndGetMessages());
        }
        return messages.toString().trim();
    }

    public String submitTestRoll(YunnoriRoll testRoll) {
        if (!isTestMode || currentGameState != GameState.AWAITING_TEST_ROLL_INPUT) {
            return "Cannot submit test roll now.";
        }
        StringBuilder messages = new StringBuilder();
        roller.setTestRoll(testRoll);
        YunnoriRoll roll = roller.roll();

        messages.append(teams.get(currentPlayerIndex).toString()).append(" rolled (test): ").append(roll).append("\n");
        earnedRollsThisPhase.add(roll);

        if (roll == YunnoriRoll.YUT || roll == YunnoriRoll.MO) {
            messages.append("Earned an extra roll opportunity! Enter the next test roll.\n");
            // currentGameState remains AWAITING_TEST_ROLL_INPUT;
        } else {
            messages.append("Test rolling sequence complete.\n");
            messages.append(processEarnedRollsAndGetMessages());
        }
        return messages.toString().trim();
    }

    public String cancelTestRolling() {
        if (!isTestMode || currentGameState != GameState.AWAITING_TEST_ROLL_INPUT) {
            return ""; // No message if not in correct state
        }
        earnedRollsThisPhase.clear();
        currentGameState = GameState.WAITING_FOR_ROLL;
        return "Test roll input cancelled. Roll again.";
    }

    private String processEarnedRollsAndGetMessages() {
        rollsToProcess.addAll(earnedRollsThisPhase);
        earnedRollsThisPhase.clear();

        boolean canReorder = rollsToProcess.stream().anyMatch(r -> r == YunnoriRoll.YUT || r == YunnoriRoll.MO);
        if (canReorder && rollsToProcess.size() > 1) {
            currentGameState = GameState.WAITING_FOR_REORDER;
            return "You rolled: " + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                    + ". Reorder rolls?";
        } else if (!rollsToProcess.isEmpty()) {
            return String.join("\n", determinePlayableMovesAndUpdateState());
        } else {
            // This case (no rolls from phase) might lead to concluding turn segment
            List<String> conclusionMessages = concludeTurnSegment();
            return "No rolls earned or processed. Turn segment ends.\n" + String.join("\n", conclusionMessages);
        }
    }

    public String submitReorderedRolls(List<YunnoriRoll> newOrder) {
        if (currentGameState != GameState.WAITING_FOR_REORDER)
            return "Not time to reorder.";
        rollsToProcess.clear();
        rollsToProcess.addAll(newOrder);
        String msg = "Rolls will be processed in this order: "
                + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "));
        msg += "\n" + String.join("\n", determinePlayableMovesAndUpdateState());
        return msg;
    }

    public String cancelReorder() {
        if (currentGameState != GameState.WAITING_FOR_REORDER)
            return "";
        String msg = "Reordering cancelled. Using original order: "
                + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "));
        msg += "\n" + String.join("\n", determinePlayableMovesAndUpdateState());
        return msg;
    }

    public List<String> handlePieceClicked(Piece piece) {
        List<String> messages = new ArrayList<>();
        if (currentGameState != GameState.WAITING_FOR_PIECE_SELECTION
                && currentGameState != GameState.WAITING_FOR_STACK_SELECTION) {
            messages.add("Not time to select a piece. Current state: " + currentGameState);
            return messages;
        }
        if (piece.getTeamId() != currentPlayerIndex) {
            messages.add("That's not your piece!");
            return messages;
        }
        if (rollsToProcess.isEmpty()) {
            messages.add("No rolls available to make a move.");
            return messages;
        }

        YunnoriRoll currentRoll = rollsToProcess.get(0);
        if (board.isValidMoveStart(piece, currentRoll.getSteps())) {
            // Message generation for piece selection is usually handled by UI or the first line of executeMove
            rollsToProcess.remove(0);
            messages.addAll(executeMove(piece, currentRoll));
        } else {
            messages.add("Piece " + piece + " cannot move with " + currentRoll + ". Choose another piece.");
        }
        return messages;
    }

    public List<String> submitGroupingChoice(boolean userChoseToGroup) {
        List<String> messages = new ArrayList<>();
        if (currentGameState != GameState.AWAITING_GROUPING_CHOICE || pieceAwaitingGroupingDecision == null) {
            messages.add("Not awaiting grouping choice or context is missing.");
            if (currentGameState == GameState.AWAITING_GROUPING_CHOICE) { // Try to recover state
                if (!rollsToProcess.isEmpty())
                    messages.addAll(determinePlayableMovesAndUpdateState());
                else
                    messages.addAll(concludeTurnSegment());
            }
            return messages;
        }

        Piece pieceThatMoved = this.pieceAwaitingGroupingDecision;
        List<Piece> friendlies = this.friendlyPiecesAtTargetForGrouping;
        int targetPos = pieceThatMoved.getCurrentPositionIndex();

        if (userChoseToGroup && friendlies != null && !friendlies.isEmpty()) {
            for (Piece existingPiece : friendlies) {
                pieceThatMoved.addToStack(existingPiece);
            }
            messages.add(pieceThatMoved.toString() + " now leads the stack at position " + targetPos + ".");
        } else {
            messages.add("Pieces not grouped at " + targetPos + ".");
        }

        YunnoriRoll originalRoll = this.rollUsedForPausedMove;
        int originalOldPos = this.oldPositionForPausedMove;

        this.pieceToPotentiallyGroupWith = null;
        this.friendlyPiecesAtTargetForGrouping = null;
        this.pieceAwaitingGroupingDecision = null;
        this.rollUsedForPausedMove = null;
        this.oldPositionForPausedMove = -1;

        messages.addAll(completeMoveExecution(pieceThatMoved, targetPos, originalRoll, originalOldPos));
        return messages;
    }

    public List<String> restartGame() {
        List<String> messages = new ArrayList<>();
        messages.add("--- Restarting Game ---");
        setupGame(this.numTeams, this.numPieces, this.boardType, this.isTestMode);
        messages.add(startGameAndGetMessages());
        return messages;
    }

    /*
    public List<String> handleGameOver(Team winningTeam) {
        List<String> messages = new ArrayList<>();
        currentGameState = GameState.GAME_OVER; // Ensure state is set
        messages.add("\n**************************************");
        messages.add(winningTeam.toString() + " wins the game!");
        messages.add("**************************************");
        return messages;
    }*/

    private void setGameOverState(Team winningTeam) { // Changed from handleGameOver, made private for internal use
        currentGameState = GameState.GAME_OVER;
        // The actual winner is now known and can be retrieved by getWinner()
        // System.out.println("GLC DEBUG: Game over. Winner: " + winningTeam);
    }

    // The public method for UI to get formatted win messages
    public List<String> getGameOverMessages() {
        List<String> messages = new ArrayList<>();
        if (currentGameState == GameState.GAME_OVER) {
            Team winner = getWinner(); // getWinner() should find the team with all pieces finished
            if (winner != null) {
                messages.add("\n**************************************");
                messages.add(winner.toString() + " wins the game!");
                messages.add("**************************************");
            } else {
                messages.add("\n**************************************");
                messages.add("The Game is Over! (Winner couldn't be determined by getWinner)");
                messages.add("**************************************");
            }
        }
        return messages;
    }

    // --- Internal Game Flow and Logic Methods ---
    private String startNewTurnPhase() {
        catchOccurredInThisPhase = false;
        earnedRollsThisPhase.clear();
        rollsToProcess.clear();
        currentGameState = GameState.WAITING_FOR_ROLL;
        return "--- Starting turn for " + teams.get(currentPlayerIndex).toString() + " ---\n" +
                teams.get(currentPlayerIndex).toString() + "'s turn. Roll!";
    }

    private List<String> determinePlayableMovesAndUpdateState() {
        List<String> messages = new ArrayList<>();
        if (rollsToProcess.isEmpty()) { // Should ideally be caught before calling this
            messages.addAll(concludeTurnSegment());
            return messages;
        }

        List<Piece> playablePieces = getPlayablePiecesForCurrentRoll();

        if (playablePieces.isEmpty()) { // No pieces can move with current roll
            YunnoriRoll skippedRoll = rollsToProcess.remove(0);
            messages.add(teams.get(currentPlayerIndex).toString() + " has no piece/group that can move with "
                    + skippedRoll + ". Skipping roll.");
            if (rollsToProcess.isEmpty()) {
                messages.addAll(concludeTurnSegment());
            } else {
                messages.add("Rolls left: " + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                        + ". Checking next...");
                messages.addAll(determinePlayableMovesAndUpdateState()); // Recursive call
            }
        } else { // Moves are possible
            currentGameState = GameState.WAITING_FOR_PIECE_SELECTION;
            messages.add(
                    "Rolls to process: " + rollsToProcess.stream().map(Enum::name).collect(Collectors.joining(", "))
                            + ". Select a piece/group.");
        }
        return messages;
    }

    private List<String> executeMove(Piece pieceToMove, YunnoriRoll rollUsed) {
        List<String> messages = new ArrayList<>();
        this.pieceAwaitingGroupingDecision = null;
        this.rollUsedForPausedMove = null;
        this.oldPositionForPausedMove = -1;

        int targetPosition = board.calculateTargetPosition(pieceToMove, rollUsed.getSteps());
        int oldPosition = pieceToMove.getCurrentPositionIndex();

        pieceToMove.moveTo(targetPosition, this.board);
        messages.add(
                teams.get(currentPlayerIndex).toString() + " " + pieceToMove.toString() + " moved from " + oldPosition
                        + " to " + pieceToMove.getCurrentPositionIndex() + " with " + rollUsed.name() + ".");

        boolean groupingPromptNeeded = false;
        if (pieceToMove.getCurrentPositionIndex() > board.getStartPointIndex() &&
                pieceToMove.getCurrentPositionIndex() < board.getFinishPointIndex() &&
                !pieceToMove.isFinished()) {
            Team currentTeam = teams.get(currentPlayerIndex);
            List<Piece> friendlyTarget = new ArrayList<>();
            for (Piece p : currentTeam.getInteractivePiecesAt(targetPosition)) {
                if (p != pieceToMove)
                    friendlyTarget.add(p);
            }
            if (!friendlyTarget.isEmpty()) {
                this.pieceToPotentiallyGroupWith = pieceToMove;
                this.friendlyPiecesAtTargetForGrouping = friendlyTarget;
                this.currentGameState = GameState.AWAITING_GROUPING_CHOICE;

                this.pieceAwaitingGroupingDecision = pieceToMove;
                this.rollUsedForPausedMove = rollUsed;
                this.oldPositionForPausedMove = oldPosition;

                groupingPromptNeeded = true;
                messages.add("Grouping choice needed for " + pieceToMove + ". Landed on " +
                        friendlyTarget.stream().map(Piece::toString).collect(Collectors.joining(", ")));
            }
        }

        if (!groupingPromptNeeded) {
            messages.addAll(completeMoveExecution(pieceToMove, targetPosition, rollUsed, oldPosition));
        }
        return messages;
    }

    private List<String> completeMoveExecution(Piece pieceToMove, int targetPosition, YunnoriRoll rollUsed,
            int oldPosition) {
        List<String> messages = new ArrayList<>();
        boolean caughtThisMove = false;
        if (targetPosition > board.getStartPointIndex() && targetPosition < board.getFinishPointIndex()) { // Use targetPosition for catch check
            List<Piece> caughtOpponents = board.findOpponentPiecesAt(targetPosition, teams.get(currentPlayerIndex),
                    teams);
            if (!caughtOpponents.isEmpty()) {
                StringBuilder sb = new StringBuilder(pieceToMove.toString() + " caught: ");
                for (Piece p : caughtOpponents)
                    sb.append(p.toString()).append(" (Team ").append(p.getTeamId() + 1).append(") ");
                sb.append("at position ").append(targetPosition).append("!");
                messages.add(sb.toString());
                board.resetPiecesToStart(caughtOpponents);
                caughtThisMove = true;
                this.catchOccurredInThisPhase = true;
            }
        }

        if (pieceToMove.isFinished()) {
            messages.add(teams.get(currentPlayerIndex).toString() + " " + pieceToMove.toString() + " finished!");
        }
        // The "moved from X to Y" message is already in executeMove's list.
        // Only add if specific conditions like catch or finish apply.

        if (teams.get(currentPlayerIndex).isWinner()) {
            setGameOverState(teams.get(currentPlayerIndex)); // Set state, don't add messages here
            // The game over messages will be fetched by the UI in updateUIBasedOnGameState
            // No specific "win" message is added to 'messages' list here.
            // The UI will react to the GAME_OVER state.
            return messages;
        }

        if (currentGameState == GameState.GAME_OVER)
            return messages; // If win was processed.

        if (rollsToProcess.isEmpty()) {
            messages.addAll(concludeTurnSegment());
        } else {
            messages.addAll(determinePlayableMovesAndUpdateState());
        }
        return messages;
    }

    private List<String> concludeTurnSegment() {
        List<String> messages = new ArrayList<>();
        if (currentGameState == GameState.GAME_OVER)
            return messages; // Already handled

        // Double check winner, though should be caught by completeMoveExecution
        if (teams.get(currentPlayerIndex).isWinner()) {
            messages.addAll(getGameOverMessages());
            return messages;
        }

        if (catchOccurredInThisPhase) {
            messages.add(teams.get(currentPlayerIndex).toString() + " earned an extra turn!");
            messages.add(startNewTurnPhase());
        } else {
            messages.add("End of " + teams.get(currentPlayerIndex).toString() + "'s turn.");
            currentPlayerIndex = (currentPlayerIndex + 1) % numTeams;
            messages.add(startNewTurnPhase());
        }
        return messages;
    }
}