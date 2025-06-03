package yunnori.fxui;

import yunnori.core.Board;
import yunnori.core.BoardType;
import yunnori.core.GameLogicController;
import yunnori.core.Piece;
import yunnori.core.Team;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class BoardCanvas extends Canvas {

    private GameLogicController gameController; // To get board and team data

    private final double pieceSize = 30; // Diameter
    private final double pointSize = 15; // Diameter
    private final double highlightOffset = 5; // For selection highlight

    private Piece uiSelectedPieceForMoveFX; // From YunnoriJavaFXView
    private List<Integer> validMoveTargetIndicesFX; // From YunnoriJavaFXView

    public BoardCanvas(GameLogicController controller, double width, double height) {
        super(width, height);
        this.gameController = controller;

        // Mouse click handler for the canvas (will be set up in YunnoriJavaFXView)
    }

    public void setHighlightData(Piece selectedPiece, List<Integer> validTargets) {
        this.uiSelectedPieceForMoveFX = selectedPiece;
        this.validMoveTargetIndicesFX = validTargets;
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight()); // Clear canvas

        Board board = gameController.getBoard();
        if (board == null)
            return;

        // --- Draw Board Lines (Example for RECTANGLE) ---
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        if (board.getBoardType() == BoardType.RECTANGLE) {
            // Manually draw lines for rectangle board as in BoardPanel
            // This requires translating autoDrawLine logic
            for (int i = 0; i < 20; i++)
                drawBoardLine(gc, board, i, i + 1);
            drawBoardLine(gc, board, 0, board.getFinishPointIndex()); // Assuming 31 is finish
            drawBoardLine(gc, board, 5, 21);
            drawBoardLine(gc, board, 21, 22);
            drawBoardLine(gc, board, 22, 23);
            drawBoardLine(gc, board, 10, 26);
            drawBoardLine(gc, board, 26, 27);
            drawBoardLine(gc, board, 27, 23);
            drawBoardLine(gc, board, 23, 24);
            drawBoardLine(gc, board, 24, 25);
            drawBoardLine(gc, board, 25, 15);
            drawBoardLine(gc, board, 23, 28);
            drawBoardLine(gc, board, 28, 29);
            drawBoardLine(gc, board, 29, 30);
            drawBoardLine(gc, board, 30, 0); // Path back to start/finish for rectangle
        } else { // For Pentagon/Hexagon, use edges
            List<int[]> edges = board.getEdges();
            if (edges != null) {
                for (int[] edge : edges) {
                    drawBoardLine(gc, board, edge[0], edge[1]);
                }
            }
        }

        // --- Draw Board Points & Highlights ---
        int numPoints = board.getPointCount();
        for (int i = 0; i < numPoints; i++) {
            Board.BoardPoint point = board.getBoardPoint(i);
            if (point == null)
                continue;

            // Draw yellow highlight for valid move targets
            if (validMoveTargetIndicesFX != null && validMoveTargetIndicesFX.contains(i)) {
                gc.setFill(Color.YELLOW);
                double highlightSize = pointSize * 3;
                gc.fillOval(point.x - highlightSize / 2, point.y - highlightSize / 2, highlightSize, highlightSize);
            }

            double currentDisplayPointSize = pointSize;
            Color pointColor = Color.BLACK;

            // Customize point appearance (example for RECTANGLE)
            if (board.getBoardType() == BoardType.RECTANGLE) {
                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 23) { // Large points
                    currentDisplayPointSize = pointSize * 2;
                } else if (i == 20 || i == 30) { // Chammeki
                    currentDisplayPointSize = pointSize * 2;
                    pointColor = Color.MAGENTA;
                } else if (i == board.getFinishPointIndex()) { // Finish
                    currentDisplayPointSize = pointSize * 2;
                    pointColor = Color.CYAN;
                }
            } else if (board.getBoardType() == BoardType.PENTAGON) {
                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 20 || i == 35/*center*/)
                    currentDisplayPointSize = pointSize * 2;
                if (i == board.getFinishPointIndex())
                    pointColor = Color.CYAN;
            } else if (board.getBoardType() == BoardType.HEXAGON) {
                if (i == 0 || i == 5 || i == 10 || i == 15 || i == 20 || i == 25 || i == 42/*center*/)
                    currentDisplayPointSize = pointSize * 2;
                if (i == board.getFinishPointIndex())
                    pointColor = Color.CYAN;
            }
            // ... (adapt for Pentagon/Hexagon special points if needed) ...

            gc.setFill(pointColor);
            gc.fillOval(point.x - currentDisplayPointSize / 2, point.y - currentDisplayPointSize / 2,
                    currentDisplayPointSize, currentDisplayPointSize);

            // Draw labels (시작, 끝 etc.) - Simplified
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            if (i == board.getStartPointIndex()) {
                gc.fillText("시작", point.x, point.y - currentDisplayPointSize);
            } else if (i == board.getFinishPointIndex()) {
                gc.fillText("끝", point.x, point.y + currentDisplayPointSize + 12);
            }
            // Add other labels for Chammeki etc. if needed
        }

        // --- Draw Pieces ---
        List<Team> teams = gameController.getTeams();
        if (teams == null)
            return;

        gc.setFont(Font.font("System", FontWeight.NORMAL, 14)); // Font for piece text

        for (Team team : teams) {
            Color teamFXColor = getTeamColorFX(team.getId());
            for (Piece piece : team.getPieces()) {
                if (piece.isFinished() || piece.isStacked())
                    continue; // Don't draw stacked or finished

                Board.BoardPoint piecePos = board.getBoardPoint(piece.getCurrentPositionIndex());
                if (piecePos == null)
                    continue;

                // Highlight for the specifically selected piece (uiSelectedPieceForMoveFX)
                // This is the equivalent of the cyan highlight in BoardPanel
                if (uiSelectedPieceForMoveFX == piece) {
                    // Only draw if not already yellow highlighted to avoid double-highlighting appearance
                    boolean alreadyYellow = (validMoveTargetIndicesFX != null
                            && validMoveTargetIndicesFX.contains(piece.getCurrentPositionIndex()));
                    if (!alreadyYellow) {
                        gc.setFill(Color.LIGHTBLUE); // Or another selection color
                        gc.fillOval(piecePos.x - (pieceSize + highlightOffset) / 2,
                                piecePos.y - (pieceSize + highlightOffset) / 2,
                                pieceSize + highlightOffset, pieceSize + highlightOffset);
                    }
                }

                gc.setFill(teamFXColor);
                gc.fillOval(piecePos.x - pieceSize / 2, piecePos.y - pieceSize / 2, pieceSize, pieceSize);

                // Draw piece text (ID or group size)
                String pieceText = piece.isGroupLeader() ? "x" + piece.getGroupSize()
                        : String.valueOf(piece.getId() + 1);
                gc.setFill(Color.WHITE); // Text color on pieces
                if (team.getId() == 2 && board.getBoardType() == BoardType.RECTANGLE)
                    gc.setFill(Color.DARKGRAY); // Special case for green team on rectangle
                gc.setTextAlign(TextAlignment.CENTER);
                // Baseline offset for text to be roughly centered in the oval
                gc.fillText(pieceText, piecePos.x, piecePos.y + pieceSize / 4 - 2);
            }
        }
    }

    private void drawBoardLine(GraphicsContext gc, Board board, int p1Index, int p2Index) {
        Board.BoardPoint p1 = board.getBoardPoint(p1Index);
        Board.BoardPoint p2 = board.getBoardPoint(p2Index);
        if (p1 != null && p2 != null) {
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private Color getTeamColorFX(int teamId) {
        return switch (teamId) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.ORANGE;
            default -> Color.BLACK;
        };
    }
}