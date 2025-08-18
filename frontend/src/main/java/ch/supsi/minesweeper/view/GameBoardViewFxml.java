package ch.supsi.minesweeper.view;

import ch.supsi.minesweeper.controller.EventHandler;
import ch.supsi.minesweeper.model.AbstractModel;
import ch.supsi.minesweeper.model.GameEventHandler;
import ch.supsi.minesweeper.model.GameModel;
import ch.supsi.minesweeper.model.PlayerEventHandler;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GameBoardViewFxml implements ControlledFxView {

    private static GameBoardViewFxml myself;
    private final ResourceBundle bundle;
    private static final double BUTTON_SIZE = 37;
    private static final double IMAGE_SIZE  = 30;

    private PlayerEventHandler playerEventHandler;
    private GameEventHandler   gameEventHandler;
    private GameModel          gameModel;

    @FXML private GridPane containerPane;
    private Image flagImg, bombImg;

    // Coda per reveal progressivo
    private final Queue<int[]> revealQueue = new ArrayDeque<>();

    private GameBoardViewFxml(ResourceBundle bundle) {
        this.bundle = bundle;
        loadImages();
    }

    public static GameBoardViewFxml getInstance(ResourceBundle bundle) {
        if (myself == null) {
            myself = new GameBoardViewFxml(bundle);
            try {
                URL url = GameBoardViewFxml.class.getResource("/gameboard.fxml");
                FXMLLoader loader = new FXMLLoader(url, bundle);
                loader.setController(myself);
                loader.load();
            } catch (IOException e) {
                throw new RuntimeException("Errore caricamento gameboard.fxml", e);
            }
        }
        return myself;
    }

    @Override
    public void initialize(EventHandler evt, AbstractModel model) {
        playerEventHandler = (PlayerEventHandler) evt;
        gameEventHandler   = (GameEventHandler)   evt;
        gameModel          = (GameModel) model;
        setupGrid();

        // Avvia animazione progressiva per il reveal
        startRevealAnimator();
    }

    @Override
    public Node getNode() {
        return containerPane;
    }

    @Override
    public void update() {
        for (Node n : containerPane.getChildren()) {
            Button btn = (Button) n;
            int row = Optional.ofNullable(GridPane.getRowIndex(btn)).orElse(0);
            int col = Optional.ofNullable(GridPane.getColumnIndex(btn)).orElse(0);

            if (gameModel.isRevealed(row, col)) {
                drawCell(btn, row, col);
                btn.setDisable(true);
            }
            else if (gameModel.isFlagged(row, col)) {
                btn.setGraphic(makeIcon(flagImg));
                btn.setText("");
                btn.setDisable(false);
            }
            else {
                btn.setGraphic(null);
                btn.setText("");
                btn.setDisable(!gameModel.isStarted());
            }
        }
    }

    private void setupGrid() {
        for (Node n : containerPane.getChildren()) {
            Button btn = (Button) n;
            btn.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
            btn.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
            btn.setDisable(true);

            int row = Optional.ofNullable(GridPane.getRowIndex(btn)).orElse(0);
            int col = Optional.ofNullable(GridPane.getColumnIndex(btn)).orElse(0);
            btn.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    e -> handleClick(e, row, col, btn));
        }
    }

    private void handleClick(MouseEvent e, int r, int c, Button btn) {
        if (!gameModel.isStarted()) return;

        if (e.getButton() == MouseButton.SECONDARY) {
            gameModel.toggleFlag(r, c);
            if (gameModel.isFlagged(r, c)) {
                btn.setGraphic(makeIcon(flagImg));
                btn.setText("");
            } else {
                btn.setGraphic(null);
            }
            UserFeedbackViewFxml.getInstance().update();
        }
        else if (e.getButton() == MouseButton.PRIMARY && !gameModel.isFlagged(r, c)) {
            List<int[]> opened = gameModel.revealArea(r, c);
            revealQueue.addAll(opened);

            // controllo immediato
            if (gameModel.hasMineAt(r, c)) {
                disableAll();
                gameEventHandler.lose();
            }
        }
        else if (e.getButton() == MouseButton.PRIMARY && !gameModel.isFlagged(r, c)) {
            List<int[]> opened = gameModel.revealArea(r, c);
            revealQueue.addAll(opened); // le celle vanno aggiornate progressivamente
        }
        e.consume();
    }

    // Disegna singola cella (bomba, numero, vuoto)
    private void drawCell(Button b, int row, int col) {
        if (gameModel.hasMineAt(row, col)) {
            b.setGraphic(makeIcon(bombImg));
            b.setText("");
        } else {
            int cnt = gameModel.getNeighborCountAt(row, col);
            b.setGraphic(null);
            b.setText(cnt > 0 ? String.valueOf(cnt) : "");
        }
        b.setDisable(true);
    }

    private void disableAll() {
        for (Node n : containerPane.getChildren()) {
            Button b = (Button) n;
            int rr = Optional.ofNullable(GridPane.getRowIndex(b)).orElse(0);
            int cc = Optional.ofNullable(GridPane.getColumnIndex(b)).orElse(0);
            if (gameModel.hasMineAt(rr, cc)) {
                b.setGraphic(makeIcon(bombImg));
                b.setText("");
            }
            b.setDisable(true);
        }
    }

    private void loadImages() {
        flagImg = new Image(getClass().getResourceAsStream("/images/flag.png"));
        bombImg = new Image(getClass().getResourceAsStream("/images/bomb.png"));
    }

    private Button getButtonAt(int row, int col) {
        for (Node n : containerPane.getChildren()) {
            Button b = (Button) n;
            Integer r = GridPane.getRowIndex(b);
            Integer c = GridPane.getColumnIndex(b);
            if (Objects.equals(r, row) && Objects.equals(c, col)) {
                return b;
            }
        }
        throw new IllegalStateException("Button non trovato (" + row + "," + col + ")");
    }

    private ImageView makeIcon(Image img) {
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(IMAGE_SIZE);
        iv.setFitHeight(IMAGE_SIZE);
        return iv;
    }

    // Reveal progressivo con AnimationTimer
    private void startRevealAnimator() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                int batch = 15; // quante celle aggiornare per frame
                for (int i = 0; i < batch && !revealQueue.isEmpty(); i++) {
                    int[] pos = revealQueue.poll();
                    int row = pos[0], col = pos[1];
                    Button b = getButtonAt(row, col);
                    drawCell(b, row, col);
                }

                // check win/lose alla fine di ogni batch
                if (gameModel.isWin()) {
                    disableAll();
                    gameEventHandler.win();
                }
            }
        };
        timer.start();
    }
}