package org.tomek.engine.gameobjects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.tomek.engine.models.Coords;

/**
 * Represents a game object in the game.
 * Game objects can be painted to a canvas and have textures and coordinates.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Player.class, name = "player"),
        @JsonSubTypes.Type(value = Hazard.class, name = "hazard"),
        @JsonSubTypes.Type(value = NPC.class, name = "npc"),
        @JsonSubTypes.Type(value = Obstacle.class, name = "obstacle"),
        @JsonSubTypes.Type(value = Collectible.class, name = "collectible")
})
public interface GameObject {
    /**
     * This method is used to paint the object to a canvas.
     * It draws the object's texture at the object's coordinates.
     * @param graphicsContext - the graphics context on which to draw the object.
     */
    void paintToCanvas(GraphicsContext graphicsContext);

    /**
     * This method is used to get the object's texture.
     * @return Image - the object's texture.
     */
    Image getTexture();

    /**
     * This method is used to get the object's coordinates.
     * @return Coords - the object's coordinates.
     */
    Coords getCoords();

    /**
     * This method is used to set the object's texture.
     * @param image - the new texture for the object.
     */
    void setTexture(Image image);

    /**
     * This method is used to get the file name of the object's texture.
     * @return String - the file name of the object's texture.
     */
    String getTextureFileName();
}
