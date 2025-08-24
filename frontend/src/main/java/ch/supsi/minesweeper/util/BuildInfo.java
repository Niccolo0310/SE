package ch.supsi.minesweeper.util;

import java.io.IOException;
import java.util.Properties;

public class BuildInfo {
    private static final Properties props = new Properties();

    static {
        try {
            props.load(BuildInfo.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load app info", e);
        }
    }

    public static String getName()        { return props.getProperty("about.name", "Minesweeper"); }
    public static String getVersion()     { return props.getProperty("about.version", "0.0.1"); }
    public static String getDescription() { return props.getProperty("about.description", "Progetto sviluppato per il corso di Ingegneria del Software"); }
    public static String getAuthor()      { return props.getProperty("about.copyright", "SUPSI Memet Emre Yildirim, Niccolo Xhyra"); }
    public static String buildDate()      { return props.getProperty("about.buildDate", "unknown"); }
}