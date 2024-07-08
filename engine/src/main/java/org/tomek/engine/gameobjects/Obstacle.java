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
 * Represents an obstacle in the game.
 * An obstacle has coordinates, a texture, and a texture file name.
 */
@Getter
@Setter
public class Obstacle implements GameObject {
    @Setter
    private Coords coords;
    @JsonIgnore
    private Image texture;
    @Setter
    private String textureFileName;

    @Override
    public void paintToCanvas(GraphicsContext graphicsContext) {
        graphicsContext.drawImage(texture, coords.getX(), coords.getY());
    }
    @Override
    public void setTexture(Image image){ this.texture = image; }
    @Override
    public String getTextureFileName(){ return this.textureFileName; }

    @JsonCreator
    public Obstacle(@JsonProperty("textureFilaName")String textureFileName,
                    @JsonProperty("coords")Coords coords){
        this.texture = null;
        this.textureFileName = textureFileName;
        this.coords = coords;
    }
}
