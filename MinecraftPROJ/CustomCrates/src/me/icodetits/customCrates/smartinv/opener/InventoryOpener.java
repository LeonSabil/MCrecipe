package me.icodetits.customCrates.smartinv.opener;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import me.icodetits.customCrates.smartinv.ClickableItem;
import me.icodetits.customCrates.smartinv.SmartInventory;
import me.icodetits.customCrates.smartinv.content.InventoryContents;

public interface InventoryOpener {

    Inventory open(SmartInventory inv, Player player);
    boolean supports(InventoryType type);

    default void fill(Inventory handle, InventoryContents contents) {
        ClickableItem[] items = contents.all();

		for (int slot = 0; slot < items.length; slot++) {
			if (items[slot] != null)
				handle.setItem(slot, items[slot].getItem());
		}
	}

}
