package org.tomek.engine;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.tomek.engine.exceptions.JsonLoadingException;
import org.tomek.engine.exceptions.JsonSavingException;
import org.tomek.engine.exceptions.TextureLoadingException;
import org.tomek.engine.gameobjects.NPC;
import org.tomek.engine.gameobjects.Obstacle;
import org.tomek.engine.models.Item;
import org.tomek.engine.controls.Level;
import org.tomek.engine.gameobjects.Collectible;
import org.tomek.engine.gameobjects.GameObject;
import org.tomek.engine.models.Quest;

/**
 * Handles file operations such as saving and loading game data, assets and textures.
 * Manages a texture cache to avoid loading duplicate textures.
 */
@Slf4j
public class FileManager {

    private static final Map<String, Image> textureCache = new HashMap<>();

    private static final String TEXTURE_EXCEPTION = "Failed to load asset: ";

    /**
     * This method is used to save a level to a file.
     * It serializes the level object to a JSON string and writes it to the specified file.
     * If there is an error saving the level, it throws a JsonSavingException.
     * @param level - the level to save.
     * @param path - the path to the file to save the level to.
     * @throws IOException if there is an error writing to the file.
     */
    public static void saveLevel(Level level, String path) throws IOException {
        //here be level saving algorithm
        File lastSave = new File("src/main/data/" + path);
        try (FileWriter writer = new FileWriter(lastSave, false)) {

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(level);

            writer.write(jsonString);
        } catch (IOException e) {
            throw new JsonSavingException(e.getMessage(), e);
        }
        log.info("Saved level: {} to file: {}", level.getName(), path);
    }

    /**
     * This method is used to load a level from a file.
     * It reads the file, deserializes the JSON string to a Level object, loads the textures for the level, and returns the level.
     * If there is an error loading the level, it throws a JsonLoadingException.
     * @param filePath - the path to the file to load the level from.
     * @return Level - the loaded level.
     * @throws IOException if there is an error reading from the file.
     */
    public static Level loadLevel(String filePath) throws IOException{
        //here be level file reading algorithm
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Level newLevel = objectMapper.readValue(new File(filePath), Level.class);
            log.info("Loaded level: {} from file: {}", newLevel.getName(), filePath);
            loadTextures(newLevel);
            log.info("Loaded texture assets of level: {}", newLevel.getName());
            newLevel.getPlayer().getHealthBar().updateHealthBar();
            return newLevel;

        } catch (IOException e) {
            throw new JsonLoadingException(e.getMessage(), e);
        }
    }

    /**
     * This method is used to overwrite the saved position with the checkpoint.
     * It saves the checkpoint to the "last_session.json" file.
     * @param checkpoint - the checkpoint to save.
     * @throws IOException if there is an error saving the checkpoint.
     */
    public static void overwriteSavedPosition(Level checkpoint) throws IOException {
        saveLevel(checkpoint, "saves/last_session.json");
    }
    //TO DO include java doc for this and classes
    public static void overwriteSave(Level newGameStart) throws IOException {
        saveLevel(newGameStart, "saves/last_checkpoint.json");
    }

    /**
     * This method is used to load the textures for a level.
     * It loads the textures for the level background, game objects, collectable items, inventory items, quest rewards, inventory slot, and health bar.
     * @param level - the level to load the textures for.
     */
    public static void loadTextures(Level level){
        List<GameObject> gameObjects = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        String textureFileName;
        Image texture;

        //load level background
        texture = textureCache.get(level.getBackgroundFileName());
        if (texture == null) texture = getTexture(level.getBackgroundFileName());
        level.setBackground(texture);

        gameObjects.addAll(level.getHazards());
        gameObjects.addAll(level.getObstacles());
        gameObjects.addAll(level.getNPCs());
        gameObjects.addAll(level.getCollectibles());
        gameObjects.add(level.getPlayer());
        gameObjects.add(level.getEndPoint());

        //textures for game objects (player, collectables, hazards, obstacles, NPCs and endpoint)
        for (GameObject object : gameObjects){
            textureFileName = object.getTextureFileName();
            texture = textureCache.get(textureFileName);
            if (texture == null) texture = getTexture(textureFileName);
            object.setTexture(texture);
        }

        //add all items into one collection for ease of access
        for (Collectible collectible : level.getCollectibles()){
            items.add(collectible.getItem());
        }
        for (NPC npc : level.getNPCs()){
            for (Quest quest : npc.getQuests()){
                if(quest.getReward() != null) items.add(quest.getReward());
                if(quest.getQuestItem() != null) items.add(quest.getQuestItem());
            }
        }
        items.addAll(level.getPlayer().getInventory().getItems());

        //for all item icons and their upgrades and results of those upgrades
        for (Item item : items){
            textureFileName = item.getIconFileName();
            texture = textureCache.get(textureFileName);
            if (texture == null) texture = getTexture(textureFileName);
            item.setIcon(texture);
            //for upgrades
            if(item.getUpgradableBy() != null){
                textureFileName = item.getUpgradableBy().getIconFileName();
                texture = textureCache.get(textureFileName);
                if (texture == null) texture = getTexture(textureFileName);
                item.getUpgradableBy().setIcon(texture);
                //for upgrade results
                if(item.getUpgradableBy().getUpgradableBy() != null){
                    textureFileName = item.getUpgradableBy().getUpgradableBy().getIconFileName();
                    texture = textureCache.get(textureFileName);
                    if (texture == null) texture = getTexture(textureFileName);
                    item.getUpgradableBy().getUpgradableBy().setIcon(texture);
                }
            }
        }

        //inventory slot texture
        textureFileName = level.getPlayer().getInventory().getEmptySlotTextureFileName();
        texture = textureCache.get(textureFileName);
        if (texture == null) texture = getTexture(textureFileName);
        level.getPlayer().getInventory().setEmptySlotTexture(texture);

        //heart texture for health bar
        textureFileName = level.getPlayer().getHealthBar().getHeartTextureFileName();
        texture = textureCache.get(textureFileName);
        if (texture == null) texture = getTexture(textureFileName);
        level.getPlayer().getHealthBar().setHeartTexture(texture);

        //empty heart texture for health bar
        textureFileName = level.getPlayer().getHealthBar().getEmptyHeartTextureFileName();
        texture = textureCache.get(textureFileName);
        if (texture == null) texture = getTexture(textureFileName);
        level.getPlayer().getHealthBar().setEmptyHeartTexture(texture);

    }

    /**
     * This method is used to get a texture from a file.
     * It reads the file, creates an Image object from the file, adds the image to the texture cache, and returns the image.
     * If there is an error loading the texture, it throws a TextureLoadingException.
     * @param textureFileName - the name of the file to load the texture from.
     * @return Image - the loaded texture.
     */
    public static Image getTexture(String textureFileName){
        Image texture;
        try (InputStream stream = FileManager.class.getResourceAsStream(textureFileName)) {
            if (stream == null) {
                throw new TextureLoadingException(TEXTURE_EXCEPTION + textureFileName, null);
            }
            texture = new Image(stream);
            textureCache.put(textureFileName, texture);
        }
        catch (IOException e){
            throw new TextureLoadingException(TEXTURE_EXCEPTION + textureFileName,e);
        }
        return texture;
    }
    private FileManager(){}

}
