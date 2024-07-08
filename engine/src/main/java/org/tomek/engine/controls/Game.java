package org.tomek.engine.controls;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tomek.engine.*;
import org.tomek.engine.enums.GameState;
import org.tomek.engine.exceptions.FxmlLoadingException;
import org.tomek.engine.exceptions.LoadLevelException;
import org.tomek.engine.exceptions.SaveProgressException;
import org.tomek.engine.gameobjects.*;
import org.tomek.engine.FileManager;
import org.tomek.engine.views.DeathScreen;
import org.tomek.engine.views.PauseMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The `Game` class represents the main controller for the game engine. It manages the game loop,
 * player actions, level progression, and interaction with the game environment.
 * It handles user input, updates the game state, and displays relevant UI elements such as the
 * inventory, pause menu, and death screen.
 */
@Slf4j
public class Game {
    @Getter
    private Level currentLevel;
    @Getter
    private final StackPane pane;
    @Getter
    private final Canvas canvas;

    private final double width;

    private final double height;

    private GameState gameState;

    private final BooleanProperty aPress = new SimpleBooleanProperty();

    private final BooleanProperty dPress = new SimpleBooleanProperty();

    private final BooleanProperty jumping = new SimpleBooleanProperty();

    private final PauseMenu pauseMenu = new PauseMenu();

    private final DeathScreen deathScreen = new DeathScreen();

    private final InventoryControl inventoryControl;

    private Collectible currentlyCollectable;

    private NPC currentlyIntractable;

    private final ImageView backgroundView;

    public Game(Level level, double width, double height){
        this.currentLevel = level;
        this.pane = new StackPane();
        this.width = width;
        this.height = height;
        this.canvas =  new Canvas(this.width, this.height);
        this.gameState = GameState.RUNNING;
        this.inventoryControl = new InventoryControl(this.currentLevel.getPlayer().getInventory(), this.currentLevel.getPlayer());
        this.backgroundView = new ImageView(this.currentLevel.getBackground());
    }

