package org.tomek.engine.controls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tomek.engine.models.Item;
import org.tomek.engine.models.Quest;
import org.tomek.engine.exceptions.LoadLevelException;
import org.tomek.engine.exceptions.SaveProgressException;
import org.tomek.engine.gameobjects.*;
import org.tomek.engine.FileManager;

import java.util.List;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a level in the game serves as a base building block of game created in the engine.
 * Is loaded from a users JSON level files.
 * Each level contains all the important game data and assets: player, hazards, NPCs, obstacles, collectibles, and other game objects.
 * It manages player movement, collision detection, level completion, and saving/loading game progress.
 */
@Getter
@Setter
@Slf4j
public class Level {

    private Player player;

    private String name;

    private List<Hazard> hazards;

    private List<NPC> NPCs;

    private List<Obstacle> obstacles;

    private List<Collectible> collectibles;

    private String nextLevelFileName; //if its "end" then no level is after it
    @Setter
    private int gravityForce;
    @JsonIgnore
    private Timeline jumpTimer;

    private GameObject endPoint;

    private List<Item> required;

    private String backgroundFileName;
    @JsonIgnore
    private Image background;

    @JsonCreator
    public Level(@JsonProperty("player")Player player,
                 @JsonProperty("name")String name,
                 @JsonProperty("hazards")List<Hazard> hazards,
                 @JsonProperty("NPCs")List<NPC> NPCs,
                 @JsonProperty("obstacles")List<Obstacle> obstacles,
                 @JsonProperty("collectibles")List<Collectible> collectibles,
                 @JsonProperty("nextLevelFileName")String nextLevelFileName,
                 @JsonProperty("gravityForce")int gravityForce,
                 @JsonProperty("endPoint")GameObject endPoint,
                 @JsonProperty("required")List<Item> required,
                 @JsonProperty("backgroundFileName")String backgroundFileName){
        this.player = player;
        this.name = name;
        this.hazards = hazards;
        this.NPCs = NPCs;
        this.obstacles = obstacles;
        this.collectibles = collectibles;
        this.nextLevelFileName = nextLevelFileName;
        this.gravityForce = gravityForce;
        this.endPoint = endPoint;
        this.required = required;
        this.backgroundFileName = backgroundFileName;
    }

    /**
     * This method is used to end the current level and load the next one.
     * If the next level file name is null or "end", the method will return false indicating that there are no more levels.
     * Otherwise, it will load the next level from the file and update the player and level data.
     * If there is an error loading the level, it will throw a LoadLevelException.
     * @return boolean - returns true if the level ended successfully, false otherwise.
     */
    public boolean endLevel(){
        if(this.nextLevelFileName == null || this.nextLevelFileName.equals("end")){
            return false;
        }
        try {
            //create next level object
            Level nextLevel = FileManager.loadLevel("src/main/data/levels/" + this.nextLevelFileName);
            //preserve the player but change his position to the starting position of the next level
            this.player.getCoords().setX(nextLevel.player.getCoords().getX());
            this.player.getCoords().setY(nextLevel.player.getCoords().getY());
            //set the rest of level data to the next level data
            this.name = nextLevel.name;
            this.hazards = nextLevel.hazards;
            this.NPCs = nextLevel.NPCs;
            this.obstacles = nextLevel.obstacles;
            this.collectibles = nextLevel.collectibles;
            this.nextLevelFileName = nextLevel.nextLevelFileName;
            this.gravityForce = nextLevel.gravityForce;
            this.endPoint = nextLevel.endPoint;
            this.required = nextLevel.required;
            this.backgroundFileName = nextLevel.backgroundFileName;
            this.background = nextLevel.background;
        } catch (IOException e) {
            throw new LoadLevelException("Failed to load level: " + this.nextLevelFileName, e);
        }
        return true;
    }

