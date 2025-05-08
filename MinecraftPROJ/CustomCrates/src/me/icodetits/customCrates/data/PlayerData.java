package me.icodetits.customCrates.data;

import java.sql.ResultSet;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;
import me.icodetits.customCrates.Main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.function.Consumer;
import ninja.coelho.arkjs.extlib.ExtLibService;

public class PlayerData {
	@Getter private Player player;
	
	public PlayerData(Player player) {
		this.player = player;
	}
	
	public void setup() {
		
	}

	public void cleanup(boolean async) {
		
	}
	
	public int getKey(String crate) {
		return ExtLibService.get().currency("@customcrates." + crate).get(this.getPlayer());
	}
	
	public void giveKeys(String crate, int amount) {
		ExtLibService.get().currency("@customcrates." + crate).give(this.getPlayer(), amount);
	}
	
	public void removeKeys(String crate, int amount, Consumer<Runnable> onSuccess) {
		ExtLibService.get().currency("@customcrates." + crate).take(this.getPlayer(), amount, onSuccess);
	}
}
