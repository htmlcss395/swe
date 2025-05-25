package src.yunnori;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel {
    private Board board;
    private List<Team> teams;
    private int pieceSize = 25; // scaled from 30
    public int pointSize = 20; // scaled from 15

    private YunnoriGUI guiController;

    private Piece selectedPiece = null;
    private List<Integer> validMoveTargets = null;

    public BoardPanel(Board board, List<Team> teams, YunnoriGUI guiController) {
        this.board = board;
        this.teams = teams;
        this.guiController = guiController;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBoardClick(e);
            }
        });
    }

    public void setHighlight(Piece selectedPiece, List<Integer> validMoveTargets) {
        this.selectedPiece = selectedPiece;
        this.validMoveTargets = validMoveTargets;
        repaint();
    }

    private void handleBoardClick(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();

        for (int i = 0; i <= 31; i++) { // Iterate through all board points
            Board.BoardPoint pointCoords = board.getBoardPoint(i);
            if (pointCoords != null) {
                int pointClickArea = pointSize * 3; // Increased click area for easier selection
                int pointDrawX = pointCoords.x - pointClickArea / 2;
                int pointDrawY = pointCoords.y - pointClickArea / 2;
                int buffer = 10;

                if (clickX >= pointDrawX - buffer && clickX <= pointDrawX + pointClickArea + buffer &&
                        clickY >= pointDrawY - buffer && clickY <= pointDrawY + pointClickArea + buffer) {

                    List<Piece> interactivePiecesAtThisPoint = new ArrayList<>();
                    for (Team team : teams) {
                        // Get leaders or individuals at this visual point
                        interactivePiecesAtThisPoint.addAll(team.getInteractivePiecesAt(i));
                    }

                    if (!interactivePiecesAtThisPoint.isEmpty()) {
                        guiController.pieceStackClicked(interactivePiecesAtThisPoint, clickX, clickY);
                        return; // Click handled
                    } else {
                        // Clicked on an empty board point, let guiController handle if needed
                        // (though current boardClicked is more for deselection)
                        // If we stop iterating here, clicks on empty points with no pieces nearby won't
                        // deselect.
                        // So, we might want to continue checking or call boardClicked only if no piece
                        // stack is found after checking all points.
                        // For now, if a point is clicked, and it has pieces, we handle it. If not, the
                        // click falls through.
                        // This means guiController.boardClicked might be called if the loop finishes
                        // without finding pieces.
                        // Let's break if a point is identified, piece or not, to avoid multiple point
                        // processing.
                        // The original logic was to break; if pieces were found.
                        // If no pieces, it would fall through to boardClicked.
                        // This seems fine. If point is empty, this loop doesn't find pieces, falls to
                        // boardClicked.
                        break; // Found the clicked point, whether it has pieces or not.
                               // If it had pieces, it's handled. If not, loop continues or exits.
                               // This break ensures we only process one "board point" per click.
                    }
                }
            }
        }
        // If loop completes without returning (i.e. no interactive piece found at any
        // specific point that matched click area)
        guiController.boardClicked(clickX, clickY); // General board click (e.g., for deselection)
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < 20; i++)
            autoDrawLine(g2d, i, i + 1);

        autoDrawLine(g2d, 0, 31);
        autoDrawLine(g2d, 5, 21);
        autoDrawLine(g2d, 21, 22);
        autoDrawLine(g2d, 22, 23);
        autoDrawLine(g2d, 10, 26);
        autoDrawLine(g2d, 26, 27);
        autoDrawLine(g2d, 27, 23);
        autoDrawLine(g2d, 23, 24);
        autoDrawLine(g2d, 24, 25);
        autoDrawLine(g2d, 25, 15);
        autoDrawLine(g2d, 23, 28);
        autoDrawLine(g2d, 28, 29);
        autoDrawLine(g2d, 29, 30);
        autoDrawLine(g2d, 30, 0); // 30 to Finish (31)

        for (int i = 0; i <= 31; i++) {
            Board.BoardPoint point = board.getBoardPoint(i);
            if (point != null) {
                if (validMoveTargets != null && validMoveTargets.contains(i)) {
                    g2d.setColor(Color.YELLOW);
                    int highlightSize = pointSize * 3;
                    g2d.fillOval(point.x - highlightSize / 2, point.y - highlightSize / 2, highlightSize,
                            highlightSize);
                }

                int currentPointSize = pointSize;
                Color pointColor = Color.BLACK;

                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 23) {
                    currentPointSize = pointSize * 2;
                }
                // 참먹이 1; Just before the finish point
                else if (i == 20) {
                    currentPointSize = pointSize * 2;
                    g2d.setColor(Color.MAGENTA);
                    g2d.drawOval(point.x - currentPointSize / 2, point.y - currentPointSize / 2, currentPointSize,
                            currentPointSize);
                    pointColor = Color.MAGENTA;
                }
                // 참먹이 2; Just before the finish point
                else if (i == 30) {
                    currentPointSize = pointSize * 2;
                    g2d.setColor(Color.MAGENTA);
                    g2d.drawOval(point.x - currentPointSize / 2, point.y - currentPointSize / 2, currentPointSize,
                            currentPointSize);
                    pointColor = Color.MAGENTA;
                }
                // Finish point
                else if (i == 31) {
                    currentPointSize = pointSize * 2;
                    g2d.setColor(Color.CYAN);
                    g2d.drawOval(point.x - currentPointSize / 2, point.y - currentPointSize / 2, currentPointSize,
                            currentPointSize);
                    pointColor = Color.CYAN;
                }
                g2d.setColor(pointColor);
                g2d.fillOval(point.x - currentPointSize / 2, point.y - currentPointSize / 2, currentPointSize,
                        currentPointSize);

                // Label Start
                if (i == 0) {
                    g2d.setColor(Color.BLACK);
                    Font originalFont = g2d.getFont();
                    g2d.setFont(new Font("Arial", Font.BOLD, 15));
                    String text = "시작";
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(text, point.x + fm.stringWidth(text) / 4,
                            point.y - currentPointSize + fm.getAscent() / 2);
                    g2d.setFont(originalFont);
                }
                // Label 참먹이1
                else if (i == 20) {
                    g2d.setColor(Color.BLACK);
                    Font originalFont = g2d.getFont();
                    g2d.setFont(new Font("Arial", Font.BOLD, 11));
                    String text = "참먹이1";
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(text, point.x - fm.stringWidth(text) / 2,
                            point.y + currentPointSize + fm.getAscent() / 2);
                    g2d.setFont(originalFont);
                }
                // Label 참먹이2
                else if (i == 30) {
                    g2d.setColor(Color.BLACK);
                    Font originalFont = g2d.getFont();
                    g2d.setFont(new Font("Arial", Font.BOLD, 11));
                    String text = "참먹이2";
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(text, point.x - fm.stringWidth(text) / 2,
                            point.y - currentPointSize + fm.getAscent() / 2);
                    g2d.setFont(originalFont);
                }
                // Label Finish
                else if (i == 31) {
                    g2d.setColor(Color.BLACK);
                    Font originalFont = g2d.getFont();
                    g2d.setFont(new Font("Arial", Font.BOLD, 15));
                    String text = "끝";
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(text, point.x - fm.stringWidth(text) / 2,
                            point.y + currentPointSize + fm.getAscent() / 2);
                    g2d.setFont(originalFont);
                }
            }
        }

        for (Team team : teams) {
            Color teamColor = Color.DARK_GRAY;
            if (team.getId() == 0)
                teamColor = Color.RED;
            else if (team.getId() == 1)
                teamColor = Color.BLUE;
            else if (team.getId() == 2)
                teamColor = Color.GREEN;
            else if (team.getId() == 3)
                teamColor = Color.ORANGE;

            // Iterate over *all* pieces to find leaders or unstacked pieces to draw
            for (Piece piece : team.getPieces()) {
                if (piece.isFinished() || piece.isStacked()) {
                    // Don't draw finished pieces on the board path
                    // Don't draw pieces that are stacked (they are represented by their leader)
                    continue;
                }

                // Now 'piece' is either a leader or an un-stacked individual piece
                Board.BoardPoint point = board.getBoardPoint(piece.getCurrentPositionIndex());
                if (point != null) {
                    g2d.setColor(teamColor);

                    // Highlight if this piece/group is the one selected by the player for a move
                    if (selectedPiece != null && selectedPiece == piece) { // selectedPiece is now a leader/individual
                        Color tempHighlightColor = new Color(teamColor.getRed(), teamColor.getGreen(),
                                teamColor.getBlue(), 100); // Lighter/transparent version
                        g2d.setColor(tempHighlightColor);
                        int highlightSize = pieceSize + 10;
                        g2d.fillOval(point.x - highlightSize / 2, point.y - highlightSize / 2, highlightSize,
                                highlightSize);
                        g2d.setColor(teamColor); // Reset to original team color for the piece itself
                    }

                    g2d.fillOval(point.x - pieceSize / 2, point.y - pieceSize / 2, pieceSize, pieceSize);

                    // Text on piece
                    g2d.setColor(Color.WHITE);
                    if (team.getId() == 2)
                        g2d.setColor(Color.DARK_GRAY); // Green team text

                    String pieceText;
                    if (piece.isGroupLeader()) {
                        pieceText = "x" + piece.getGroupSize(); // e.g., "x3"
                    } else {
                        pieceText = "" + (piece.getId() + 1); // e.g., "1", "2"
                    }
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = point.x - fm.stringWidth(pieceText) / 2;
                    int textY = point.y + fm.getAscent() / 2 - fm.getDescent() / 2; // Center text
                    g2d.drawString(pieceText, textX, textY);
                }
            }
        }
    }

    private void autoDrawLine(Graphics2D g2d, int p1Index, int p2Index) {
        Board.BoardPoint p1 = board.getBoardPoint(p1Index);
        Board.BoardPoint p2 = board.getBoardPoint(p2Index);
        if (p1 != null && p2 != null) {
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }
}