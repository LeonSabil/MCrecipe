package me.icodetits.customCrates.utils;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.icodetits.customCrates.Main;

public class FireworkUtils {
	public static void firework(Location loc) {
		final Firework f = loc.getWorld().spawn(loc, Firework.class);
		FireworkMeta fm = f.getFireworkMeta();
		fm.clearEffects();
		fm.addEffect(getRandomEffect());
		f.setFireworkMeta(fm);
		new BukkitRunnable() {

			@Override
			public void run() {
				f.detonate();
			}
		}.runTaskLater(Main.getInstance(), 2L);
	}

	private static Color getColor(int i) {
		Color c = null;

		if (i == 0) {
			c = Color.AQUA;
		} else if (i == 1) {
			c = Color.BLACK;
		} else if (i == 2) {
			c = Color.BLUE;
		} else if (i == 3) {
			c = Color.FUCHSIA;
		} else if (i == 4) {
			c = Color.GRAY;
		} else if (i == 5) {
			c = Color.GREEN;
		} else if (i == 6) {
			c = Color.LIME;
		} else if (i == 7) {
			c = Color.MAROON;
		} else if (i == 8) {
			c = Color.NAVY;
		} else if (i == 9) {
			c = Color.OLIVE;
		} else if (i == 10) {
			c = Color.ORANGE;
		} else if (i == 11) {
			c = Color.PURPLE;
		} else if (i == 12) {
			c = Color.RED;
		} else if (i == 13) {
			c = Color.SILVER;
		} else if (i == 14) {
			c = Color.TEAL;
		} else if (i == 15) {
			c = Color.WHITE;
		} else if (i == 16) {
			c = Color.YELLOW;
		} else if (i == 17) {
			c = Color.TEAL;
		}
		return c;
	}

	public static FireworkEffect getRandomEffect() {

		int rt = ThreadLocalRandom.current().nextInt(4) + 1;
		FireworkEffect.Type type = FireworkEffect.Type.BALL;

		if (rt == 2) {
			type = FireworkEffect.Type.BALL_LARGE;
		} else if (rt == 3) {
			type = FireworkEffect.Type.BURST;
		} else if (rt == 4) {
			type = FireworkEffect.Type.CREEPER;
		} else if (rt == 5) {
			type = FireworkEffect.Type.STAR;
		}

		FireworkEffect effect = FireworkEffect.builder().flicker(ThreadLocalRandom.current().nextBoolean())
				.withColor(getColor(ThreadLocalRandom.current().nextInt(17)))
				.withFade(getColor(ThreadLocalRandom.current().nextInt(17))).with(type)
				.trail(ThreadLocalRandom.current().nextBoolean()).build();

		return effect;

	}
}
