package me.icodetits.customCrates.task;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface Task {

	void scroll(Inventory i, int end, int start, ItemStack item);

	void start();

	void setRainbowContents(String crateName, boolean random);

	void clean();
	
	default int getMaxDecreases(int level) {
		if (level > 0 && level < 100) {
			return 1;
		} else if (level >= 100 && level < 200) {
			return 2;
		} else if (level >= 200) {
			return 3;
		} else {
			return 1;
		}
	}
	
	default double getIncreaseRange(int level) {
		if (level > 200) {
			level = 200;
		}
		return (1 + (Math.min(level, 200) / 200));
	}
}
