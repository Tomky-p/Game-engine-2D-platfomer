package org.tomek.engine.controls;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tomek.engine.models.Inventory;
import org.tomek.engine.models.Item;
import org.tomek.engine.enums.Type;
import org.tomek.engine.gameobjects.Player;


/**
 * InventoryControl class manages the player's inventory, including updating, displaying,
 * adding, discarding, and using items.
 */
@Getter
@Setter
@Slf4j
public class InventoryControl {

    private Inventory inventory;

    private Player owner;

    /**
     * This method is used to update the inventory.
     * It clears and refills the item slot views based on the current items in the inventory.
     * If an item slot is empty, it sets the image to the empty slot texture and removes the mouse click event.
     * If an item slot is not empty, it sets the image to the item's icon and sets the mouse click event to use the item.
     */
    public void updateInventory(){
        int count = 0;
        for(int i = 0; i < inventory.getRowCount(); i++) {
            int size = inventory.getRowSize();
            if(i == inventory.getRowCount()-1) size = inventory.getSlotCount() - inventory.getRowSize()*i;
            for (int j = 0; j < size; j++) {
                if(count < inventory.getFullSlotCount()){
                    Item item = inventory.getItems().get(count);
                    inventory.getItemSlotViews()[i][j].setImage(item.getIcon());
                    inventory.getItemSlotViews()[i][j].setOnMouseClicked(event -> useItem(item));
                }
                else {
                    inventory.getItemSlotViews()[i][j].setImage(inventory.getEmptySlotTexture());
                    inventory.getItemSlotViews()[i][j].setOnMouseClicked(null);
                }
                count++;
            }
        }
        log.info("Current inventory occupancy: {} out of {} available slots", inventory.getFullSlotCount(), inventory.getSlotCount());
    }
    /**
     * This method is used to display the inventory.
     * It fills a rectangle on the graphics context with a semi-transparent gray color, adds the inventory display box to the pane, and updates the inventory.
     * @param pane - the pane to which to add the inventory display box.
     * @param graphicsContext - the graphics context on which to draw the inventory.
     */
    public void displayInventory(StackPane pane, GraphicsContext graphicsContext){
        Color color = Color.rgb(128,128,128,0.9);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(950 - (inventory.getDisplayWidth()/2), 530 - (inventory.getDisplayHeight()/2) , inventory.getDisplayWidth(), inventory.getDisplayHeight());
        inventory.getDisplayBox().setAlignment(Pos.CENTER);
        Platform.runLater(()->pane.getChildren().add(inventory.getDisplayBox()));
        updateInventory();
    }
    /**
     * This method is used to discard an item from the inventory.
     * It removes the item from the inventory items, decreases the full slot count by 1, and updates the inventory.
     * @param item - the item to discard.
     */
    public void discardItem(Item item){
        inventory.getItems().remove(item);
        inventory.setFullSlotCount(inventory.getFullSlotCount() - 1);
        log.info("Discarded item: {}", item.getName());
        updateInventory();
    }
    /**
     * This method is used to use an item.
     * If the item is a healing item, it increases the owner's health by the item's stat modifier and updates the health bar.
     * If the item is a health upgrade item, it increases the owner's max health by the item's stat modifier and updates the health bar.
     * If the item is an upgrade item, it upgrades the corresponding item in the inventory.
     * After using the item, it discards the item and updates the inventory.
     * @param item - the item to use.
     */
    public void useItem(Item item){
        if(item.getItemType() == Type.healing){
            owner.getHealthBar().setHealth(owner.getHealthBar().getHealth() + item.getStatModifier());
            owner.getHealthBar().updateHealthBar();
            log.info("Used healing item: {}", item.getName());
            discardItem(item);
        }
        if(item.getItemType() == Type.health_upgrade){
            owner.getHealthBar().setMaxHealth(owner.getHealthBar().getMaxHealth() + item.getStatModifier());
            owner.getHealthBar().updateHealthBar();
            log.info("Used health upgrade item: {}Player max health changed by: {}", item.getName(), item.getStatModifier());
            discardItem(item);
        }
        if(item.getItemType() == Type.upgrade){
            upgradeItem(item);
        }
        updateInventory();
    }
    /**
     * This method is used to add an item to the inventory.
     * If there is space in the inventory, it adds the item to the inventory items, increases the full slot count by 1, updates the inventory, and returns true.
     * If there is no space in the inventory, it returns false.
     * @param item - the item to add.
     * @return boolean - returns true if the item was added, false otherwise.
     */
    public boolean addItem(Item item){
        if(inventory.getSlotCount() > inventory.getFullSlotCount()){
            inventory.getItems().add(item);
            inventory.setFullSlotCount(inventory.getFullSlotCount() + 1);
            log.info("Added item: {}", item.getName());
            updateInventory();
            return true;
        }
        return false;
    }
    /**
     * This method is used to upgrade an item in the inventory.
     * It finds the item in the inventory that can be upgraded by the upgrade item, discards the original item and the upgrade item, adds the upgraded item to the inventory, and updates the inventory.
     * @param upgrade - the upgrade item.
     */
    public void upgradeItem(Item upgrade){
        for (Item item : this.inventory.getItems()){
            if(item.getUpgradableBy() != null && item.getUpgradableBy().getName().equals(upgrade.getName())){
                Item upgraded = upgrade.getUpgradableBy(); //gets the item that is the result of the upgrade
                discardItem(item);
                discardItem(upgrade);
                addItem(upgraded);
                log.info("Used item: {} to upgrade item: {} resulting upgraded item is: {}", upgrade.getName(), item.getName(), upgraded.getName());
            }
        }
    }

    public InventoryControl(Inventory inventory, Player owner) {
        this.inventory = inventory;
        this.owner = owner;
    }
}
