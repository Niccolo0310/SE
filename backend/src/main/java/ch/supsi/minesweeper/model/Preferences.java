package ch.supsi.minesweeper.model;

public class Preferences {
    private int bombs;
    private String lang;

    public Preferences() { }
    public Preferences(int bombs, String lang) {
        this.bombs = bombs;
        this.lang = lang;
    }

    public int getBombs() { return bombs; }
    public void setBombs(int bombs) { this.bombs = bombs; }

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
}