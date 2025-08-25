package ch.supsi.minesweeper.view;

import ch.supsi.minesweeper.controller.GameController;
import ch.supsi.minesweeper.controller.EventHandler;
import ch.supsi.minesweeper.controller.GameEventHandler;

import ch.supsi.minesweeper.uimodel.GameViewModel;
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
    @FXML private ResourceBundle resources; //iniettato da FXMLLoader, se presente


    private ResourceBundle bundle;
    private static MenuBarViewFxml myself;
    private GameEventHandler gameEventHandler;
    private GameViewModel gameModel;

    public MenuBarViewFxml() {

    }
    public static MenuBarViewFxml getInstance(ResourceBundle bundle) {
        if (myself == null) {
            try {
                URL url = MenuBarViewFxml.class.getResource("/menubar.fxml");
                FXMLLoader loader = new FXMLLoader(url, bundle);
                loader.load();
                myself = loader.getController();
                myself.bundle = bundle;//usa il bundle iniettato
            } catch (IOException ex) {
                throw new RuntimeException("Error loading menubar.fxml", ex);
            }
        }
        return myself;
    }


    @Override
    public void initialize(EventHandler h, ch.supsi.minesweeper.model.AbstractModel m) {
        this.gameEventHandler = (GameEventHandler) h;
        this.gameModel        = (GameViewModel) m;
        if (this.bundle == null && this.resources != null) {
            this.bundle = this.resources;
        }
        createBehaviour();
    }

    @Override
    public Node getNode() {
        return menuBar;
    }

    @Override
    public void update() {

    }

    private void createBehaviour() {
        newMenuItem.setOnAction(e -> {
            gameEventHandler.newGame();
            enableSaveOptions();
        });

        openMenuItem.setOnAction(e -> {
            ((GameController) gameEventHandler).open();
            enableSaveOptions();   // Quando si apre una partita, ora c’è qualcosa da salvare

        });

        // Salva partita
        saveMenuItem.setOnAction(e -> ((GameController) gameEventHandler).save());

        // Salva come
        saveAsMenuItem.setOnAction(e -> ((GameController) gameEventHandler).saveAs());

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



        // all’avvio, disabilitiamo “Save” e “Save As”
        disableSaveOptions();
    }

    private void showPreferencesDialog() {
        GameController gameController = (GameController) gameEventHandler;
        int    currentBombs = gameController.currentBombs();
        String currentLang  = gameController.currentLang();
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

                gameController.updatePreferences(bombs, langBox.getValue());

                UiNotices.getInstance().showInfo(bundle.getString("prefs.saved"));
            } catch (NumberFormatException ex) {
                UiNotices.getInstance().showError(
                        bundle.getString("prefs.error") + " 1–" + maxBombs + "."
                );
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


}