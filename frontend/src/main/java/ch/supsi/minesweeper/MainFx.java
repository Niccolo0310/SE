package ch.supsi.minesweeper;

import ch.supsi.minesweeper.controller.EventHandler;
import ch.supsi.minesweeper.controller.GameController;

import ch.supsi.minesweeper.model.AbstractModel;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.util.AppPreferences;
import ch.supsi.minesweeper.view.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainFx extends Application {

    public static final String BUNDLE_BASE = "i18n.messages";

    private final AbstractModel model;
    private final ControlledFxView menuBarView;
    private final ControlledFxView gameBoardView;
    private final UncontrolledFxView feedbackView;

    private final ResourceBundle bundle;

    public MainFx() {

        Locale locale = Locale.forLanguageTag(AppPreferences.getLang());
        bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale);

        model          = GameModel.getInstance();

        menuBarView    = MenuBarViewFxml.getInstance(bundle);
        gameBoardView  = GameBoardViewFxml.getInstance(bundle);
        feedbackView   = UserFeedbackViewFxml.getInstance(bundle);

        GameController controller = GameController.getInstance();

        ((GameBoardViewFxml) gameBoardView).setGameService(controller.getService()); // passo il GameService della stessa istanza usata dal controller

        menuBarView.initialize((EventHandler) controller, model);
        gameBoardView.initialize((EventHandler) controller, model);
        feedbackView.initialize(model);

        controller.initialize(List.of(menuBarView, gameBoardView, feedbackView));
    }

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setTop   (menuBarView.getNode());
        root.setCenter(gameBoardView.getNode());
        root.setBottom(feedbackView.getNode());

        Scene scene = new Scene(root);
        stage.setTitle(bundle.getString("app.title"));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}