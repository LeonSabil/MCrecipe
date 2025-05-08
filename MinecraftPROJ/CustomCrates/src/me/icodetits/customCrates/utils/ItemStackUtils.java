package me.icodetits.customCrates.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.icodetits.customCrates.Main;

public class ItemStackUtils {

	private static List<String> replaceColors(List<String> list, ObjectSet... placeholders) {
		List<String> listTemp = new ArrayList<>();
		for (String s : list) {
			s = ChatColor.translateAlternateColorCodes('&', s);
			for (ObjectSet placeholder : placeholders) {
				s = s.replace(placeholder.getA().toString(), placeholder.getB().toString());
			}
			listTemp.add(s);
		}
		return listTemp;
	}

	@SuppressWarnings("unchecked")
	public static ItemStack load(Map<String, Object> keys, ObjectSet... placeholders) {
		try {
			ItemStack stack = null;
			String item = "";
			
			if (keys.containsKey("material")) {
				if (keys.get("material") instanceof List<?>) {
					List<String> list = ((List<String>) keys.get("material"));
					item = list.get((keys.containsKey("index") ? (int) keys.get("index") : ThreadLocalRandom.current().nextInt(list.size())));
				} else {
					item = keys.get("material").toString();
				}
			}

			if (keys.containsKey("material") && keys.containsKey("amount")) {
				String amountStr = keys.get("amount").toString();
				for (ObjectSet placeholder : placeholders) {
					amountStr = amountStr.replace(placeholder.getA().toString(), placeholder.getB().toString());
				}
				
				int parsed = Integer.parseInt(amountStr);
				
				stack = Main.getInstance().getEss().getItemDb().get(item, (parsed < 1 ? 1 : Math.min(parsed, 64)));
			} else {
				stack = Main.getInstance().getEss().getItemDb().get(item, 1);
			}

			ItemMeta meta = stack.getItemMeta();

			if (keys.containsKey("name")) {
				String name = ChatColor.translateAlternateColorCodes('&', keys.get("name").toString());
				name = name.replace("<name>", WordUtils.capitalizeFully(stack.getType().name().replace("_", " ")));
				
				for (ObjectSet placeholder : placeholders) {
					name = name.replace(placeholder.getA().toString(), placeholder.getB().toString());
				}
				meta.setDisplayName(name);
			}

			if (keys.containsKey("playerhead")) {
				String owner = ChatColor.translateAlternateColorCodes('&', keys.get("playerhead").toString());
				for (ObjectSet placeholder : placeholders) {
					owner = owner.replace(placeholder.getA().toString(), placeholder.getB().toString());
				}
				((SkullMeta) meta).setOwner(owner);
			}

			if (keys.containsKey("lore")) {
				List<String> lore = replaceColors((List<String>) keys.get("lore"));
				meta.setLore(lore);
			}

			if (keys.containsKey("enchants")) {
				List<String> enchants = (List<String>) keys.get("enchants");
				for (String s : enchants) {
					String[] parts = s.split(":");
					if (EnchantUtils.argsToEnchant(parts[0]) == null) {
						continue;
					}
					if (meta instanceof EnchantmentStorageMeta) {
						((EnchantmentStorageMeta) meta).addStoredEnchant(EnchantUtils.argsToEnchant(parts[0]), Integer.parseInt(parts[1]), true);
					} else {
						meta.addEnchant(EnchantUtils.argsToEnchant(parts[0]), Integer.parseInt(parts[1]), true);
					}
				}
			}
			stack.setItemMeta(meta);
			
			if (keys.containsKey("enchanted")) {
				boolean enchanted = Boolean.valueOf(keys.get("enchanted").toString());
				if (enchanted) {
					EnchantGlow.addGlow(stack);
				}
			}

			return stack;
		} catch (Exception ignore) {
			Main.getInstance().getLogger().severe(ChatColor.stripColor(ignore.getMessage()));
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean isSimilar(ItemStack item, ItemStack compare) {
		if (item == null || compare == null) {
			return false;
		}
		
		if (item == compare) {
			return true;
		}
		
		if (item.getTypeId() != compare.getTypeId()) {
			return false;
		}
		
		if (item.getDurability() != compare.getDurability()) {
			return false;
		}
		
		if (item.hasItemMeta() != compare.hasItemMeta()) {
			return false;
		}
		
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			if (item.getItemMeta().hasDisplayName() != compare.getItemMeta().hasDisplayName()) {
				return false;
			}
			
			if (!(item.getItemMeta().getDisplayName().equals(compare.getItemMeta().getDisplayName()))) {
				return false;
			}
		}

		return true;
	}
}
