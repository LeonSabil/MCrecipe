package me.icodetits.customCrates.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class MenuItem {
	private Menu menu;
	private int slot;

	protected void addToMenu(Menu menu) {
		this.menu = menu;
	}

	protected void removeFromMenu(Menu menu) {
		if (this.menu == menu) {
			this.menu = null;
		}
	}

	public Menu getMenu() {
		return this.menu;
	}

	public int getSlot() {
		return this.slot;
	}

    public void setSlot(int slot) {
        this.slot = slot;
    }

	public abstract void onClick(Player p, InventoryClickType click);

	public abstract ItemStack getItemStack();

	public static abstract class UnclickableMenuItem extends MenuItem {
		@Override
		public void onClick(Player player, InventoryClickType clickType) {}
	}
}
