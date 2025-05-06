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
}
