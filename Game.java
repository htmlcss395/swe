import java.util.Scanner;

public class Game {
    Piece[][] teams;
    int numTeams;
    boolean[] teamTurns;
    int numPieces;
    Board board;
    boolean isTest; // choose between manual or random roll

    public Game(int numTeams, int numPieces, boolean isTest) {
        this.numTeams = numTeams;
        this.numPieces = numPieces;
        this.teams = new Piece[numTeams][numPieces];
        this.teamTurns = new boolean[numTeams];
        this.board = new Board(numTeams, numPieces);
        this.isTest = isTest;

        // Initialize the pieces for each team
        for (int i = 0; i < numTeams; i++) {
            for (int j = 0; j < numPieces; j++) {
                teams[i][j] = new Piece();
            }
        }

        // Initially, Team 1 starts
        teamTurns[0] = true;
    }

    public void startGame(Scanner scanner) {
        while (!board.isGameOver()) {
            int currentTeam = getCurrentTeam();
            System.out.println("Team " + (currentTeam + 1) + "'s turn!");
            board.displayBoard();

            // Roll Yut sticks and get the sequence
            int[] rolls = YutThrow.throwSticks(scanner, isTest);
            System.out.println("Rolled sequence: ");
            for (int roll : rolls) {
                System.out.print(roll + " ");
            }
            System.out.println();

            // Assign each roll to a piece
            for (int roll : rolls) {
                // Let the player choose which piece to move
                System.out.print("Choose piece to move (1-" + numPieces + "): ");
                int pieceIndex = scanner.nextInt() - 1; // Convert to 0-based index
                if (pieceIndex >= 0 && pieceIndex < numPieces) {
                    teams[currentTeam][pieceIndex].move(roll);
                } else {
                    System.out.println("Invalid piece selection. Try again.");
                    // Since the player is choosing for each roll, we handle invalid input
                    break; // Exit the current loop and retry
                }
            }

            // Check if they get an extra turn (Yut or Mo)
            boolean extraTurn = false;
            for (int roll : rolls) {
                if (YutThrow.isExtraTurn(roll)) {
                    extraTurn = true;
                    break;
                }
            }

            // Switch turns if no extra turn
            if (!extraTurn) {
                switchTurn();
            }
        }

        // Declare the winner
        System.out.println("Team " + (getCurrentTeam() + 1) + " wins!");
    }

    private int getCurrentTeam() {
        for (int i = 0; i < numTeams; i++) {
            if (teamTurns[i]) {
                return i;
            }
        }
        return -1; // This should never happen
    }

    private void switchTurn() {
        for (int i = 0; i < numTeams; i++) {
            teamTurns[i] = !teamTurns[i]; // Switch the current team to false, and the next to true
            if (teamTurns[i]) {
                break;
            }
        }
    }
}
