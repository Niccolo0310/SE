package ch.supsi.minesweeper.controller;

import ch.supsi.minesweeper.application.PreferenceService;
import ch.supsi.minesweeper.application.Services;
import ch.supsi.minesweeper.uimodel.GameViewModel;
import ch.supsi.minesweeper.view.*;

import javafx.application.Platform;
import javafx.scene.control.Button;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.ResourceBundle;

public class GameController implements EventHandler {

    private static GameController myself;
    private final GameViewModel vm;

    private       List<DataView>    views;
    private final int               defaultBombs;
    private final ResourceBundle    bundle;
    // mostra win/lose una sola volta per partita
    private volatile boolean gameEndNotified = false;

    private GameBoardViewFxml boardView;
    private UiNotices uiNotices;
    private MenuBarViewFxml menuView;
    private UserFeedbackViewFxml feedbackView;


    private GameController() {

        var service    = Services.defaultService(); //recupera il GameService preconfigurato (model + repo) dalla factory Services
        this.vm = new GameViewModel(service);       //lo espone al FE tramite il ViewModel

        this.defaultBombs = PreferenceService.get().getBombs();
        this.bundle       = ResourceBundle.getBundle(
                "i18n.messages",
                Locale.forLanguageTag(PreferenceService.get().getLang()));
    }

    public static GameController getInstance() {
        if (myself == null) myself = new GameController();
        return myself;
    }

    public void initialize(List<DataView> views) {
        this.views = views;
        MenuController.getInstance().initialize(views);
        // save e save as restano disabilitate fino a newGame() o open()

        // cattura la board view
        for (DataView v : views) {
            if (v instanceof GameBoardViewFxml gv) this.boardView = gv;
            if (v instanceof MenuBarViewFxml mv)   this.menuView  = mv;
            if (v instanceof UserFeedbackViewFxml fv) this.feedbackView = fv;
        }if (this.boardView == null){
            throw new IllegalStateException("GameBoardView non trovata nelle views");
        }
        if (this.menuView  == null){
            throw new IllegalStateException("MenuBarView non trovata nelle views");
        }

        if (this.feedbackView == null){
            throw new IllegalStateException("UserFeedbackView non trovata nelle views");
        }

        this.uiNotices = UiNotices.getInstance();

    }
    public void resetEndNotification() {
        this.gameEndNotified = false;
    }
    // salva sul path passato
    public void saveTo(Path path) throws IOException {
        vm.save(path); // usa il service
    }
    // carica dal path passato
    public void loadFrom(Path path) throws IOException {
        vm.load(path);
    }

    private ResourceBundle rb() {
        return bundle;
    }

    @Override
    public void newGame() {
        Platform.runLater(() -> {
            gameEndNotified = false;
            int max   = vm.getRows() * vm.getCols() - 1;
            int bombs = Math.max(1, Math.min(defaultBombs, max));

            vm.newGame(bombs);

            // aggiorna solo board e feedback bar
            views.stream()
                    .filter(v -> !(v instanceof MenuBarViewFxml))
                    .forEach(DataView::update);

            // riabilita Save e Save As su nuova partita
            menuView.enableSaveOptions();

            uiNotices.showNewGameInfo(bombs);
        });
    }
    public void onCellClick(int r, int c, boolean rightClick,
                            Button btn, Queue<int[]> revealQueue) {

        if (!vm.isStarted()) return;
        var result = vm.handleClick(r, c, rightClick);

        switch (result.getType()) {
            case FLAG -> {
                // delega alla view l'icona
                boardView.applyFlagGraphic(btn, vm.isFlagged(r, c));
                if (feedbackView != null){
                    feedbackView.update();
                }
            }
            case REVEAL -> {
                revealQueue.addAll(result.getOpened());
                if (result.isMineHit()) {
                    boardView.revealAllMinesAndDisable();
                    lose();
                } else if (result.isWin()) {
                    boardView.revealAllMinesAndDisable();
                    win();
                }
            }
            case NONE -> {}
        }
    }

    @Override
    public void save() { MenuController.getInstance().save(); }

    public void saveAs() { MenuController.getInstance().saveAs(); }

    @Override
    public void load() { MenuController.getInstance().load(); }

    public void open() { MenuController.getInstance().open(); }

    @Override
    public void help() { MenuController.getInstance().help(); }

    @Override
    public void about() { MenuController.getInstance().about(); }

    @Override
    public void win() {
        if (gameEndNotified) return;   //evita loop di popup
        gameEndNotified = true;
        Platform.runLater(() -> {
            menuView.disableSaveOptions();
            uiNotices.showWin();
        });
    }

    @Override
    public void lose() {
        if (gameEndNotified) return;
        gameEndNotified = true;
        Platform.runLater(() -> {
            menuView.disableSaveOptions();
            uiNotices.showLose();
        });
    }

    @Override
    public void move() {}

    public GameViewModel model() { return vm; }

    public int currentBombs() { return PreferenceService.get().getBombs(); }
    public String currentLang() { return PreferenceService.get().getLang(); }
    public void updatePreferences(int bombs, String lang) {
        PreferenceService.get().setBombs(bombs);
        PreferenceService.get().setLang(lang);
    }



}

