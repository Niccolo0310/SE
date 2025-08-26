package ch.supsi.minesweeper.controller;


public interface GameEventHandler {
    void newGame();
    void save();
    void load();
    void help();
    void about();
    void win();
    void lose();
    void open();
    void saveAs();
    void exit();
}