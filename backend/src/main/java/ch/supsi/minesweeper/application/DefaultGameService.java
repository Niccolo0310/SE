package ch.supsi.minesweeper.application;

import ch.supsi.minesweeper.model.GameActionResult;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.persistence.GameRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DefaultGameService implements GameService {

    private final GameModel model;
    private final GameRepository repository;
    private final Random rnd = new Random();

    public DefaultGameService(GameModel model, GameRepository repository) {
        this.model = model;
        this.repository = repository;
    }

    @Override
    public GameModel model() {
        return model;
    }

    @Override
    public void newGame(int bombs) {
        // valida mines
        int max = model.getRows() * model.getCols() - 1;
        int safeBombs = Math.max(1, Math.min(bombs, max));
        model.setMines(safeBombs);

        initField();
        placeMines();
        computeNeighborCounts();
        model.setStarted(true);
    }

    @Override
    public GameActionResult handleClick(int r, int c, boolean rightClick) {
        if (!model.isStarted()) return GameActionResult.none();

        if (rightClick) {
            if (!model.isRevealed(r, c)) {
                toggleFlag(r, c);
                return GameActionResult.flag();
            } else {
                return GameActionResult.none();
            }
        }

        if (model.isFlagged(r, c)) return GameActionResult.none();

        var opened = revealArea(r, c);
        boolean mineHit = model.hasMineAt(r, c);
        boolean win     = model.isWin();

        if (mineHit || win) {
            model.setStarted(false);
        }


        return GameActionResult.reveal(opened, mineHit, win);
    }

    @Override
    public void save(Path path) throws IOException {
        repository.save(model, path);
    }

    @Override
    public void load(Path path) throws IOException {
        repository.load(model, path);
        recomputeNeighborCountsFromMines();
        recomputeRevealedCount();
        model.setStarted(true);
    }


    private void initField() {
        int rows = model.getRows(), cols = model.getCols();
        model.setHasMine(new boolean[rows][cols]);
        model.setNeighborCount(new int[rows][cols]);
        model.setRevealed(new boolean[rows][cols]);
        model.setFlagged(new boolean[rows][cols]);
        model.setRevealedCount(0);
    }
    private void placeMines() {
        int rows = model.getRows(), cols = model.getCols();
        boolean[][] hasMine = model.getHasMine();

        int placed = 0, target = model.getMines();
        while (placed < target) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);
            if (!hasMine[r][c]) {
                hasMine[r][c] = true;
                placed++;
            }
        }
    }

    private void computeNeighborCounts() {
        int rows = model.getRows(), cols = model.getCols();
        boolean[][] mines = model.getHasMine();
        int[][] nc = model.getNeighborCount();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c]) { nc[r][c] = 0; continue; }
                int cnt = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, ncC = c + dc;
                        if (nr >= 0 && nr < rows && ncC >= 0 && ncC < cols && mines[nr][ncC]) cnt++;
                    }
                }
                nc[r][c] = cnt;
            }
        }
    }

    private void recomputeNeighborCountsFromMines() {
        // utile se il file salvato non ha neighborCount persistito
        boolean[][] hasMine = model.getHasMine();
        int rows = model.getRows(), cols = model.getCols();
        int[][] nc = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (hasMine[r][c]) { nc[r][c] = 0; continue; }
                int cnt = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc2 = c + dc;
                        if (nr >= 0 && nr < rows && nc2 >= 0 && nc2 < cols && hasMine[nr][nc2]) cnt++;
                    }
                }
                nc[r][c] = cnt;
            }
        }
        model.setNeighborCount(nc);
    }

    private void recomputeRevealedCount() {
        int rows = model.getRows(), cols = model.getCols();
        boolean[][] rev = model.getRevealed();
        boolean[][] mine = model.getHasMine();
        int count = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (rev[r][c] && !mine[r][c]) count++;
        model.setRevealedCount(count);
    }

    private void toggleFlag(int r, int c) {
        boolean[][] rev = model.getRevealed();
        boolean[][] flg = model.getFlagged();
        if (!rev[r][c]) flg[r][c] = !flg[r][c];
    }

    private List<int[]> revealArea(int r, int c) {
        List<int[]> opened = new ArrayList<>();
        boolean[][] rev = model.getRevealed();
        boolean[][] flg = model.getFlagged();
        boolean[][] mine = model.getHasMine();
        int[][] nc = model.getNeighborCount();

        if (rev[r][c] || flg[r][c]) return opened;

        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{r, c});
        rev[r][c] = true;

        while (!q.isEmpty()) {
            int[] pos = q.poll();
            int row = pos[0], col = pos[1];
            opened.add(pos);

            if (mine[row][col]) continue;
            if (nc[row][col] != 0) continue;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, ncC = col + dc;
                    if (nr < 0 || nr >= model.getRows() || ncC < 0 || ncC >= model.getCols()) continue;
                    if (!rev[nr][ncC] && !flg[nr][ncC]) {
                        rev[nr][ncC] = true;
                        q.add(new int[]{nr, ncC});
                    }
                }
            }
        }
        model.setRevealedCount(model.getRevealedCount() + opened.size());
        return opened;
    }
}