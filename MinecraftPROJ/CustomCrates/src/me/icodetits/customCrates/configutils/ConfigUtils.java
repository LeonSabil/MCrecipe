package me.icodetits.customCrates.configutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableList;

import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.particles.Particle;
import me.icodetits.customCrates.particles.ParticleEffect;
import me.icodetits.customCrates.task.TaskType;
import me.icodetits.customCrates.utils.FireworkUtils;
import me.icodetits.customCrates.utils.ItemStackUtils;
import me.icodetits.customCrates.utils.LocationUtils;
import me.icodetits.customCrates.utils.ObjectSet;
import me.icodetits.customCrates.utils.RandomCollection;
import me.icodetits.customCrates.utils.Sounds;

public class ConfigUtils {

	private static ConfigUtils instance;

	public static ConfigUtils getInstance() {
		if (instance == null) {
			instance = new ConfigUtils();
		}

		return instance;
	}
	
	private ConfigUtils() {}

	public void setupParticles() {
		if (getCrates().isEmpty()) {
			return;
		}

		String str = "Options.ParticleEffect";
		String particle = str + ".particle";
		String enabled = str + ".enabled";
		String onlywhenopening = str + ".onlywhenopening";
		String part = Main.getInstance().getConfig().getString(particle).toUpperCase();

		if (!Main.getInstance().getConfig().getBoolean(enabled)) {
			return;
		}
		if (Main.getInstance().getConfig().getBoolean(onlywhenopening)) {
			return;
		}
		if (part.equalsIgnoreCase("none")) {
			return;
		}
		if (part.equalsIgnoreCase("firework") || part.contains(":")) {
			return;
		}

		if (Main.getInstance().getCrateLocs().getConfigurationSection("Crates") == null) {
			return;
		}
		
		float offsetX = (float) Main.getInstance().getConfig().getDouble(str + ".offsetX");
		float offsetY = (float) Main.getInstance().getConfig().getDouble(str + ".offsetY");
		float offsetZ = (float) Main.getInstance().getConfig().getDouble(str + ".offsetZ");
		float speed = (float) Main.getInstance().getConfig().getDouble(str + ".speed");
		int amount = Main.getInstance().getConfig().getInt(str + ".amount");

		for (String s : Main.getInstance().getCrateLocs().getConfigurationSection("Crates").getKeys(false)) {
			Particle.create(LocationUtils.stringtoLocation(s), ParticleEffect.valueOf(part), offsetX, offsetY, offsetZ, speed, amount);
		}
	}

	public void playParticles(Location loc, String crate) {
		String str = "Options.ParticleEffect";
		String particle = str + ".particle";
		String enabled = str + ".enabled";
		String onlywhenopening = str + ".onlywhenopening";
		String part = Main.getInstance().getConfig().getString(particle).toUpperCase();

		if (!Main.getInstance().getConfig().getBoolean(enabled)) {
			return;
		}
		if (!Main.getInstance().getConfig().getBoolean(onlywhenopening)) {
			return;
		}
		if (part.equalsIgnoreCase("none")) {
			return;
		}
		if (part.equalsIgnoreCase("firework") || part.contains(":")) {
			return;
		}
		
		float offsetX = (float) Main.getInstance().getConfig().getDouble(str + ".offsetX");
		float offsetY = (float) Main.getInstance().getConfig().getDouble(str + ".offsetY");
		float offsetZ = (float) Main.getInstance().getConfig().getDouble(str + ".offsetZ");
		float speed = (float) Main.getInstance().getConfig().getDouble(str + ".speed");
		int amount = Main.getInstance().getConfig().getInt(str + ".amount");
		
		for (Player p : loc.getWorld().getPlayers()) {
			if (p.getLocation().distance(loc) <= 16) {
				ParticleEffect.valueOf(part).display(offsetX, offsetY, offsetZ, speed, amount, loc, p);
			}
		}
	}

