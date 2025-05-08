package me.icodetits.customCrates;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

import me.icodetits.customCrates.commands.CratesCommand;
import me.icodetits.customCrates.commands.TabComplete;
import me.icodetits.customCrates.commands.manager.CrateCommandManager;
import me.icodetits.customCrates.configutils.ConfigUtils;
import me.icodetits.customCrates.crateplayer.CPManager;
import me.icodetits.customCrates.data.DropperManager;
import me.icodetits.customCrates.data.ParkourManager;
import me.icodetits.customCrates.data.PlayerManager;
import me.icodetits.customCrates.listeners.KeyListener;
import me.icodetits.customCrates.listeners.MultiversePatchListener;
import me.icodetits.customCrates.menus.Menu;
import me.icodetits.customCrates.menus.MenuAPI;
import me.icodetits.customCrates.smartinv.InventoryManager;
import me.icodetits.customCrates.utils.EnchantGlow;

public class Main extends JavaPlugin {
	
//	private static final String BACKEND_URL = "http://icodetits.xyz/plugin-backend/";
	
	private static Main finalInstance;

	private Essentials ess = null;
	private boolean supportPets = false;

	private File crateLocations;
	private FileConfiguration crateLocationsConfig;
	
	private File dropperSaves;
	private FileConfiguration dropperSavesConfig;
	
	private File parkourSaves;
	private FileConfiguration parkourSavesConfig;
	
	private File messages;
	private FileConfiguration messagesConfig;
	
	private InventoryManager invManager;
	
	private int messageVersion = 4;
	
	@Override
	public void onLoad() {
		EnchantGlow.getGlow();
	}

