package me.icodetits.customCrates.data;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import lombok.Setter;
import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.utils.LocationUtils;
import me.icodetits.customCrates.utils.RandomCollection;

public class ParkourManager {

	private static ParkourManager instance;

	public static ParkourManager getInstance() {
		if (instance == null) {
			synchronized (ParkourManager.class) {
				if (instance == null) {
					instance = new ParkourManager();
				}
			}
		}

		return instance;
	}

	@Getter @Setter private CopyOnWriteArrayList<ParkourData> parkourMaps;
	@Getter @Setter private RandomCollection<ParkourData> randomSelector;

	public void register() {
		if (this.parkourMaps != null) {
			this.parkourMaps.clear();
		}
				
		this.parkourMaps = new CopyOnWriteArrayList<ParkourData>();
		
		FileConfiguration parkourSaves = Main.getInstance().getParkourSaves();
		if (!(parkourSaves.isConfigurationSection("parkour-maps"))) {
			return;
		}
		
		for (String key : parkourSaves.getConfigurationSection("parkour-maps").getKeys(false)) {
			Location location = LocationUtils.stringtoLocation(parkourSaves.getString("parkour-maps." + key + ".location"));
			
			getParkourMaps().add(new ParkourData(key, location));
		}
		
		reinitializeSelector();
	}
	
	public void unregister() {	
		if (this.parkourMaps != null) {			
			this.parkourMaps.clear();
			this.parkourMaps = null;	
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
		
		this.randomSelector = new RandomCollection<ParkourData>();
		
		for (ParkourData data : getParkourMaps()) {
			this.randomSelector.add(50.0D, data);
		}
	}
	
	public void registerParkourMap(String name, Location location) {
		ParkourData data = new ParkourData(name, location);
		this.parkourMaps.add(data);

		FileConfiguration parkourSaves = Main.getInstance().getParkourSaves();
		parkourSaves.set("parkour-maps." + name + ".location", LocationUtils.locationToString(location));
		Main.getInstance().saveParkourSaves();
		
		reinitializeSelector();
	}
	
	public void removeParkourMap(String name) {
		ParkourData data = getByName(name);
		if (data != null) {
			this.parkourMaps.remove(data);
		}

		FileConfiguration parkourSaves = Main.getInstance().getDropperSaves();
		parkourSaves.set("parkour-maps." + name, null);
		Main.getInstance().saveParkourSaves();
		
		reinitializeSelector();
	}
	
	public ParkourData getByName(String name) {
		return this.parkourMaps.stream().filter(data -> data.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public ParkourData getNextMap() {
		if (this.parkourMaps == null || this.parkourMaps.isEmpty()) {
			return null;
		}
		
		return getRandomSelector().next();
	}
}
