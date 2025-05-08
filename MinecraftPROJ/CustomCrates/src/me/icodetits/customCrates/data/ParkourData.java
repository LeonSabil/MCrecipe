package me.icodetits.customCrates.data;

import org.bukkit.Location;

import lombok.Getter;
import lombok.Setter;

public class ParkourData {
	
	@Getter @Setter private String name;
	@Getter @Setter private Location center;
	
	public ParkourData(String name, Location center) {
		this.name = name;
		this.center = center;
	}
}
