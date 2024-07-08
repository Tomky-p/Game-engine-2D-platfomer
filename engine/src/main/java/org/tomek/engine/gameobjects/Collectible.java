package org.tomek.engine.gameobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.tomek.engine.models.Coords;
import org.tomek.engine.models.Item;

/**
 * Represents a collectible item in the game.
 * A collectible has coordinates, a texture, a texture file name, and an associated item.
 */
@Getter
@Setter
public class Collectible implements GameObject {
    @Setter
    private Coords coords; //setter included if the user would want to implement moving items
    @JsonIgnore
    private Image texture;

    private Item item;
    @Setter
    private String textureFileName;

    @JsonIgnore
    public Item getCollected(){
        return this.item;
    }

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
    public void setTexture(Image image){ this.texture = image;}
    @Override
    public String getTextureFileName(){ return this.textureFileName; }

    @JsonCreator
    public Collectible(@JsonProperty("coords") Coords coords,
                       @JsonProperty("item") Item item,
                       @JsonProperty("textureFileName") String textureFileName) {
        this.coords = coords;
        this.texture = null;
        this.item = item;
        this.textureFileName = textureFileName;
    }
}
