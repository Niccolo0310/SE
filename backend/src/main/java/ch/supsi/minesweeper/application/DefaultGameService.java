package ch.supsi.minesweeper.application;

import ch.supsi.minesweeper.model.GameActionResult;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.persistence.GameRepository;

import java.io.IOException;
import java.nio.file.Path;

public class DefaultGameService implements GameService {

    private final GameModel model;
    private final GameRepository repository;

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
        model.setMines(bombs);
        model.newGame();
    }

    @Override
    public GameActionResult handleClick(int row, int col, boolean rightClick) {
        // se la partita non è iniziata non fare nulla
        if (!model.isStarted()) {
            return GameActionResult.none();
        }

        // Click destro: toggle bandiera (solo se non rivelata)
        if (rightClick) {
            if (!model.isRevealed(row, col)) {
                model.toggleFlag(row, col);
                return GameActionResult.flag();
            } else {
                return GameActionResult.none();
            }
        }

        // Click sinistro: se è flaggata, non aprire
        if (model.isFlagged(row, col)) {
            return GameActionResult.none();
        }

        // Apri area (BFS già nel model). La lista include la cella cliccata.
        var opened = model.revealArea(row, col);

        boolean mineHit = model.hasMineAt(row, col);
        boolean win     = !mineHit && model.isWin();

        return GameActionResult.reveal(opened, mineHit, win);
    }

    @Override
    public void save(Path path) throws IOException {
        repository.save(model, path);
    }

    @Override
    public void load(Path path) throws IOException {
        repository.load(model, path);
    }
}