    /**
     * This method is used to start the game and run the whole game loop.
     * It adds the background view, canvas, pause menu, death screen, and health bar to the pane, updates the inventory, sets the key event handlers, and refreshes the scene.
     * Then it starts the main game loop in a new thread, which handles player movement, gravity, collisions, death, and level ending.
     * If the game state becomes QUIT, it breaks the loop, shuts down the executor service, saves the progress, and quits to the menu.
     */
    public void runGame(){
        Stage stage = (Stage) pane.getScene().getWindow();
        this.pane.getChildren().add(backgroundView);
        this.pane.getChildren().add(canvas);
        this.pauseMenu.initPauseMenu();
        this.deathScreen.initDeathScreen();
        this.pane.getChildren().add(this.currentLevel.getPlayer().getHealthBar().getHbox());
        this.inventoryControl.updateInventory();
        setKeyEventHandler();
        refreshScene();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        //main game loop
        executorService.submit(() -> {
            do {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (this.gameState == GameState.RUNNING) {
                    //movement and gravity handling
                    if (aPress.get()) {
                        this.currentLevel.moveLeft();
                    }
                    if (dPress.get()) {
                        this.currentLevel.moveRight();
                    }
                    this.currentLevel.gravity();
                    //get interactable elements
                    this.currentlyCollectable = this.currentLevel.checkForCollectable();
                    this.currentlyIntractable = this.currentLevel.checkForNPC();
                    //check if the player reached the end of level
                    if (this.currentLevel.reachedEndOfLevel()) {
                        log.info("Completed level {}", this.currentLevel.getName());
                        if (!this.currentLevel.endLevel()) {
                            log.info("Completed the game.");
                            this.gameState = GameState.QUIT;
                        } else {
                            this.currentLevel.saveProgress(true);
                            this.backgroundView.setImage(this.currentLevel.getBackground());
                            log.info("Saved a checkpoint at: {}", this.currentLevel.getName());
                        }
                    }
                    //refresh the scene
                    Platform.runLater(() -> {
                        refreshScene();
                        stage.show();
                    });
                    //check whether the player died
                    if (this.currentLevel.getPlayer().getHealthBar().getHealth() <= 0) {
                        handleDeath();
                    }
                }
            } while (this.gameState != GameState.QUIT);
        //once the game is quit the loop is terminated shutdown the thread and load to menu
        executorService.shutdown();
        try {
            log.info("Quitting to menu");
            quitToMenu();
        } catch (IOException e) {
            throw new FxmlLoadingException("Failed to initialize menu FXML file.",e);
        }
        });
    }
    /**
     * This method is used to refresh the scene.
     * It clears the canvas and then paints the player, end point, obstacles, collectibles, hazards, and NPCs to the canvas.
     */
    private void refreshScene() {
        this.canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
        List<GameObject> gameObjects = new ArrayList<>();
        gameObjects.addAll(this.currentLevel.getHazards());
        gameObjects.addAll(this.currentLevel.getObstacles());
        gameObjects.addAll(this.currentLevel.getNPCs());
        gameObjects.addAll(this.currentLevel.getCollectibles());
        gameObjects.add(this.currentLevel.getPlayer());
        gameObjects.add(this.currentLevel.getEndPoint());
        for(GameObject gameObject : gameObjects){
            gameObject.paintToCanvas(this.canvas.getGraphicsContext2D());
        }
    }
    /**
     * This method is used to pause or unpause the game.
     * If the game is running, it pauses the game and shows the pause menu if openMenu is true.
     * If the game is not running, it unpauses the game and removes the pause menu if openMenu is true.
     * If the inventory is open, it closes it.
     * @param openMenu - a boolean indicating whether to open the pause menu when pausing the game or remove it when unpausing the game.
     */
    public void pause(boolean openMenu){
        if(this.gameState == GameState.RUNNING) {
            log.info("Game paused");
            this.gameState = GameState.PAUSED;
            if(openMenu){
                this.pauseMenu.showPauseMenu(canvas.getGraphicsContext2D(), this.pane);
                handlePauseMenu();
            }
        }
        else {
            log.info("Game unpaused");
            this.gameState = GameState.RUNNING;
            if(openMenu){
                this.pane.getChildren().remove(pauseMenu.getVbox());
            }
            //if the inventory is open close it
            if(pane.getChildren().contains(this.currentLevel.getPlayer().getInventory().getDisplayBox())){
                this.pane.getChildren().remove(inventoryControl.getInventory().getDisplayBox());
            }
        }

    }
    /**
     * This method is used to quit the current game and return to the main menu.
     * It loads the "menu-view.fxml" file and sets the scene of the current stage to the main menu.
     * @throws IOException if there is an error loading the FXML file.
     */
    @FXML
    public void quitToMenu() throws IOException {
        Stage stage = (Stage) pane.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("menu-view.fxml"));
        Scene scene = new Scene(loader.load(), this.width, this.height);
        loader.setController(new Menu());

