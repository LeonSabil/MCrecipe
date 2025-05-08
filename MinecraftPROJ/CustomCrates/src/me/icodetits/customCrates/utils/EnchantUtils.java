package me.icodetits.customCrates.utils;

import org.bukkit.enchantments.Enchantment;

public class EnchantUtils {

	public static Enchantment argsToEnchant(String args) {
		if (args.equalsIgnoreCase("Power")) {
			return Enchantment.ARROW_DAMAGE;
		} else if (args.equalsIgnoreCase("Flame")) {
			return Enchantment.ARROW_FIRE;
		} else if (args.equalsIgnoreCase("Infinity")) {
			return Enchantment.ARROW_INFINITE;
		} else if (args.equalsIgnoreCase("Punch")) {
			return Enchantment.ARROW_KNOCKBACK;
		} else if (args.equalsIgnoreCase("Sharpness")) {
			return Enchantment.DAMAGE_ALL;
		} else if (args.equalsIgnoreCase("BaneofArthropods")) {
			return Enchantment.DAMAGE_ARTHROPODS;
		} else if (args.equalsIgnoreCase("Smite")) {
			return Enchantment.DAMAGE_UNDEAD;
		} else if (args.equalsIgnoreCase("Efficiency")) {
			return Enchantment.DIG_SPEED;
		} else if (args.equalsIgnoreCase("Unbreaking")) {
			return Enchantment.DURABILITY;
		} else if (args.equalsIgnoreCase("Fireaspect")) {
			return Enchantment.FIRE_ASPECT;
		} else if (args.equalsIgnoreCase("Knockback")) {
			return Enchantment.KNOCKBACK;
		} else if (args.equalsIgnoreCase("Fortune")) {
			return Enchantment.LOOT_BONUS_BLOCKS;
		} else if (args.equalsIgnoreCase("Looting")) {
			return Enchantment.LOOT_BONUS_MOBS;
		} else if (args.equalsIgnoreCase("LuckoftheSea")) {
			return Enchantment.LUCK;
		} else if (args.equalsIgnoreCase("Lure")) {
			return Enchantment.LURE;
		} else if (args.equalsIgnoreCase("Respiration")) {
			return Enchantment.OXYGEN;
		} else if (args.equalsIgnoreCase("Protection")) {
			return Enchantment.PROTECTION_ENVIRONMENTAL;
		} else if (args.equalsIgnoreCase("BlastProtection")) {
			return Enchantment.PROTECTION_EXPLOSIONS;
		} else if (args.equalsIgnoreCase("FeatherFalling")) {
			return Enchantment.PROTECTION_FALL;
		} else if (args.equalsIgnoreCase("FireProtection")) {
			return Enchantment.PROTECTION_FIRE;
		} else if (args.equalsIgnoreCase("ProjectileProtection")) {
			return Enchantment.PROTECTION_PROJECTILE;
		} else if (args.equalsIgnoreCase("Silktouch")) {
			return Enchantment.SILK_TOUCH;
		} else if (args.equalsIgnoreCase("Thorns")) {
			return Enchantment.THORNS;
		} else if (args.equalsIgnoreCase("Aqua Affinity")) {
			return Enchantment.WATER_WORKER;
		} else {
			return Enchantment.getByName(args);
		}
	}

}
