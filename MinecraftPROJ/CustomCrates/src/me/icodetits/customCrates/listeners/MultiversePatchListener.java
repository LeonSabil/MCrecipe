package me.icodetits.customCrates.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;

import me.icodetits.customCrates.crateplayer.CPManager;
import me.icodetits.customCrates.crateplayer.CratePlayer;

public class MultiversePatchListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTeleport(MVPlayerTouchedPortalEvent event) {
		Player player = event.getPlayer();
		if (player == null) {
			return;
		}
		
		CratePlayer cratePlayer = CPManager.getCP(player);
		if (cratePlayer != null) {
			event.setCanUseThisPortal(true);
			event.setCancelled(true);
		}
	}
}
