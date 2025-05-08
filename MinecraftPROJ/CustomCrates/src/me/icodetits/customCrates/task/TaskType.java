package me.icodetits.customCrates.task;

public enum TaskType {
	HORIZONTAL(27),
	VERTICAL(45),
	WHEEL(54),
	ROULETTE(27),
	ROULETTE_V2(54), // ArkJs
	CSGO(27),
	DROPPER(27),
	PARKOUR(27);

	private int inventory_size;

	private TaskType(int inventory_size) {
		this.inventory_size = inventory_size;
	}

	public int getInventorySize() {
		return this.inventory_size;
	}

	public static TaskType getByName(String name) {
		for (TaskType type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}
