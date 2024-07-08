package org.tomek.engine.gameobjects;

import org.junit.jupiter.api.Test;
import org.tomek.engine.FileManager;
import org.tomek.engine.controls.HealthBar;
import org.tomek.engine.controls.InventoryControl;
import org.tomek.engine.enums.QuestState;
import org.tomek.engine.enums.Type;
import org.tomek.engine.models.Coords;
import org.tomek.engine.models.Inventory;
import org.tomek.engine.models.Item;
import org.tomek.engine.models.Quest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
class NPCTest {
    @Test
    public void testGetWordCount() {
        InputStream stream = FileManager.class.getResourceAsStream("cigara.png");
        assert stream != null;
        NPC npc = new NPC(new Coords(50, 50), new ArrayList<>(), "", "skeleton.png" );

        String testString1 = "Hello world!";
        assertEquals(2, npc.getWordCount(testString1), "Expected word count of 2");

        String testString2 = "This is a longer test string with seven words.";
        assertEquals(7, npc.getWordCount(testString2), "Expected word count of 7");

        String testString3 = "";
        assertEquals(0, npc.getWordCount(testString3), "Expected word count of 0");
    }
    @Test
    public void testUpdateDialog() {
        // Initialize necessary objects and parameters
        Coords coords = new Coords(0, 0);
        List<Quest> quests = new ArrayList<>();
        quests.add(new Quest(new Item("item", Type.quest, 0, null, "branik_icon.png"), new ArrayList<>(), 0,0, QuestState.NOT_STARTED, false, null));

        NPC npc = getNpc(coords, quests);

        // Check the dialog text
        // This assumes that you have a getter for the text property in your NPC class
        assertEquals("Expected dialog text", npc.getText().getText());
    }

    private static NPC getNpc(Coords coords, List<Quest> quests) {
        Player player = new Player(new Inventory(10, 5, 2, new ArrayList<>(), 500, 400, "emptyslot.png"), coords, 6, 6, 6, new HealthBar(6,6, "heart.png", "emptyheart.png"), "franta2.png");

        // Create a NPC object
        NPC npc = new NPC(coords, quests, "Hello!", "skeleton.png");

        // Create a mock InventoryControl object
        InventoryControl inventoryControl = new InventoryControl(player.getInventory(), player);

        // Call the updateDialog method
        npc.updateDialog(inventoryControl);
        return npc;
    }
}