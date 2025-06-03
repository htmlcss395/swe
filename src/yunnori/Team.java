package src.yunnori;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int id;
    private List<Piece> pieces; // This list contains ALL original pieces for the team

    public Team(int id, int numPieces) {
        this.id = id;
        this.pieces = new ArrayList<>();
        for (int i = 0; i < numPieces; i++) {
            pieces.add(new Piece(i, id));
        }
    }

    public int getId() {
        return id;
    }

    public List<Piece> getPieces() {
        return pieces; // Returns all pieces, including those stacked
    }

    public int getFinishedPiecesCount() {
        // Counts all original pieces that are finished, regardless of grouping
        return (int) pieces.stream().filter(Piece::isFinished).count();
    }

    public boolean isWinner() {
        return getFinishedPiecesCount() == pieces.size();
    }

    /**
     * Gets pieces (leaders or individuals) at a specific position.
     * These are the pieces that can be "clicked" or interacted with directly at that spot.
     */
    public List<Piece> getInteractivePiecesAt(int positionIndex) {
        List<Piece> piecesAtPos = new ArrayList<>();
        for (Piece piece : pieces) {
            // Only consider pieces that are physically at this position
            // AND are not being carried by another (i.e., they are leaders or standalone
            // individuals).
            if (!piece.isFinished() && piece.getCurrentPositionIndex() == positionIndex && !piece.isStacked()) {
                piecesAtPos.add(piece);
            }
        }
        return piecesAtPos;
    }

    /**
     * Gets pieces that can currently make a move.
     * These are leaders or individual pieces that are not finished.
     */
    public List<Piece> getPlayablePieces() {
        List<Piece> playable = new ArrayList<>();
        for (Piece piece : this.pieces) {
            // A piece is playable if it's not finished AND it's not being carried by
            // another.
            if (!piece.isFinished() && !piece.isStacked()) {
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