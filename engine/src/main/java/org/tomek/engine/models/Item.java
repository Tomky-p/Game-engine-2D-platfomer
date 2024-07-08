package org.tomek.engine.models;

import com.fasterxml.jackson.annotation.*;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.tomek.engine.enums.Type;

/**
 * This class represents an item in the game.
 * An item can be used, discarded, or upgraded.
 */
@Getter
@Setter
public class Item {

    private String name;
    @Setter
    @JsonIgnore
    private Image icon;

    private Type itemType;

    private int statModifier;
    //for items that are upgrades(of "upgrade" type) this is used to store the item that is the result of the upgrade
    private Item upgradableBy; //is null when the item is not upgradable

    private String iconFileName;

    @JsonCreator
    public Item(@JsonProperty("name") String name,
                @JsonProperty("itemType") Type itemType,
                @JsonProperty("statModifier") int statModifier,
                @JsonProperty("upgradableBy") Item upgradableBy,
                @JsonProperty("iconFileName") String iconFileName) {
        this.name = name;
        this.icon = null;
        this.itemType = itemType;
        this.statModifier = statModifier;
        this.upgradableBy = upgradableBy;
        this.iconFileName = iconFileName;
    }
}
