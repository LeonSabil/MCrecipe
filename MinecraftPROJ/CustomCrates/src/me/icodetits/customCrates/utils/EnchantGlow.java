package me.icodetits.customCrates.utils;

import java.lang.reflect.Field;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class EnchantGlow extends EnchantmentWrapper {

	private static Enchantment glow;

	public EnchantGlow(int id) {
		super(id);
	}

	@Override
	public boolean canEnchantItem(ItemStack item) {
		return true;
	}

	@Override
	public boolean conflictsWith(Enchantment other) {
		return false;
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		return null;
	}

	@Override
	public int getMaxLevel() {
		return 10;
	}

	@Override
	public String getName() {
		return "Glow";
	}

	@Override
	public int getStartLevel() {
		return 1;
	}

	public static Enchantment getGlow() {

		try {
			if (glow != null) {
				return glow;
			}
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);

			glow = new EnchantGlow(255);
			Enchantment.registerEnchantment(glow);

			return glow;
		} catch (Exception e) {
			return null;
		}

	}

	public static void addGlow(ItemStack item) {
		try {
			Enchantment glow = getGlow();

			item.addEnchantment(glow, 1);	
		} catch (Exception ignore) {
			
		}
	}

}