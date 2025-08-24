package ch.supsi.minesweeper.repository;

import ch.supsi.minesweeper.model.GameModel;

import java.io.IOException;
import java.nio.file.Path;


public interface GameRepository {
    void save(GameModel model, Path path) throws IOException;
    void load(GameModel model, Path path) throws IOException;
}