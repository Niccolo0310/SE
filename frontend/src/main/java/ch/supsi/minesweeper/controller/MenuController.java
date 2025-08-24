package ch.supsi.minesweeper.controller;

import ch.supsi.minesweeper.application.PreferenceService;

import ch.supsi.minesweeper.view.DataView;
import ch.supsi.minesweeper.view.MenuBarViewFxml;
import ch.supsi.minesweeper.view.UiNotices;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MenuController {

    private static MenuController myself;
    private List<DataView> views;
    private final ResourceBundle bundle;
    private Path currentFile = null;
    private final ResourceBundle aboutProps = ResourceBundle.getBundle("config");

    private MenuController() {
        this.bundle = ResourceBundle.getBundle("i18n.messages",
                Locale.forLanguageTag(PreferenceService.get().getLang()));
    }

    public static MenuController getInstance() {
        if (myself == null) myself = new MenuController();
        return myself;
    }

    //viene Chiamata da GameController.initialize() per passare le view.
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
            UiNotices.getInstance().showInfo(rb().getString("dialog.save.success"));
        } catch (IOException e) {
            e.printStackTrace();
            UiNotices.getInstance().showError(rb().getString("dialog.save.error"));
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

            UiNotices.getInstance().showInfo(rb().getString("dialog.load.success"));
        } catch (IOException ex) {
            ex.printStackTrace();
            UiNotices.getInstance().showError(rb().getString("dialog.load.error"));
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
        UiNotices.getInstance().showHelp();
    }

    public void about() {
        String name        = getProp("about.name", "Minesweeper");
        String version     = getProp("about.version", "0.0.1");
        String description = getProp("about.description", "Software Engineering project");
        String author      = getProp("about.copyright", "Authors");
        String buildDate   = getProp("about.buildDate", "unknown");

        javafx.application.Platform.runLater(() -> {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("About " + name);
            a.setHeaderText(name + "\nAutori: " + author + "    Versione: " + version);
            a.setContentText(description + "\nBuild info: " + buildDate);
            a.showAndWait();
        });
    }
    private String getProp(String key, String defVal) {
        try { return aboutProps.getString(key); }
        catch (MissingResourceException e) { return defVal; }
    }


    public void exit() {
        Platform.exit();
    }
}