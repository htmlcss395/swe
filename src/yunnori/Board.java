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

                boardPoints[42] = new BoardPoint(cx, cy);

                for (int v = 0; v < 6; v++) {
                    int vertex = v*5;
                    int idx1   = 30 + v;
                    int idx2   = 36 + v;

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


                this.pentagonEdges = edges;

                mainRoute = new int[]{
                        5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                        15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                        25, 26, 27, 28, 29, 0, 1, 2, 3, 4,
                        35, 40, 25, 30, 42
                };
                branchTable = new int[][]{
                        {0 ,30},{30,36},{36,42},
                        {5 ,31},{31,40},{40,42},
                        {10,32},{32,41},{41,42},
                        {15,33},{33,37},{37,42},
                        {20,34},{34,38},{38,42},
                        {25,35},{35,39},{39,42}
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
                    int nxt = nextPos(pos, i == 0);
                    if (nxt == pos) break;
                    pos = nxt;
                }
                return pos;
            }

            case PENTAGON:
                // 0) 센터 탈출 예약이 있으면 최우선
                if (piece.getCenterExitNext() != null) {
                    int tmp = piece.getCenterExitNext();
                    piece.setCenterExitNext(null);
                    if (steps == 0) return tmp;

                    int posOut = tmp;
                    int remain = steps - 1;
                    while (remain-- > 0) {
                        int nxt = prevPos(posOut);
                        if (nxt == posOut) break;
                        posOut = nxt;
                        if (posOut <= 29) break;
                    }
                    if (remain <= 0 || posOut <= 29) return posOut;

                    // 아직 스텝이 남았고 외곽에도 못 갔으면 재귀로 계속
                    piece.setCurrentPositionIndex(posOut);
                    return calculateTargetPosition(piece, remain);
                }

                // 1) 한 칸씩 이동하면서, “센터에 도달(=35)했는지”를 체크
                for (int i = 0; i < steps; i++) {
                    piece.setPrevPositionIndex(pos);

                    int prev = pos;
                    int nxt  = nextPos(prev, i == 0);
                    piece.setCurrentPositionIndex(nxt);

                    // 1-1) 다음 위치가 센터(35)라면
                    if (nxt == 35) {
                        // (1-1-가) “마지막 스텝에 멈추는 경우”
                        if (i == steps - 1) {
                            // 멈춘 prev(30~34 중 하나)에 따라, 다음 턴 탈출 인덱스를 예약
                            //예: prev=31 -> ((31-30+3)%5)+30 = 34
                            int exitIdx2 = ((prev - 30 + 3) % 5) + 30;
                            piece.setCenterExitNext(exitIdx2);

                            // 실제 턴 종료 시점에는 pos=35인 채로 멈춤
                            pos = 35;
                            break;
                        }
                        //(1-1-나) “중간에 센터를 지나치는 경우”
                        else {
                            // ↳ prev가 32라면 ((32-30+1)%5)+30 = 33 (-> 33->28->15… 경로)
                            // ↳ prev가 33라면 ((33-30+1)%5)+30 = 34
                            int passExit = ((prev - 30 + 1) % 5) + 30;
                            pos = passExit;
                            // 남은 스텝이 더 있으므로, i++ 대신 continue로 loop 유지
                            continue;
                        }
                    }

                    // 1-2) 센터가 아닌 일반 이동(외곽 또는 내부 경로)
                    if (nxt == prev) {
                        // 더 이상 이동 불가능하면 중단
                        break;
                    }
                    pos = nxt;
                }

                return pos;



            case HEXAGON: {
                int jump = hardMove(pos, piece.getPrevPositionIndex(), steps);
                if (jump != -1) {
                    piece.setCurrentPositionIndex(jump);
                    return jump;
                }

                for (int i=0;i<steps;i++){
                    int prev = pos;
                    int nxt  = nextPos(prev, i==0);

                    if (nxt == 42){
                        if (i == steps-1){
                            int exit = (prev>=30&&prev<=35)? prev+6 : prev-6;
                            piece.setCenterExitNext(exit);
                            pos = 42; break;
                        } else {
                            pos = (prev>=30&&prev<=35)? prev+6 : prev-6;
                            continue;
                        }
                    }

                    if (nxt == prev) break;
                    pos = nxt;
                }
                piece.setCurrentPositionIndex(pos);
                return pos;
            }


