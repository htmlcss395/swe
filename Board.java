
// Board.java (Remove internal Center rule prints)
import java.util.ArrayList;
import java.util.List;
// Scanner is not needed for Board logic now
// import java.util.Scanner;

// Handles the Yunnori board logic and piece movements
public class Board {
    // No longer needs scanner
    public Board() {
    }

    // getPreviousPosition method (remains the same with 0-30 mapping and
    // simplification for BackDo from 23)
    private int getPreviousPosition(int currentPos) {
        if (currentPos == 0)
            return 0;
        if (currentPos == 30)
            return 29;

        if (currentPos == 29)
            return 28;
        if (currentPos == 28)
            return 23;

        if (currentPos == 23) {
            return 29;
        }

        if (currentPos == 15)
            return 25;
        if (currentPos == 25)
            return 24;
        if (currentPos == 24)
            return 23;

        if (currentPos == 22)
            return 21;
        if (currentPos == 21)
            return 5;

        if (currentPos == 27)
            return 26;
        if (currentPos == 26)
            return 10;

        return currentPos - 1;
    }

    // calculateTargetPosition method (Remove internal Center rule prints)
    public int calculateTargetPosition(Piece piece, int steps) {
        int originalPos = piece.getCurrentPositionIndex();
        int currentSimulationPos = originalPos;

        if (piece.isFinished()) {
            return 30;
        }
        if (steps == -1) {
            if (originalPos == 0) {
                System.out.println("Piece at Start (0) moves with Back Do to position 20."); // Keep special rule print
                return 20;
            }
            return getPreviousPosition(originalPos);
        }

        for (int i = 0; i < steps; i++) {
            if (currentSimulationPos == 30) {
                break;
            }

            int nextPosAfterOneStep = -1;

            // --- Center (23) transition: Based on ORIGINAL start position ---
            if (currentSimulationPos == 23) {
                if (originalPos == 23) {
                    nextPosAfterOneStep = 28; // Started at 23 -> towards Finish
                } else {
                    nextPosAfterOneStep = 24; // Landed on 23 mid-roll -> towards 15
                }
            }
            // --- Other standard transitions ---
            else if (currentSimulationPos == 0)
                nextPosAfterOneStep = 1;
            // Start roll at Corner 1 (5) or Corner 2 (10) rule (only applies on first step,
            // moves to shortcut)
            else if (i == 0 && originalPos == 5) {
                nextPosAfterOneStep = 21;
            } else if (i == 0 && originalPos == 10) {
                nextPosAfterOneStep = 26;
            }
            // End of Outer (20)
            else if (currentSimulationPos == 20)
                nextPosAfterOneStep = 30;
            // End of Shortcut 1 (22) -> Center (23)
            else if (currentSimulationPos == 22)
                nextPosAfterOneStep = 23;
            // End of Shortcut 2 (27) -> Center (23)
            else if (currentSimulationPos == 27)
                nextPosAfterOneStep = 23;
            // End of Path from 23 towards 15 (25) -> Corner 3 (15)
            else if (currentSimulationPos == 25)
                nextPosAfterOneStep = 15;
            // End of Common Path towards Finish (29) -> Finish (30)
            else if (currentSimulationPos == 29)
                nextPosAfterOneStep = 30;
            // --- Standard linear movement ---
            else {
                nextPosAfterOneStep = currentSimulationPos + 1;
            }

            currentSimulationPos = nextPosAfterOneStep;
        }

        if (currentSimulationPos > 30) {
            currentSimulationPos = 30;
        }

        return currentSimulationPos;
    }

    // findOpponentPiecesAt, resetPiecesToStart, printBoardState, isValidMoveStart
    // methods
    // (These remain the same, using Finish = 30)
    // ... (copy/paste from previous response) ...
    public List<Piece> findOpponentPiecesAt(int targetPosition, Team currentPlayerTeam, List<Team> allTeams) {
        List<Piece> caughtPieces = new ArrayList<>();
        if (targetPosition == 0 || targetPosition == 30) {
            return caughtPieces;
        }

        for (Team team : allTeams) {
            if (team.getId() != currentPlayerTeam.getId()) {
                caughtPieces.addAll(team.getPiecesAt(targetPosition));
            }
        }
        return caughtPieces;
    }

    public void resetPiecesToStart(List<Piece> piecesToReset) {
        for (Piece piece : piecesToReset) {
            piece.reset();
        }
    }

    public void printBoardState(List<Team> teams) {
        System.out.println("\n--- Current Board State ---");
        for (Team team : teams) {
            System.out.print(team + ": ");
            for (Piece piece : team.getPieces()) {
                System.out.print(piece + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------------");
    }

    public boolean isValidMoveStart(Piece piece, int steps) {
        return piece.canMove(steps);
    }
}