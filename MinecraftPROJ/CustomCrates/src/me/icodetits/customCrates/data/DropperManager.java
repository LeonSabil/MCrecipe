package me.icodetits.customCrates.data;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import lombok.Setter;
import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.utils.LocationUtils;
import me.icodetits.customCrates.utils.RandomCollection;

public class DropperManager {

	private static DropperManager instance;

	public static DropperManager getInstance() {
		if (instance == null) {
			synchronized (DropperManager.class) {
				if (instance == null) {
					instance = new DropperManager();
				}
			}
		}

		return instance;
	}

	@Getter @Setter private CopyOnWriteArrayList<DropperData> droppers;
	@Getter @Setter private RandomCollection<DropperData> randomSelector;

	public void register() {
		if (this.droppers != null) {
			this.droppers.clear();
		}
				
		this.droppers = new CopyOnWriteArrayList<DropperData>();
		
		FileConfiguration dropperSaves = Main.getInstance().getDropperSaves();
		if (!(dropperSaves.isConfigurationSection("droppers"))) {
			return;
		}
		
		for (String key : dropperSaves.getConfigurationSection("droppers").getKeys(false)) {
			Location location = LocationUtils.stringtoLocation(dropperSaves.getString("droppers." + key + ".location"));
			
			getDroppers().add(new DropperData(key, location));
		}
		
		reinitializeSelector();
	}
	
	public void unregister() {	
		if (this.droppers != null) {			
			this.droppers.clear();
			this.droppers = null;	
		}
		
		if (this.randomSelector != null) {
			this.randomSelector.destroy();
			this.randomSelector = null;
		}
	}
	
	public void reinitializeSelector() {
		if (this.randomSelector != null) {
			this.randomSelector.destroy();
			this.randomSelector = null;
		}
		
		this.randomSelector = new RandomCollection<DropperData>();
		
		for (DropperData data : getDroppers()) {
			this.randomSelector.add(50.0D, data);
		}
	}
	
	public void registerDropper(String name, Location location) {
		DropperData data = new DropperData(name, location);
		this.droppers.add(data);

		FileConfiguration dropperSaves = Main.getInstance().getDropperSaves();
		dropperSaves.set("droppers." + name + ".location", LocationUtils.locationToString(location));
		Main.getInstance().saveDropperSaves();
		
		reinitializeSelector();
	}
	
	public void removeDropper(String name) {
		DropperData data = getByName(name);
		if (data != null) {
			this.droppers.remove(data);
		}

		FileConfiguration dropperSaves = Main.getInstance().getDropperSaves();
		dropperSaves.set("droppers." + name, null);
		Main.getInstance().saveDropperSaves();
		
		reinitializeSelector();
	}
	
	public DropperData getByName(String name) {
		return this.droppers.stream().filter(data -> data.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public DropperData getNextDropper() {
		if (this.droppers == null || this.droppers.isEmpty()) {
			return null;
		}
		
		return getRandomSelector().next();
	}
}
