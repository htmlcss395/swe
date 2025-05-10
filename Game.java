import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Game {
    private Board board;
    private List<Team> teams;
    private YunnoriRoller roller;
    private Scanner scanner;
    private int numTeams;
    private int numPieces;

    public Game(int numTeams, int numPieces, boolean isTest, Scanner scanner) {
        this.numTeams = numTeams;
        this.numPieces = numPieces;
        this.scanner = scanner;
        this.board = new Board(); // Board doesn't need scanner now
        this.roller = new YunnoriRoller(isTest, scanner);
        this.teams = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team(i, numPieces));
        }
    }

    public void startGame() {
        System.out.println("\n--- Yunnori Game Started ---");
        System.out.println("Number of Teams: " + numTeams);
        System.out.println("Pieces per Team: " + numPieces);
        System.out.println("----------------------------\n");

        int currentPlayerIndex = 0;
        boolean gameWon = false;

        while (!gameWon) {
            Team currentTeam = teams.get(currentPlayerIndex);
            System.out.println("\n--- " + currentTeam + "'s turn ---");
            board.printBoardState(teams);

            // playTurn will return true if this team wins during its turn (including extra
            // turns)
            gameWon = playTurn(currentTeam);

            if (gameWon) {
                System.out.println("\n**************************************");
                System.out.println(currentTeam + " wins the game!");
                System.out.println("**************************************");
                // The while loop condition will now be false and terminate
            } else {
                System.out.println("\nEnd of " + currentTeam + "'s turn.");
                currentPlayerIndex = (currentPlayerIndex + 1) % numTeams;
            }
        }
        System.out.println("\n--- Game Over ---");
    }

    // playTurn now returns boolean: true if the team wins during this turn (or its
    // extra turns)
    private boolean playTurn(Team team) {
        boolean extraTurnEarned = false;
        boolean caughtOpponentThisTurn = false; // Track catches across all rolls in this turn phase

        // Phase 1: Rolling
        List<YunnoriRoll> earnedRolls = new ArrayList<>();
        boolean continueRolling = true;
        System.out.println(team + " is rolling...");
        while (continueRolling) {
            YunnoriRoll roll = roller.roll();
            System.out.println(team + " rolled: " + roll);
            earnedRolls.add(roll);

            if (roll == YunnoriRoll.YUT || roll == YunnoriRoll.MO) {
                extraTurnEarned = true;
                System.out.println("Earned an extra roll!");
                System.out.println("Rolling again...");
            } else {
                continueRolling = false;
            }
        }

        // Phase 2: Reordering (if applicable)
        boolean canReorder = earnedRolls.stream().anyMatch(r -> r == YunnoriRoll.YUT || r == YunnoriRoll.MO);
        if (canReorder && earnedRolls.size() > 1) {
            reorderRolls(earnedRolls);
        } else if (!earnedRolls.isEmpty()) {
            System.out.println(
                    "\nRolls to process: " + earnedRolls.stream().map(Enum::name).collect(Collectors.joining(", ")));
        } else {
            System.out.println("\nNo rolls earned this turn.");
        }

        // Phase 3: Processing Rolls - Only if there are rolls to process
        boolean teamBecameWinner = false; // Flag to indicate if the team won *during* processing rolls
        if (!earnedRolls.isEmpty()) {
            // processRolls now returns true if ANY piece was caught OR if the team wins
            boolean processOutcome = processRolls(team, earnedRolls);
            // Check if the team became winner immediately after processing rolls
            if (team.isWinner()) {
                teamBecameWinner = true;
            } else {
                // If not a winner yet, check if any piece was caught during processing
                caughtOpponentThisTurn = processOutcome; // Use the boolean returned by processRolls
            }
        }

        // Phase 4: Check for Extra Turn and handle Recursion
        if (teamBecameWinner) {
            // If the team became winner, stop any potential extra turns and signal win back
            // up the call stack.
            return true; // Signal that the team won
        } else if (extraTurnEarned || caughtOpponentThisTurn) {
            System.out.print("\n" + team + " earned an extra turn");
            if (extraTurnEarned && caughtOpponentThisTurn) {
                System.out.println(" (due to Yut/Mo and catching)!");
            } else if (extraTurnEarned) {
                System.out.println(" (due to Yut/Mo)!");
            } else if (caughtOpponentThisTurn) {
                System.out.println(" (due to catching)!");
            }

            // Recursive call: Play another turn for the same team
            // IMPORTANT: Return the result of the recursive call. This propagates the win
            // flag up.
            return playTurn(team);
        } else {
            // No extra turn and not a winner yet. This turn is fully complete.
            return false; // Signal that the team did NOT win in this turn
        }
    }

    // processRolls now returns boolean: true if ANY piece was caught, OR if the
    // team is now a winner
    private boolean processRolls(Team team, List<YunnoriRoll> rolls) {
        boolean caughtOpponentDuringProcessing = false;

        for (int i = 0; i < rolls.size(); i++) {
            YunnoriRoll currentRoll = rolls.get(i);
            System.out.println("\nProcessing roll " + (i + 1) + "/" + rolls.size() + ": " + currentRoll);

            List<Piece> playablePieces = team.getPlayablePieces();

            if (playablePieces.isEmpty()) {
                System.out.println(team + " has no pieces left to move for roll " + currentRoll + ".");
                continue;
            }

            List<Piece> piecesWithValidMove = new ArrayList<>();
            for (Piece piece : playablePieces) {
                if (board.isValidMoveStart(piece, currentRoll.getSteps())) {
                    piecesWithValidMove.add(piece);
                }
            }

            if (piecesWithValidMove.isEmpty()) {
                System.out.println(team + " has no piece that can make a valid move with " + currentRoll + " ("
                        + currentRoll.getSteps() + " steps).");
                continue;
            }

            Piece chosenPiece = selectPieceToMove(team, piecesWithValidMove);

            int oldPosition = chosenPiece.getCurrentPositionIndex();
            int targetPosition = board.calculateTargetPosition(chosenPiece, currentRoll.getSteps());

            chosenPiece.moveTo(targetPosition);

            // Announce the move result
            if (oldPosition != chosenPiece.getCurrentPositionIndex() || chosenPiece.isFinished()) {
                System.out.println(team + " " + chosenPiece.toString() + " moved from " + oldPosition + ".");

                // Check for catches at the *final* target position
                if (chosenPiece.getCurrentPositionIndex() > 0 && chosenPiece.getCurrentPositionIndex() < 30) {
                    List<Piece> caughtOpponentPieces = board.findOpponentPiecesAt(chosenPiece.getCurrentPositionIndex(),
                            team, teams);
                    if (!caughtOpponentPieces.isEmpty()) {
                        System.out.println(team + " caught opponent pieces at position "
                                + chosenPiece.getCurrentPositionIndex() + "!");
                        board.resetPiecesToStart(caughtOpponentPieces);
                        caughtOpponentDuringProcessing = true; // Set flag if any catch occurs
                    }
                }

                // Announce if the piece finished
                if (chosenPiece.isFinished()) {
                    System.out.println(team + " " + chosenPiece.toString() + " finished!");
                }
            }

            // Check immediately if the team is now a winner after ANY move within this
            // processing loop
            if (team.isWinner()) {
                // If the team wins, we can stop processing the remaining rolls immediately
                // and signal the win up the call stack.
                // This processRolls method should signal this win back.
                return true; // Signal that the team has won
            }

            board.printBoardState(teams);
        }
        // If the loop finishes without the team winning, return whether any catches
        // occurred.
        return caughtOpponentDuringProcessing;
    }

    private Piece selectPieceToMove(Team team, List<Piece> validPieces) {
        Piece chosenPiece = null;
        while (chosenPiece == null) {
            System.out.println("\n" + team + ", choose a piece to move:");
            for (int i = 0; i < validPieces.size(); i++) {
                System.out.println((i + 1) + ". " + validPieces.get(i));
            }
            System.out.print("Enter piece number: ");
            try {
                int choice = scanner.nextInt();
                if (choice > 0 && choice <= validPieces.size()) {
                    chosenPiece = validPieces.get(choice - 1);
                } else {
                    System.out.println("Invalid piece number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }
        scanner.nextLine();
        return chosenPiece;
    }

    private void reorderRolls(List<YunnoriRoll> rolls) {
        System.out.println("\nYou rolled: " + rolls.stream().map(Enum::name).collect(Collectors.joining(", ")));
        System.out.println("You can reorder these rolls.");

        List<YunnoriRoll> originalRolls = new ArrayList<>(rolls);
        List<YunnoriRoll> reordered = new ArrayList<>();
        boolean validInput = false;

        while (!validInput) {
            System.out.print("Enter the desired order separated by spaces (e.g., Do Yut Gae): ");
            String inputLine = scanner.nextLine().trim();
            String[] rollNames = inputLine.split("\\s+");

            reordered.clear();
            List<YunnoriRoll> tempOriginal = new ArrayList<>(originalRolls);

            boolean currentOrderValid = true;
            if (rollNames.length != originalRolls.size()) {
                System.out.println("Invalid number of rolls entered. Expected " + originalRolls.size() + ".");
                currentOrderValid = false;
            } else {
                for (String name : rollNames) {
                    YunnoriRoll roll = YunnoriRoll.fromName(name);
                    if (roll == null) {
                        System.out.println("Invalid roll name '" + name + "'.");
                        currentOrderValid = false;
                        break;
                    }
                    if (!tempOriginal.remove(roll)) {
                        System.out.println("Roll '" + name + "' was not in your original rolls or was already used.");
                        currentOrderValid = false;
                        break;
                    }
                    reordered.add(roll);
                }
                if (!tempOriginal.isEmpty()) {
                    System.out.println("Error in roll matching. Some rolls from the original list were not used.");
                    currentOrderValid = false;
                }
            }

            if (currentOrderValid) {
                rolls.clear();
                rolls.addAll(reordered);
                validInput = true;
                System.out.println("Rolls will be processed in this order: "
                        + rolls.stream().map(Enum::name).collect(Collectors.joining(", ")));
            } else {
                System.out.println("Please try again.");
            }
        }
    }
}