        Platform.runLater(()->{
            stage.setTitle("My Game Engine");
            stage.setScene(scene);
            stage.show();
        });

    }
    /**
     * This method sets the key event handlers for the game.
     * It sets the actions to be performed when certain keys are pressed or released.
     */
    public void setKeyEventHandler(){
        this.canvas.getScene().setOnKeyPressed(e ->{
            if(e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT){
                aPress.set(true);
            }
            if(e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT){
                dPress.set(true);
            }
            if(e.getCode() == KeyCode.TAB){
                openInventory();
            }
            if(e.getCode() == KeyCode.ESCAPE){
                pause(true);
            }
            if((e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.UP) && !jumping.get()){
                this.currentLevel.jump();
                jumping.set(true);
            }
            if(e.getCode() == KeyCode.E ){
                if(currentlyIntractable != null){
                    currentlyIntractable.showDialog(this.pane, inventoryControl);
                }
                if(currentlyCollectable != null) {
                    if (inventoryControl.addItem(currentlyCollectable.getCollected())) {
                        this.currentLevel.getCollectibles().remove(currentlyCollectable);
                    } else {
                        log.info("Inventory is full");
                    }
                }
            }
        });
        this.canvas.getScene().setOnKeyReleased(e ->{
            if(e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT){
                aPress.set(false);
            }
            if(e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT){
                dPress.set(false);
            }
            if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.SPACE){
                jumping.set(false);
            }
        });

    }

    /**
     * This method is used to open or close the inventory.
     * If the game is running, it pauses the game and displays the inventory.
     * If the game is not running, it unpauses the game and removes the inventory display.
     */
    public void openInventory(){
        if(gameState == GameState.RUNNING){
            pause(false);
            inventoryControl.displayInventory(this.pane, this.canvas.getGraphicsContext2D());
            log.info("Opened inventory.");
        }
        else {
            pause(false);
            this.pane.getChildren().remove(inventoryControl.getInventory().getDisplayBox());
        }
    }

    /**
     * This method sets the actions to be performed when the "Quit" and "Unpause" buttons in the pause menu are clicked.
     */
    public void handlePauseMenu(){
        this.pauseMenu.getQuit().setOnAction(event ->{
            this.currentLevel.saveProgress(false);
            this.gameState = GameState.QUIT;
        });
        this.pauseMenu.getUnpause().setOnAction(event ->
                pause(true)
        );
    }

    /**
     * This method sets the actions to be performed when the "Quit" and "Load Game" buttons in the death screen are clicked.
     * @param checkpoint - the level to load when the "Load Game" button is clicked.
     */
    public void handleDeathScreen(Level checkpoint){
        this.deathScreen.getQuit().setOnAction(event ->
                this.gameState = GameState.QUIT
        );
        this.deathScreen.getLoadGame().setOnAction(event ->{
            this.pane.getChildren().remove(this.currentLevel.getPlayer().getHealthBar().getHbox());
            this.currentLevel = checkpoint;
            this.pane.getChildren().remove(deathScreen.getVbox());
            this.pane.getChildren().add(this.currentLevel.getPlayer().getHealthBar().getHbox());
            this.currentLevel.getPlayer().getHealthBar().updateHealthBar();
            this.inventoryControl.setInventory(this.currentLevel.getPlayer().getInventory());
            this.inventoryControl.setOwner(this.currentLevel.getPlayer());
            this.gameState = GameState.RUNNING;
            setKeyEventHandler();
            refreshScene();
        });
    }

    /**
     * This method is called when the player dies.
     * It pauses the game, loads the last checkpoint, overwrites the saved position, and displays the death screen.
     */
    public void handleDeath(){
        pause(false);
        Level checkpoint;
        //load the last checkpoint (i.e. beginning of last reached level)
        try {
            checkpoint = FileManager.loadLevel("src/main/data/saves/last_checkpoint.json");
        } catch (IOException e) {
            throw new LoadLevelException("Failed to load level: last_checkpoint.json",e);
        }
        //overwrite the saved position saved while quitting mid-game (to prevent player to from placing his own checkpoints by quitting and then loading game)
        log.info("Loaded checkpoint after death.");
        try {
            FileManager.overwriteSavedPosition(checkpoint);
        } catch (IOException e) {
            throw new SaveProgressException("Failed to overwrite your last saved session position upon death: last_session.json",e);
        }
        log.info("Overwrote the last session save with the checkpoint");

        this.canvas.getScene().setOnKeyReleased(null);
        this.canvas.getScene().setOnKeyPressed(null);
        log.info("Player died at coordinates x: {} y: {}", this.currentLevel.getPlayer().getCoords().getX(), this.currentLevel.getPlayer().getCoords().getY());
        Platform.runLater(()-> this.deathScreen.showDeathScreen(this.canvas.getGraphicsContext2D(), this.pane));
        handleDeathScreen(checkpoint);

    }
}
