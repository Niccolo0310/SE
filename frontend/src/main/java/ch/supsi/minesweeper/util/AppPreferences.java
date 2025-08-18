package ch.supsi.minesweeper.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppPreferences {

    //config.properties dentro al JAR (backend/src/main/resources/config.properties)
    private static final String CLASSPATH_FILE = "/config.properties";

    //in HOME dell’utente, in una cartella nascosta “.minesweeper”
    private static final Path USER_FILE = Paths.get(
            System.getProperty("user.home"),
            ".minesweeper",
            "config.properties"
    );

    private static final String KEY_BOMBS = "bombs";
    private static final String KEY_LANG  = "lang";
    private static final int    DEF_BOMBS = 10;
    private static final String DEF_LANG  = "en";
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppPreferences.class.getResourceAsStream(CLASSPATH_FILE)) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("Warning: non trovato " + CLASSPATH_FILE + " nel classpath.");
            }
        } catch (IOException e) {
            System.err.println("Errore caricamento risorse interne: " + e.getMessage());
        }

        Path userPath = USER_FILE;
        if (Files.exists(userPath)) {
            try (InputStream in = Files.newInputStream(userPath)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Impossibile leggere " + userPath + ": " + e.getMessage());
            }
        }
    }

    private AppPreferences() {  }

    public static int getBombs() {
        return getInt(KEY_BOMBS, DEF_BOMBS);
    }

    public static String getLang() {
        return props.getProperty(KEY_LANG, DEF_LANG);
    }

    public static void setBombs(int bombs) {
        props.setProperty(KEY_BOMBS, String.valueOf(bombs));
        save();
    }


    public static void setLang(String lang) {
        props.setProperty(KEY_LANG, lang);
        save();
    }

    private static void save() {
        try {
            // se non esiste la cartella ~/.minesweeper, la creo
            Files.createDirectories(USER_FILE.getParent());
            try (OutputStream out = Files.newOutputStream(USER_FILE)) {
                props.store(out, "Minesweeper preferences – cold reload");
            }
        } catch (IOException e) {
            System.err.println("Impossibile scrivere " + USER_FILE + ": " + e.getMessage());
        }
    }

    private static int getInt(String key, int defVal) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defVal)).trim());
        } catch (NumberFormatException e) {
            return defVal;
        }
    }
}