package org.tomek.engine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.tomek.engine.controls.Menu;

import java.io.IOException;

/**
 * The entry point of the game engine application.
 * Launches the JavaFX application and displays the menu.
 */
public class Launcher extends Application {

    private static final int WINDOW_WIDTH = 1920;

    private static final int WINDOW_HEIGHT = 1080;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("menu-view.fxml"));
        Scene scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        loader.setController(new Menu());
        stage.setTitle("My Game Engine");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
