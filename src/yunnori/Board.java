package src.yunnori;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static class BoardPoint {
        public int x;
        public int y;

        public BoardPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private BoardPoint[] boardPoints = new BoardPoint[40]; // Indices 0-31


    private BoardType boardType;

    public Board(BoardType boardType) {
        this.boardType = boardType;
        initializeBoardPoints();
    }

    public Board() {
        this(BoardType.RECTANGLE);
         //
    }

    private void initializeBoardPoints() {


        switch (boardType) {
            case RECTANGLE: {
                double m = 50;
                double os = 900;
                double ps_outer = os / 5;
                double ps_c_c = os / 6;
                double ps_c_bl = os / 6;
                double ps_c_fin = os / 6;

                BoardPoint brCorner = new BoardPoint((int) (m + os), (int) (m + os));
                BoardPoint blCorner = new BoardPoint((int) (m), (int) (m + os));
                BoardPoint tlCorner = new BoardPoint((int) (m), (int) (m));
                BoardPoint trCorner = new BoardPoint((int) (m + os), (int) (m));
                BoardPoint center = new BoardPoint((int) (m + os / 2), (int) (m + os / 2));

                boardPoints[0] = brCorner;
                boardPoints[1] = new BoardPoint((int) (m + os), (int) (m + os - ps_outer));
                boardPoints[2] = new BoardPoint((int) (m + os), (int) (m + os - 2 * ps_outer));
                boardPoints[3] = new BoardPoint((int) (m + os), (int) (m + os - 3 * ps_outer));
                boardPoints[4] = new BoardPoint((int) (m + os), (int) (m + os - 4 * ps_outer));
                boardPoints[5] = trCorner;
                boardPoints[6] = new BoardPoint((int) (m + os - ps_outer), (int) (m));
                boardPoints[7] = new BoardPoint((int) (m + os - 2 * ps_outer), (int) (m));
                boardPoints[8] = new BoardPoint((int) (m + os - 3 * ps_outer), (int) (m));
                boardPoints[9] = new BoardPoint((int) (m + os - 4 * ps_outer), (int) (m));
                boardPoints[10] = tlCorner;
                boardPoints[11] = new BoardPoint((int) (m), (int) (m + ps_outer));
                boardPoints[12] = new BoardPoint((int) (m), (int) (m + 2 * ps_outer));
                boardPoints[13] = new BoardPoint((int) (m), (int) (m + 3 * ps_outer));
                boardPoints[14] = new BoardPoint((int) (m), (int) (m + 4 * ps_outer));
                boardPoints[15] = blCorner;
                boardPoints[16] = new BoardPoint((int) (m + ps_outer), (int) (m + os));
                boardPoints[17] = new BoardPoint((int) (m + 2 * ps_outer), (int) (m + os));
                boardPoints[18] = new BoardPoint((int) (m + 3 * ps_outer), (int) (m + os));
                boardPoints[19] = new BoardPoint((int) (m + 4 * ps_outer), (int) (m + os));

                // Point 20: Last point on outer track before 31
                boardPoints[20] = new BoardPoint((int) (m + os - ps_outer / 6), (int) (m + os));

                // Point 23: Center of Board
                boardPoints[23] = center;

                // Shortcut path from the top right corner(Point 5)
                boardPoints[21] = new BoardPoint((int) (m + os - ps_c_c), (int) (m + ps_c_c));
                boardPoints[22] = new BoardPoint((int) (m + os - 2 * ps_c_c), (int) (m + 2 * ps_c_c));
                boardPoints[24] = new BoardPoint((int) (m + os / 2 - ps_c_bl), (int) (m + os / 2 + ps_c_bl));
                boardPoints[25] = new BoardPoint((int) (m + os / 2 - 2 * ps_c_bl), (int) (m + os / 2 + 2 * ps_c_bl));

                // Shortcut path from the top left corner(Point 10)
                boardPoints[26] = new BoardPoint((int) (m + ps_c_c), (int) (m + ps_c_c));
                boardPoints[27] = new BoardPoint((int) (m + 2 * ps_c_c), (int) (m + 2 * ps_c_c));

                // Shortcut path from the center(Point 23)
                boardPoints[28] = new BoardPoint((int) (m + os / 2 + ps_c_fin), (int) (m + os / 2 + ps_c_fin));
                boardPoints[29] = new BoardPoint((int) (m + os / 2 + 2 * ps_c_fin), (int) (m + os / 2 + 2 * ps_c_fin));

                // Point 30: Last point on diagonal track before 31
                boardPoints[30] = new BoardPoint((int) (m + os - ps_c_fin / 6), (int) (m + os - ps_c_fin / 6));

                // Point 31: Finish point
                boardPoints[31] = new BoardPoint((int) (m + os + ps_outer), (int) (m + os));
                break;
            }
            case PENTAGON: {
                double m = 50;
                double os = 900;
                int cx = (int) (m + os / 2);
                int cy = (int) (m + os / 2);
                int pent_r_outer = (int) (os * 0.40);
                int pent_r_in = (int) (os * 0.23);

                for (int i = 0; i < 5; i++) {
                    double angle = Math.PI / 2 + 2 * Math.PI * i / 5;
                    int x = (int) (cx + pent_r_outer * Math.cos(angle));
                    int y = (int) (cy - pent_r_outer * Math.sin(angle));
                    boardPoints[i] = new BoardPoint(x, y);
                }

                int idx = 5;
                for (int i = 0; i < 5; i++) {
                    double angle1 = Math.PI / 2 + 2 * Math.PI * i / 5;
                    double angle2 = Math.PI / 2 + 2 * Math.PI * ((i + 1) % 5) / 5;
                    for (int j = 1; j <= 4; j++) {
                        double frac = j / 5.0;
                        int x = (int) (cx + pent_r_outer * ((1 - frac) * Math.cos(angle1) + frac * Math.cos(angle2)));
                        int y = (int) (cy - pent_r_outer * ((1 - frac) * Math.sin(angle1) + frac * Math.sin(angle2)));
                        boardPoints[idx++] = new BoardPoint(x, y);
                    }
                }

                boardPoints[25] = new BoardPoint(cx, cy);

                idx = 26;
                for (int i = 0; i < 5; i++) {
                    double angle = Math.PI / 2 + 2 * Math.PI * i / 5;
                    for (int j = 1; j <= 2; j++) {
                        double frac = j / 3.0;
                        int x = (int) (cx + (pent_r_in * frac) * Math.cos(angle));
                        int y = (int) (cy - (pent_r_in * frac) * Math.sin(angle));
                        boardPoints[idx++] = new BoardPoint(x, y);
                    }
                }

                ArrayList<int[]> edges = new ArrayList<>();

                for (int i = 0; i < 5; i++) {
                    int start = i;
                    int base = 5 + i * 4;
                    edges.add(new int[]{start, base});
                    edges.add(new int[]{base, base + 1});
                    edges.add(new int[]{base + 1, base + 2});
                    edges.add(new int[]{base + 2, base + 3});
                    int next = (i + 1) % 5;
                    edges.add(new int[]{base + 3, next});
                }

                for (int i = 0; i < 5; i++) {
                    int mid1 = 26 + i * 2;
                    int mid2 = mid1 + 1;
                    edges.add(new int[]{25, mid1});
                    edges.add(new int[]{mid1, mid2});
                    edges.add(new int[]{mid2, i});
                }

          //      this.PentagonEdges = edges;
                break;
            }




            case HEXAGON: {
//                int cx = (int) (m + os / 2);
//                int cy = (int) (m + os / 2);
//                int hex_r = (int) (os * 0.40);
//
//                for (int i = 0; i < 6; i++) {
//                    double angle = -Math.PI / 2 + 2 * Math.PI * i / 6;
//                    int x = (int) (cx + hex_r * Math.cos(angle));
//                    int y = (int) (cy + hex_r * Math.sin(angle));
//                    boardPoints[i] = new BoardPoint(x, y);
//                }
//                boardPoints[6] = new BoardPoint(cx, cy);
//
//                // TODO: 육각형의 점 분리 등을 추가
//                break;
            }
        }
    }


    public BoardPoint getBoardPoint(int index) {
        if (index >= 0 && index <= 31) {
            return boardPoints[index];
        }
        return null;
    }

    private int getPreviousPosition(int currentPos) {
        if (currentPos == 0)
            return 20;
        if (currentPos == 21)
            return 5;
        if (currentPos == 26)
            return 10;
        if (currentPos == 28)
            return 23;
        if (currentPos == 31)
            return 30; // not gonna be used anyway

        /*
         * TBA: More detailed rules needed
         */
        if (currentPos == 15)
            return 14;
        if (currentPos == 23)
            return 22;

        else
            return currentPos - 1;
    }

    public int calculateTargetPosition(Piece piece, int steps) {
        int originalPos = piece.getCurrentPositionIndex();
        int currentSimulationPos = originalPos;

        if (piece.isFinished()) {
            return 31;
        }
        if (steps == -1) {
            if (originalPos == 0) {
                return 20;
            }
            return getPreviousPosition(originalPos);
        }

        for (int i = 0; i < steps; i++) {
            if (currentSimulationPos == 31) {
                break;
            }
            int nextPosAfterOneStep = -1;

            if (currentSimulationPos == 23) { // Center
                if (originalPos == 5 || originalPos == 21 || originalPos == 22) {
                    nextPosAfterOneStep = 24; // Path 3 (23->24->25->15)
                } else { // Came from 10 originally OR started at 23
                    nextPosAfterOneStep = 28; // Path 4 (23->28->29->30->31)
                }
            } else if (currentSimulationPos == 0)
                nextPosAfterOneStep = 1;
            else if (currentSimulationPos == 20)
                nextPosAfterOneStep = 31; // End of outer -> Finish
            else if (currentSimulationPos == 30)
                nextPosAfterOneStep = 31; // Point before Finish -> Finish

            else if (currentSimulationPos == 22)
                nextPosAfterOneStep = 23; // Path 1 (5->21->22->23) end
            else if (currentSimulationPos == 27)
                nextPosAfterOneStep = 23; // Path 2 (10->26->27->23) end

            else if (currentSimulationPos == 25)
                nextPosAfterOneStep = 15; // Path 3 (23->24->25->15) end
            else if (currentSimulationPos == 29)
                nextPosAfterOneStep = 30; // Path 4 (23->28->29->30) end

            else if (i == 0 && originalPos == 5) {
                nextPosAfterOneStep = 21;
            } // Start of Path 1
            else if (i == 0 && originalPos == 10) {
                nextPosAfterOneStep = 26;
            } // Start of Path 2
            else {
                nextPosAfterOneStep = currentSimulationPos + 1; // Linear move
            }
            currentSimulationPos = nextPosAfterOneStep;
        }
        if (currentSimulationPos > 31) {
            currentSimulationPos = 31;
        }
        return currentSimulationPos;
    }

    public List<Piece> findOpponentPiecesAt(int targetPosition, Team currentPlayerTeam, List<Team> allTeams) {
        List<Piece> caughtPieces = new ArrayList<>();
        if (targetPosition == 0 || targetPosition == 31) {
            return caughtPieces;
        }
        for (Team team : allTeams) {
            if (team.getId() != currentPlayerTeam.getId()) {
                caughtPieces.addAll(team.getPiecesAt(targetPosition));
            }
        }
        return caughtPieces;
    }

    public void resetPiecesToStart(List<Piece> piecesToReset) {
        for (Piece piece : piecesToReset) {
            piece.reset();
        }
    }

    public boolean isValidMoveStart(Piece piece, int steps) {
        return piece.canMove(steps);
    }
}