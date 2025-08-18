package ch.supsi.minesweeper.controller;

import ch.supsi.minesweeper.model.GameEventHandler;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.model.JsonGamePersistence;
import ch.supsi.minesweeper.model.GamePersistence;
import ch.supsi.minesweeper.model.PlayerEventHandler;
import ch.supsi.minesweeper.view.DataView;
import ch.supsi.minesweeper.view.MenuBarViewFxml;
import ch.supsi.minesweeper.util.AppPreferences;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class GameController implements GameEventHandler, PlayerEventHandler {

    private static GameController myself;
    private final GameModel        gameModel;
    private final GamePersistence   persistence;
    private       List<DataView>    views;
    private final int               defaultBombs;
    private final ResourceBundle    bundle;

    private Path currentFile = null;

    private GameController() {
        this.gameModel    = GameModel.getInstance();
        this.persistence  = new JsonGamePersistence();
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
        // save e save as restano disabilitate fino a newGame() o open()
    }

    private ResourceBundle rb() {
        return bundle;
    }

    @Override
    public void newGame() {
        Platform.runLater(() -> {
            int max   = gameModel.getRows() * gameModel.getCols() - 1;
            int bombs = Math.max(1, Math.min(defaultBombs, max));

            gameModel.setMines(bombs);
            gameModel.newGame();

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
    public void save() {
        if (currentFile == null) {
            saveAs();
            return;
        }
        try {
            persistence.save(gameModel, currentFile);
            Platform.runLater(() -> {
                Alert info = new Alert(AlertType.INFORMATION,
                        rb().getString("dialog.save.success"));
                info.setHeaderText(null);
                info.showAndWait();
            });
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert err = new Alert(AlertType.ERROR,
                        rb().getString("dialog.save.error"));
                err.setHeaderText(null);
                err.showAndWait();
            });
        }
    }

    public void saveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(rb().getString("menu.file.saveas"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            currentFile = file.toPath();
            try {
                persistence.save(gameModel, currentFile);
               // views.forEach(DataView::update);
                Platform.runLater(() -> {
                    Alert info = new Alert(AlertType.INFORMATION,
                            rb().getString("dialog.save.success"));
                    info.setHeaderText(null);
                    info.showAndWait();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert err = new Alert(AlertType.ERROR,
                            rb().getString("dialog.save.error"));
                    err.setHeaderText(null);
                    err.showAndWait();
                });
            }
        }
    }

    @Override
    public void load() {
        if (currentFile == null) {
            open();
            return;
        }
        try {
            persistence.load(gameModel, currentFile);

            // aggiorna board e feedback bar, non il menu
            views.stream()
                    .filter(v -> !(v instanceof MenuBarViewFxml))
                    .forEach(DataView::update);

            MenuBarViewFxml.getInstance().enableSaveOptions();

            Platform.runLater(() -> {
                Alert info = new Alert(AlertType.INFORMATION,
                        rb().getString("dialog.load.success"));
                info.setHeaderText(null);
                info.showAndWait();
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert err = new Alert(AlertType.ERROR,
                        rb().getString("dialog.load.error"));
                err.setHeaderText(null);
                err.showAndWait();
            });
        }
    }

    public void open() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(rb().getString("menu.file.open"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            currentFile = file.toPath();
            try {
                persistence.load(gameModel, currentFile);

                // aggiorna board e feedback bar, non il menu
                views.stream()
                        .filter(v -> !(v instanceof MenuBarViewFxml))
                        .forEach(DataView::update);

                MenuBarViewFxml.getInstance().enableSaveOptions();

                Platform.runLater(() -> {
                    Alert info = new Alert(AlertType.INFORMATION,
                            rb().getString("dialog.load.success"));
                    info.setHeaderText(null);
                    info.showAndWait();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert err = new Alert(AlertType.ERROR,
                            rb().getString("dialog.load.error"));
                    err.setHeaderText(null);
                    err.showAndWait();
                });
            }
        }
    }

    @Override
    public void help() {
        Platform.runLater(() -> {
            Alert a = new Alert(AlertType.INFORMATION);
            a.setTitle(rb().getString("help.title"));
            a.setHeaderText(rb().getString("help.header"));
            a.setContentText(rb().getString("help.content"));
            a.showAndWait();
        });
    }

    @Override
    public void about() {
        Platform.runLater(() -> {
            Alert a = new Alert(AlertType.INFORMATION);
            a.setTitle(rb().getString("about.title"));
            a.setHeaderText(rb().getString("about.header"));
            a.setContentText(rb().getString("about.content"));
            a.showAndWait();
        });
    }

    @Override
    public void win() {
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