	public void playFirework(Location loc, String crate, boolean end) {
		String str = "Options.ParticleEffect";
		String particle = str + ".particle";
		String enabled = str + ".enabled";
		String part = Main.getInstance().getConfig().getString(particle);
		if (!Main.getInstance().getConfig().getBoolean(enabled)) {
			return;
		}
		String firework = part.split(":")[0];

		if (!firework.equalsIgnoreCase("firework")) {
			return;
		}
		
		String time = null;

		try {
			time = part.split(":")[1];
		} catch (Exception ignore) {
			time = "start";
		}

		if (time.equalsIgnoreCase("start") && end) {
			return;
		}

		if (time.equalsIgnoreCase("end") && !end) {
			return;
		}

		FireworkUtils.firework(loc);
	}
	
	public boolean hide(String CrateName) {
		return Main.getInstance().getConfig().getBoolean("Crates." + CrateName + ".Hide");
	}
	
	public boolean isOldKey(String CrateName) {
		return Main.getInstance().getConfig().getBoolean("Crates." + CrateName + ".Old-Key");
	}

	public RandomCollection<ItemStack> getCrateRands(String crate) {
		RandomCollection<ItemStack> imList = new RandomCollection<ItemStack>();

		for (String items : Main.getInstance().getConfig().getConfigurationSection("Crates." + crate + ".Items").getKeys(false)) {
			String itemKey = "Crates." + crate + ".Items." + items;

			ItemStack im = ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection(itemKey).getValues(true));

			imList.add(Main.getInstance().getConfig().getDouble(itemKey + ".chance"), im);
		}

