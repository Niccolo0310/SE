package ch.supsi.minesweeper.view;

import ch.supsi.minesweeper.controller.EventHandler;
import ch.supsi.minesweeper.controller.GameController;
import ch.supsi.minesweeper.model.*;
import ch.supsi.minesweeper.model.GameViewModel;
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

    private GameViewModel          gameModel;
    private GameController controller;

    @FXML private GridPane containerPane;
    private Image flagImg, bombImg;

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
        this.controller = (GameController) evt;
        this.gameModel  = (GameViewModel) model;
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
        boolean right = (e.getButton() == MouseButton.SECONDARY);
        controller.onCellClick(r, c, right, btn, revealQueue);
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

    public void disableAll() {
        for (Node n : containerPane.getChildren()) {
            (n).setDisable(true);
        }
    }

    public void applyFlagGraphic(Button btn, boolean flagged) {
        if (flagged) {
            btn.setGraphic(makeIcon(flagImg));
            btn.setText("");
        } else {
            btn.setGraphic(null);
            btn.setText("");
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

    //Reveal progressivo con AnimationTimer
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

                // check win/lose alla fine
                if (gameModel.isWin() && controller != null) {
                    disableAll();
                    controller.win();
                }
            }
        };
        timer.start();
    }
    public void revealAllMinesAndDisable() {
        for (Node n : containerPane.getChildren()) {
            Button b = (Button) n;
            int r = Optional.ofNullable(GridPane.getRowIndex(b)).orElse(0);
            int c = Optional.ofNullable(GridPane.getColumnIndex(b)).orElse(0);

            if (gameModel.hasMineAt(r, c)) {
                b.setGraphic(makeIcon(bombImg));
                b.setText("");
            } else if (gameModel.isRevealed(r, c)) {
                drawCell(b, r, c); // già mette numero o vuoto
            } else {
                b.setGraphic(null);
                b.setText("");
            }
            b.setDisable(true);
        }
    }

}