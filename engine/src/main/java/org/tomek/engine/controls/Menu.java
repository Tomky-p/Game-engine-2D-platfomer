package org.tomek.engine.controls;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.tomek.engine.exceptions.LoadLevelException;
import org.tomek.engine.FileManager;

import java.io.IOException;

/**
 * Represents the menu of the game used as a controller for the elements of the menu-view.fxml.
 * The menu provides options to start a new game, load a saved game, or quit the application.
 */
public class Menu {
    @FXML
    public Button closeButton;
    @FXML
    public Button startButton;
    @FXML
    public Button loadButton;

    /**
     * This method is used to start a new game.
     * It loads the start level from the "start.json" file, creates a new Game object with the loaded level, sets the scene of the current stage to the game, and runs the game.
     * If there is an error loading the level, it throws a LoadLevelException.
     */
    @FXML
    public void startGame(){
        Stage stage = (Stage) startButton.getScene().getWindow();
        //get and load level file (convert to level object)
        try {
             //NOTE: the level that is the starting point (first level) for the game is always called "start.json"
            Level level = FileManager.loadLevel("src/main/data/levels/start.json");
            //start a new game i.e. overwrite the last checkpoints saved in previous run
            FileManager.overwriteSave(level);
            //create a new game object and run it
            Game game = new Game(level, 1900, 1060);
            Scene scene = new Scene(game.getPane(), 1900, 1060);
            stage.setTitle("Platformer");
            stage.setScene(scene);
            game.runGame();

        } catch (IOException e) {
            throw new LoadLevelException("Failed to load level: start.json",e);
        }

    }

    /**
     * This method is used to load a game from the last saved session.
     * It loads the level from the "last_session.json" file, creates a new Game object with the loaded level, sets the scene of the current stage to the game, and runs the game.
     * If there is an error loading the level, it throws a LoadLevelException.
     */
    @FXML
    public void loadGame(){
        Stage stage = (Stage) loadButton.getScene().getWindow();
        //get and load level file to level object (convert to level object)
        try {
            Level level = FileManager.loadLevel("src/main/data/saves/last_session.json"); // the last saved position when the called "last_session.json"
            //create a game with loaded level and run it
            Game game = new Game(level, 1900, 1060);
            Scene scene = new Scene(game.getPane(), 1900, 1060);
            stage.setTitle("Platformer");
            stage.setScene(scene);
            game.runGame();
        } catch (IOException e) {
            throw new LoadLevelException("Failed to load level: last_session.json",e);
        }

    }
    /**
     * This method is used to quit the application.
     * It closes the current stage.
     */
    @FXML
    public void quit(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
