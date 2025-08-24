package ch.supsi.minesweeper.infrastructure;

import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.persistence.GameRepository;
import ch.supsi.minesweeper.persistence.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class JsonGameRepository implements GameRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void save(GameModel model, Path path) throws IOException {
        GameState dto = new GameState();
        dto.mines    = model.getMines();
        dto.hasMine  = model.getHasMine();
        dto.revealed = model.getRevealed();
        dto.flagged  = model.getFlagged();

        writeJson(dto, path);
    }

    @Override
    public void load(GameModel model, Path path) throws IOException {

        //leggi il JSON
        GameState dto = readJson(path); // tua funzione interna

        //dati grezzi nel model
        model.setMines(dto.mines);
        model.setHasMine(dto.hasMine);
        model.setRevealed(dto.revealed);
        model.setFlagged(dto.flagged);

    }


    private void writeJson(GameState dto, Path path) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), dto);
    }

    private GameState readJson(Path path) throws IOException {
        return MAPPER.readValue(path.toFile(), GameState.class);
    }
}