		return imList;
	}
	
	public List<ItemStack> getCrateItems(String crate) {
		List<ItemStack> imList = new ArrayList<ItemStack>();

		for (String items : Main.getInstance().getConfig().getConfigurationSection("Crates." + crate + ".Items").getKeys(false)) {
			String itemKey = "Crates." + crate + ".Items." + items;

			ItemStack im = ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection(itemKey).getValues(true));
			if (im == null) {
				continue;
			}

			imList.add(im);
		}

		return imList;
	}


	public ItemStack getBorder(String crate, int data) {
		ItemStack im = ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection("Crates." + crate + ".Border").getValues(true));
		im.setDurability((short) data);
		return im;
	}
	
	public ItemStack getBorder(String crate, double chance) {
		short dura = 0;
		
		if (chance <= 5) {
			dura = 4;
		} else if (chance > 5 && chance <= 15) {
			dura = 14;
		} else if (chance > 15 && chance <= 40) {
			dura = 6;
		} else if (chance > 40 && chance <= 60) {
			dura = 10;
		} else if (chance > 60 && chance <= 100) {
			dura = 11;
		}

		return getBorder(crate, dura);
	}

	public RandomCollection<Integer> getBorderDatas(String crate) {
		RandomCollection<Integer> imList = new RandomCollection<Integer>();
		for (Integer strDatas : Main.getInstance().getConfig().getIntegerList("Crates." + crate + ".Border.datas")) {
			imList.add(50, strDatas);
		}
		return imList;
	}

	public void finish(Player p, String crate, ItemStack endedOn) {
		for (String items : Main.getInstance().getConfig().getConfigurationSection("Crates." + crate + ".Items").getKeys(false)) {
			String itemKey = "Crates." + crate + ".Items." + items;

			ItemStack im = ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection(itemKey).getValues(true));
			
			if (ItemStackUtils.isSimilar(im, endedOn)) {
				if (!(Main.getInstance().getConfig().isList(itemKey + ".commands"))) {
					p.getInventory().addItem(im);
					continue;
				}
				
				for (String s : Main.getInstance().getConfig().getStringList(itemKey + ".commands")) {
					s = ChatColor.translateAlternateColorCodes('&', s);
					s = s.replaceAll("%(?i)player%", p.getName());
					s = s.replaceAll("%(?i)crate%", crate);
					s = s.replaceAll("%(?i)prefix%", Main.getInstance().getPrefix());

					if (s.startsWith("msg:")) {
						p.sendMessage(s.replace("msg:", ""));
					} else if (s.startsWith("bc:")) {
						for (Player online : Bukkit.getOnlinePlayers()) {
							online.sendMessage(s.replace("bc:", ""));
						}
					} else {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
					}
				}	
			}
		}
	}
	
	public void preCommands(Player p, String crate) {
		for (String s : Main.getInstance().getConfig().getStringList("Crates." + crate + ".PreCommands")) {

			s = ChatColor.translateAlternateColorCodes('&', s);
			s = s.replaceAll("%(?i)player%", p.getName());
			s = s.replaceAll("%(?i)crate%", crate);
			s = s.replaceAll("%(?i)prefix%", Main.getInstance().getPrefix());

			if (s.startsWith("msg:")) {
				p.sendMessage(s.replace("msg:", ""));
			} else if (s.startsWith("bc:")) {
				for (Player online : Bukkit.getOnlinePlayers()) {
					online.sendMessage(s.replace("bc:", ""));
				}
			} else {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
			}
		}
	}

	public ItemStack getCrateKey(Player p, String crate, int amount) {
		ItemStack im = ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection("Crates." + crate + ".Key").getValues(true));
		im.setAmount((amount < 1 ? 1 : Math.min(amount, 64)));
		
		ItemMeta meta = im.getItemMeta();		
		if (meta.hasLore()) {
			List<String> lore = meta.getLore();
			for (int i = 0; i < lore.size(); i++) {
				String s = lore.get(i);
				s = s.replaceAll("%(?i)crate%", crate);
				s = s.replaceAll("%(?i)amount%", Integer.toString(amount));
				lore.set(i, s);
			}
			
			meta.setLore(lore);
			im.setItemMeta(meta);
		}
		return im;
	}
	
	public ItemStack getSpacer() {
		return ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection("Options.Menu.spacer").getValues(true));
	}

	public TaskType getType(String crate) {
		try {
			return TaskType.getByName(Main.getInstance().getConfig().getString("Crates." + crate + ".OpenInv.type"));	
		} catch (Exception ignore) {
			return null;
		}
	}
	
	public int getSlot(String crate) {
		return Main.getInstance().getConfig().getInt("Crates." + crate + ".Key.slot", 0);
	}
	
	public String getSelectionName(String crate, int max) {
		return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("Options.Selection.name")).replace("%clicks%", Integer.toString(getSelectionClicks(crate, max)));
	}
	
	public int getSelectionClicks(String crate, int max) {
		return Math.min(max, Main.getInstance().getConfig().getInt("Crates." + crate + ".OpenInv.clicks", 1));
	}
	
	public ItemStack getSelectedIcon(int slot) {
		return ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection("Options.Selection.selected-icon").getValues(true), new ObjectSet("%slot%", Integer.toString(slot)));
	}
	
	public ItemStack getSelectIcon(int slot) {
		return ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection("Options.Selection.select-icon").getValues(true), new ObjectSet("%slot%", Integer.toString(slot)));
	}
	
	public int getMenuSize() {
		if (Main.getInstance().getConfig().getInt("Options.Menu.size") <= 0) {
			return fit(getCrates().size());
		}
		return Main.getInstance().getConfig().getInt("Options.Menu.size") % 9 == 0 ? Main.getInstance().getConfig().getInt("Options.Menu.size") / 9 : fit(getCrates().size());
	}
	
	public int getMenuUpdateSpeed() {
		return Main.getInstance().getConfig().getInt("Options.Menu.updateSpeed");
	}
	
	public boolean supportOldKeys() {
		return Main.getInstance().getConfig().getBoolean("Options.Menu.disableOldKeys", false);
	}
	
	public String getLicenseKey() {
		return Main.getInstance().getConfig().getString("Options.LicenseKey", getDefaultLicenseKey());
	}

	public String getDefaultLicenseKey() {
		return "LICENSE-KEY-HERE";
	}

	public int fit(int slots) {
		if (slots < 10) {
			return 1;
		} else if (slots < 19) {
			return 2;
		} else if (slots < 28) {
			return 3;
		} else if (slots < 37) {
			return 4;
		} else if (slots < 46) {
			return 5;
		} else {
			return 6;
		}
	}
	
	public void fixVersions(String crate) {
		Main MAIN_PLUGIN = Main.getInstance();
		if (!(MAIN_PLUGIN.getConfig().isSet("Options.BlockType"))) {
			MAIN_PLUGIN.getConfig().set("Options.BlockType", "CHEST");
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Options.ParticleEffect.onlywhenopening"))) {
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.onlywhenopening", false);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Options.Menu.name")) || !(MAIN_PLUGIN.getConfig().isSet("Options.Menu.size")) || !(MAIN_PLUGIN.getConfig().isSet("Options.Menu.spacer.enchanted")) || !(MAIN_PLUGIN.getConfig().isSet("Options.Menu.spacer.name")) || !(MAIN_PLUGIN.getConfig().isSet("Options.Menu.spacer.material"))) {
			MAIN_PLUGIN.getConfig().set("Options.Menu.spacer.material", "STAINED_GLASS_PANE:7");
			MAIN_PLUGIN.getConfig().set("Options.Menu.spacer.name", " ");
			MAIN_PLUGIN.getConfig().set("Options.Menu.size", 0);
			MAIN_PLUGIN.getConfig().set("Options.Menu.spacer.enchanted", false);
			MAIN_PLUGIN.getConfig().set("Options.Menu.name", "&8Select a Key.");
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Options.Menu.updateSpeed"))) {
			MAIN_PLUGIN.getConfig().set("Options.Menu.updateSpeed", 0);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Options.LicenseKey"))) {
			MAIN_PLUGIN.getConfig().set("Options.LicenseKey", getDefaultLicenseKey());
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Options.ParticleEffect.offsetX")) || !(MAIN_PLUGIN.getConfig().isSet("Options.ParticleEffect.offsetY")) || !(MAIN_PLUGIN.getConfig().isSet("Options.ParticleEffect.offsetZ")) || !(MAIN_PLUGIN.getConfig().isSet("Options.ParticleEffect.speed")) || !(MAIN_PLUGIN.getConfig().isSet("Options.ParticleEffect.amount"))) {
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.offsetX", 1.5F);
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.offsetY", 1.5F);
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.offsetZ", 1.5F);
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.speed", 1.0F);
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.amount", 30);
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.enabled", true);
			MAIN_PLUGIN.getConfig().set("Options.ParticleEffect.particle", "MOB_SPELL");
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isConfigurationSection("Options.Selection"))) {
			MAIN_PLUGIN.getConfig().set("Options.Selection.name", "&8Click &n%clicks%&8 slots to begin.");
			MAIN_PLUGIN.getConfig().set("Options.Selection.clicks", 1);
			MAIN_PLUGIN.getConfig().set("Options.Selection.selected-icon.material", "CHEST");
			MAIN_PLUGIN.getConfig().set("Options.Selection.selected-icon.name", "&bSlot selected!");
			MAIN_PLUGIN.getConfig().set("Options.Selection.selected-icon.lore", Arrays.asList("&fSelect other slots to begin opening.", ""));
			MAIN_PLUGIN.getConfig().set("Options.Selection.select-icon.material", "ENDER_CHEST");
			MAIN_PLUGIN.getConfig().set("Options.Selection.select-icon.name", "&dSlot %slot% &7(Click)");
			MAIN_PLUGIN.getConfig().set("Options.Selection.select-icon.lore", Arrays.asList("&fClick to select this slot.", ""));
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".Hide"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Hide", false);
			MAIN_PLUGIN.saveConfig();
		}
		if (MAIN_PLUGIN.getConfig().isConfigurationSection("Crates." + crate + ".ParticleEffect")) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".ParticleEffect", null);
			MAIN_PLUGIN.saveConfig();
		}
		if (MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".BlockType")) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".BlockType", null);
			MAIN_PLUGIN.saveConfig();
		}
		if (MAIN_PLUGIN.getConfig().isConfigurationSection("Crates." + crate + ".LeftClickInv")) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".LeftClickInv", null);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".PreviewInv.enabled")) || !(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".PreviewInv.name"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".PreviewInv.enabled", true);
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".PreviewInv.name", "&8Viewing contents for &n%crate%");
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isConfigurationSection("Crates." + crate + ".PrizeItem"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".PrizeItem.material", "LEVER");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".PrizeItem.name", "&b&nSelected Item");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".PrizeItem.enchanted", true);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isConfigurationSection("Crates." + crate + ".Sound"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.movingSound", "SUCCESSFUL_HIT");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.prizeSound", "BAT_TAKEOFF");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.closeInventorySound", "LEVEL_UP");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.volume", 1.0F);
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.pitch", 1.5F);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isConfigurationSection("Crates." + crate + ".Border"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Border.material", "STAINED_GLASS_PANE");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Border.enchanted", false);
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Border.name", " ");
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Border.amount", 1);
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Border.datas", Arrays.asList(1, 2, 3, 4, 5, 6));
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".Sound.volume")) || !(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".Sound.pitch"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.volume", 1.0F);
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Sound.pitch", 1.5F);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".PreCommands"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".PreCommands", Arrays.asList("msg:%prefix%Now opening &6&n%crate%&e crate."));
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".OpenInv.name"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".OpenInv.name", "&8Spinning &n%crate%");
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".OpenInv.type"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".OpenInv.type", "horizontal");
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".OpenInv.clicks"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".OpenInv.clicks", 1);
			MAIN_PLUGIN.saveConfig();
		}
		if (!(MAIN_PLUGIN.getConfig().isSet("Crates." + crate + ".Key.slot"))) {
			MAIN_PLUGIN.getConfig().set("Crates." + crate + ".Key.slot", 0);
			MAIN_PLUGIN.saveConfig();
		}
		
		for (String items : MAIN_PLUGIN.getConfig().getConfigurationSection("Crates." + crate + ".Items").getKeys(false)) {
			String itemKey = "Crates." + crate + ".Items." + items + ".";
			if (!(MAIN_PLUGIN.getConfig().isSet(itemKey + "enchanted"))) {
				MAIN_PLUGIN.getConfig().set(itemKey + "enchanted", false);
				MAIN_PLUGIN.saveConfig();
			}
			if (!(MAIN_PLUGIN.getConfig().isSet(itemKey + "amount"))) {
				MAIN_PLUGIN.getConfig().set(itemKey + "amount", 1);
				MAIN_PLUGIN.saveConfig();
			}
		}
	}

	private ItemStack blockTypeCache = null;

	public ItemStack getBlockType() {
		if (blockTypeCache == null) {
			blockTypeCache = this.getBlockTypeImpl();
		}
		return blockTypeCache;
	}

	public ItemStack getBlockTypeImpl() {
		ItemStack stack = null;
		
		try {
			stack = Main.getInstance().getEss().getItemDb().get(Main.getInstance().getConfig().getString("Options.BlockType"), 1);
		} catch (Exception ignore) {}

		if (stack == null || !(stack.getType().isBlock())) {
			return new ItemStack(Material.CHEST, 1);
		}

		stack.setAmount(1);

		return stack;
	}

	public void playSound(Player p, String crate, String sound) {
		String str = "Crates." + crate + ".Sound";
		float volume = (float) Main.getInstance().getConfig().getDouble(str + ".volume");
		float pitch = (float) Main.getInstance().getConfig().getDouble(str + ".pitch");
		p.playSound(p.getLocation(), Sounds.valueOf(Main.getInstance().getConfig().getString(str + "." + sound).toUpperCase()).bukkitSound(), volume, pitch);
	}

	public ItemStack getPrizeItem(String crate) {
		return ItemStackUtils.load(Main.getInstance().getConfig().getConfigurationSection("Crates." + crate + ".PrizeItem").getValues(true));
	}

	public List<String> getCrates() {
		return ImmutableList.copyOf(Main.getInstance().getConfig().getConfigurationSection("Crates").getKeys(false));
	}

	public String getOpenInvName(String crate) {
		return ChatColor.translateAlternateColorCodes('&', StringUtils.abbreviate(Main.getInstance().getConfig().getString("Crates." + crate + ".OpenInv.name").replaceAll("%(?i)crate%", crate), 32));
	}
	
	public String getMenuName() {
		return ChatColor.translateAlternateColorCodes('&', StringUtils.abbreviate(Main.getInstance().getConfig().getString("Options.Menu.name"), 32));
	}
	
	public String getPreviewInvName(String crate) {
		return ChatColor.translateAlternateColorCodes('&', StringUtils.abbreviate(Main.getInstance().getConfig().getString("Crates." + crate + ".PreviewInv.name").replaceAll("%(?i)crate%", crate), 32));
	}
	
	public boolean isPreviewEnabled(String crate) {
		return Main.getInstance().getConfig().getBoolean("Crates." + crate + ".PreviewInv.enabled");
	}
}