	@Override
	public void onEnable() {
		setup();

//		if (!(new Licenses(ConfigUtils.getInstance().getLicenseKey(), BACKEND_URL, this).register())) {
//			return;
//		}

		this.ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		this.supportPets = getServer().getPluginManager().isPluginEnabled("PiratePets");
		
		this.invManager = new InventoryManager(this);
		this.invManager.init();
		
		PlayerManager.getInstance().register();
		getServer().getPluginManager().registerEvents(PlayerManager.getInstance(), this);

		ConfigUtils.getInstance().setupParticles();
		
		getServer().getPluginManager().registerEvents(MenuAPI.getInstance(), this);
		getServer().getPluginManager().registerEvents(new KeyListener(), this);
		getServer().getPluginManager().registerEvents(new CPManager(), this);
		
		if (getServer().getPluginManager().isPluginEnabled("Multiverse-Core") && getServer().getPluginManager().isPluginEnabled("Multiverse-NetherPortals")) {
			getServer().getPluginManager().registerEvents(new MultiversePatchListener(), this);
		}
		
		getCommand("crate").setExecutor(new CrateCommandManager());
		getCommand("crate").setTabCompleter(new TabComplete());
		getCommand("crates").setExecutor(new CratesCommand());
		
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				DropperManager.getInstance().register();
				ParkourManager.getInstance().register();
			}
		}, 20L);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		CPManager.clearCratePlayers();
		
		PlayerManager.getInstance().unregister();
		
		DropperManager.getInstance().unregister();
		ParkourManager.getInstance().unregister();

		this.ess = null;
		this.crateLocations = null;
		this.crateLocationsConfig = null;
		this.dropperSaves = null;
		this.dropperSavesConfig = null;
		this.parkourSaves = null;
		this.parkourSavesConfig = null;
		this.messages = null;
		this.invManager = null;
		this.messagesConfig = null;
		
		for (Player online : getServer().getOnlinePlayers()) {
			if (CPManager.getCP(online) != null || (online.getOpenInventory() != null && online.getOpenInventory().getTopInventory().getHolder() instanceof Menu)) {
				online.closeInventory();
			}
		}
		
		finalInstance = null;
	}

	private void setup() {
		try {
			saveDefaultConfig();
			this.crateLocations = new File(getDataFolder(), "crateLocations.yml");
			this.dropperSaves = new File(getDataFolder(), "dropperSaves.yml");
			this.parkourSaves = new File(getDataFolder(), "parkourSaves.yml");
			this.messages = new File(getDataFolder(), "messages.yml");
			
			File oldCrateLocations = new File(getDataFolder(), "CrateLocations.yml");
			File oldMessages = new File(getDataFolder(), "Messages.yml");
			
			if(oldMessages.exists()){
				oldMessages.renameTo(this.messages);
				this.messagesConfig = YamlConfiguration.loadConfiguration(this.messages);
			}
			
			if(oldCrateLocations.exists()){
				oldCrateLocations.renameTo(this.crateLocations);
				this.crateLocationsConfig = YamlConfiguration.loadConfiguration(this.crateLocations);
			}

			if (!this.messages.exists()) {
				this.messages.createNewFile();
				saveResource("messages.yml", true);
				this.messagesConfig = YamlConfiguration.loadConfiguration(this.messages);
			}
			if (!this.crateLocations.exists()) {
				this.crateLocations.createNewFile();
				this.crateLocationsConfig = YamlConfiguration.loadConfiguration(this.crateLocations);
			}
			if (!this.dropperSaves.exists()) {
				this.dropperSaves.createNewFile();
				this.dropperSavesConfig = YamlConfiguration.loadConfiguration(this.dropperSaves);
			}
			if (!this.parkourSaves.exists()) {
				this.parkourSaves.createNewFile();
				this.parkourSavesConfig = YamlConfiguration.loadConfiguration(this.parkourSaves);
			}

			this.crateLocationsConfig = YamlConfiguration.loadConfiguration(this.crateLocations);
			
			this.dropperSavesConfig = YamlConfiguration.loadConfiguration(this.dropperSaves);
			
			this.parkourSavesConfig = YamlConfiguration.loadConfiguration(this.parkourSaves);
			
			this.messagesConfig = YamlConfiguration.loadConfiguration(this.messages);
			
			if (!(this.messagesConfig.isSet("MESSAGES-VERSION")) || this.messagesConfig.getInt("MESSAGES-VERSION") < this.messageVersion) {
				String savedPrefix = getMessages().getString("Prefix");

				saveResource("messages.yml", true);
				
				this.messagesConfig = YamlConfiguration.loadConfiguration(this.messages);
				
				getMessages().set("Prefix", savedPrefix);
				getMessages().options().header("DO NOT EDIT MESSAGES-VERSION!");
				getMessages().set("MESSAGES-VERSION", this.messageVersion);
				saveMessages();
				
				System.out.println("Updating messages.yml");
			}
			
			for (String crates : ConfigUtils.getInstance().getCrates()) {
				ConfigUtils.getInstance().fixVersions(crates);
			}
		} catch (Exception ignore) {}

	}

	public static Main getInstance() {
		if (finalInstance == null) {
			synchronized (Main.class) {
				if (finalInstance == null) {
					finalInstance = getPlugin(Main.class);
				}
			}
		}
		
		return finalInstance;
	}

	public FileConfiguration getCrateLocs() {
		return this.crateLocationsConfig;
	}

	public void saveCrateLocs() {
		try {
			this.crateLocationsConfig.save(this.crateLocations);
		} catch (Exception e) {}
	}
	
	public FileConfiguration getDropperSaves() {
		return this.dropperSavesConfig;
	}

	public void saveDropperSaves() {
		try {
			this.dropperSavesConfig.save(this.dropperSaves);
		} catch (Exception e) {}
	}
	
	public FileConfiguration getParkourSaves() {
		return this.parkourSavesConfig;
	}

	public void saveParkourSaves() {
		try {
			this.parkourSavesConfig.save(this.parkourSaves);
		} catch (Exception e) {}
	}

	public FileConfiguration getMessages() {
		return this.messagesConfig;
	}
	
	public InventoryManager getInvManager() {
		return this.invManager;
	}

	public void saveMessages() {
		try {
			this.messagesConfig.save(this.messages);
		} catch (Exception e) {}
	}

	public void reloadMessages() {
		try {
			this.messagesConfig.load(this.messages);
			this.messagesConfig.save(this.messages);
		} catch (Exception ignore) {}
	}

	public String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', this.messagesConfig.getString("Prefix")) + " ";
	}

	public Essentials getEss() {
		return this.ess;
	}
	
	public boolean externalPetHook() {
		return this.supportPets;
	}
	
}