    /**
     * This method is used to save the current progress of the game.
     * If the player chooses to quit mid-game, checkpoint is false and the game is saved as "last_session.json".
     * If the player reaches a checkpoint i.e. new level, the game is saved as "last_checkpoint.json".
     * If there is an error saving the level, it will throw a SaveProgressException.
     * @param checkpoint - a boolean indicating whether the save is a checkpoint or a session save.
     */
    public void saveProgress(boolean checkpoint){
        //if player chooses to quit mid-game checkpoint is false, if the player reaches a checkpoint i.e. new level saves as a checkpoint
        String saveLocation = (checkpoint) ? "last_checkpoint.json" : "last_session.json";
        try {
            FileManager.saveLevel(this, "/saves/" + saveLocation);
        } catch (IOException e) {
            throw new SaveProgressException("Failed to save level: " + this.name, e);
        }
    }

    /**
     * This method is used to check if the player would collide with any obstacle or hazard with a given offset in the x and y directions.
     * If the player's new position would intersect with any obstacle or hazard, the method will return false.
     * If the player intersects with a hazard, it will also damage the player by the hazard's damage value.
     * @param offsetX - the offset in the x direction.
     * @param offsetY - the offset in the y direction.
     * @return boolean - returns true if the player would not collide with anything, false otherwise.
     */
    public boolean checkCollision(double offsetX, double offsetY){
        double newPlayerX = this.player.getCoords().getX() + offsetX;
        double newPlayerY = this.player.getCoords().getY() + offsetY;

        for(Obstacle obstacle : this.obstacles){
            // Get the obstacle's position and size
            double obstacleX = obstacle.getCoords().getX();
            double obstacleY = obstacle.getCoords().getY();
            double obstacleWidth = obstacle.getTexture().getWidth();
            double obstacleHeight = obstacle.getTexture().getHeight();

            // Check if the player's new position would intersect with the obstacle
            if(newPlayerX < obstacleX + obstacleWidth &&
                    newPlayerX + this.player.getTexture().getWidth() > obstacleX &&
                    newPlayerY < obstacleY + obstacleHeight &&
                    newPlayerY + this.player.getTexture().getHeight() > obstacleY){
                log.debug("Collision with obstacle at x: {} y: {} ", obstacle.getCoords().getX(), obstacle.getCoords().getY());
                return false;
            }
        }
        //if end point is obstacle check its collision
        if(endPoint instanceof Obstacle){
            double obstacleX = endPoint.getCoords().getX();
            double obstacleY = endPoint.getCoords().getY();
            double obstacleWidth = endPoint.getTexture().getWidth();
            double obstacleHeight = endPoint.getTexture().getHeight();

            // Check if the player's new position would intersect with the obstacle
            if(newPlayerX < obstacleX + obstacleWidth &&
                    newPlayerX + this.player.getTexture().getWidth() > obstacleX &&
                    newPlayerY < obstacleY + obstacleHeight &&
                    newPlayerY + this.player.getTexture().getHeight() > obstacleY){
                log.debug("Collision with obstacle end point at x: {} y: {} ", endPoint.getCoords().getX(), endPoint.getCoords().getY());
                return false;
            }
        }
        for(Hazard hazard : this.hazards){
            double hazardX = hazard.getCoords().getX();
            double hazardY = hazard.getCoords().getY();
            double hazardWidth = hazard.getTexture().getWidth() - 2;
            double hazardHeight = hazard.getTexture().getHeight() -2;

            if(newPlayerX < hazardX + hazardWidth &&
                    newPlayerX + this.player.getTexture().getWidth()> hazardX &&
                    newPlayerY < hazardY + hazardHeight &&
                    newPlayerY + this.player.getTexture().getHeight() > hazardY){
                damagePlayer(hazard.getDamage());
                log.debug("Collision with hazard at x: {} y: {} damage taken: {}", hazard.getCoords().getX(), hazard.getCoords().getY(), hazard.getDamage());
                return false;
            }
        }
        return true;
    }
    /**
     * This method checks if the player is currently colliding with a collectable item.
     * If the player's position intersects with a collectable, it returns that collectable.
     * Otherwise, it returns null.
     * @return Collectible - the collectible item the player is currently colliding with, or null if there is none.
     */
    public Collectible checkForCollectable(){
        for(Collectible collectable : this.collectibles){

            double collectableX = collectable.getCoords().getX();
            double collectableY = collectable.getCoords().getY();
            double collectableWidth = collectable.getTexture().getWidth();
            double collectableHeight = collectable.getTexture().getHeight();

            if(this.player.getCoords().getX() < collectableX + collectableWidth &&
                    this.player.getCoords().getX() + this.player.getTexture().getWidth() > collectableX &&
                    this.player.getCoords().getY() < collectableY + collectableHeight &&
                    this.player.getCoords().getY() + this.player.getTexture().getHeight() > collectableY){
                log.debug("Currently collectable item: {} at x: {} y: {}", collectable.getItem().getName(), collectable.getCoords().getX(), collectable.getCoords().getY());
                return collectable;
            }
        }
        return null;
    }
    /**
     * This method checks if the player is currently interacting with a non-player character (NPC).
     * If the player's position intersects with an NPC, it returns that NPC.
     * Otherwise, it returns null.
     * @return NPC - the NPC the player is currently interacting with, or null if there is none.
     */
    public NPC checkForNPC(){
        //check if end point is npc and if so ensure its function
        if(this.endPoint instanceof NPC) {
            double npcX = endPoint.getCoords().getX();
            double npcY = endPoint.getCoords().getY();
            double npcWidth = endPoint.getTexture().getWidth();
            double npcHeight = endPoint.getTexture().getHeight();

            if(this.player.getCoords().getX() < npcX + npcWidth &&
                    this.player.getCoords().getX() + this.player.getTexture().getWidth() > npcX &&
                    this.player.getCoords().getY() < npcY + npcHeight &&
                    this.player.getCoords().getY() + this.player.getTexture().getHeight() > npcY){
                log.debug("Currently interactable end point NPC at x: {} y: {}", endPoint.getCoords().getX(), endPoint.getCoords().getY());
                return (NPC) endPoint;
            }
        }
        for(NPC npc : this.getNPCs()/*npcs*/){
            double npcX = npc.getCoords().getX();
            double npcY = npc.getCoords().getY();
            double npcWidth = npc.getTexture().getWidth();
            double npcHeight = npc.getTexture().getHeight();

            if(this.player.getCoords().getX() < npcX + npcWidth &&
                    this.player.getCoords().getX() + this.player.getTexture().getWidth() > npcX &&
                    this.player.getCoords().getY() < npcY + npcHeight &&
                    this.player.getCoords().getY() + this.player.getTexture().getHeight() > npcY){
                log.debug("Currently interactable NPC at x: {} y: {}", npc.getCoords().getX(), npc.getCoords().getY());
                return npc;
            }
        }
        return null;
    }

