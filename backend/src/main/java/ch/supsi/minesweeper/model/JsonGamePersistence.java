package ch.supsi.minesweeper.model;

import ch.supsi.minesweeper.persistence.GamePersistence;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class JsonGamePersistence implements GamePersistence {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void save(GameModel model, Path path) throws IOException {
        GameStateJson state = new GameStateJson(model);
        MAPPER.writeValue(path.toFile(), state);
    }

    @Override
    public void load(GameModel model, Path path) throws IOException {
        GameStateJson state = MAPPER.readValue(path.toFile(), GameStateJson.class);
        model.loadFromState(state);
        model.markStarted();
    }
}