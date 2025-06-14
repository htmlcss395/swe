package yunnori.swingui;

import yunnori.core.Board; // Assuming Board.BoardPoint is used
import yunnori.core.BoardType;
import yunnori.core.Piece;
import yunnori.core.Team;

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
    public Board board;
    private int pieceSize = 30;
    public int pointSize = 15;

    private YunnoriSwingView guiController;

    private Piece selectedPiece = null;
    private List<Integer> validMoveTargets = null;

    public List<Team> teams;

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public BoardPanel(Board board, List<Team> teams, YunnoriSwingView guiController) {
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

        for (int i = 0; i < board.getPointCount(); i++) {// Iterate through all board points
            Board.BoardPoint pointCoords = board.getBoardPoint(i);
            if (pointCoords != null) {
                int pointClickArea = pointSize * 3; // Increased click area for easier selection
                int pointDrawX = pointCoords.x - pointClickArea / 2;
                int pointDrawY = pointCoords.y - pointClickArea / 2;
                int buffer = 10;

                if (clickX >= pointDrawX - buffer && clickX <= pointDrawX + pointClickArea + buffer &&
                        clickY >= pointDrawY - buffer && clickY <= pointDrawY + pointClickArea + buffer) {

                    List<Piece> piecesAtThisPoint = new ArrayList<>();
                    for (Team team : teams) {
                        // Get leaders or individuals at this visual point
                        piecesAtThisPoint.addAll(team.getInteractivePiecesAt(i));
                    }

                    if (!piecesAtThisPoint.isEmpty()) {
                        guiController.pieceStackClicked(piecesAtThisPoint, clickX, clickY);
                        return;
                    } else {
                        // Clicked on an empty board point, let guiController handle if needed (though current boardClicked is more for deselection)
                        // If we stop iterating here, clicks on empty points with no pieces nearby won't deselect.
                        // So, we might want to continue checking or call boardClicked only if no piece stack is found after checking all points.
                        // For now, if a point is clicked, and it has pieces, we handle it. If not, the click falls through.
                        // This means guiController.boardClicked might be called if the loop finishes without finding pieces.
                        // Let's break if a point is identified, piece or not, to avoid multiple point processing.
                        // The original logic was to break; if pieces were found.
                        // If no pieces, it would fall through to boardClicked.
                        // This seems fine. If point is empty, this loop doesn't find pieces, falls to boardClicked.

                        continue;
                        // break;
                        // Found the clicked point, whether it has pieces or not.
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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        if (board.getBoardType() == BoardType.RECTANGLE) {
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

            for (int i = 0; i < board.getPointCount(); i++) {
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
                        g2d.setFont(new Font("Gulim", Font.BOLD, 15));
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
                        g2d.setFont(new Font("Gulim", Font.BOLD, 11));
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
                        g2d.setFont(new Font("Gulim", Font.BOLD, 11));
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
                        g2d.setFont(new Font("Gulim", Font.BOLD, 15));
                        String text = "끝";
                        FontMetrics fm = g2d.getFontMetrics();
                        g2d.drawString(text, point.x - fm.stringWidth(text) / 2,
                                point.y + currentPointSize + fm.getAscent() / 2);
                        g2d.setFont(originalFont);
                    }
                }
            }

        } else {
            for (int[] e : board.getEdges())
                autoDrawLine(g2d, e[0], e[1]);
        }

        int N = board.getPointCount();
        for (int i = 0; i < N; i++) {
            Board.BoardPoint point = board.getBoardPoint(i);
            if (point == null)
                continue;

            if (validMoveTargets != null && validMoveTargets.contains(i)) {
                g2d.setColor(Color.YELLOW);
                int hs = pointSize * 3;
                g2d.fillOval(point.x - hs / 2, point.y - hs / 2, hs, hs);
            }

            int currentSize = pointSize;
            Color dotColor = Color.BLACK;

            if (board.getBoardType() == BoardType.RECTANGLE) {
                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 23)
                    currentSize = pointSize * 2;
                else if (i == 20 || i == 30) {
                    currentSize = pointSize * 2;
                    dotColor = Color.MAGENTA;
                } else if (i == 31) {
                    currentSize = pointSize * 2;
                    dotColor = Color.CYAN;
                }
            } else if (board.getBoardType() == BoardType.PENTAGON) {
                if (i == 0)
                    drawText(g2d, "시작", point, currentSize, 1);
                else if (i == 10)
                    drawText(g2d, "P", point, currentSize, -1);
                else if (i == 35)
                    drawText(g2d, "C", point, currentSize, 1);
                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 20 || i == 35)
                    currentSize = pointSize * 2;

            } else if (board.getBoardType() == BoardType.HEXAGON) {
                if (i == 0)
                    drawText(g2d, "시작", point, currentSize, 1);
                else if (i == 10)
                    drawText(g2d, "P", point, currentSize, -1);
                else if (i == 42)
                    drawText(g2d, "C", point, currentSize, 1);
                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 20 || i == 25 || i == 42)

                    currentSize = pointSize * 2;
            }

            g2d.setColor(dotColor);
            g2d.fillOval(point.x - currentSize / 2, point.y - currentSize / 2,
                    currentSize, currentSize);
        }

        for (Team team : teams) {
            Color teamColor = switch (team.getId()) {
                case 0 -> Color.RED;
                case 1 -> Color.BLUE;
                case 2 -> Color.GREEN;
                default -> Color.ORANGE;
            };
            // Iterate over *all* pieces to find leaders or unstacked pieces to draw
            for (Piece p : team.getPieces()) {
                // Don't draw finished pieces on the board path
                // Don't draw pieces that are stacked (they are represented by their leader)
                if (p.isFinished() || p.isStacked())
                    continue;

                // Now 'piece' is either a leader or an un-stacked individual piece
                Board.BoardPoint pt = board.getBoardPoint(p.getCurrentPositionIndex());
                if (pt == null)
                    continue;

                g2d.setColor(teamColor);

                // Highlight if this piece/group is the one selected by the player for a move
                /*
                if (p == selectedPiece) {
                    g2d.setColor(Color.CYAN);
                    int hs = pieceSize + 10;
                    g2d.fillOval(pt.x - hs / 2, pt.y - hs / 2, hs, hs);
                    g2d.setColor(teamColor); // Reset to original team color for the piece itself
                }*/
                g2d.fillOval(pt.x - pieceSize / 2, pt.y - pieceSize / 2,
                        pieceSize, pieceSize);
                g2d.setColor(team.getId() == 2 ? Color.DARK_GRAY : Color.WHITE);
                String txt = p.isGroupLeader() ? "x" + p.getGroupSize() : "" + (p.getId() + 1);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(txt, pt.x - fm.stringWidth(txt) / 2,
                        pt.y + fm.getAscent() / 2 - fm.getDescent() / 2);
            }
        }
    }

    private void drawText(Graphics2D g2d, String txt,
            Board.BoardPoint pt, int radius, int vdir) {
        Font orig = g2d.getFont();
        g2d.setFont(new Font("Gulim", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int x = pt.x - fm.stringWidth(txt) / 2;
        int y = pt.y + vdir * (radius + fm.getAscent());
        g2d.setColor(Color.BLACK);
        g2d.drawString(txt, x, y);
        g2d.setFont(orig);
    }

    private void autoDrawLine(Graphics2D g2d, int p1Index, int p2Index) {
        Board.BoardPoint p1 = board.getBoardPoint(p1Index);
        Board.BoardPoint p2 = board.getBoardPoint(p2Index);
        if (p1 != null && p2 != null) {
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

}
