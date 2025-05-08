package me.icodetits.customCrates.particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.icodetits.customCrates.Main;

public class Particle {

	private ParticleEffect particleName;
	private Location loc;
	private float[] data;
	private int amount;

	public Particle(Location loc, ParticleEffect particleName, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		this.loc = loc.add(new Vector(0.5D, 0.5D, 0.5D));
		this.particleName = particleName;
		this.data = new float[4];
		this.data[0] = offsetX;
		this.data[1] = offsetY;
		this.data[2] = offsetZ;
		this.data[3] = speed;
		this.amount = amount;
	}

	public static void create(Location loc, ParticleEffect particleName, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		Particle particle = new Particle(loc, particleName, offsetX, offsetY, offsetZ, speed, amount);
		particle.start();
	}

	private void start() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				try {
					if (loc.getWorld().getPlayers().isEmpty()) {
						return;
					}
					
					for (Player p : loc.getWorld().getPlayers()) {
						if (p.getLocation().distance(loc) <= 16) {
							particleName.display(data[0], data[1], data[2], data[3], amount, loc, p);
						}
					}
				} catch (Exception e) {}
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 5L, 5L);
	}
}
