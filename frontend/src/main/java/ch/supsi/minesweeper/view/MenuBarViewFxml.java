package ch.supsi.minesweeper.view;

import ch.supsi.minesweeper.controller.GameController;
import ch.supsi.minesweeper.controller.EventHandler;
import ch.supsi.minesweeper.model.GameEventHandler;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.util.AppPreferences;
import ch.supsi.minesweeper.controller.MenuController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuBarViewFxml implements ControlledFxView {

    @FXML private MenuBar  menuBar;
    @FXML private MenuItem newMenuItem;
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem quitMenuItem;
    @FXML private MenuItem preferencesMenuItem;
    @FXML private MenuItem helpMenuItem;
    @FXML private MenuItem aboutMenuItem;

    private final ResourceBundle bundle;
    private static MenuBarViewFxml myself;
    private GameEventHandler gameEventHandler;
    private GameModel        gameModel;
    private final MenuController menu = MenuController.getInstance();

    public MenuBarViewFxml() {
        this.bundle = ResourceBundle.getBundle(
                "i18n.messages",
                Locale.forLanguageTag(AppPreferences.getLang())
        );
    }
    public static MenuBarViewFxml getInstance(ResourceBundle bundle) {
        if (myself == null) {
            try {
                URL url = MenuBarViewFxml.class.getResource("/menubar.fxml");
                FXMLLoader loader = new FXMLLoader(url, bundle);
                loader.load();
                myself = loader.getController();
            } catch (IOException ex) {
                throw new RuntimeException("Error loading menubar.fxml", ex);
            }
        }
        return myself;
    }

    public static MenuBarViewFxml getInstance() {
        ResourceBundle def = ResourceBundle.getBundle(
                "i18n.messages",
                Locale.forLanguageTag(AppPreferences.getLang()));
        return getInstance(def);
    }

    @Override
    public void initialize(EventHandler h, ch.supsi.minesweeper.model.AbstractModel m) {
        this.gameEventHandler = (GameEventHandler) h;
        this.gameModel        = (GameModel) m;
        createBehaviour();
    }

    @Override
    public Node getNode() {
        return menuBar;
    }

    @Override
    public void update() {
        // Non usato nel menu, ma richiesto dall’interfaccia
    }

    private void createBehaviour() {
        // Nuova partita
        newMenuItem.setOnAction(e -> {
            gameEventHandler.newGame();
            enableSaveOptions();
        });

        // Apri partita
        openMenuItem.setOnAction(e -> {
            ((GameController) gameEventHandler).open();
            // Quando si apre una partita, ora c’è qualcosa da salvare
            enableSaveOptions();
        });

        // Salva partita
        saveMenuItem.setOnAction(e -> ((GameController) gameEventHandler).save());

        // Salva come…
        saveAsMenuItem.setOnAction(e -> ((GameController) gameEventHandler).saveAs());

        // Help e About
        helpMenuItem.setOnAction(e -> gameEventHandler.help());
        aboutMenuItem.setOnAction(e -> gameEventHandler.about());

        // Preferenze
        preferencesMenuItem.setOnAction(e -> showPreferencesDialog());

        // Esci
        quitMenuItem.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    bundle.getString("quit.ask"));
            confirm.setHeaderText(null);
            confirm.setTitle(bundle.getString("quit.title"));
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            confirm.showAndWait().filter(bt -> bt == ButtonType.YES)
                    .ifPresent(bt -> Platform.exit());
        });



        // all’avvio (prima di creare una partita), disabilitiamo “Save” e “Save As”
        disableSaveOptions();
    }

    private void showPreferencesDialog() {
        int    currentBombs = AppPreferences.getBombs();
        String currentLang  = AppPreferences.getLang();
        int maxBombs = gameModel.getRows() * gameModel.getCols() - 1;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(bundle.getString("menu.preferences"));
        dlg.setHeaderText(bundle.getString("prefs.header"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        javafx.scene.control.TextField bombsField = new javafx.scene.control.TextField(String.valueOf(currentBombs));
        javafx.scene.control.ComboBox<String> langBox = new javafx.scene.control.ComboBox<>();
        langBox.getItems().addAll("en", "it");
        langBox.setValue(currentLang);

        grid.addRow(0,
                new javafx.scene.control.Label(bundle.getString("prefs.bombs.label")), bombsField);
        grid.addRow(1,
                new javafx.scene.control.Label(bundle.getString("prefs.lang.label")), langBox);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
            try {
                int bombs = Integer.parseInt(bombsField.getText().trim());
                if (bombs < 1 || bombs > maxBombs) throw new NumberFormatException();

                AppPreferences.setBombs(bombs);
                AppPreferences.setLang(langBox.getValue());

                new Alert(Alert.AlertType.INFORMATION,
                        bundle.getString("prefs.saved"))
                        .showAndWait();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR,
                        bundle.getString("prefs.error") + " 1–" + maxBombs + ".")
                        .showAndWait();
            }
        });
    }

    public void disableSaveOptions() {
        saveMenuItem.setDisable(true);
        saveAsMenuItem.setDisable(true);
    }
    public void enableSaveOptions() {
        saveMenuItem.setDisable(false);
        saveAsMenuItem.setDisable(false);
    }

    @FXML
    private void onNew() { MenuController.getInstance().newGame(); }

    @FXML
    private void onOpen() { MenuController.getInstance().open(); }

    @FXML
    private void onSave() { MenuController.getInstance().save(); }

    @FXML
    private void onSaveAs() { MenuController.getInstance().saveAs(); }

    @FXML
    private void onAbout() { MenuController.getInstance().about(); }

    @FXML
    private void onHelp() { MenuController.getInstance().help(); }

    @FXML
    private void onExit() { MenuController.getInstance().exit(); }
}