package org.tomek.engine.gameobjects;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.tomek.engine.models.Coords;
import org.tomek.engine.controls.HealthBar;
import org.tomek.engine.models.Inventory;

/**
 * Represents the player character in the game.
 * The player has an inventory, coordinates, movement attributes, texture, and health bar.
 */
@Getter
@Setter
public class Player implements GameObject {

    private Inventory inventory;

    private Coords coords;
    @Setter
    private int walkSpeed; //setter not used but included for if user wants to add items or events that modify this value
    @Setter
    private int jumpHeight; //setter not used but included for if user wants to add items or events that modify this value
    @Setter
    private int jumpForce; //setter not used but included for if user wants to add items or events that modify this value
    @JsonIgnore
    private Image texture;
    @Setter
    private boolean invulnerable;

    private HealthBar healthBar;
    @Setter
    private String textureFileName;

    @JsonCreator
    public Player(@JsonProperty("inventory")Inventory inventory,
                  @JsonProperty("coords")Coords coords,
                  @JsonProperty("walkSpeed")int walkSpeed,
                  @JsonProperty("jumpHeight")int jumpHeight,
                  @JsonProperty("jumpForce")int jumpForce,
                  @JsonProperty("healthBar")HealthBar healthBar,
                  @JsonProperty("textureFileName")String textureFileName){
        this.inventory = inventory;
        this.coords = coords;
        this.walkSpeed = walkSpeed;
        this.jumpHeight = jumpHeight;
        this.jumpForce = jumpForce;
        this.texture = null;
        this.healthBar = healthBar;
        this.textureFileName = textureFileName;
        this.invulnerable = false;
    }

    @Override
    public void paintToCanvas(GraphicsContext graphicsContext) {
        graphicsContext.drawImage(texture, coords.getX(), coords.getY());
    }
    @Override
    public Coords getCoords() {
        return coords;
    }
    @Override
    public Image getTexture() { return texture;}
    @Override
    public void setTexture(Image image){
        this.texture = image;
    }
    @Override
    public String getTextureFileName(){ return this.textureFileName; }

}
