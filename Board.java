public class Board {

    public static final int BOARD_SIZE = 29; // 0 to 29, 30 is finish line
    Piece[][] teams;

    public Board(int numTeams, int numPieces) {
        this.teams = new Piece[numTeams][numPieces];
        // Initialize all pieces as 0
        for (int i = 0; i < numTeams; i++) {
            for (int j = 0; j < numPieces; j++) {
                teams[i][j] = new Piece();
            }
        }
    }

    public void movePiece(int teamIdx, int pieceIdx, int steps){
        Piece p = teams[teamIdx][pieceIdx];

        //move
        p.move(steps);


        //
        for (int t = 0; t < teams.length; t++){
            if (t == teamIdx) continue;
            for (Piece other : teams[t])
            {
                if (other.getPosition() == p.getPosition()){
                    other.reset();
                }
            }
        }

        for (Piece buddy : teams[teamIdx]) {
            if (buddy != p && buddy.getPosition() == p.getPosition()) {
                buddy.groupWith(p);
            }
        }


    }

    public enum BranchPath {
        CENTRAL,
        OUTER,
        PENTAGON_INNER1,
        PENTAGON_INNER2,
        HEXAGON_INNER1,
        HEXAGON_INNER2,
        HEXAGON_INNER3
    }
    // Determining which direction the piece will move at the junction
    private void handleBranching(Piece p) {
        int pos = p.getPosition();
        boolean justStopped = p.hasJustStoppedAt(pos);

        if (pos == 5 && p.hasJustStoppedAt(pos)){
            p.setBranchPath(BranchPath.CENTRAL);

        }

        else if (pos == 10 && justStopped){
            p.setBranchPath(BranchPath.OUTER);
        }

        else if (pos == 7 && justStopped){
            p.setBranchPath(BranchPath.PENTAGON_INNER1);
        }

        else if (pos == 17 && justStopped) {
            p.setBranchPath(BranchPath.PENTAGON_INNER2);
        }

        else if (pos == 6 && justStopped) {
            p.setBranchPath(BranchPath.HEXAGON_INNER1);
        }

        else if (pos == 14 && justStopped) {
            p.setBranchPath(BranchPath.HEXAGON_INNER2);
        }

        else if (pos == 22 && justStopped) {
            p.setBranchPath(BranchPath.HEXAGON_INNER3);
        }


        //todo: if p.hasMultipleStops 구현해오기

    }


        // Get the positions of pieces for all teams (for display purposes)
    public void displayBoard() {
        for (int i = 0; i < teams.length; i++) {
            System.out.print("Team " + (i + 1) + " positions: ");
            for (int j = 0; j < teams[i].length; j++) {
                System.out.print(teams[i][j].getPosition() + " ");
            }
            System.out.println();
        }
    }



    // Check whether any team has completed the game
    public boolean isGameOver() {
        for (int i = 0; i < teams.length; i++) {
            if (allPiecesFinished(teams[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean allPiecesFinished(Piece[] team) {
        for (Piece piece : team) {
            if (piece.getPosition() <= BOARD_SIZE) {
                return false;
            }
        }
        return true;
    }


    // When the game ends, return the team that completed all of its pieces first, otherwise return -1.
    public int getWinningTeam() {
        for (int i = 0; i < teams.length; i++) {
            if (allPiecesFinished(teams[i])) {
                return i;
            }
        }
        return -1;
    }



}