    /**
     * This method moves the player to the left by the player's walk speed.
     * The player will only move if there is no collision at the new position.
     */
    public void moveLeft(){
        if(checkCollision(-this.player.getWalkSpeed(), 0)) {
            this.player.getCoords().setX(this.player.getCoords().getX() - this.player.getWalkSpeed());
            log.debug("Moving player left at speed: {}", this.player.getWalkSpeed());
        }
    }
    /**
     * This method moves the player to the right by the player's walk speed.
     * The player will only move if there is no collision at the new position.
     */
    public void moveRight(){
        if(checkCollision(this.player.getWalkSpeed(), 0)) {
            this.player.getCoords().setX(this.player.getCoords().getX() + this.player.getWalkSpeed());
            log.debug("Moving player right at speed: {}", this.player.getWalkSpeed());
        }
    }
    /**
     * This method is used to make the player jump.
     * The player will only jump if they are on the ground (i.e., not colliding with anything when moving downwards by the gravity force).
     * The player's y-coordinate is updated every 0.016 seconds to simulate the jump, until the player reaches their maximum jump height or collides with something.
     */
    public void jump(){
        //check if the player is the on ground
        if(checkCollision(0, +gravityForce)){
            return;
        }
        log.debug("Player jumped.");
        if (jumpTimer != null) {
            jumpTimer.stop();
        }
        double height = this.player.getCoords().getY();
        jumpTimer = new Timeline(new KeyFrame(Duration.seconds(0.016), event -> {
            if (this.player.getCoords().getY() > height-this.player.getJumpHeight() && checkCollision(0, -this.player.getJumpForce()/*, obstacles, hazards*/)) {
                this.player.getCoords().setY(this.player.getCoords().getY() - this.player.getJumpForce());
            } else {
                jumpTimer.stop();
            }
        }));

        jumpTimer.setCycleCount(Animation.INDEFINITE);
        jumpTimer.play();
    }
    /**
     * This method is used to apply gravity to the player.
     * The player's y-coordinate is increased by the gravity force if they are not colliding with anything when moving downwards by the gravity force.
     */
    public void gravity(){
        if (checkCollision(0, gravityForce/*, obstacles, hazards*/)) {
            this.player.getCoords().setY(this.player.getCoords().getY() + gravityForce);
        }
        //check if the player has fallen out the map if so then kill them
        if(this.player.getCoords().getY() > 1080){
            damagePlayer(this.player.getHealthBar().getHealth());
        }
    }
    /**
     * This method is used to damage the player.
     * The player's health is decreased by the damage value, and the health bar is updated.
     * If the player's health drops below 0, it is set to 0.
     * After taking damage, the player becomes invulnerable for 1 second.
     * If the player is already invulnerable, they do not take any damage.
     * @param damage - the amount of damage to deal to the player.
     */
    public void damagePlayer(int damage){
        if(!this.player.isInvulnerable()) {
            this.player.getHealthBar().setHealth(this.player.getHealthBar().getHealth() - damage);
            if(this.player.getHealthBar().getHealth() < 0) this.player.getHealthBar().setHealth(0);
            this.player.getHealthBar().updateHealthBar();
            this.player.setInvulnerable(true);

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            try {
                executorService.schedule(() -> this.player.setInvulnerable(false), 1, TimeUnit.SECONDS);
            } finally {
                executorService.shutdown();
            }

        }else {
            log.debug("Player received no damage due to prior damage invulnerability");
        }
    }

