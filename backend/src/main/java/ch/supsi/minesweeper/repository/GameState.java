package ch.supsi.minesweeper.repository;

public class GameState {
    public int rows;
    public int cols;
    public int mines;
    public boolean[][] hasMine;
    public boolean[][] revealed;
    public boolean[][] flagged;
}