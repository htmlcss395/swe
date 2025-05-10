package src.yunnori;

public class Piece {
    private int id;
    private int teamId;
    private int currentPositionIndex; // 0-31 mapping the board, + 31 for Finish
    private boolean isFinished;

    public Piece(int id, int teamId) {
        this.id = id;
        this.teamId = teamId;
        this.currentPositionIndex = 0; // Start position
        this.isFinished = false;
    }

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
        if (newPositionIndex == 31) { // Finish is now 31
            this.isFinished = true;
        }
    }

    public void reset() {
        this.currentPositionIndex = 0;
        this.isFinished = false;
    }

    public boolean canMove(int steps) {
        if (isFinished()) {
            return false;
        }
        if (currentPositionIndex == 31) {
            return false;
        } // Already at finish (31)
        if (currentPositionIndex == 0 && steps == -1) {
            return true;
        } // Special BackDo from start
        if (steps == -1) {
            return currentPositionIndex > 0;
        } // Can BackDo if not at 0
        if (steps > 0) {
            return true;
        } // Can always move forward if not finished
        return false;
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