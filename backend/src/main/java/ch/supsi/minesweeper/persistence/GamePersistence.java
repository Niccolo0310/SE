package ch.supsi.minesweeper.persistence;

import ch.supsi.minesweeper.model.GameModel;

import java.io.IOException;
import java.nio.file.Path;


public interface GamePersistence {
    void save(GameModel model, Path path) throws IOException;
    void load(GameModel model, Path path) throws IOException;
}