//            case HEXAGON: {
//                if (piece.getCenterExitNext() != null) {
//                    int tmp = piece.getCenterExitNext();
//                    piece.setCenterExitNext(null);
//
//                    if (steps == 0) {
//                        piece.setCurrentPositionIndex(tmp); // 반드시 이동 직전 prev 갱신
//                        return tmp;
//                    }
//                    piece.setCurrentPositionIndex(tmp);
//                    return calculateTargetPosition(piece, steps - 1);
//                }
//                int jump = hardMove(pos, piece.getPrevPositionIndex(), steps);
//
//                if (jump != -1) {              // 특수 이동이면
//                    piece.setCurrentPositionIndex(jump);
//                    return jump;
//                }
//
//                for (int i = 0; i < steps; i++) {
//                    int prev = pos;
//
//                    if (i == 0 && prev == 5) {
//                        switch (i + 1) {
//                            case 1:
//                                pos = 31;
//                                break;
//                            case 2:
//                                pos = 37;
//                                break;
//                            case 3:
//                                pos = 42;
//                                break;
//                            case 4:
//                                pos = 40;
//                                break;
//                            case 5:
//                                pos = 34;
//                                break;
//                            default:
//                                break;
//                        }
//                        continue;
//                    }
//
//                    if (i == 0 && prev == 31) {
//                        switch (i + 1) {
//                            case 1:
//                                pos = 37;
//                                break;
//                            case 2:
//                                pos = 42;
//                                break;
//                            case 3:
//                                pos = 40;
//                                break;
//                            case 4:
//                                pos = 34;
//                                break;
//                            case 5:
//                                pos = 20;
//                                break;
//                            default:
//                                break;
//                        }
//                        continue;
//                    }
//
//                    if (i == 0 && prev == 37) {
//                        switch (i + 1) {
//                            case 1:
//                                pos = 42;
//                                break;
//                            case 2:
//                                pos = 40;
//                                break;
//                            case 3:
//                                pos = 34;
//                                break;
//                            case 4:
//                                pos = 20;
//                                break;
//                            case 5:
//                                pos = 21;
//                                break;
//                            default:
//                                break;
//                        }
//                        continue;
//                    }
//
//                    if (i == 0 && prev == 42) {
//                        int fromPrev = piece.getPrevPositionIndex();
//                        if (fromPrev == 5 || fromPrev == 31 || fromPrev == 37) {
//                            switch (i + 1) {
//                                case 1:
//                                    pos = 41;
//                                    break;
//                                case 2:
//                                    pos = 35;
//                                    break;
//                                case 3:
//                                    pos = 26;
//                                    break;
//                                case 4:
//                                    pos = 27;
//                                    break;
//                                case 5:
//                                    pos = 28;
//                                    break;
//                                default:
//                                    break;
//                            }
//                            continue;
//                        }
//                    }
//
//                    if (i == 0 && prev == 41 && piece.getPrevPositionIndex() == 42) {
//                        switch (i + 1) {
//                            case 1:
//                                pos = 35;
//                                break;
//                            case 2:
//                                pos = 25;
//                                break;
//                            case 3:
//                                pos = 26;
//                                break;
//                            case 4:
//                                pos = 27;
//                                break;
//                            case 5:
//                                pos = 28;
//                                break;
//                            default:
//                                break;
//                        }
//                        continue;
//                    }
//
//                    int nxt = nextPos(prev, i == 0);
//                    if (nxt == 42) {
//                        if (i == steps - 1) {
//                            int exitIndex;
//                            if (prev >= 30 && prev <= 35) {
//                                exitIndex = prev + 6;
//                            } else if (prev >= 36 && prev <= 41) {
//                                exitIndex = prev - 6;
//                            } else {
//                                exitIndex = prev;
//                            }
//                            piece.setCenterExitNext(exitIndex);
//                            piece.setCurrentPositionIndex(42);
//                            pos = 42;
//                            break;
//                        } else {
//                            int passExit;
//                            if (prev >= 30 && prev <= 35) {
//                                passExit = ((prev - 30 + 3) % 6) + 30;
//                            } else if (prev >= 36 && prev <= 41) {
//                                passExit = ((prev - 36 + 3) % 6) + 36;
//                            } else {
//                                passExit = prev;
//                            }
//                            piece.setCurrentPositionIndex(passExit);
//                            pos = passExit;
//                            continue;
//                        }
//                    }
//
//                    if (nxt == prev) break;
//                    pos = nxt;   // 💡 여기는 Piece 객체에 직접 반영 X
//                }
//
//                piece.setCurrentPositionIndex(pos);  // ✅ 루프 끝난 후 최종 업데이트
//                return pos;
//            }



            default:
                return pos;
        }
    }







    private int hardMove(int start, int prev, int steps) {
        if (steps < 1 || steps > 5) return -1;
        switch (start) {
            case 5:
                return switch (steps) {
                    case 1 -> 31;
                    case 2 -> 37;
                    case 3 -> 42;
                    case 4 -> 40;
                    case 5 -> 34;
                    default -> -1;
                };
            case 31:
                return switch (steps) {
                    case 1 -> 37;
                    case 2 -> 42;
                    case 3 -> 40;
                    case 4 -> 34;
                    case 5 -> 20;
                    default -> -1;
                };
            case 37:
                return switch (steps) {
                    case 1 -> 42;
                    case 2 -> 40;
                    case 3 -> 34;
                    case 4 -> 20;
                    case 5 -> 21;
                    default -> -1;
                };
            case 42:
                if (prev == 5) {
                    return switch (steps) {
                        case 1 -> 41;
                        case 2 -> 35;
                        case 3 -> 26;
                        case 4 -> 27;
                        case 5 -> 28;
                        default -> -1;
                    };
                } else if (prev == 31) {
                    return switch (steps) {
                        case 1 -> 35;
                        case 2 -> 35;
                        case 3 -> 26;
                        case 4 -> 27;
                        case 5 -> 20;
                        default -> -1;
                    };
                } else if (prev == 37) {
                    return switch (steps) {
                        case 1 -> 35;
                        case 2 -> 35;
                        case 3 -> 26;
                        case 4 -> 27;
                        case 5 -> 21;
                        default -> -1;
                    };
                }
                break;
            case 41:
                if (prev == 42) {
                    return switch (steps) {
                        case 1 -> 35;
                        case 2 -> 25;
                        case 3 -> 26;
                        case 4 -> 27;
                        case 5 -> 28;
                        default -> -1;
                    };
                }
                break;
        }
        return -1;
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

    private int nextPos(int from, boolean firstStep) {
        switch (boardType) {
            case RECTANGLE:
                for (int[] b : branchTable)
                    if (b[0] == from) return b[1];
                for (int i = 0; i < mainRoute.length - 1; i++)
                    if (mainRoute[i] == from) return mainRoute[i + 1];
                return from;

            case PENTAGON:
                /* 1) 첫 스텝이면서 꼭짓점(5·10·15·20)이면 -> 안쪽 분기(25‥34) */
                if (firstStep && from % 5 == 0 && from > 0 && from <= 20) {
                    for (int[] e : branchTable)
                        if (e[0] == from && e[1] >= 25 && e[1] <= 34)
                            return e[1];            // 5->26, 10->27, 15->28, 20->29
                }

                /* 2) 바깥 둘레(0‥24) 그대로 진행 */
                for (int[] e : branchTable)
                    if (e[0] == from && e[1] < 25)
                        return e[1];

                /* 3) 안쪽 분기(25‥34)는 센터(35)로 */
                if (from >= 25 && from <= 34) return 35;

                return from;                        // 더 못 가면 제자리



            case HEXAGON: {
                if (firstStep && from % 5 == 0 && from > 0 && from <= 24) {
                    for (int[] e : branchTable)
                        if (e[0] == from && e[1] >= 30 && e[1] <= 35)
                            return e[1];          // 5→30, 10→31, 15→32, 20→33, 25→34
                }

                /* ② 외곽 기본 전진 : 0‥29 → (from+1)%30 */
                if (from >= 0 && from <= 29)
                    return (from + 1) % 30;

                /* ③ interior1(30‥35) → interior2(36‥41) */
                if (from >= 30 && from <= 35) {
                    for (int[] e : branchTable)
                        if (e[0] == from && e[1] >= 36 && e[1] <= 41)
                            return e[1];
                }

                /* ④ interior2(36‥41) → 센터(42) */
                if (from >= 36 && from <= 41)
                    return 42;

                /* ⑤ 그 외 이동 불가 */
                return from;
            }



            default:
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