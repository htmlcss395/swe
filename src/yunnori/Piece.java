package src.yunnori;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private int id;
    private int teamId;
    public int currentPositionIndex; // 0-31 mapping the board, + 31 for Finish

    private int[] prevPositions = new int[2]; // [0]=직전, [1]=그 전

      private int prevPositionIndex = 0;
    private boolean isFinished;
    // --- New fields for grouping ---
    private List<Piece> stackedPieces; // Pieces this piece is carrying (if it's a leader)
    private Piece groupLeader; // The leader of the group this piece belongs to (if not the leader itself)

    private Integer centerExitNext = null;   // 35에 서서 ‘다음에 34/33 …’ 으로 나갈 곳
    public Integer getCenterExitNext()        { return centerExitNext; }
    public void    setCenterExitNext(Integer i){ centerExitNext = i;   }

    public Piece(int id, int teamId) {
        this.id = id;
        this.teamId = teamId;
        this.currentPositionIndex = 0; // Start position
        this.isFinished = false;
        this.stackedPieces = new ArrayList<>();
        this.groupLeader = null;
        prevPositions[0] = 0;
        prevPositions[1] = 0;
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
            // If pieceToAdd is already stacked, its current leader should be targeted for
            // merging if desired.
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

        // Add pieceToAdd itself to the stack, if not already (could happen if it was a
        // leader and its stack was merged)
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

    public void moveTo(int newPositionIndex) {
        System.out.printf("[DEBUG] moveTo: id=%d, from %d → %d (prev was %d)\n",
                id, this.currentPositionIndex, newPositionIndex, this.prevPositionIndex);
        prevPositions[1] = prevPositions[0];
        prevPositions[0] = this.currentPositionIndex;

        this.prevPositionIndex = this.currentPositionIndex;
        this.currentPositionIndex = newPositionIndex;
        if (newPositionIndex == 31) { // Finish is 31
            this.isFinished = true;
        }

        if (isGroupLeader()) {
            for (Piece stackedPiece : stackedPieces) {
                stackedPiece.currentPositionIndex = this.currentPositionIndex;
                stackedPiece.isFinished = this.isFinished;
            }
        }
        debugPrint();
    }

    public void setCurrentPositionIndex(int idx) {
        System.out.printf("[DEBUG] setCurrentPositionIndex: id=%d, from %d → %d (prev was %d)\n",
                id, this.currentPositionIndex, idx, this.prevPositionIndex);
        prevPositions[1] = prevPositions[0];
        prevPositions[0] = this.currentPositionIndex;
        this.prevPositionIndex = this.currentPositionIndex;
        this.currentPositionIndex = idx;
        if (isGroupLeader()) {
            for (Piece stacked : this.stackedPieces) {
                stacked.currentPositionIndex = idx;
            }
        }
        debugPrint();
    }



    public void reset() {
        leaveGroup(); // If stacked, leave its current group.

        // If this piece was a leader, its former stacked pieces become independent and
        // need reset.
        // Create a copy for safe iteration as their reset() will modify
        // this.stackedPieces via leaveGroup().
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

    public boolean canMove(int steps) {
        if (isFinished()) {
            return false;
        }
        if (isStacked()) { // If this piece is part of a group and not the leader, it cannot move
            // independently
            return false;
        }
        // Original logic for leaders or individual pieces
        if (currentPositionIndex == 31)
            return false;
        if (currentPositionIndex == 0 && steps == -1)
            return true;
        if (steps == -1)
            return currentPositionIndex > 0; // Can BackDo if not at 0
        if (steps > 0)
            return true;
        return false;
    }
    public void setPrevPositionIndex(int idx) {
        this.prevPositionIndex = idx;
    }

    public int getPrevPositionIndex() {
        return this.prevPositionIndex;

    }
    public int getPrevPositionIndexs() {
        return prevPositions[0];
    }

    public int getPrevPrevPositionIndex() {
        return prevPositions[1];
    }

    public void debugPrint() {
        System.out.printf("Piece[%d]: pos=%d, prev=%d, finished=%b\n", id, currentPositionIndex, prevPositionIndex, isFinished);
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
        // No need for "[S]" if isStacked(), as they won't be directly displayed or
        // selected usually.
        // Leaders' toString will represent the group.
        return base;
    }
}