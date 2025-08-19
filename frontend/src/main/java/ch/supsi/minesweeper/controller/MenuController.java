package ch.supsi.minesweeper.controller;

import ch.supsi.minesweeper.infrastructure.JsonGameRepository;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.persistence.GameRepository;
import ch.supsi.minesweeper.util.AppPreferences;
import ch.supsi.minesweeper.view.DataView;
import ch.supsi.minesweeper.view.MenuBarViewFxml;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuController {

    private static MenuController myself;

    private final GameModel gameModel;
    private final GameRepository persistence;
    private List<DataView> views;
    private final ResourceBundle bundle;
    private Path currentFile = null;

    private MenuController() {
        this.gameModel  = GameModel.getInstance();
        this.persistence   = new JsonGameRepository();
        this.bundle = ResourceBundle.getBundle("i18n.messages",
                Locale.forLanguageTag(AppPreferences.getLang()));
    }

    public static MenuController getInstance() {
        if (myself == null) myself = new MenuController();
        return myself;
    }

    //viene Chiamata da GameController.initialize(...) per passare le view.
    public void initialize(List<DataView> views) {
        this.views = views;
    }

    private ResourceBundle rb() { return bundle; }


    public void save() {
        if (currentFile == null) {
            saveAs();
            return;
        }
        try {
            persistence.save(gameModel, currentFile);
            Platform.runLater(() -> {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        rb().getString("dialog.save.success"));
                info.setHeaderText(null);
                info.showAndWait();
            });
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert err = new Alert(Alert.AlertType.ERROR,
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
                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.INFORMATION,
                            rb().getString("dialog.save.success"));
                    info.setHeaderText(null);
                    info.showAndWait();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert err = new Alert(Alert.AlertType.ERROR,
                            rb().getString("dialog.save.error"));
                    err.setHeaderText(null);
                    err.showAndWait();
                });
            }
        }
    }

    public void load() {
        if (currentFile == null) {
            open();
            return;
        }
        try {
            persistence.load(gameModel, currentFile);

            GameController.getInstance().resetEndNotification(); // evita loop popup
            // aggiorna board/feedback, non il menu
            if (views != null) {
                views.stream()
                        .filter(v -> !(v instanceof MenuBarViewFxml))
                        .forEach(DataView::update);
            }
            MenuBarViewFxml.getInstance().enableSaveOptions();

            Platform.runLater(() -> {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        rb().getString("dialog.load.success"));
                info.setHeaderText(null);
                info.showAndWait();
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert err = new Alert(Alert.AlertType.ERROR,
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

                GameController.getInstance().resetEndNotification();
                if (views != null) {
                    views.stream()
                            .filter(v -> !(v instanceof MenuBarViewFxml))
                            .forEach(DataView::update);
                }
                MenuBarViewFxml.getInstance().enableSaveOptions();

                Platform.runLater(() -> {
                    Alert info = new Alert(Alert.AlertType.INFORMATION,
                            rb().getString("dialog.load.success"));
                    info.setHeaderText(null);
                    info.showAndWait();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert err = new Alert(Alert.AlertType.ERROR,
                            rb().getString("dialog.load.error"));
                    err.setHeaderText(null);
                    err.showAndWait();
                });
            }
        }
    }

    public void help() {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(rb().getString("help.title"));
            a.setHeaderText(rb().getString("help.header"));
            a.setContentText(rb().getString("help.content"));
            a.showAndWait();
        });
    }

    public void about() {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(rb().getString("about.title"));
            a.setHeaderText(rb().getString("about.header"));
            a.setContentText(rb().getString("about.content"));
            a.showAndWait();
        });
    }
}