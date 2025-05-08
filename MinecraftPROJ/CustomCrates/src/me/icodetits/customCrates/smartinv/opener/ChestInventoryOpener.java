package me.icodetits.customCrates.smartinv.opener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.google.common.base.Preconditions;

import me.icodetits.customCrates.smartinv.InventoryManager;
import me.icodetits.customCrates.smartinv.SmartInventory;

public class ChestInventoryOpener implements InventoryOpener {

    @Override
    public Inventory open(SmartInventory inv, Player player) {
        Preconditions.checkArgument(inv.getSize() % 9 == 0,
                "The size count for the chest inventory must be factor of 9, found: %s.", inv.getSize());

        InventoryManager manager = inv.getManager();
        Inventory handle = Bukkit.createInventory(player, inv.getSize(), inv.getTitle());

        fill(handle, manager.getContents(player).get());

        player.openInventory(handle);
        return handle;
    }

    @Override
    public boolean supports(InventoryType type) {
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }
}
