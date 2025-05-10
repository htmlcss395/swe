import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Keep if used for other things, otherwise remove

public class Team {
    private int id;
    private List<Piece> pieces;
    // removed finishedPiecesCount field and pieceFinished() method
    // private int finishedPiecesCount;

    public Team(int id, int numPieces) {
        this.id = id;
        this.pieces = new ArrayList<>();
        for (int i = 0; i < numPieces; i++) {
            pieces.add(new Piece(i, id));
        }
        // removed initialization
        // this.finishedPiecesCount = 0;
    }

    public int getId() {
        return id;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public int getFinishedPiecesCount() {
        // Always recalculate directly from pieces
        return (int) pieces.stream().filter(Piece::isFinished).count();
    }

    // removed pieceFinished() method
    // public void pieceFinished() { }

    public boolean isWinner() {
        return getFinishedPiecesCount() == pieces.size();
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