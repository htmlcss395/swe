package src.yunnori;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public ArrayList<int[]> pentagonEdges;

    private int[]   mainRoute;
    private int[][] branchTable;
    private java.util.Set<Integer> noCatchSet;

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


                mainRoute  = new int[]{ 0,1,2,3,4,5,6,7,8,9,10,
                        11,12,13,14,15,16,17,18,19,20,
                        31 };
                branchTable = new int[][]{
                        {5,21},{21,22},{22,23},
                        {10,26},{26,27},{27,23},
                        {23,24},{24,25},{25,15},
                        {23,28},{28,29},{29,30},{30,31}
                };
                noCatchSet  = java.util.Set.of(0,31);
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

                mainRoute = new int[]{
                        // 외곽 25칸
                        0,1,2,3,4,5,6,7,8,9,
                        10,11,12,13,14,15,16,17,18,19,
                        20,21,22,23,24,
                        // 지름길 24->29->20(P)-> finish(10) 예시
                        29,20,        // P
                        35,           // 센터
                        30,25,15,     // 다른 분기 (=원래 길)
                        35            // finish 를 35 로 가정
                };
                branchTable = new int[][]{
                        // 외곽 순환
                        {0,1},{1,2},{2,3},{3,4},{4,5},
                        {5,6},{6,7},{7,8},{8,9},{9,10},
                        {10,11},{11,12},{12,13},{13,14},{14,15},
                        {15,16},{16,17},{17,18},{18,19},{19,20},
                        {20,21},{21,22},{22,23},{23,24},{24,0},

                        // 0번 꼭짓점에서 센터로
                        {0,25},{25,30},{30,35},
                        // 5번 꼭짓점에서 센터로
                        {5,26},{26,31},{31,35},
                        // 10번 꼭짓점에서 센터로
                        {10,27},{27,32},{32,35},
                        // 15번 꼭짓점에서 센터로
                        {15,28},{28,33},{33,35},
                        // 20번 꼭짓점에서 센터로
                        {20,29},{29,34},{34,35}
                };

                noCatchSet  = java.util.Set.of(0/*start*/);
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

                mainRoute = new int[]{
                        5,6,7,8,9,10,11,12,13,14,15,
                        16,17,18,19,20,21,22,23,24,25,
                        26,27,28,29,0,1,2,3,4,
                        35,40,25,30,42
                };
                branchTable = new int[][]{
                        {5,30},{30,36},{36,42},
                        {10,31},{31,37},{37,42},
                        {15,32},{32,38},{38,42},
                        {20,33},{33,39},{39,42},
                        {25,34},{34,40},{40,42}
                };
                noCatchSet = java.util.Set.of(5);   // hexagon start
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


//    private int getPreviousPosition(int currentPos) {
//        if (currentPos == 0)
//            return 20;
//        if (currentPos == 21)
//            return 5;
//        if (currentPos == 26)
//            return 10;
//        if (currentPos == 28)
//            return 23;
//        if (currentPos == 31)
//            return 30; // not gonna be used anyway
//
//        /*
//         * TBA: More detailed rules needed
//         */
//        if (currentPos == 15)
//            return 14;
//        if (currentPos == 23)
//            return 22;
//
//        else
//            return currentPos - 1;
//    } // 기존의 하드코드된 부분을 제거
private int getPreviousPosition(int currentPos) {
    return prevPos(currentPos);
}




