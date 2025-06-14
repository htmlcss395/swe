package yunnori.core;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private int id;
    private int teamId;
    public int currentPositionIndex;
    private boolean isFinished;

    // --- New fields for grouping ---
    private List<Piece> stackedPieces; // Pieces this piece is carrying (if it's a leader)
    private Piece groupLeader; // The leader of the group this piece belongs to (if not the leader itself)

    public Piece(int id, int teamId) {
        this.id = id;
        this.teamId = teamId;
        this.currentPositionIndex = 0; // Start position
        this.isFinished = false;
        this.stackedPieces = new ArrayList<>();
        this.groupLeader = null;
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
        return currentPositionIndex == 0 && !isFinished;
    }

    // --- New/modified methods for grouping ---
    public boolean isGroupLeader() {
        return !stackedPieces.isEmpty();
    }

    public boolean isStacked() {
        return groupLeader != null;
    }

    public Piece getGroupLeader() {
        return groupLeader;
    }

    public List<Piece> getStackedPieces() {
        return new ArrayList<>(stackedPieces); // Return a copy for safety
    }

    public int getGroupSize() {
        return 1 + stackedPieces.size(); // Itself + carried pieces
    }

    public void addToStack(Piece pieceToAdd) {
        if (pieceToAdd == this || pieceToAdd.getTeamId() != this.teamId || pieceToAdd.isStacked()) {
            // Cannot stack itself, an opponent, or a piece already stacked by someone else.
            // If pieceToAdd is already stacked, its current leader should be targeted for merging if desired.
            return;
        }
        if (isStacked()) {
            System.err.println("Error: " + this.toString() + " cannot lead a new stack as it's already stacked by "
                    + this.groupLeader.toString());
            return; // This piece is already led, it cannot lead others.
        }

        // If pieceToAdd is already a leader, merge its stack into this one.
        if (pieceToAdd.isGroupLeader()) {
            for (Piece subPiece : pieceToAdd.getStackedPieces()) {
                if (!this.stackedPieces.contains(subPiece) && subPiece != this) {
                    this.stackedPieces.add(subPiece);
                    subPiece.groupLeader = this; // subPiece is now led by 'this'
                }
            }
            pieceToAdd.stackedPieces.clear(); // pieceToAdd is no longer a leader of its old stack
        }

        // Add pieceToAdd itself to the stack, if not already (could happen if it was a leader and its stack was merged)
        if (!this.stackedPieces.contains(pieceToAdd)) {
            this.stackedPieces.add(pieceToAdd);
        }
        pieceToAdd.groupLeader = this; // pieceToAdd is now led by 'this'

        // Synchronize all stacked pieces (including newly added ones)
        for (Piece stacked : this.stackedPieces) {
            stacked.currentPositionIndex = this.currentPositionIndex;
            stacked.isFinished = this.isFinished;
        }
    }

    public void removeFromStack(Piece pieceToRemove) {
        if (stackedPieces.remove(pieceToRemove)) {
            pieceToRemove.groupLeader = null;
        }
    }

    public void leaveGroup() {
        if (this.groupLeader != null) {
            this.groupLeader.removeFromStack(this); // This will set this.groupLeader to null via removeFromStack
        }
    }

    public void moveTo(int newPositionIndex, Board board) {
        this.currentPositionIndex = newPositionIndex;
        if (newPositionIndex == board.getFinishPointIndex())
            this.isFinished = true;

        if (isGroupLeader()) {
            for (Piece stackedPiece : stackedPieces) {
                stackedPiece.currentPositionIndex = this.currentPositionIndex;
                stackedPiece.isFinished = this.isFinished;
            }
        }
    }

    public void reset() {
        leaveGroup(); // If stacked, leave its current group.

        // If this piece was a leader, its former stacked pieces become independent and need reset.
        // Create a copy for safe iteration as their reset() will modify this.stackedPieces via leaveGroup().
        List<Piece> piecesFormerlyStacked = new ArrayList<>(this.stackedPieces);
        this.stackedPieces.clear(); // Crucial to clear before resetting children to avoid cycles/errors

        for (Piece formerlyStackedPiece : piecesFormerlyStacked) {
            formerlyStackedPiece.groupLeader = null; // Explicitly detach
            formerlyStackedPiece.reset(); // Reset them to start individually
        }

        this.currentPositionIndex = 0;
        this.isFinished = false;
        this.groupLeader = null;
    }

    public boolean canMove(int steps, Board board) {
        if (isFinished()) {
            return false;
        }
        if (isStacked()) { // If this piece is part of a group and not the leader, it cannot move independently
            return false;
        }
        // Update the logic to check whether the game is finished or not
        if (currentPositionIndex == board.getFinishPointIndex())
            return false;

        if (currentPositionIndex == 0 && steps == -1)
            return true;
        if (steps == -1)
            return currentPositionIndex > 0; // Can BackDo if not at 0
        if (steps > 0)
            return true;
        return false;
    }

    @Override
    public String toString() {
        String base;
        if (isFinished) {
            base = "P" + (id + 1) + "(F)";
        } else if (currentPositionIndex == 0 && !isFinished) { // Check !isFinished for start pos
            base = "P" + (id + 1) + "(S)";
        } else {
            base = "P" + (id + 1) + "(" + currentPositionIndex + ")";
        }
        if (isGroupLeader()) {
            base += "x" + getGroupSize();
        }
        // No need for "[S]" if isStacked(), as they won't be directly displayed or selected usually.
        // Leaders' toString will represent the group.
        return base;
    }
}