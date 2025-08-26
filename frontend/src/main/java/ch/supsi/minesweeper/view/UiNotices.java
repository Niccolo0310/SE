package ch.supsi.minesweeper.view;

import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;

import javafx.stage.Stage;

public final class UiNotices {
    private static UiNotices INSTANCE;
    private final ResourceBundle bundle;
    private volatile boolean exitArmed = false;

    private UiNotices() {
        this.bundle = ResourceBundle.getBundle(
                "i18n.messages",
                java.util.Locale.forLanguageTag(
                        ch.supsi.minesweeper.application.PreferenceService.get().getLang()
                )
        );
    }

    public static synchronized UiNotices getInstance() {
        if (INSTANCE == null) INSTANCE = new UiNotices();
        return INSTANCE;
    }

    //feedback feedback bar
    public void showNewGameInfo(int bombs) {
        String pattern = bundle.getString("dialog.new.body");
        String msg = java.text.MessageFormat.format(pattern, bombs);
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


    public void installExitConfirmation(Stage stage, BooleanSupplier unsaved) {
        stage.setOnCloseRequest(evt -> {
            if (shouldBlockExit(unsaved)) {
                evt.consume(); //al primo tentativo, blocca e mostra messaggio
            }
        });



   }

    public boolean shouldBlockExit(BooleanSupplier unsaved) {
        // Se non ci sono modifiche non salvate: esci subito
        if (!unsaved.getAsBoolean()) {
            exitArmed = false;
            return false; //non bloccare l'uscita
        }

        //se ci sono modifiche non salvate, avvisa e blocca
        if (!exitArmed) {
            UserFeedbackViewFxml.getInstance().showError(bundle.getString("exit.pressAgain"));
            exitArmed = true;
            return true;

        }

        exitArmed = false;
        return false;
    }

    public void disarmExitPrompt() {
        exitArmed = false;
    }
}