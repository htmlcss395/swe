import java.util.ArrayList;
import java.util.List;

class Piece {
    /*
     * 0 = off board
     * [1, 29] = inside the board
     */
    private int position = 0;
 //   private Path currentPath;
 private Board.BranchPath currentPath;
    private boolean justStopped;

    private Piece groupLeader = null;
    private final List<Piece> groupMembers = new ArrayList<>();


    public void move(int steps) {
        position += steps;
        if (position >= 30)
            position = 30; // Piece has to go off the board to win.
    }




    public int getPosition() {
        return position;
    }

    public void reset() { position = 0; }
    public void groupWith(Piece leader) {
        if (groupLeader != null) {
            groupLeader.groupMembers.remove(this);
        }

        this.groupLeader = leader;
        if (!leader.groupMembers.contains(this)) {
            leader.groupMembers.add(this);
        }

        this.position = leader.position;
        this.currentPath = leader.currentPath;
        this.justStopped = leader.justStopped;
    }


    public void setBranchPath(Board.BranchPath path) {
        this.currentPath = path;
    }

    public boolean hasJustStoppedAt(int pos) {
        return justStopped && this.position == pos;
    }

//


}