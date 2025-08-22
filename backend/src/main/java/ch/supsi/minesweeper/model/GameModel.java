package ch.supsi.minesweeper.model;


public class GameModel extends AbstractModel {

    private static GameModel myself;
    private final int rows  = 9;
    private final int cols  = 9;
    private int mines = 10;
    private boolean[][] hasMine;
    private int[][]     neighborCount;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int revealedCount;
    private boolean started = false;

    public GameModel() {
        super();
        hasMine       = new boolean[rows][cols];
        neighborCount = new int[rows][cols];
        revealed      = new boolean[rows][cols];
        flagged       = new boolean[rows][cols];
        revealedCount = 0;

    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getMines() { return mines; }
    public void setMines(int mines) { this.mines = mines; }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public boolean[][] getHasMine() { return hasMine; }
    public void setHasMine(boolean[][] hasMine) { this.hasMine = hasMine; }

    public int[][] getNeighborCount() { return neighborCount; }
    public void setNeighborCount(int[][] neighborCount) { this.neighborCount = neighborCount; }

    public boolean[][] getRevealed() { return revealed; }
    public void setRevealed(boolean[][] revealed) { this.revealed = revealed; }

    public boolean[][] getFlagged() { return flagged; }
    public void setFlagged(boolean[][] flagged) { this.flagged = flagged; }

    public int getRevealedCount() { return revealedCount; }
    public void setRevealedCount(int revealedCount) { this.revealedCount = revealedCount; }

    // sola lettura
    public boolean isRevealed(int r, int c) { return revealed[r][c]; }
    public boolean hasMineAt(int r, int c)  { return hasMine[r][c]; }
    public boolean isFlagged(int r, int c)  { return flagged[r][c]; }
    public int getNeighborCountAt(int r, int c) { return neighborCount[r][c]; }

    // query consentite che non mutano lo stato
    public boolean isWin() { return revealedCount == (rows * cols - mines); }

    public int getFlaggedCount() {
        int cnt = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (flagged[r][c]) cnt++;
        return cnt;
    }



}