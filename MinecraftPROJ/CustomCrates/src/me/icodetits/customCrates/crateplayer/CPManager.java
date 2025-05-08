package me.icodetits.customCrates.crateplayer;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Lists;

public class CPManager implements Listener {
	private static List<CratePlayer> cratePlayers;

	public static List<CratePlayer> getCratePlayers() {
		if (cratePlayers == null) {
			cratePlayers = Lists.newArrayList();
		}
		return cratePlayers;
	}

	public static void clearCratePlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			destroyCP(p);
		}
		getCratePlayers().clear();
	}

	public static CratePlayer getCP(Player p) {
		for (CratePlayer cp : getCratePlayers()) {
			if (cp.getUniqueId().equals(p.getUniqueId())) {
				return cp;
			}
		}
		return null;
	}

	public static CratePlayer makeCP(Player p) {
		CratePlayer cp = new CratePlayer(p);
		cp.add();
		return cp;
	}

	public static boolean hasCP(Player p) {
		return getCP(p) != null;
	}

	public static void destroyCP(Player p) {
		if (!(hasCP(p))) {
			return;
		}
		CratePlayer cp = getCP(p);
		cp.destroy(p);
		p.closeInventory();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (hasCP(e.getPlayer())) {
			destroyCP(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		if (hasCP(e.getPlayer())) {
			destroyCP(e.getPlayer());
		}
	}
}
