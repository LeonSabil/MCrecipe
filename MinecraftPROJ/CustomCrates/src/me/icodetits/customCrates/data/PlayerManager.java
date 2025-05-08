package me.icodetits.customCrates.data;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import lombok.Getter;
import lombok.Setter;

public class PlayerManager implements Listener {

	private static PlayerManager instance;

	public static PlayerManager getInstance() {
		if (instance == null) {
			synchronized (PlayerManager.class) {
				if (instance == null) {
					instance = new PlayerManager();
				}
			}
		}

		return instance;
	}

	public void register() {
		
	}
	
	public void unregister() {	
		
	}

	public PlayerData getByPlayer(Player p) {
		return new PlayerData(p);
	}
	
}
