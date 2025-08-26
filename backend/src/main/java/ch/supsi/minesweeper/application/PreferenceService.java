package ch.supsi.minesweeper.application;

import ch.supsi.minesweeper.model.Preferences;
import ch.supsi.minesweeper.repository.AppPreferencesRepository;
import ch.supsi.minesweeper.repository.PreferenceRepository;

import java.io.IOException;

public class PreferenceService {
    private static PreferenceService INSTANCE;

    private final PreferenceRepository repo;
    private Preferences cache;

    private PreferenceService() {
        this.repo = new AppPreferencesRepository();
        try {
            this.cache = repo.load();
        } catch (IOException e) {
            this.cache = new Preferences(10, "en");
        }
    }

    public static synchronized PreferenceService get() {
        if (INSTANCE == null) INSTANCE = new PreferenceService();
        return INSTANCE;
    }


    public int getBombs() { return cache.getBombs(); }
    public String getLang() { return cache.getLang(); }

    public void setBombs(int bombs) { cache.setBombs(bombs); persist(); }
    public void setLang(String lang) { cache.setLang(lang); persist(); }

    private void persist() {
        try { repo.save(cache); } catch (IOException ignored) {}
    }
}