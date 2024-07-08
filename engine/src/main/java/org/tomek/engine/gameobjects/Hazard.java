package org.tomek.engine.gameobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.tomek.engine.models.Coords;

/**
 * Represents a hazard that damages the player in the game.
 * A hazard has coordinates, a texture, a texture file name, and a damage value.
 */
@Getter
@Setter
public class Hazard implements GameObject {
    @Setter
    private Coords coords; //setter included if the user wanted to add moving hazards to their level
    @JsonIgnore
    private Image texture;

    private int damage;
    @Setter
    private String textureFileName;

    public void paintToCanvas(GraphicsContext graphicsContext) {
        graphicsContext.drawImage(texture, coords.getX(), coords.getY());
    }
    @Override
    public Coords getCoords() {
        return coords;
    }
    @Override
    public Image getTexture() {
        return texture;
    }
    @Override
    public void setTexture(Image image){ this.texture = image; }
    @Override
    public String getTextureFileName(){ return this.textureFileName; }

    @JsonCreator
    public Hazard(@JsonProperty("coords") Coords coords,
                  @JsonProperty("damage") int damage,
                  @JsonProperty("textureFileName") String textureFileName) {
        this.coords = coords;
        this.texture = null;
        this.damage = damage;
        this.textureFileName = textureFileName;
    }
}
