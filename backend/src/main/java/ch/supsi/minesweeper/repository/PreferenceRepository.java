package ch.supsi.minesweeper.repository;

import ch.supsi.minesweeper.model.Preferences;
import java.io.IOException;

public interface PreferenceRepository {
    Preferences load() throws IOException;
    void save(Preferences prefs) throws IOException;
}