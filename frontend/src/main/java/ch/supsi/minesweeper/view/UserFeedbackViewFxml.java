package ch.supsi.minesweeper.view;

import ch.supsi.minesweeper.application.PreferenceService;
import ch.supsi.minesweeper.model.AbstractModel;
import ch.supsi.minesweeper.model.GameViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class UserFeedbackViewFxml implements UncontrolledFxView {

    private static UserFeedbackViewFxml myself;
    private final ResourceBundle bundle;
    private GameViewModel gameModel;
    @FXML private ScrollPane containerPane;
    @FXML private Text       userFeedbackBar;
    private UserFeedbackViewFxml(ResourceBundle bundle) { this.bundle = bundle; }

    public static UserFeedbackViewFxml getInstance(ResourceBundle bundle) {
        if (myself == null) {
            myself = new UserFeedbackViewFxml(bundle);
            try {
                URL url = UserFeedbackViewFxml.class.getResource("/userfeedbackbar.fxml");
                FXMLLoader loader = new FXMLLoader(url, bundle);
                loader.setController(myself);
                loader.load();
            } catch (IOException e) {
                throw new RuntimeException("Impossibile caricare userfeedbackbar.fxml", e);
            }
        }
        return myself;
    }

    public static UserFeedbackViewFxml getInstance() {
        ResourceBundle def = ResourceBundle.getBundle(
                "i18n.messages",
                Locale.forLanguageTag(PreferenceService.get().getLang()));
        return getInstance(def);
    }

    @Override public void initialize(AbstractModel model) {
        gameModel = (GameViewModel) model;
        update();
    }
    @Override public Node getNode() { return containerPane; }

    @Override
    public void update() {
        int total = gameModel.getMines();
        int flags = gameModel.getFlaggedCount();
        int remaining = total - flags;
        String fmt = bundle.getString("status.bombs");
        userFeedbackBar.setText(java.text.MessageFormat.format(fmt, remaining, total));
    }

    public void showInfo(String text) {
        Platform.runLater(() -> {
            userFeedbackBar.setText(text);
        });
    }

    public void showError(String text) {
        Platform.runLater(() -> {
            userFeedbackBar.setText(text);
        });
    }
}