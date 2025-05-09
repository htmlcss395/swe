// Piece.java (Updated Finish Position)
// ... (Imports and class definition remain the same) ...

public class Piece {
    private int id;
    private int teamId;
    private int currentPositionIndex; // 0-30 mapping the board, + 30 for Finish
    private boolean isFinished;

    public Piece(int id, int teamId) {
        this.id = id;
        this.teamId = teamId;
        this.currentPositionIndex = 0; // Start position
        this.isFinished = false;
    }

    // Getters (remain public)
    public int getId() {
        return id;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getCurrentPositionIndex() {
        return currentPositionIndex;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isAtStart() {
        return currentPositionIndex == 0;
    }

    public void moveTo(int newPositionIndex) {
        this.currentPositionIndex = newPositionIndex;
        if (newPositionIndex == 30) { // Finish is now 30
            this.isFinished = true;
        }
    }

    public void reset() {
        System.out.println("Team " + (teamId + 1) + " Piece " + (id + 1) + " caught! Returning to start (0).");
        this.currentPositionIndex = 0;
        this.isFinished = false;
    }

    // Method to check if the piece can start a move from its current position with
    // the given steps
    public boolean canMove(int steps) {
        if (isFinished()) {
            return false; // Cannot move if finished
        }
        if (currentPositionIndex == 30) { // Already at finish (30)
            return false; // Cannot move forward or backward from finish
        }
        if (currentPositionIndex == 0 && steps == -1) {
            return true; // Special BackDo from start is allowed (moves to 20)
        }
        // For any other position > 0, BackDo is generally allowed unless it would go
        // "behind" 0 in a way the board doesn't map.
        // Our getPreviousPosition handles mapping back from 1 to 0 correctly, and other
        // spots to valid spots >= 0.
        if (steps == -1) {
            return currentPositionIndex > 0; // Can BackDo if not at 0
        }
        // Forward move (steps > 0) is always possible unless already at finish (30)
        if (steps > 0) {
            return true; // Can always move forward if not finished
        }

        return false; // steps is 0 or other invalid scenario
    }

    @Override
    public String toString() {
        if (isFinished) {
            return "P" + (id + 1) + "(F)";
        } else if (currentPositionIndex == 0) {
            return "P" + (id + 1) + "(S)";
        } else {
            return "P" + (id + 1) + "(" + currentPositionIndex + ")";
        }
    }
}