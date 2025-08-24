package ch.supsi.minesweeper.controller;

import ch.supsi.minesweeper.util.AppPreferences;
import ch.supsi.minesweeper.util.BuildInfo;
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
    private List<DataView> views;
    private final ResourceBundle bundle;
    private Path currentFile = null;

    private MenuController() {
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


    public void newGame() {
        GameController.getInstance().newGame();
    }

    public void save() {
        if (currentFile == null) {
            saveAs();
            return;
        }
        try {
            GameController.getInstance().saveTo(currentFile);
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
            save();
        }
    }

    public void load() {
        if (currentFile == null) {
            return;
        }
        try {
            GameController.getInstance().loadFrom(currentFile);
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
            load();
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
            a.setTitle("About " + BuildInfo.getName());
            a.setHeaderText(BuildInfo.getName() + "\nAutori: " + BuildInfo.getAuthor() + "    Versione: " + BuildInfo.getVersion());
            a.setContentText(BuildInfo.getDescription()  +
                    "\nBuild info: " + BuildInfo.buildDate()
            );
            a.showAndWait();
        });
    }
    public void exit() {
        Platform.exit();
    }
}