//    public int calculateTargetPosition(Piece piece, int steps) {
//        int originalPos = piece.getCurrentPositionIndex();
//        int currentSimulationPos = originalPos;
//
//        if (piece.isFinished()) {
//            return 31;
//        }
//        if (steps == -1) {
//            if (originalPos == 0) {
//                return 20;
//            }
//            return getPreviousPosition(originalPos);
//        }
//
//        for (int i = 0; i < steps; i++) {
//            if (currentSimulationPos == 31) {
//                break;
//            }
//            int nextPosAfterOneStep = -1;
//
//            if (currentSimulationPos == 23) { // Center
//                if (originalPos == 5 || originalPos == 21 || originalPos == 22) {
//                    nextPosAfterOneStep = 24; // Path 3 (23->24->25->15)
//                } else { // Came from 10 originally OR started at 23
//                    nextPosAfterOneStep = 28; // Path 4 (23->28->29->30->31)
//                }
//            } else if (currentSimulationPos == 0)
//                nextPosAfterOneStep = 1;
//            else if (currentSimulationPos == 20)
//                nextPosAfterOneStep = 31; // End of outer -> Finish
//            else if (currentSimulationPos == 30)
//                nextPosAfterOneStep = 31; // Point before Finish -> Finish
//
//            else if (currentSimulationPos == 22)
//                nextPosAfterOneStep = 23; // Path 1 (5->21->22->23) end
//            else if (currentSimulationPos == 27)
//                nextPosAfterOneStep = 23; // Path 2 (10->26->27->23) end
//
//            else if (currentSimulationPos == 25)
//                nextPosAfterOneStep = 15; // Path 3 (23->24->25->15) end
//            else if (currentSimulationPos == 29)
//                nextPosAfterOneStep = 30; // Path 4 (23->28->29->30) end
//
//            else if (i == 0 && originalPos == 5) {
//                nextPosAfterOneStep = 21;
//            } // Start of Path 1
//            else if (i == 0 && originalPos == 10) {
//                nextPosAfterOneStep = 26;
//            } // Start of Path 2
//            else {
//                nextPosAfterOneStep = currentSimulationPos + 1; // Linear move
//            }
//            currentSimulationPos = nextPosAfterOneStep;
//        }
//        if (currentSimulationPos > 31) {
//            currentSimulationPos = 31;
//        }
//        return currentSimulationPos;
//    }
    
    // 새로운 공통 전진 로직, mainToute / branchTable 사용
    public int calculateTargetPosition(Piece piece, int steps) {
        int pos = piece.getCurrentPositionIndex();
        if (piece.isFinished()) {
            int finish = mainRoute[mainRoute.length - 1];
            return finish;
        }
        if (steps == -1) return prevPos(pos);

        switch (boardType) {
            case RECTANGLE: {
                for (int i = 0; i < steps; i++) {
                    int nxt = nextPos(pos);
                    if (nxt == pos) break;
                    pos = nxt;
                }
                return pos;
            }

            case PENTAGON: {
                for (int i = 0; i < steps; i++) {
                    int nxt = nextPos(pos);   // 외곽/내부 전진
                    if (nxt == pos) break;
                    pos = nxt;
                }
                // 이동이 끝난 후, 꼭짓점(5,10,15,20)에 "멈췄을 때"만 내부로 진입
                if (pos % 5 == 0 && pos > 0 && pos <= 20) {
                    for (int[] e : branchTable)
                        if (e[0] == pos && e[1] >= 25 && e[1] <= 34) {
                            pos = e[1];
                            break;
                        }
                }
                return pos;
            }



            case HEXAGON: {
                for (int i = 0; i < steps; i++) {
                    int nxt = nextPos(pos);
                    if (nxt == pos) break;
                    pos = nxt;
                }
                return pos;
            }

            default:
                // 혹시 모를 예외
                return pos;
        }
    }




//    public List<Piece> findOpponentPiecesAt(int targetPosition, Team currentPlayerTeam, List<Team> allTeams) {
//        List<Piece> opponentLeadersOrIndividualsAtPos = new ArrayList<>();
//        if (targetPosition == 0 || targetPosition == 31) { // No catches at start/finish
//            return opponentLeadersOrIndividualsAtPos;
//        }
//        for (Team team : allTeams) {
//            if (team.getId() != currentPlayerTeam.getId()) {
//                // Use getInteractivePiecesAt to get only leaders or individual pieces of the
//                // opponent team
//                opponentLeadersOrIndividualsAtPos.addAll(team.getInteractivePiecesAt(targetPosition));            }
//        }
//        return opponentLeadersOrIndividualsAtPos;
//    }

        public List<Piece> findOpponentPiecesAt(int targetPosition, Team me, List<Team> allTeams) {
                List<Piece> caught = new ArrayList<>();
                if (noCatchSet.contains(targetPosition)) return caught;
                       for (Team t : allTeams) {
                        if (t.getId() == me.getId()) continue;
                        caught.addAll(t.getInteractivePiecesAt(targetPosition));
                    }
                return caught;
            }


    public void resetPiecesToStart(List<Piece> piecesToReset) {
        // The Piece.reset() method now handles detaching from groups and resetting
        // stacked pieces if it's a leader.
        for (Piece piece : piecesToReset) {
            piece.reset();
        }
    }

//    private int nextPos(int from){
//        for (int[] b: branchTable)
//            if (b[0]==from) return b[1];
//        // mainRoute 에서 다음
//        for (int i=0;i<mainRoute.length-1;i++)
//            if (mainRoute[i]==from) return mainRoute[i+1];
//        return from;          // 끝 = 더 못감
//    }

    private int nextPos(int from) {
        switch (boardType) {
            case RECTANGLE:
                for (int[] b : branchTable)
                    if (b[0] == from) return b[1];
                for (int i = 0; i < mainRoute.length - 1; i++)
                    if (mainRoute[i] == from) return mainRoute[i + 1];
                return from;

            case PENTAGON:
                if (from == 0) {
                    for (int[] b : branchTable)
                        if (b[0] == from && b[1] == 1) return b[1];
                    return from;
                }
                for (int[] b : branchTable)
                    if (b[0] == from && b[1] < 25) return b[1];
                if (from >= 25 && from <= 34) return 35;
                return from;


            case HEXAGON:
                for (int[] b : branchTable)
                    if (b[0] == from) return b[1];
                for (int i = 0; i < mainRoute.length - 1; i++)
                    if (mainRoute[i] == from) return mainRoute[i + 1];
                return from;

            default:
                // 혹시나 모를 예외
                return from;
        }
    }





    // 한 스텝 후진
    private int prevPos(int from){
        for (int[] b: branchTable)
            if (b[1]==from) return b[0];
        for (int i=1;i<mainRoute.length;i++)
            if (mainRoute[i]==from) return mainRoute[i-1];
        return from;
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
        // piece.canMove() now correctly checks if the piece is stacked (and thus
        // unmovable independently)
        return piece.canMove(steps);
    }
}