
// Game.java (Call team.pieceFinished(), remove comments)
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
        this.board = new Board();
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

            playTurn(currentTeam);

            if (currentTeam.isWinner()) {
                System.out.println("\n**************************************");
                System.out.println(currentTeam + " wins the game!");
                System.out.println("**************************************");
                gameWon = true;
            } else {
                if (!gameWon) {
                    System.out.println("\nEnd of " + currentTeam + "'s turn.");
                    currentPlayerIndex = (currentPlayerIndex + 1) % numTeams;
                }
            }
        }
        System.out.println("\n--- Game Over ---");
    }

    private void playTurn(Team team) {
        boolean extraTurnEarned = false;
        boolean caughtOpponentThisTurn = false;

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

        boolean canReorder = earnedRolls.stream().anyMatch(r -> r == YunnoriRoll.YUT || r == YunnoriRoll.MO);
        if (canReorder && earnedRolls.size() > 1) {
            reorderRolls(earnedRolls);
        } else if (!earnedRolls.isEmpty()) {
            System.out.println(
                    "\nRolls to process: " + earnedRolls.stream().map(Enum::name).collect(Collectors.joining(", ")));
        } else {
            System.out.println("\nNo rolls earned this turn.");
        }

        if (!earnedRolls.isEmpty()) {
            caughtOpponentThisTurn = processRolls(team, earnedRolls);
        }

        if (extraTurnEarned || caughtOpponentThisTurn) {
            System.out.print("\n" + team + " earned an extra turn");
            if (extraTurnEarned && caughtOpponentThisTurn) {
                System.out.println(" (due to Yut/Mo and catching)!");
            } else if (extraTurnEarned) {
                System.out.println(" (due to Yut/Mo)!");
            } else if (caughtOpponentThisTurn) {
                System.out.println(" (due to catching)!");
            }

            playTurn(team); // Recursive call
        } else {
            System.out.println("\nEnd of " + team + "'s turn.");
        }
    }

    private boolean processRolls(Team team, List<YunnoriRoll> rolls) {
        boolean caughtOpponentThisTurn = false;

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

            if (oldPosition != chosenPiece.getCurrentPositionIndex() || chosenPiece.isFinished()) {
                System.out.println(team + " " + chosenPiece.toString() + " moved from " + oldPosition + ".");

                // Check for catches at the final target position
                if (chosenPiece.getCurrentPositionIndex() > 0 && chosenPiece.getCurrentPositionIndex() < 30) {
                    List<Piece> caughtOpponentPieces = board.findOpponentPiecesAt(chosenPiece.getCurrentPositionIndex(),
                            team, teams);
                    if (!caughtOpponentPieces.isEmpty()) {
                        System.out.println(team + " caught opponent pieces at position "
                                + chosenPiece.getCurrentPositionIndex() + "!");
                        board.resetPiecesToStart(caughtOpponentPieces);
                        caughtOpponentThisTurn = true;
                    }
                }

                // Announce if the piece finished AND update the team's finished count
                if (chosenPiece.isFinished()) {
                    System.out.println(team + " " + chosenPiece.toString() + " finished!");
                    team.pieceFinished(); // CALL THE METHOD HERE
                }

            }

            board.printBoardState(teams);
        }
        return caughtOpponentThisTurn;
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