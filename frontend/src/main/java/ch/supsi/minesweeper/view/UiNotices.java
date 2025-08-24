package ch.supsi.minesweeper.view;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import ch.supsi.minesweeper.application.PreferenceService;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public final class UiNotices {
    private static UiNotices INSTANCE;
    private final ResourceBundle bundle;

    private UiNotices() {
        this.bundle = ResourceBundle.getBundle(
                "i18n.messages",
                Locale.forLanguageTag(PreferenceService.get().getLang())
        );
    }

    public static synchronized UiNotices getInstance() {
        if (INSTANCE == null) INSTANCE = new UiNotices();
        return INSTANCE;
    }

    // Feedback feedback bar
    public void showNewGameInfo(int bombs) {
        String msg = MessageFormat.format(bundle.getString("dialog.new.body"), bombs);
        UserFeedbackViewFxml.getInstance().showInfo(msg);
    }

    public void showWin()  {
        UserFeedbackViewFxml.getInstance().showInfo(bundle.getString("alert.win.text"));
    }

    public void showLose() {
        UserFeedbackViewFxml.getInstance().showError(bundle.getString("alert.lose.text"));
    }

    public void showHelp() {
        UserFeedbackViewFxml.getInstance().showInfo(bundle.getString("help.content"));
    }

    public void showInfo(String text) {
        UserFeedbackViewFxml.getInstance().showInfo(text);
    }

    public void showError(String text) {
        UserFeedbackViewFxml.getInstance().showError(text);
    }


    public void installExitConfirmation(Stage stage) {
        // handler unico per la X della finestra
        stage.setOnCloseRequest(evt -> {
            // ricarico il bundle in base alla lingua corrente (cold reload)
            ResourceBundle b = ResourceBundle.getBundle(
                    "i18n.messages",
                    Locale.forLanguageTag(PreferenceService.get().getLang())
            );

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    b.getString("quit.ask"), ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle(b.getString("quit.title"));

            var result = confirm.showAndWait().orElse(ButtonType.NO);
            if (result != ButtonType.YES) {
                evt.consume(); // annulla la chiusura
            }
        });



   }
}