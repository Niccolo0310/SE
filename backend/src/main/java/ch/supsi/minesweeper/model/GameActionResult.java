package ch.supsi.minesweeper.model;

import java.util.Collections;
import java.util.List;

public class GameActionResult {

    public enum ActionType { NONE, FLAG, REVEAL }

    private final ActionType type;
    private final List<int[]> opened; // (r,c) aperte in questo click
    private final boolean mineHit;
    private final boolean win;

    private GameActionResult(ActionType type, List<int[]> opened, boolean mineHit, boolean win) {
        this.type = type;
        this.opened = opened == null ? Collections.emptyList() : opened;
        this.mineHit = mineHit;
        this.win = win;
    }

    public static GameActionResult none() { return new GameActionResult(ActionType.NONE, List.of(), false, false); }
    public static GameActionResult flag() { return new GameActionResult(ActionType.FLAG, List.of(), false, false); }
    public static GameActionResult reveal(List<int[]> opened, boolean mineHit, boolean win) {
        return new GameActionResult(ActionType.REVEAL, opened, mineHit, win);
    }

    public ActionType getType() { return type; }
    public List<int[]> getOpened() { return opened; }
    public boolean isMineHit() { return mineHit; }
    public boolean isWin() { return win; }
}