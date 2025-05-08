package me.icodetits.customCrates.task.tasks;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Player;

import me.icodetits.customCrates.crateplayer.CPManager;
import me.icodetits.customCrates.crateplayer.CratePlayer;
import me.icodetits.customCrates.task.Task;

public abstract class RouletteV2Task implements Task {
	
	private WeakReference<Player> player;
	private CratePlayer cp;
	
	public RouletteV2Task(Player p) {
		this.player = new WeakReference<Player>(p);
		this.cp = CPManager.getCP(p);
		this.cp.setCrateInv(null);
	}
	
	public CratePlayer getCPlayer() {
		return this.cp;
	}
	
	public void onClose() {
		if (this.cp != null) {
			this.cp.destroy(null);
			this.cp = null;
		}
		if (this.player != null) {
			this.player.clear();
			this.player = null;
		}
	}
}