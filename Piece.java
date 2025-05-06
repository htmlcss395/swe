class Piece {
    /*
     * 0 = off board
     * [1, 29] = inside the board
     */
    private int position = 0;

    public void move(int steps) {
        position += steps;
        if (position >= 30)
            position = 30; // Piece has to go off the board to win.
    }

    public int getPosition() {
        return position;
    }
}