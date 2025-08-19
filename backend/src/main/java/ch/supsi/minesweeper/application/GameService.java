package ch.supsi.minesweeper.application;

import ch.supsi.minesweeper.model.GameActionResult;
import ch.supsi.minesweeper.model.GameModel;

import java.io.IOException;
import java.nio.file.Path;

public interface GameService {
    //Espone il model per le view del FE (read-only via API pubbliche del model)
    GameModel model();

    //Avvia una nuova partita con il numero di bombe deciso dal FE (preferences)
    void newGame(int bombs);

    // Unica entrypoint per i click provenienti dalla view
    GameActionResult handleClick(int row, int col, boolean rightClick);

    // Persistenza astratta dietro repository
    void save(Path path) throws IOException;
    void load(Path path) throws IOException;
}