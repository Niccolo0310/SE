package ch.supsi.minesweeper.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameStateJson {
    private final int rows;
    private final int cols;
    private final int mines;
    private final boolean[][] hasMine;
    private final boolean[][] revealed;
    private final boolean[][] flagged;

    @JsonCreator
    public GameStateJson(
            @JsonProperty("rows")    int rows,
            @JsonProperty("cols")    int cols,
            @JsonProperty("mines")   int mines,
            @JsonProperty("hasMine") boolean[][] hasMine,
            @JsonProperty("revealed")boolean[][] revealed,
            @JsonProperty("flagged") boolean[][] flagged
    ) {
        this.rows     = rows;
        this.cols     = cols;
        this.mines    = mines;
        this.hasMine  = hasMine;
        this.revealed = revealed;
        this.flagged  = flagged;
    }

    public GameStateJson(GameModel model) {
        this.rows     = model.getRows();
        this.cols     = model.getCols();
        this.mines    = model.getMines();
        this.hasMine  = new boolean[rows][cols];
        this.revealed = new boolean[rows][cols];
        this.flagged  = new boolean[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                hasMine[r][c]   = model.hasMineAt(r, c);
                revealed[r][c]  = model.isRevealed(r, c);
                flagged[r][c]   = model.isFlagged(r, c);
            }
        }
    }

    public int getMines()            { return mines; }
    public boolean[][] getHasMine()  { return hasMine; }
    public boolean[][] getRevealed() { return revealed; }
    public boolean[][] getFlagged()  { return flagged; }
}