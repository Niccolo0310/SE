package ch.supsi.minesweeper.controller;

import ch.supsi.minesweeper.application.Services;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.application.GameService;
import ch.supsi.minesweeper.view.DataView;
import ch.supsi.minesweeper.view.MenuBarViewFxml;
import ch.supsi.minesweeper.util.AppPreferences;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class GameController implements EventHandler {

    private static GameController myself;

    private final GameModel gameModel;     // usato dalle view
    private final GameService service;         // Nuovo strato applicativo

    private       List<DataView>    views;
    private final int               defaultBombs;
    private final ResourceBundle    bundle;
    // mostra win/lose una sola volta per partita
    private volatile boolean gameEndNotified = false;

    private Path currentFile = null;

    private GameController() {
        // Costruisco il service con il repository concreto dal backend
        this.service     = Services.defaultService();
        this.gameModel      = service.model(); // reference per le view

        this.defaultBombs = AppPreferences.getBombs();
        this.bundle       = ResourceBundle.getBundle(
                "i18n.messages",
                Locale.forLanguageTag(AppPreferences.getLang()));
    }

    public static GameController getInstance() {
        if (myself == null) myself = new GameController();
        return myself;
    }

    public void initialize(List<DataView> views) {
        this.views = views;
        MenuController.getInstance().initialize(views);
        // save e save as restano disabilitate fino a newGame() o open()
    }
    public void resetEndNotification() {
        this.gameEndNotified = false;
    }
    // salva sul path passato
    public void saveTo(Path path) throws IOException {
        service.save(path); // usa il service
    }
    // carica dal path passato
    public void loadFrom(Path path) throws IOException {
        service.load(path);
    }

    private ResourceBundle rb() {
        return bundle;
    }

    @Override
    public void newGame() {
        Platform.runLater(() -> {
            gameEndNotified = false;
            int max   = gameModel.getRows() * gameModel.getCols() - 1;
            int bombs = Math.max(1, Math.min(defaultBombs, max));

            service.newGame(bombs); //ora coordina il service

            // aggiorna solo board e feedback bar
            views.stream()
                    .filter(v -> !(v instanceof MenuBarViewFxml))
                    .forEach(DataView::update);

            // riabilita Save e Save As su nuova partita
            MenuBarViewFxml.getInstance().enableSaveOptions();

            Alert info = new Alert(AlertType.INFORMATION);
            info.setTitle(rb().getString("dialog.new.title"));
            info.setHeaderText(null);
            info.setContentText(
                    MessageFormat.format(rb().getString("dialog.new.body"), bombs));
            info.showAndWait();
        });
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
            // disabilita Save e Save As quando si vince
            MenuBarViewFxml.getInstance().disableSaveOptions();
            Alert a = new Alert(AlertType.INFORMATION);
            a.setTitle(rb().getString("alert.win.title"));
            a.setHeaderText(null);
            a.setContentText(rb().getString("alert.win.text"));
            a.showAndWait();
        });
    }

    @Override
    public void lose() {
        if (gameEndNotified) return;   //evita loop di popup
        gameEndNotified = true;
        Platform.runLater(() -> {
            // disabilita Save e Save As quando si perde
            MenuBarViewFxml.getInstance().disableSaveOptions();
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle(rb().getString("alert.lose.title"));
            a.setHeaderText(rb().getString("alert.lose.header"));
            a.setContentText(rb().getString("alert.lose.text"));
            a.showAndWait();
        });
    }

    @Override
    public void move() {
        gameModel.move();
    }
}