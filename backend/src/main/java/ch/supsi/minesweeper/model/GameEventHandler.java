package ch.supsi.minesweeper.model;


public interface GameEventHandler {
    void newGame();
    void save();
    void load();
    void help();
    void about();
    void win();
    void lose();
}