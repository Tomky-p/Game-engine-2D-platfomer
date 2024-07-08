package org.tomek.engine.views;

import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.Getter;

/**
 * Represents the death screen displayed when the player dies.
 * It provides options to quit the game or load the last checkpoint.
 */
@Getter
public class DeathScreen {

    private final Button quit = new Button("Quit");

    private final Button loadGame = new Button("Load last checkpoint");

    private final VBox vbox = new VBox(10);

    private final Text deathMessage = new Text("You died!");

    /**
     * This method is used to initialize the death screen.
     * It sets the alignment of the vbox to center and adds the deathMessage, loadGame, and quit nodes to the vbox.
     */
    public void initDeathScreen(){
        this.vbox.setAlignment(Pos.CENTER);
        this.vbox.getChildren().addAll(deathMessage, loadGame, quit);
    }
    /**
     * This method is used to display the death screen.
     * It adds the vbox to the pane and fills a rectangle on the graphics context with a semi-transparent gray color.
     * @param graphicsContext - the graphics context on which to draw the death screen.
     * @param pane - the pane to which to add the vbox.
     */
    public void showDeathScreen(GraphicsContext graphicsContext, StackPane pane){
        pane.getChildren().add(this.vbox);
        Color color = Color.rgb(128,128,128,0.9);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(950 - ((double)200/2), 530 - ((double)100/2) , 200, 100);
    }
}
