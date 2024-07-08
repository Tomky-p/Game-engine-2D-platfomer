package org.tomek.engine.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * This class represents an inventory system for a player.
 * It holds items and manages their display and usage.
 */
@Getter
@Setter
public class Inventory{

    private int slotCount;

    private int rowSize;

    private int rowCount;

    private List<Item> items;
    @Setter
    private int fullSlotCount;

    private double displayWidth;

    private double displayHeight;
    @JsonIgnore
    private final GridPane displayBox;
    @Setter
    @JsonIgnore
    private Image emptySlotTexture;
    @JsonIgnore
    private ImageView[][] itemSlotViews;

    private String emptySlotTextureFileName;


    @JsonCreator
    public Inventory(@JsonProperty("slotCount")int slotCount,
                     @JsonProperty("rowSize")int rowSize,
                     @JsonProperty("rowCount")int rowCount,
                     @JsonProperty("items")List<Item> items,
                     @JsonProperty("displayWidth")double displayWidth,
                     @JsonProperty("displayHeight")double displayHeight,
                     @JsonProperty("emptySlotTextureFileName")String emptySlotTextureFileName){
        this.slotCount = slotCount;
        this.rowSize = rowSize;
        this.rowCount = rowCount;
        this.items = items;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.emptySlotTexture = null;
        this.emptySlotTextureFileName = emptySlotTextureFileName;
        this.displayBox = new GridPane();
        this.fullSlotCount = 0;
        this.itemSlotViews = new ImageView[rowCount][];
        for(int i = 0; i < this.rowCount; i++) {
            int size = rowSize;
            if(i == this.rowCount-1) size = slotCount - rowSize*i;
            this.itemSlotViews[i] = new ImageView[size];
            for (int j = 0; j < size; j++) {
                ImageView view = new ImageView();
                this.itemSlotViews[i][j] = view;
                this.displayBox.add(view, j, i);
            }
        }
    }
}
