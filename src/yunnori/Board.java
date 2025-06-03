package src.yunnori;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public ArrayList<int[]> pentagonEdges;

    public static class BoardPoint {
        public int x;
        public int y;

        public BoardPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private BoardPoint[] boardPoints; // Instance variable to display points
    private BoardType boardType;
    private int startPointIndex = 0;
    private int finishPointIndex; // Instance variable to store the finish index for this board

    public Board(BoardType boardType) {
        this.boardType = boardType;
        initializeBoardProperties(); // New method to set up indices and points
        initializeBoardPoints();
    }

    public BoardType getBoardType() {
        return this.boardType;
    }

    // Getter for the finish index
    public int getFinishPointIndex() {
        return this.finishPointIndex;
    }

    public int getStartPointIndex() {
        return this.startPointIndex;
    }

    private void initializeBoardProperties() {
        switch (boardType) {
            case RECTANGLE:
                this.finishPointIndex = 31; // Current finish for rectangle
                this.boardPoints = new BoardPoint[32];
                break;
            case PENTAGON:
                this.finishPointIndex = 36;
                this.boardPoints = new BoardPoint[37];
                break;
            case HEXAGON:
                this.finishPointIndex = 43;
                this.boardPoints = new BoardPoint[44];
                break;
            default:
                this.finishPointIndex = 31; // Default fallback
                break;
        }
    }

    // Method to display points: DO NOT TOUCH ANYMORE
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
                double m = 50, os = 900;
                int cx = (int) (m + os / 2), cy = (int) (m + os / 2);
                int r = (int) (os * 0.40); // 외곽 반지름

                //boardPoints = new BoardPoint[36]; // 0-24 외곽, 25-34 내부, 35 센터

                double startAng = Math.PI / 2 + 2 * Math.PI / 5;
                for (int v = 0; v < 5; v++) {
                    double th1 = startAng + 2 * Math.PI * v / 5;
                    double th2 = startAng + 2 * Math.PI * (v + 1) / 5;
                    int base = v * 5; // 0,5,10,15,20

                    boardPoints[base] = new BoardPoint(
                            (int) (cx + r * Math.cos(th1)),
                            (int) (cy - r * Math.sin(th1)));

                    for (int j = 1; j <= 4; j++) {
                        double t = j / 5.0;
                        boardPoints[base + j] = new BoardPoint(
                                (int) (cx + r * ((1 - t) * Math.cos(th1) + t * Math.cos(th2))),
                                (int) (cy - r * ((1 - t) * Math.sin(th1) + t * Math.sin(th2))));
                    }
                }

                boardPoints[35] = new BoardPoint(cx, cy); // C = 35

                for (int v = 0; v < 5; v++) {
                    int vertex = v * 5; // 0,5,10,15,20
                    int idx1 = 25 + v; // 25‥29 (1/3)
                    int idx2 = 30 + v; // 30‥34 (2/3)

                    BoardPoint pV = boardPoints[vertex];
                    boardPoints[idx1] = new BoardPoint(
                            (pV.x * 2 + cx) / 3,
                            (pV.y * 2 + cy) / 3);
                    boardPoints[idx2] = new BoardPoint(
                            (pV.x + cx * 2) / 3,
                            (pV.y + cy * 2) / 3);
                }

                ArrayList<int[]> edges = new ArrayList<>();

                // 외곽 순환
                for (int i = 0; i < 25; i++)
                    edges.add(new int[] { i, (i + 1) % 25 });

                // 꼭짓점 -> idx1 -> idx2 -> 센터
                for (int v = 0; v < 5; v++) {
                    int vertex = v * 5;
                    int idx1 = 25 + v;
                    int idx2 = 30 + v;
                    edges.add(new int[] { vertex, idx1 });
                    edges.add(new int[] { idx1, idx2 });
                    edges.add(new int[] { idx2, 35 });
                }
                this.pentagonEdges = edges;

                // mainRoute = new int[] {
                //         // 외곽 25칸
                //         0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                //         10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                //         20, 21, 22, 23, 24,
                //         // 지름길
                //         29, 20, // P
                //         35, // 센터
                //         30, 25, 15, // 다른 분기 (=원래 길)
                //         35 // finish 를 35 로 가정
                // };
                // branchTable = new int[][] {
                //         // 외곽 순환
                //         { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 },
                //         { 5, 6 }, { 6, 7 }, { 7, 8 }, { 8, 9 }, { 9, 10 },
                //         { 10, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 }, { 14, 15 },
                //         { 15, 16 }, { 16, 17 }, { 17, 18 }, { 18, 19 }, { 19, 20 },
                //         { 20, 21 }, { 21, 22 }, { 22, 23 }, { 23, 24 }, { 24, 0 },

                //         // 0번 꼭짓점에서 센터로
                //         { 0, 25 }, { 25, 30 }, { 30, 35 },
                //         // 5번 꼭짓점에서 센터로
                //         { 5, 26 }, { 26, 31 }, { 31, 35 },
                //         // 10번 꼭짓점에서 센터로
                //         { 10, 27 }, { 27, 32 }, { 32, 35 },
                //         // 15번 꼭짓점에서 센터로
                //         { 15, 28 }, { 28, 33 }, { 33, 35 },
                //         // 20번 꼭짓점에서 센터로
                //         { 20, 29 }, { 29, 34 }, { 34, 35 }
                // };

                // noCatchSet = java.util.Set.of(0/* start */);
                break;

            }

            case HEXAGON: {

                /*
                 * 1) 중심(cx, cy)과 “바깥 꼭짓점” 반지름 r 계산
                 */
                double m = 50;
                double os = 900;
                int cx = (int) (m + os / 2);
                int cy = (int) (m + os / 2);
                int r = (int) (os * 0.42); // 외곽 꼭짓점까지의 반지름

                /*
                 * 2) 외곽 30칸(인덱스 0..29)을 6각형 둘레 형태로 배치
                 * - 시작 각도 = '왼쪽 위' 위치
                 * - 이후 시계 방향으로 30칸 균등 분할
                 */
                double startAngle = Math.PI / 2 + Math.PI / 6 + 2 * Math.PI / 6;
                for (int v = 0; v < 6; v++) {
                    double theta1 = startAngle + 2 * Math.PI * v / 6;
                    double theta2 = startAngle + 2 * Math.PI * (v + 1) / 6;
                    int base = v * 5; // 0,5,10,15,20,25

                    // “v번째 꼭짓점” 좌표
                    boardPoints[base] = new BoardPoint(
                            (int) (cx + r * Math.cos(theta1)),
                            (int) (cy - r * Math.sin(theta1)));

                    // 그 꼭짓점과 그 다음 꼭짓점 사이에 4칸씩 균등분할해서 할당
                    for (int j = 1; j <= 4; j++) {
                        double t = j / 5.0;
                        double x = cx + r * ((1 - t) * Math.cos(theta1) + t * Math.cos(theta2));
                        double y = cy - r * ((1 - t) * Math.sin(theta1) + t * Math.sin(theta2));
                        boardPoints[base + j] = new BoardPoint((int) x, (int) y);
                    }
                }
                // 이 시점에 boardPoints[0..29]가 외곽 6각형 둘레가 됩니다.

                /*
                 * 3) 인덱스 42를 “정 중앙”으로 배치
                 */
                boardPoints[42] = new BoardPoint(cx, cy);

                /*
                 * 4) “꼭짓점 → 중앙(42)” 방향 경로를 1/3, 2/3 지점에 할당:
                 * - 인덱스 30..35 = 6각형 꼭짓점(0,5,10,15,20,25)에서 중앙까지 1/3 지점
                 * - 인덱스 36..41 = 위 1/3 지점에서 중앙까지 2/3 지점
                 */
                for (int v = 0; v < 6; v++) {
                    int vertexIdx = v * 5; // 0,5,10,15,20,25
                    int idx1 = 30 + v; // 30..35
                    int idx2 = 36 + v; // 36..41

                    BoardPoint pVert = boardPoints[vertexIdx];
                    // 1/3 지점
                    boardPoints[idx1] = new BoardPoint(
                            (pVert.x * 2 + cx) / 3,
                            (pVert.y * 2 + cy) / 3);
                    // 2/3 지점
                    boardPoints[idx2] = new BoardPoint(
                            (pVert.x + cx * 2) / 3,
                            (pVert.y + cy * 2) / 3);
                }

                /*
                 * 5) (필요시) 엣지 리스트(edges)도 함께 설정
                 * -> Canvas(그리기)에서 이어서 직선을 그릴 때 사용
                 */
                ArrayList<int[]> edges = new ArrayList<>();
                // 외곽 둘레 0..29 사이 연결
                for (int i = 0; i < 30; i++) {
                    edges.add(new int[] { i, (i + 1) % 30 });
                }
                // 각 꼭짓점에서 1/3(idx1) → 2/3(idx2) → 중앙(42)
                for (int v = 0; v < 6; v++) {
                    int vertexIdx = v * 5; // 0,5,10,15,20,25
                    int idx1 = 30 + v; // 30..35
                    int idx2 = 36 + v; // 36..41
                    edges.add(new int[] { vertexIdx, idx1 });
                    edges.add(new int[] { idx1, idx2 });
                    edges.add(new int[] { idx2, 42 });
                }
                this.pentagonEdges = edges; // 필드 이름은 pentagonEdges이지만, HEXAGON에서도 활용

                /*
                 * 
                 */
                // ───────────────────────────────────────────────
                // 7) 이동 경로(mainRoute)와 분기(branchTable), noCatchSet 설정
                // ───────────────────────────────────────────────
                // mainRoute = new int[] {
                //         // 30칸 외곽 (0..29) → index 순서대로 “시계방향” 배열
                //         5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                //         15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                //         25, 26, 27, 28, 29, 0, 1, 2, 3, 4,
                //         // (외곽 끝에서) → 1/3(35) → 2/3(40) → 1/3(25) → 1/3(30) → 중앙(42)
                //         35, 40, 25, 30, 42
                // };
                // branchTable = new int[][] {
                //         { 0, 30 }, { 30, 36 }, { 36, 42 },
                //         { 5, 31 }, { 31, 40 }, { 40, 42 },
                //         { 10, 32 }, { 32, 41 }, { 41, 42 },
                //         { 15, 33 }, { 33, 37 }, { 37, 42 },
                //         { 20, 34 }, { 34, 38 }, { 38, 42 },
                //         { 25, 35 }, { 35, 39 }, { 39, 42 }
                // };
                // noCatchSet = java.util.Set.of(5); // 헥사곤 시작은 인덱스 5 (⑤번 꼭짓점)

                break;
            }

        }
    }

    public BoardPoint getBoardPoint(int i) {
        return (i >= 0 && i < boardPoints.length) ? boardPoints[i] : null;
    }

    private int getPreviousPosition(int currentPos) {
        if (boardType == BoardType.RECTANGLE) {
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
        } else if (boardType == BoardType.PENTAGON) {
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
        } else if (boardType == BoardType.HEXAGON) {
            if (currentPos == 0)
                return 29;

            // Path 1: 5-31-37-42-40-34-20
            else if (currentPos == 31)
                return 5;
            else if (currentPos == 37)
                return 31;
            else if (currentPos == 40)
                return 42;
            else if (currentPos == 34)
                return 40;
            else if (currentPos == 20)
                return 34;

            // Path 2: 10-32-38-42-41-35-25
            else if (currentPos == 32)
                return 10;
            else if (currentPos == 38)
                return 32;
            else if (currentPos == 41)
                return 42;
            else if (currentPos == 35)
                return 41;
            else if (currentPos == 25)
                return 35;

            // Path 3: 15-33-37-42-36-30-43
            else if (currentPos == 33)
                return 15;
            else if (currentPos == 37)
                return 33;
            else if (currentPos == 36)
                return 42;
            else if (currentPos == 30)
                return 36;

            /*
            * TBA: More detailed rules needed
            */
            if (currentPos == 25)
                return 35;
            if (currentPos == 42)
                return 38;

            else
                return currentPos - 1;
        }
        return -1;
    }

    public int calculateTargetPosition(Piece piece, int steps) {
        int originalPos = piece.getCurrentPositionIndex();
        int currentSimulationPos = originalPos;

        switch (boardType) {
            case RECTANGLE:
                if (piece.isFinished()) {
                    return this.finishPointIndex;
                }
                if (steps == -1) {
                    if (originalPos == 0) {
                        return 20;
                    }
                    return getPreviousPosition(originalPos);
                }

                for (int i = 0; i < steps; i++) {
                    if (currentSimulationPos == this.finishPointIndex) {
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
                        nextPosAfterOneStep = this.getFinishPointIndex(); // End of outer -> Finish
                    else if (currentSimulationPos == 30)
                        nextPosAfterOneStep = this.getFinishPointIndex(); // Point before Finish -> Finish

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
                if (currentSimulationPos > this.finishPointIndex) {
                    currentSimulationPos = this.finishPointIndex;
                }
                return currentSimulationPos;

            case PENTAGON:
                if (piece.isFinished()) {
                    return this.finishPointIndex;
                }
                if (steps == -1) {
                    if (originalPos == 0) {
                        return 24;
                    }
                    return getPreviousPosition(originalPos);
                }

                for (int i = 0; i < steps; i++) {
                    if (currentSimulationPos == this.finishPointIndex) {
                        break;
                    }
                    int nextPosAfterOneStep = -1; // initialized to negative value

                    if (currentSimulationPos == 35) { // Center
                        if (originalPos == 5 || originalPos == 26 || originalPos == 31 || originalPos == 10
                                || originalPos == 27 || originalPos == 32) {
                            /*
                             * Path: 5, 26, 31 to Center
                             * Path: 10, 27, 32 to Center
                             */
                            nextPosAfterOneStep = 34;
                        } else {
                            /* 
                             * Came from 15 originally
                             * Started at 35
                            */
                            nextPosAfterOneStep = 30; // Path: 35->30->25
                        }
                    } else if (currentSimulationPos == 24)
                        nextPosAfterOneStep = this.getFinishPointIndex(); // End of outer -> Finish

                    // Path 1: 5->26->31->35
                    else if (currentSimulationPos == 26)
                        nextPosAfterOneStep = 31;
                    else if (currentSimulationPos == 31)
                        nextPosAfterOneStep = 35;

                    // Path 2: 10->27->32->35
                    else if (currentSimulationPos == 27)
                        nextPosAfterOneStep = 32;
                    else if (currentSimulationPos == 32)
                        nextPosAfterOneStep = 35;

                    // Path 3: 15->28->33->35
                    else if (currentSimulationPos == 28)
                        nextPosAfterOneStep = 33;
                    else if (currentSimulationPos == 33)
                        nextPosAfterOneStep = 35;

                    // Path 4: 35->34->29->20
                    else if (currentSimulationPos == 34)
                        nextPosAfterOneStep = 29;
                    else if (currentSimulationPos == 29)
                        nextPosAfterOneStep = 20;

                    // Path 5: 35->30->25->36(Finish)
                    else if (currentSimulationPos == 30)
                        nextPosAfterOneStep = 25;
                    else if (currentSimulationPos == 25)
                        nextPosAfterOneStep = 36;

                    // Start of Path 1
                    else if (i == 0 && originalPos == 5) {
                        nextPosAfterOneStep = 26;
                    }
                    // Start of Path 2
                    else if (i == 0 && originalPos == 10) {
                        nextPosAfterOneStep = 27;
                    }
                    // Start of Path 3
                    else if (i == 0 && originalPos == 15) {
                        nextPosAfterOneStep = 28;
                    }
                    // General cases: Just moves one more index
                    else {
                        nextPosAfterOneStep = currentSimulationPos + 1; // Linear move
                    }
                    currentSimulationPos = nextPosAfterOneStep;
                }
                if (currentSimulationPos > this.finishPointIndex) {
                    currentSimulationPos = this.finishPointIndex;
                }
                return currentSimulationPos;
            case HEXAGON:
                if (piece.isFinished()) {
                    return this.finishPointIndex;
                }
                if (steps == -1) {
                    if (originalPos == 0) {
                        return 29;
                    }
                    return getPreviousPosition(originalPos);
                }

                for (int i = 0; i < steps; i++) {
                    if (currentSimulationPos == this.finishPointIndex) {
                        break;
                    }
                    int nextPosAfterOneStep = -1; // initialized to negative value

                    if (currentSimulationPos == 42) { // Center
                        if (originalPos == 5 || originalPos == 31 || originalPos == 37) {
                            /*
                             * Path: 5, 31, 47 to Center
                             */
                            nextPosAfterOneStep = 40;
                        } else if (originalPos == 10 || originalPos == 32 || originalPos == 38) {
                            /* 
                             * Path: 10, 32, 38 to Center
                            */
                            nextPosAfterOneStep = 41;
                        } else {
                            /* 
                             * Came from 15 originally
                             * Started at 42
                            */
                            nextPosAfterOneStep = 36;
                        }
                    } else if (currentSimulationPos == 29)
                        nextPosAfterOneStep = this.getFinishPointIndex(); // End of outer -> Finish

                    // Path 1-1: 5->31->37->42
                    else if (currentSimulationPos == 31)
                        nextPosAfterOneStep = 37;
                    else if (currentSimulationPos == 37)
                        nextPosAfterOneStep = 42;
                    // Path 1-2: 42->40->34->20
                    else if (currentSimulationPos == 40)
                        nextPosAfterOneStep = 34;
                    else if (currentSimulationPos == 34)
                        nextPosAfterOneStep = 20;

                    // Path 2-1: 10->32->38->42
                    else if (currentSimulationPos == 32)
                        nextPosAfterOneStep = 38;
                    else if (currentSimulationPos == 38)
                        nextPosAfterOneStep = 42;
                    // Path 2-2: 42->41->35->25
                    else if (currentSimulationPos == 41)
                        nextPosAfterOneStep = 35;
                    else if (currentSimulationPos == 35)
                        nextPosAfterOneStep = 25;

                    // Path 3: 15->33->37->42
                    else if (currentSimulationPos == 33)
                        nextPosAfterOneStep = 37;
                    else if (currentSimulationPos == 37)
                        nextPosAfterOneStep = 42;

                    // Path 4: 42->36->30->43(Finish)
                    else if (currentSimulationPos == 36)
                        nextPosAfterOneStep = 30;
                    else if (currentSimulationPos == 30)
                        nextPosAfterOneStep = 43;

                    // Start of Path 1
                    else if (i == 0 && originalPos == 5) {
                        nextPosAfterOneStep = 31;
                    }
                    // Start of Path 2
                    else if (i == 0 && originalPos == 10) {
                        nextPosAfterOneStep = 32;
                    }
                    // Start of Path 3
                    else if (i == 0 && originalPos == 15) {
                        nextPosAfterOneStep = 33;
                    }
                    // General cases: Just moves one more index
                    else {
                        nextPosAfterOneStep = currentSimulationPos + 1; // Linear move
                    }
                    currentSimulationPos = nextPosAfterOneStep;
                }
                if (currentSimulationPos > this.finishPointIndex) {
                    currentSimulationPos = this.finishPointIndex;
                }
                return currentSimulationPos;
            default:
                return 0;
        }

    }

    public List<Piece> findOpponentPiecesAt(int targetPosition, Team currentPlayerTeam, List<Team> allTeams) {
        List<Piece> opponentLeadersOrIndividualsAtPos = new ArrayList<>();
        if (targetPosition == this.startPointIndex || targetPosition == this.finishPointIndex) { // No catches at start/finish
            return opponentLeadersOrIndividualsAtPos;
        }
        for (Team team : allTeams) {
            if (team.getId() != currentPlayerTeam.getId()) {
                // Use getInteractivePiecesAt to get only leaders or individual pieces of the opponent team
                opponentLeadersOrIndividualsAtPos.addAll(team.getInteractivePiecesAt(targetPosition));
            }
        }
        return opponentLeadersOrIndividualsAtPos;
    }

    public void resetPiecesToStart(List<Piece> piecesToReset) {
        // The Piece.reset() method now handles detaching from groups and resetting
        // stacked pieces if it's a leader.
        for (Piece piece : piecesToReset) {
            piece.reset();
        }
    }

    public int getPointCount() {
        return boardPoints.length;
    }

    public List<int[]> getEdges() {
        return this.pentagonEdges; // 사각형이면 null
    }

    public boolean isValidMoveStart(Piece piece, int steps) {
        // piece.canMove() now correctly checks if the piece is stacked (and thus unmovable independently)
        return piece.canMove(steps, this);
    }
}