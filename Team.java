
// Team.java (Bring back finishedPiecesCount counter)
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors; // Not needed if only using counter

// Represents a team
public class Team {
    private int id;
    private List<Piece> pieces;
    private int finishedPiecesCount; // Track finished pieces count

    public Team(int id, int numPieces) {
        this.id = id;
        this.pieces = new ArrayList<>();
        for (int i = 0; i < numPieces; i++) {
            pieces.add(new Piece(i, id));
        }
        this.finishedPiecesCount = 0; // Initialize count
    }

    public int getId() {
        return id;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    // Use the tracked counter
    public int getFinishedPiecesCount() {
        return finishedPiecesCount;
    }

    // Call this when a piece finishes
    public void pieceFinished() {
        this.finishedPiecesCount++;
    }

    public boolean isWinner() {
        // Check if the tracked finished count equals the total number of pieces
        return finishedPiecesCount == pieces.size();
    }

    public List<Piece> getPiecesAt(int positionIndex) {
        List<Piece> piecesAtPos = new ArrayList<>();
        for (Piece piece : pieces) {
            if (!piece.isFinished() && piece.getCurrentPositionIndex() == positionIndex) {
                piecesAtPos.add(piece);
            }
        }
        return piecesAtPos;
    }

    public List<Piece> getPlayablePieces() {
        List<Piece> playable = new ArrayList<>();
        for (Piece piece : pieces) {
            if (!piece.isFinished()) {
                playable.add(piece);
            }
        }
        return playable;
    }

    @Override
    public String toString() {
        return "Team " + (id + 1);
    }
}