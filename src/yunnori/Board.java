package src.yunnori;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public ArrayList<int[]> pentagonEdges;  // <= 이거 선언!

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
                double os = 1050;
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
                double m = 50, os = 900;
                int cx = (int)(m + os / 2),  cy = (int)(m + os / 2);
                int r  = (int)(os * 0.40);          // 외곽 반지름

                boardPoints = new BoardPoint[36];   // 0-24 외곽, 25-34 내부, 35 센터

                double startAng = Math.PI/2 + 2*Math.PI/5;
                for (int v = 0; v < 5; v++) {
                    double th1 = startAng + 2*Math.PI*v/5;
                    double th2 = startAng + 2*Math.PI*(v+1)/5;
                    int base = v * 5;                           // 0,5,10,15,20

                    boardPoints[base] = new BoardPoint(
                            (int)(cx + r*Math.cos(th1)),
                            (int)(cy - r*Math.sin(th1)));

                    for (int j = 1; j <= 4; j++) {
                        double t = j / 5.0;
                        boardPoints[base + j] = new BoardPoint(
                                (int)(cx + r*((1-t)*Math.cos(th1)+t*Math.cos(th2))),
                                (int)(cy - r*((1-t)*Math.sin(th1)+t*Math.sin(th2))));
                    }
                }

                boardPoints[35] = new BoardPoint(cx, cy);            // C = 35

                for (int v = 0; v < 5; v++) {
                    int vertex = v * 5;          // 0,5,10,15,20
                    int idx1   = 25 + v;         // 25‥29  (1/3)
                    int idx2   = 30 + v;         // 30‥34  (2/3)

                    BoardPoint pV = boardPoints[vertex];
                    boardPoints[idx1] = new BoardPoint(
                            (pV.x*2 + cx) / 3,
                            (pV.y*2 + cy) / 3);
                    boardPoints[idx2] = new BoardPoint(
                            (pV.x   + cx*2) / 3,
                            (pV.y   + cy*2) / 3);
                }

                ArrayList<int[]> edges = new ArrayList<>();

                // 외곽 순환
                for (int i = 0; i < 25; i++)
                    edges.add(new int[]{i, (i + 1) % 25});

                // 꼭짓점 -> idx1 -> idx2 -> 센터
                for (int v = 0; v < 5; v++) {
                    int vertex = v * 5;
                    int idx1 = 25 + v;
                    int idx2 = 30 + v;
                    edges.add(new int[]{vertex, idx1});
                    edges.add(new int[]{idx1 , idx2});
                    edges.add(new int[]{idx2 , 35});
                }
                this.pentagonEdges = edges;
                break;
            }












            case HEXAGON: {
                double m = 50, os = 900;
                int cx = (int)(m + os/2),  cy = (int)(m + os/2);
                int r  = (int)(os * 0.42);          // 외곽 반지름 (6각형이라 약간 키움)


                boardPoints = new BoardPoint[43];   // 0~42

                double startAng = Math.PI/2 + Math.PI/6 + 2*Math.PI/6;   // ‘왼쪽 위’가 0, 그다음 5가 왼쪽
                for (int v = 0; v < 6; v++) {              // 꼭짓점 6
                    double th1 = startAng + 2*Math.PI*v/6;
                    double th2 = startAng + 2*Math.PI*(v+1)/6;
                    int base = v*5;                        // 0,5,10,15,20,25

                    boardPoints[base] = new BoardPoint(
                            (int)(cx + r*Math.cos(th1)),
                            (int)(cy - r*Math.sin(th1)));

                    for (int j = 1; j <= 4; j++) {         // 변 사이 4칸
                        double t = j/5.0;
                        boardPoints[base+j] = new BoardPoint(
                                (int)(cx + r*((1-t)*Math.cos(th1)+t*Math.cos(th2))),
                                (int)(cy - r*((1-t)*Math.sin(th1)+t*Math.sin(th2))));
                    }
                }

                boardPoints[42] = new BoardPoint(cx, cy);   // C

                for (int v = 0; v < 6; v++) {
                    int vertex = v*5;       // 0,5,10,15,20,25
                    int idx1   = 30 + v;    // 30‥35 (1/3)
                    int idx2   = 36 + v;    // 36‥41 (2/3)

                    BoardPoint pV = boardPoints[vertex];
                    boardPoints[idx1] = new BoardPoint(
                            (pV.x*2 + cx)/3,
                            (pV.y*2 + cy)/3);
                    boardPoints[idx2] = new BoardPoint(
                            (pV.x   + cx*2)/3,
                            (pV.y   + cy*2)/3);
                }

                ArrayList<int[]> edges = new ArrayList<>();

                for (int i = 0; i < 30; i++)
                    edges.add(new int[]{i, (i+1)%30});

                for (int v = 0; v < 6; v++) {
                    int vertex = v*5;
                    int idx1 = 30 + v;
                    int idx2 = 36 + v;
                    edges.add(new int[]{vertex, idx1});
                    edges.add(new int[]{idx1 , idx2});
                    edges.add(new int[]{idx2 , 42});
                }


                this.pentagonEdges = edges;   // 같은 필드 재활용
                break;
            }

        }
    }


//    public BoardPoint getBoardPoint(int index) {
//        if (index >= 0 && index <= 31) {
//            return boardPoints[index];
//        }
//        return null;
//    }

    public BoardPoint getBoardPoint(int idx){
        return (idx>=0 && idx<boardPoints.length) ? boardPoints[idx] : null;
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
        List<Piece> opponentLeadersOrIndividualsAtPos = new ArrayList<>();
        if (targetPosition == 0 || targetPosition == 31) {
            return opponentLeadersOrIndividualsAtPos;
        }
        for (Team team : allTeams) {
            if (team.getId() != currentPlayerTeam.getId()) {
                opponentLeadersOrIndividualsAtPos.addAll(team.getInteractivePiecesAt(targetPosition));
            }
        }
        return opponentLeadersOrIndividualsAtPos;
    }


    public void resetPiecesToStart(List<Piece> piecesToReset) {
        for (Piece piece : piecesToReset) {
            piece.reset();
        }
    }

    public BoardType getBoardType() {
        return this.boardType;
    }

    public int getPointCount() {
        return boardPoints.length;
    }


    public List<int[]> getEdges() {
        return this.pentagonEdges;   // 사각형이면 null
    }

    public boolean isValidMoveStart(Piece piece, int steps) {
        return piece.canMove(steps);
    }
}