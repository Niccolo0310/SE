package ch.supsi.minesweeper.application;

import ch.supsi.minesweeper.infrastructure.JsonGameRepository;
import ch.supsi.minesweeper.model.GameModel;

public final class Services {
    private static GameService DEFAULT;

    private Services() {}

    public static synchronized GameService defaultService() {
        if (DEFAULT == null) {
            DEFAULT = new DefaultGameService(
                    new GameModel(),
                    new JsonGameRepository()
            );
        }
        return DEFAULT;
    }
}