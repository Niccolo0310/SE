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
        // Delego tutta la logica al dominio (no UI qui)
        return model.handleClick(row, col, rightClick);
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