package org.tomek.engine.controls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the health bar of a game character controls updating its display and values.
 * The health bar displays the character's current health and maximum health.
 */
@Getter
@Setter
@Slf4j
public class HealthBar {
    @JsonIgnore
    private final HBox hbox;
    @Setter
    @JsonIgnore
    private Image heartTexture;
    @Setter
    @JsonIgnore
    private Image emptyHeartTexture;
    @JsonIgnore
    private List<ImageView> hearts;

    private int health;
    @Setter
    private int maxHealth;
    @Setter
    private String heartTextureFileName;
    @Setter
    private String emptyHeartTextureFileName;

    /**
     * This method is used to update the health bar.
     * It clears the hearts list, adds filled heart images for each point of health, and adds empty heart images for the remaining points up to maxHealth.
     * Then it clears the hbox and adds all the heart images to it.
     */
    public void updateHealthBar(){
        int i = 0;
        this.hearts.clear();
        for(; i < health; i++){
            this.hearts.add(new ImageView(heartTexture));
        }
        for (; i < maxHealth; i++){
            this.hearts.add(new ImageView(emptyHeartTexture));
        }
        Platform.runLater(()-> {
            this.hbox.getChildren().clear();
            this.hbox.getChildren().addAll(this.hearts);
        });
    }

    /**
     * This method is used to set the health of the health bar.
     * If the new health is greater than maxHealth, it sets the health to maxHealth.
     * Otherwise, it sets the health to the new health.
     * @param health - the new health value.
     */
    public void setHealth(int health) {
        if(health > this.maxHealth){
            log.info("Changed players heath from: {}, to max health: {}", this.health, this.maxHealth);
            this.health = this.maxHealth;
        }else {
            log.info("Changed players heath from: {}, to current HP: {}", this.health, health);
            this.health = health;
        }
    }

    @JsonCreator
    public HealthBar(
                     @JsonProperty("health")int health,
                     @JsonProperty("maxHealth")int maxHealth,
                     @JsonProperty("heartTextureFileName")String heartTextureFileName,
                     @JsonProperty("emptyHeartTextureFileName")String emptyHeartTextureFileName){
        this.heartTexture = null;
        this.emptyHeartTexture = null;
        this.health = health;
        this.maxHealth = maxHealth;
        this.heartTextureFileName = heartTextureFileName;
        this.emptyHeartTextureFileName = emptyHeartTextureFileName;
        this.hbox = new HBox();
        this.hearts = new ArrayList<>();
        this.hbox.setAlignment(Pos.TOP_LEFT);
    }

}
