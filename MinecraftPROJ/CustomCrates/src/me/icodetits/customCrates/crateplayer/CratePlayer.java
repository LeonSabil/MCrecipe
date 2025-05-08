package me.icodetits.customCrates.crateplayer;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;

import me.icodetits.customCrates.data.DropperData;
import me.icodetits.customCrates.data.ParkourData;
import me.icodetits.customCrates.task.OpenCrate;

public class CratePlayer {

	private transient UUID uuid;
	private transient Location previousLocation;
	private transient Inventory inv;
	private transient OpenCrate openCrate;
	private transient String crateName;
	private transient DropperData dropperData;
	private transient ParkourData parkourData;

	public CratePlayer(Player p) {
		this.uuid = p.getUniqueId();
	}
	
	public UUID getUniqueId() {
		return this.uuid;
	}

	public void setCrateInv(Inventory inv) {
		this.inv = inv;
	}

	public Inventory getCrateInv() {
		return this.inv;
	}
	
	public void setCrateName(String crateName) {
		this.crateName = crateName;
	}

	public String getCrateName() {
		return this.crateName;
	}

	public void setOpenCrate(OpenCrate openCrate) {
		this.openCrate = openCrate;
	}

	public OpenCrate getOpenCrate() {
		return this.openCrate;
	}
	
	public void setDropperData(DropperData dropperData) {
		this.dropperData = dropperData;
	}
	
	public DropperData getDropperData() {
		return this.dropperData;
	}
	
	public void setParkourData(ParkourData parkourData) {
		this.parkourData = parkourData;
	}
	
	public ParkourData getParkourData() {
		return this.parkourData;
	}
	
	public void setPreviousLocation(Location previousLocation) {
		this.previousLocation = previousLocation;
	}
	
	public Location getPreviousLocation() {
		return this.previousLocation;
	}

	public void add() {
		CPManager.getCratePlayers().add(this);
	}

	public void destroy(Player p) {
		if (this.previousLocation != null) {
			if (p != null && (this.dropperData != null || this.parkourData != null)) {
				p.teleport(this.previousLocation, TeleportCause.UNKNOWN);
			}
			
			this.previousLocation = null;
		}

		if (this.uuid != null) {
			this.uuid = null;
		}
		
		if (this.dropperData != null) {
			this.dropperData = null;
		}
		
		if (this.parkourData != null) {
			this.parkourData = null;
		}

		if (this.inv != null) {
			this.inv.clear();
			this.inv = null;
		}

		if (this.openCrate != null) {
			this.openCrate.destroy();
			this.openCrate = null;
		}

		CPManager.getCratePlayers().remove(this);
	}
}
