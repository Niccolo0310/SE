package ch.supsi.minesweeper.uimodel;

import ch.supsi.minesweeper.application.GameService;
import ch.supsi.minesweeper.model.AbstractModel;
import ch.supsi.minesweeper.model.GameActionResult;
import ch.supsi.minesweeper.model.GameModel;

import java.io.IOException;
import java.nio.file.Path;


 //Compone il GameService (BE) e delega al GameModel di dominio
 //esponendo solo API di lettura per le view

public class GameViewModel extends AbstractModel {

    private final GameService service; // BE
    private final GameModel   domain;  //(nascosto alle view)

    public GameViewModel(GameService service) {
        this.service = service;
        this.domain  = service.model();
    }

    //Letture read-only usate dalla UI (delegate al domain)
    public int getRows()                 { return domain.getRows(); }
    public int getCols()                 { return domain.getCols(); }
    public int getMines()                { return domain.getMines(); }
    public boolean isStarted()           { return domain.isStarted(); }
    public boolean isRevealed(int r,int c){ return domain.isRevealed(r,c); }
    public boolean hasMineAt(int r,int c){ return domain.hasMineAt(r,c); }
    public boolean isFlagged(int r,int c){ return domain.isFlagged(r,c); }
    public int getNeighborCountAt(int r,int c){ return domain.getNeighborCountAt(r,c); }
    public int getFlaggedCount()         { return domain.getFlaggedCount(); }
    public boolean isWin()               { return domain.isWin(); }

    // delegate al service
    public void newGame(int bombs)                       { service.newGame(bombs); }
    public GameActionResult handleClick(int r,int c,boolean right) { return service.handleClick(r,c,right); }
    public void save(Path p) throws IOException          { service.save(p); }
    public void load(Path p) throws IOException          { service.load(p); }
}