    /**
     * This method checks if the player has reached the end of the level and met all the conditions to complete the level.
     * The conditions are: the player is at the end of the level, the player has completed all mandatory quests, and the player has collected all mandatory items.
     * If the player meets all the conditions, the method returns true.
     * Otherwise, it returns false.
     * @return boolean - returns true if the player has met all conditions to complete the level, false otherwise.
     */
    public boolean reachedEndOfLevel(){
        double endX = this.endPoint.getCoords().getX();
        double endY = this.endPoint.getCoords().getY();
        double endWidth = this.endPoint.getTexture().getWidth();
        double endHeight = this.endPoint.getTexture().getHeight();
        //account for the fact player cannot move inside an Obstacle
        if(endPoint instanceof Obstacle){
            endX = endX - this.player.getWalkSpeed();
            endY = endY - this.gravityForce;
            endWidth = endWidth + this.player.getWalkSpeed();
            endHeight = endHeight + this.gravityForce;
        }

        //check if the player is at the end of the level
        if(this.player.getCoords().getX() < endX + endWidth &&
                this.player.getCoords().getX() + this.player.getTexture().getWidth() > endX &&
                this.player.getCoords().getY() < endY + endHeight &&
                this.player.getCoords().getY() + this.player.getTexture().getHeight() > endY){
            //check if the player has completed all quests
            for (NPC npc : this.NPCs){
                for (Quest quest : npc.getQuests()){
                    if(!quest.isSideQuest()) {
                        log.debug("Player hasn't completed all mandatory quests to finish level.");
                        return false;
                    }
                }
            }
            //check if the player has collected all required items
            for (Item requirement : this.required){
                boolean ret = false;
                for(Item item : this.player.getInventory().getItems()){
                    if(item.getName().equals(requirement.getName())){
                        ret = true;
                        break;
                    }
                }
                if(!ret){
                    log.debug("Player hasn't collected all mandatory items to finish level.");
                    return false;
                }
            }
            log.info("Player met all conditions to complete level.");
            return true;

        }
        return false;
    }
}
