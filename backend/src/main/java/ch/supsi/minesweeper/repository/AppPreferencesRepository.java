package ch.supsi.minesweeper.repository;

import ch.supsi.minesweeper.model.Preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppPreferencesRepository implements PreferenceRepository {

    private static final String CLASSPATH_FILE = "/config.properties";

    private static final Path USER_FILE = Paths.get(
            System.getProperty("user.home"),
            ".minesweeper",
            "config.properties"
    );

    private static final String KEY_BOMBS = "bombs";
    private static final String KEY_LANG  = "lang";
    private static final int    DEF_BOMBS = 10;
    private static final String DEF_LANG  = "en";


    @Override
    public Preferences load() throws IOException {
        Properties p = new Properties();

        try (InputStream in = getClass().getResourceAsStream(CLASSPATH_FILE)) {
            if (in != null) p.load(in);
        }

        if (Files.exists(USER_FILE)) {
            try (InputStream in = Files.newInputStream(USER_FILE)) {
                p.load(in);
            }
        }

        int bombs = parseInt(p.getProperty(KEY_BOMBS), DEF_BOMBS);
        String lang = p.getProperty(KEY_LANG, DEF_LANG);
        return new Preferences(bombs, lang);


    }
    @Override
    public void save(Preferences prefs) throws IOException {
        Properties p = new Properties();
        p.setProperty(KEY_BOMBS, String.valueOf(prefs.getBombs()));
        p.setProperty(KEY_LANG, prefs.getLang());

        Files.createDirectories(USER_FILE.getParent());
        try (OutputStream out = Files.newOutputStream(USER_FILE)) {
            p.store(out, "Minesweeper preferences");
        }
    }

    private static int parseInt(String s, int defVal) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return defVal; }
    }

}