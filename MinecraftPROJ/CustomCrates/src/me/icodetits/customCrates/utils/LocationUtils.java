package me.icodetits.customCrates.utils;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {
	public static String locationToString(Location loc) {
		DecimalFormat mat = new DecimalFormat("###");
		return loc.getWorld().getName() + ";" + mat.format(loc.getBlockX()) + ";" + mat.format(loc.getBlockY()) + ";"
				+ mat.format(loc.getBlockZ());
	}

	public static Location stringtoLocation(String str) {
		String[] parts = str.split(";");
		return new Location(Bukkit.getWorld(parts[0]), Integer.valueOf(parts[1]), Integer.valueOf(parts[2]),
				Integer.valueOf(parts[3]));
	}
}
