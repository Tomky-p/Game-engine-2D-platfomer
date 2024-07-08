package org.tomek.engine.views;

import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * Represents the pause menu displayed when the game is paused.
 * It provides options to return to the game or quit the game.
 */
@Getter
public class PauseMenu{

    private final Button quit = new Button("Quit");

    private final Button unpause = new Button("Return to game");

    private final VBox vbox = new VBox(10);

    /**
     * This method is used to initialize the pause menu.
     * It sets the alignment of the vbox to center and adds the unpause and quit nodes to the vbox.
     */
    public void initPauseMenu(){
        this.vbox.setAlignment(Pos.CENTER);
        this.vbox.getChildren().addAll(unpause,quit);
    }
    /**
     * This method is used to display the pause menu.
     * It adds the vbox to the pane and fills a rectangle on the graphics context with a semi-transparent gray color.
     * @param graphicsContext - the graphics context on which to draw the pause menu.
     * @param pane - the pane to which to add the vbox.
     */
    public void showPauseMenu(GraphicsContext graphicsContext, StackPane pane){
        pane.getChildren().add(vbox);
        Color color = Color.rgb(128,128,128,0.9);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(950 - ((double)200/2), 530 - ((double)100/2) , 200, 100);
    }
}
