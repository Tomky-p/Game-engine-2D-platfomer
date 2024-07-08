package org.tomek.engine.controls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomek.engine.gameobjects.Player;
import org.tomek.engine.models.Inventory;
import org.tomek.engine.models.Item;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class InventoryControlTest {
    private Inventory inventory;
    private InventoryControl inventoryControl;

    @BeforeEach
    void setUp() {
        inventory = mock(Inventory.class);
        Player owner = mock(Player.class);
        inventoryControl = new InventoryControl(inventory, owner);
    }

    @Test
    void testAddItem() {
        Item item = mock(Item.class);
        when(inventory.getSlotCount()).thenReturn(10);
        when(inventory.getFullSlotCount()).thenReturn(5);

        boolean result = inventoryControl.addItem(item);

        assert (result);
        verify(inventory, times(1)).getItems();
        verify(inventory, times(1)).setFullSlotCount(6);
    }

    @Test
    void testDiscardItem() {
        Item item = mock(Item.class);
        when(inventory.getFullSlotCount()).thenReturn(5);

        inventoryControl.discardItem(item);

        verify(inventory, times(1)).getItems();
        verify(inventory, times(1)).setFullSlotCount(4);
    }
}