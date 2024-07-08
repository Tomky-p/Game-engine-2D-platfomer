package org.tomek.engine.gameobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.tomek.engine.models.Coords;
import org.tomek.engine.controls.InventoryControl;
import org.tomek.engine.models.Item;
import org.tomek.engine.models.Quest;
import org.tomek.engine.enums.QuestState;

import java.util.List;

/**
 * Represents a non-player character (NPC) in the game.
 * An NPC can have quests, a dialog, and coordinates.
 * NPC serves as a controller for its own quests
 */
@Getter
@Setter
@Slf4j
public class NPC implements GameObject {

    private Coords coords; //setter included if the user would want to implement moving NPCs
    @JsonIgnore
    private Image texture;

    private List<Quest> quests;
    @JsonIgnore
    private final HBox textBox;
    @JsonIgnore
    private final Label text;

    private String commonDialog;

    private String textureFileName;
    @JsonIgnore
    private PauseTransition dialogPause;

    /**
     * This method is used to display a dialog from the NPC.
     * If the NPC has quests, it updates the dialog based on the current state of the quests.
     * If the NPC does not have quests, it shows the NPC's common dialog.
     * The dialog is displayed for a duration based on the word count of the dialog.
     * If a dialog box is already displayed, it removes the dialog box before displaying the new dialog.
     * @param pane - the pane to which to add the dialog box.
     * @param inventoryControl - the inventory control passed down to updateDialog.
     */
    public void showDialog(StackPane pane, InventoryControl inventoryControl){
        //get current appropriate dialog if the NPC has no quest show its common dialog
        if(!this.quests.isEmpty()){
            updateDialog(inventoryControl);
        }else {
            this.text.setText(this.commonDialog);
        }
        //if a dialog box is already displayed remove the dialog box
        if(pane.getChildren().contains(this.textBox)) Platform.runLater(() -> pane.getChildren().remove(this.textBox));

        //set timer to the value based on the length of the displayed dialog
        double dialogDisplayTime = getWordCount(text.getText()) * 0.9;
        //display dialog box

        dialogPause.setDuration(Duration.seconds(dialogDisplayTime));
        dialogPause.setOnFinished(event -> Platform.runLater(() -> pane.getChildren().remove(this.textBox)));

        // Display dialog box
        Platform.runLater(() -> pane.getChildren().add(this.textBox));

        // Start timer
        dialogPause.playFromStart();
    }

    /**
     * This method is used to update the NPC's dialog based on the current state of the quests.
     * If the NPC is not awaiting an item, it prepares the next dialog.
     * If the NPC is awaiting an item, it checks the inventory for the quest item.
     * If the quest item is found, it completes the quest, changes the dialog to the last one, deletes the quest item from the inventory, and adds the quest reward to the inventory.
     * If the NPC has explained the quest, it starts awaiting the item.
     * @param inventoryControl - the inventory control used to check for the quest item.
     */
    public void updateDialog(InventoryControl inventoryControl){
        //set current dialog
        this.text.setText(this.quests.getFirst().getCurrentDialog());

        //if the NPC is not awaiting the item get prepare next dialog
        if(this.quests.getFirst().getState() == QuestState.NOT_STARTED) {
            this.quests.getFirst().setDialogIndex(this.quests.getFirst().getDialogIndex() + 1);
        }

        //if the NPC has told the player what they want then search inventory for the item
        if(this.quests.getFirst().getState().equals(QuestState.AWAITING_ITEM)) {
            for (Item item : inventoryControl.getInventory().getItems()) {
                //if the quest item is found complete the quest and change the dialog to the last one and deletes quest item from inventory and
                if (item.getName().equals(this.quests.getFirst().getQuestItem().getName())) {
                    this.quests.getFirst().setState(QuestState.COMPLETED);
                    this.quests.getFirst().setDialogIndex(this.quests.getFirst().getDialogIndex() + 1);
                    this.text.setText(this.quests.getFirst().getCurrentDialog());
                    inventoryControl.discardItem(item);
                    if(this.quests.getFirst().getReward() != null) inventoryControl.addItem(this.quests.getFirst().getReward());
                    this.quests.remove(this.quests.getFirst());
                    return;
                }
            }
        }
        //if the NPC has explained the quest start awaiting item
        if(this.quests.getFirst().getDialogIndex() == this.quests.getFirst().getIndexOfChange()){
            this.quests.getFirst().setState(QuestState.AWAITING_ITEM);
        }
    }

    @Override
    public void paintToCanvas(GraphicsContext graphicsContext) {
        graphicsContext.drawImage(texture, coords.getX(), coords.getY());
    }
    /**
     * This method is used to count the number of words in a string for the purpose of getting dialog display time.
     * It trims the string, splits it into words by whitespace, and returns the number of words.
     * If the string is empty, it returns 0.
     * @param string - the string to count the words in.
     * @return double - the number of words in the string.
     */
    public double getWordCount(String string){
        string = string.trim();
        if (string.isEmpty()) {
            return 0;
        }
        String[] words = string.split("\\s+");
        return words.length;
    }
    @Override
    public void setTexture(Image image){
        this.texture = image;
    }
    @Override
    public String getTextureFileName(){ return this.textureFileName; }
    @Override
    public Coords getCoords(){ return this.coords; }

    @Override
    public Image getTexture(){ return  this.texture; }

    @JsonCreator
    public NPC(@JsonProperty("coords")Coords coords,
               @JsonProperty("quests")List<Quest> quests,
               @JsonProperty("commonDialog")String commonDialog,
               @JsonProperty("textureFileName")String textureFileName){
        this.coords = coords;
        this.texture = null;
        this.quests = quests;
        this.commonDialog = commonDialog;
        this.textureFileName = textureFileName;
        this.text = new Label();
        this.textBox = new HBox();
        //set background and padding of text box
        this.text.setPadding(new Insets(10, 500, 100, 500));
        Font font = Font.font("System", FontWeight.BOLD, 20);
        this.text.setFont(font);
        BackgroundFill backgroundFill = new BackgroundFill(Color.rgb(128,128,128, 0.9), null, null);
        Background background = new Background(backgroundFill);
        this.text.setBackground(background);

        //set alignment of the display and add the text box to it
        this.textBox.setAlignment(Pos.BOTTOM_CENTER);
        this.textBox.getChildren().add(this.text);

        //create and initialize timer for dialogs
        this.dialogPause = new PauseTransition(Duration.seconds(1));
    }
}
