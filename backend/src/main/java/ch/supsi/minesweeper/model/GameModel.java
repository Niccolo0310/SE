package ch.supsi.minesweeper.model;

import java.util.*;

public class GameModel extends AbstractModel
        implements GameEventHandler, PlayerEventHandler {

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

    private GameModel() {
        super();
        initField();
    }

    public static GameModel getInstance() {
        if (myself == null) {
            myself = new GameModel();
        }
        return myself;
    }


    public int getRows()     { return rows; }
    public int getCols()     { return cols; }
    public int getMines()    { return mines; }

    public void setMines(int mines) {
        if (mines < 1 || mines >= rows * cols) {
            throw new IllegalArgumentException("Numero di mine invalido: " + mines);
        }
        this.mines = mines;
    }

    public boolean isStarted()            { return started; }
    public boolean isRevealed(int r, int c) { return revealed[r][c]; }
    public boolean hasMineAt(int r, int c)  { return hasMine[r][c]; }
    public boolean isFlagged(int r, int c)  { return flagged[r][c]; }
    public int getNeighborCountAt(int r, int c) { return neighborCount[r][c]; }

    public int getFlaggedCount() {
        int cnt = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (flagged[r][c]) cnt++;
            }
        }
        return cnt;
    }

    private void initField() {
        hasMine       = new boolean[rows][cols];
        neighborCount = new int[rows][cols];
        revealed      = new boolean[rows][cols];
        flagged       = new boolean[rows][cols];
        revealedCount = 0;
    }

    @Override
    public void newGame() {
        initField();
        generateField();
        started = true;
    }


    private void generateField() {
        // reset
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                hasMine[r][c]       = false;
                neighborCount[r][c] = 0;
                revealed[r][c]      = false;
                flagged[r][c]       = false;
            }
        }
        //  piazza mine
        Random rnd = new Random();
        int placed = 0;
        while (placed < mines) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);
            if (!hasMine[r][c]) {
                hasMine[r][c] = true;
                placed++;
            }
        }
        // calcola bombe adiacenti
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (hasMine[r][c]) continue;
                int cnt = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && hasMine[nr][nc]) {
                            cnt++;
                        }
                    }
                }
                neighborCount[r][c] = cnt;
            }
        }
    }
    public List<int[]> revealArea(int r, int c) {
        List<int[]> opened = new ArrayList<>();
        if (revealed[r][c] || flagged[r][c]) return opened;

        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{r, c});
        revealed[r][c] = true;

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int row = pos[0], col = pos[1];
            opened.add(pos);

            if (hasMine[row][col]) continue;
            if (neighborCount[row][col] != 0) continue;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                    if (!revealed[nr][nc] && !flagged[nr][nc]) {
                        revealed[nr][nc] = true;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }
        revealedCount += opened.size();
        return opened;
    }


    public void toggleFlag(int r, int c) {
        if (!revealed[r][c]) {
            flagged[r][c] = !flagged[r][c];
        }
    }

    public boolean isWin() {
        return revealedCount == (rows * cols - mines);
    }

    public void loadFromState(GameStateJson state) {
        this.mines = state.getMines();

        // Ricreo matrici e copio campi
        initField();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                this.hasMine[r][c]  = state.getHasMine()[r][c];
                this.revealed[r][c] = state.getRevealed()[r][c];
                this.flagged[r][c]  = state.getFlagged()[r][c];
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (hasMine[r][c]) {
                    neighborCount[r][c] = 0;
                } else {
                    int cnt = 0;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = r + dr, nc = c + dc;
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && hasMine[nr][nc]) {
                                cnt++;
                            }
                        }
                    }
                    neighborCount[r][c] = cnt;
                }
            }
        }
        // Riconta revealedCount (escludendo le mine)
        revealedCount = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (revealed[r][c] && !hasMine[r][c]) {
                    revealedCount++;
                }
            }
        }
    }

    public void markStarted() {
        this.started = true;
    }

    @Override
    public void move() {
    }


    @Override
    public void save() {
        throw new UnsupportedOperationException(
                "Usa JsonGamePersistence o un altro GamePersistence per salvare."
        );
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException(
                "Usa JsonGamePersistence o un altro GamePersistence per caricare."
        );
    }

    @Override
    public void help() {
    }

    @Override
    public void about() {
    }

    @Override
    public void win() {
    }

    @Override
    public void lose() {
    }

    public GameActionResult handleClick(int r, int c, boolean rightClick) {
        //se la partita non è iniziata, non fare nulla
        if (!isStarted()) {
            return GameActionResult.none();
        }

        if (rightClick) {
            // click destro toglie bandiera
            if (!isRevealed(r, c)) {
                toggleFlag(r, c);
                return GameActionResult.flag();
            } else {
                return GameActionResult.none();
            }
        }

        // click sinistro se ce' bandiera, non aprire
        if (isFlagged(r, c)) {
            return GameActionResult.none();
        }

        //apri area secondo la logica esistente
        List<int[]> opened = revealArea(r, c);

        //stato finale da comunicare alla UI
        boolean mineHit = hasMineAt(r, c);  // se la cella cliccata era bomba
        boolean win     = isWin();          // controlla vittoria

        return GameActionResult.reveal(opened, mineHit, win);
    }
}