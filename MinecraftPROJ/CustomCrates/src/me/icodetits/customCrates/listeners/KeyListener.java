package me.icodetits.customCrates.listeners;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.configutils.ConfigUtils;
import me.icodetits.customCrates.crateplayer.CPManager;
import me.icodetits.customCrates.crateplayer.CratePlayer;
import me.icodetits.customCrates.data.DropperData;
import me.icodetits.customCrates.data.DropperManager;
import me.icodetits.customCrates.data.ParkourData;
import me.icodetits.customCrates.data.ParkourManager;
import me.icodetits.customCrates.data.PlayerData;
import me.icodetits.customCrates.data.PlayerManager;
import me.icodetits.customCrates.events.CrateKeyOpenEvent;
import me.icodetits.customCrates.events.PreCrateKeyOpenEvent;
import me.icodetits.customCrates.smartinv.ClickableItem;
import me.icodetits.customCrates.smartinv.SmartInventory;
import me.icodetits.customCrates.smartinv.content.InventoryContents;
import me.icodetits.customCrates.smartinv.content.InventoryProvider;
import me.icodetits.customCrates.smartinv.content.Pagination;
import me.icodetits.customCrates.smartinv.content.SlotIterator;
import me.icodetits.customCrates.task.OpenCrate;
import me.icodetits.customCrates.task.Task;
import me.icodetits.customCrates.task.TaskType;
import me.icodetits.customCrates.task.tasks.CSGOTask;
import me.icodetits.customCrates.task.tasks.DropperTask;
import me.icodetits.customCrates.task.tasks.HorizontalTask;
import me.icodetits.customCrates.task.tasks.ParkourTask;
import me.icodetits.customCrates.task.tasks.RouletteTask;
import me.icodetits.customCrates.task.tasks.VerticalTask;
import me.icodetits.customCrates.task.tasks.WheelTask;
import me.icodetits.customCrates.utils.CleanItem;
import me.icodetits.customCrates.utils.LocationUtils;
import me.icodetits.customCrates.utils.Sounds;

public class KeyListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		if (e.hasBlock() && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Location bLoc = e.getClickedBlock().getLocation();
			if (bLoc.getBlock().getType() != ConfigUtils.getInstance().getBlockType().getType()) {
				return;
			}
			
			if (!(Main.getInstance().getCrateLocs().isString("Crates." + LocationUtils.locationToString(bLoc)))) {
				return;
			}

			e.setCancelled(true);

			openKeyMenu(p, bLoc);
		}
	}
	
	public static void openKeyMenu(Player p, Location bLoc) {
		if (CPManager.getCP(p) != null) {
			Message.sendMessage(p, "ALREADY-OPENING");
			return;
		}

		if (!(p.hasPermission("crates.bypasscreative")) && p.getGameMode() == GameMode.CREATIVE) {
			Message.sendMessage(p, "OPEN-WHEN-CREATIVE");
			return;
		}
		
		PlayerData data = PlayerManager.getInstance().getByPlayer(p);
		if (data == null) {
			return;
		}
		
		SmartInventory menu = SmartInventory.builder()
				.id(RandomStringUtils.randomAlphabetic(16))
				.size(ConfigUtils.getInstance().getMenuSize() * 9)
				.manager(Main.getInstance().getInvManager())
				.title(ConfigUtils.getInstance().getMenuName())
				.type(InventoryType.CHEST)
				.provider(new InventoryProvider() {
					
					@Override
					public void init(Player p, InventoryContents contents) {
						for (int index = 0; index < ConfigUtils.getInstance().getCrates().size(); index++) {
							final String crates = ConfigUtils.getInstance().getCrates().get(index);
							
							if (ConfigUtils.getInstance().hide(crates) || ConfigUtils.getInstance().isOldKey(crates)) {
								continue;
							}
							
							ClickableItem icon = ClickableItem.of(ConfigUtils.getInstance().getCrateKey(p, crates, data.getKey(crates)), e -> {
								if (!(e.getWhoClicked() instanceof Player)) {
									return;
								}
								
								PlayerData data = PlayerManager.getInstance().getByPlayer(p);
								if (data == null) {
									return;
								}
								
								if (e.isLeftClick()) {
									if (data.getKey(crates) < 1) {
										Message.sendMessage(p, Message.generate("OPEN-WITHOUT-KEY").replace("%crate%", crates));
										p.updateInventory();
										p.closeInventory();
										return;
									}

									p.closeInventory();

									data.removeKeys(crates, 1, rollback -> {
										TaskType type = ConfigUtils.getInstance().getType(crates);
										if (type == null) {
											type = TaskType.HORIZONTAL;
										}
										
										DropperData dropperData = null;
										if (type == TaskType.DROPPER) {
											dropperData = DropperManager.getInstance().getNextDropper();
											if (dropperData == null) {
												Message.sendMessage(p, Message.generate("NO-MAPS-AVAILABLE"));
												p.updateInventory();
												p.closeInventory();
												rollback.run();
												return;
											}
										}
										
										ParkourData parkourData = null;
										if (type == TaskType.PARKOUR) {
											parkourData = ParkourManager.getInstance().getNextMap();
											if (parkourData == null) {
												Message.sendMessage(p, Message.generate("NO-MAPS-AVAILABLE").replace("Dropper", "Parkour").replace("dropper", "parkour"));
												p.updateInventory();
												p.closeInventory();
												rollback.run();
												return;
											}
										}
										
										PreCrateKeyOpenEvent preEvent = new PreCrateKeyOpenEvent(p, crates, type);
										Bukkit.getPluginManager().callEvent(preEvent);
										if (preEvent.isCancelled()) {
											rollback.run();
											return;
										}
										
										ConfigUtils.getInstance().preCommands(p, crates);
										
										Inventory inv = Bukkit.createInventory(p, type.getInventorySize(), ConfigUtils.getInstance().getOpenInvName(crates));
	
										CratePlayer cp = CPManager.makeCP(p);
										cp.setCrateName(crates);
										cp.setCrateInv(inv);
										cp.setOpenCrate(new OpenCrate());
										cp.setPreviousLocation(p.getLocation());
										
										Task task = null;
	
										if (type == TaskType.VERTICAL) {
											task = new VerticalTask(p, bLoc);
										} else if (type == TaskType.HORIZONTAL) {
											task = new HorizontalTask(p, bLoc);
										} else if (type == TaskType.ROULETTE) {
											task = new RouletteTask(p, bLoc);
										} else if (type == TaskType.CSGO) {
											task = new CSGOTask(p, bLoc);
										} else if (type == TaskType.WHEEL) {
											task = new WheelTask(p, bLoc);
										} else if (type == TaskType.DROPPER) {
											cp.setDropperData(dropperData);
											
											task = new DropperTask(p, bLoc);
										} else if (type == TaskType.PARKOUR) {
											cp.setParkourData(parkourData);
											
											task = new ParkourTask(p, bLoc);
										} else {
											task = new HorizontalTask(p, bLoc);
										}
										
										CrateKeyOpenEvent crateEvent = new CrateKeyOpenEvent(p, crates, type, task);
										Bukkit.getPluginManager().callEvent(crateEvent);
										if (crateEvent.isCancelled()) {
											rollback.run();
											return;
										}

										SmartInventory openedGUI = Main.getInstance().getInvManager().getInventory(p).orElse(null);
										if (openedGUI != null) {
											openedGUI.close(p);
										}
										
										crateEvent.getTask().start();
									});
								} else if (e.isRightClick()) {
									if (!(ConfigUtils.getInstance().isPreviewEnabled(crates))) {
										return;
									}
									
									if (CPManager.hasCP(p)) {
										p.updateInventory();
										p.closeInventory();
										return;
									}
									
									SmartInventory rewards = SmartInventory.builder()
											.parent(contents.inventory())
											.id(RandomStringUtils.randomAlphabetic(16))
											.size(Math.min(6, (ConfigUtils.getInstance().fit(ConfigUtils.getInstance().getCrateItems(crates).size()) + 1)) * 9)
											.manager(Main.getInstance().getInvManager())
											.title(ConfigUtils.getInstance().getPreviewInvName(crates))
											.type(InventoryType.CHEST)
											.provider(new InventoryProvider() {

												@Override
												public void init(Player player, InventoryContents contents) {
													Pagination pagination = contents.pagination();
													
													List<ItemStack> rewards = ConfigUtils.getInstance().getCrateItems(crates);
												    ClickableItem[] items = new ClickableItem[rewards.size()];
							
												    for(int i = 0; i < items.length; i++)
												        items[i] = ClickableItem.empty(rewards.get(i));
							
												    pagination.setItems(items);
												    pagination.setItemsPerPage(45);
							
												    pagination.addToIterator(contents.newIterator(SlotIterator.Type.ORDER, 0));
												    
												    contents.set(contents.inventory().getSize() - 5, ClickableItem.of(new CleanItem(Material.IRON_DOOR).withName("&e&l<- BACK TO MAIN MENU").withLore("&7&oGoes back to the main menu...").toItemStack(), e -> {												    	
												    	if (contents.inventory().getParent().isPresent()) {
												    		contents.inventory().getParent().orElse(null).open(player);
												    		player.playSound(player.getLocation(), Sounds.CHEST_CLOSE.bukkitSound(), 1.5F, 0.65F);
												    	}
												    }));
							
													if (pagination.getPage() == 0 && pagination.isLast()) {
														return;
													}
													
												    contents.set(contents.inventory().getSize() - 4, ClickableItem.of(new CleanItem(Material.ARROW).withName("&a&lNEXT PAGE ->").withLore("&7&oGoes to the next page of rewards...").toItemStack(), e -> {
												    	if (pagination.isLast()) {
												    		return;
												    	}
												    	
												    	contents.inventory().open(player, pagination.next().getPage());
												    	player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.5F, 1.5F);
												    }));
												    
												    contents.set(contents.inventory().getSize() - 6, ClickableItem.of(new CleanItem(Material.ARROW).withName("&c&l<- PREVIOUS PAGE").withLore("&7&oGoes back to the previous page of rewards...").toItemStack(), e -> {
												    	if (pagination.isFirst()) {
												    		return;
												    	}
												    	
												    	contents.inventory().open(player, pagination.previous().getPage());
												    	player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.5F, 0.75F);
												    }));
												}

												@Override
												public void update(Player player, InventoryContents contents) {}
											})
											.closeable(false)
											.build();
									
									SmartInventory openedGUI = Main.getInstance().getInvManager().getInventory(p).orElse(null);
									if (openedGUI != null) {
										openedGUI.close(p);
									}
									
									rewards.open(p);
								}
							});

							contents.set((ConfigUtils.getInstance().getSlot(crates) <= 0 ? index : ConfigUtils.getInstance().getSlot(crates)), icon);
						}
						
						if  (!(ConfigUtils.getInstance().supportOldKeys())) {
							contents.set(contents.inventory().getSize() - 1, ClickableItem.of(new CleanItem(Material.ACACIA_DOOR_ITEM).withName("&c&lOLD KEYS &7(Click here)").withLore("&7** &fClick here&7 to open the old key selection menu. **").toItemStack(), e -> {
								p.closeInventory();
								p.playSound(p.getLocation(), Sounds.CHEST_OPEN.bukkitSound(), 1.0F, 1.5F);
								
								openOldKeyMenu(p, bLoc, contents.inventory());
							}));
						}

						for (int free = 0; free < contents.inventory().getSize(); free++) {
							if (!(contents.get(free).isPresent())) {
								contents.set(free, ClickableItem.empty(ConfigUtils.getInstance().getSpacer()));
							}
						}
					}
					
					@Override
					public void update(Player p, InventoryContents contents) {
						if (ConfigUtils.getInstance().getMenuUpdateSpeed() <= 0) {
							return;
						}

						int state = contents.property("state", 0);
						contents.setProperty("state", state + 1);

						if (state % ConfigUtils.getInstance().getMenuUpdateSpeed() != 0) {
							return;
						}
						
						for (int index = 0; index < ConfigUtils.getInstance().getCrates().size(); index++) {
							final String crates = ConfigUtils.getInstance().getCrates().get(index);
							
							if (ConfigUtils.getInstance().hide(crates) || ConfigUtils.getInstance().isOldKey(crates)) {
								continue;
							}
							
							int slot = (ConfigUtils.getInstance().getSlot(crates) <= 0 ? index : ConfigUtils.getInstance().getSlot(crates));
							
							ClickableItem item = contents.get(slot).orElse(null);
							if (item != null) {
								contents.set(slot, ClickableItem.of(ConfigUtils.getInstance().getCrateKey(p, crates, data.getKey(crates)), item.getConsumer()));
							}
						}
					}
				})
				.closeable(true)
				.build();
		
		menu.open(p);
	}

	public static void openOldKeyMenu(Player p, Location bLoc, SmartInventory parent) {
		/*if (CPManager.getCratePlayers().size() + 1 > 5) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r &r"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r            &c&lCRATES ARE CURRENTLY OVERLOADED"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r    &7You must wait until less players are trying to open crates!"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r &r"));
			return;
		}*/
		
		if (CPManager.getCP(p) != null) {
			Message.sendMessage(p, "ALREADY-OPENING");
			return;
		}

		if (!(p.hasPermission("crates.bypasscreative")) && p.getGameMode() == GameMode.CREATIVE) {
			Message.sendMessage(p, "OPEN-WHEN-CREATIVE");
			return;
		}
		
		PlayerData data = PlayerManager.getInstance().getByPlayer(p);
		if (data == null) {
			return;
		}
		
		SmartInventory menu = SmartInventory.builder()
				.id(RandomStringUtils.randomAlphabetic(16))
				.parent(parent)
				.size(ConfigUtils.getInstance().getMenuSize() * 9)
				.manager(Main.getInstance().getInvManager())
				.title(ConfigUtils.getInstance().getMenuName())
				.type(InventoryType.CHEST)
				.provider(new InventoryProvider() {
					
					@Override
					public void init(Player p, InventoryContents contents) {
						for (int index = 0; index < ConfigUtils.getInstance().getCrates().size(); index++) {
							final String crates = ConfigUtils.getInstance().getCrates().get(index);
							
							if (!(ConfigUtils.getInstance().isOldKey(crates)) || ConfigUtils.getInstance().hide(crates)) {
								continue;
							}
							
							ClickableItem icon = ClickableItem.of(ConfigUtils.getInstance().getCrateKey(p, crates, data.getKey(crates)), e -> {
								
								PlayerData data = PlayerManager.getInstance().getByPlayer(p);
								if (data == null) {
									return;
								}
								
								if (e.isLeftClick()) {
									if (data.getKey(crates) < 1) {
										Message.sendMessage(p, Message.generate("OPEN-WITHOUT-KEY").replace("%crate%", crates));
										p.updateInventory();
										p.closeInventory();
										return;
									}

									p.closeInventory();

									data.removeKeys(crates, 1, rollback -> {
										TaskType type = ConfigUtils.getInstance().getType(crates);
										if (type == null) {
											type = TaskType.HORIZONTAL;
										}
										
										DropperData dropperData = null;
										if (type == TaskType.DROPPER) {
											dropperData = DropperManager.getInstance().getNextDropper();
											if (dropperData == null) {
												Message.sendMessage(p, Message.generate("NO-MAPS-AVAILABLE"));
												p.updateInventory();
												p.closeInventory();
												rollback.run();
												return;
											}
										}
										
										ParkourData parkourData = null;
										if (type == TaskType.PARKOUR) {
											parkourData = ParkourManager.getInstance().getNextMap();
											if (parkourData == null) {
												Message.sendMessage(p, Message.generate("NO-MAPS-AVAILABLE").replace("Dropper", "Parkour Map").replace("dropper", "parkour map"));
												p.updateInventory();
												p.closeInventory();
												rollback.run();
												return;
											}
										}
										
										PreCrateKeyOpenEvent preEvent = new PreCrateKeyOpenEvent(p, crates, type);
										Bukkit.getPluginManager().callEvent(preEvent);
										if (preEvent.isCancelled()) {
											rollback.run();
											return;
										}
										
										ConfigUtils.getInstance().preCommands(p, crates);
										
										Inventory inv = Bukkit.createInventory(p, type.getInventorySize(), ConfigUtils.getInstance().getOpenInvName(crates));
	
										CratePlayer cp = CPManager.makeCP(p);
										cp.setCrateName(crates);
										cp.setCrateInv(inv);
										cp.setOpenCrate(new OpenCrate());
										cp.setPreviousLocation(p.getLocation());
										
										Task task = null;
	
										if (type == TaskType.VERTICAL) {
											task = new VerticalTask(p, bLoc);
										} else if (type == TaskType.HORIZONTAL) {
											task = new HorizontalTask(p, bLoc);
										} else if (type == TaskType.ROULETTE) {
											task = new RouletteTask(p, bLoc);
										} else if (type == TaskType.CSGO) {
											task = new CSGOTask(p, bLoc);
										} else if (type == TaskType.WHEEL) {
											task = new WheelTask(p, bLoc);
										} else if (type == TaskType.DROPPER) {
											cp.setDropperData(dropperData);
											
											task = new DropperTask(p, bLoc);
										} else if (type == TaskType.PARKOUR) {
											cp.setParkourData(parkourData);
											
											task = new ParkourTask(p, bLoc);
										} else {
											task = new HorizontalTask(p, bLoc);
										}
										
										CrateKeyOpenEvent crateEvent = new CrateKeyOpenEvent(p, crates, type, task);
										Bukkit.getPluginManager().callEvent(crateEvent);
										if (crateEvent.isCancelled()) {
											rollback.run();
											return;
										}

										SmartInventory openedGUI = Main.getInstance().getInvManager().getInventory(p).orElse(null);
										if (openedGUI != null) {
											openedGUI.close(p);
										}
										
										crateEvent.getTask().start();
									});
								} else if (e.isRightClick()) {
									if (!(ConfigUtils.getInstance().isPreviewEnabled(crates))) {
										return;
									}
									
									if (CPManager.hasCP(p)) {
										p.updateInventory();
										p.closeInventory();
										return;
									}
									
									SmartInventory rewards = SmartInventory.builder()
											.parent(contents.inventory())
											.id(RandomStringUtils.randomAlphabetic(16))
											.size(Math.min(6, (ConfigUtils.getInstance().fit(ConfigUtils.getInstance().getCrateItems(crates).size()) + 1)) * 9)
											.manager(Main.getInstance().getInvManager())
											.title(ConfigUtils.getInstance().getPreviewInvName(crates))
											.type(InventoryType.CHEST)
											.provider(new InventoryProvider() {

												@Override
												public void init(Player player, InventoryContents contents) {
													Pagination pagination = contents.pagination();
													
													List<ItemStack> rewards = ConfigUtils.getInstance().getCrateItems(crates);
												    ClickableItem[] items = new ClickableItem[rewards.size()];
							
												    for(int i = 0; i < items.length; i++)
												        items[i] = ClickableItem.empty(rewards.get(i));
							
												    pagination.setItems(items);
												    pagination.setItemsPerPage(45);
							
												    pagination.addToIterator(contents.newIterator(SlotIterator.Type.ORDER, 0));
												    
												    contents.set(contents.inventory().getSize() - 5, ClickableItem.of(new CleanItem(Material.IRON_DOOR).withName("&e&l<- BACK TO MAIN MENU").withLore("&7&oGoes back to the main menu...").toItemStack(), e -> {												    	
												    	if (contents.inventory().getParent().isPresent()) {
												    		contents.inventory().getParent().orElse(null).open(player);
												    		player.playSound(player.getLocation(), Sounds.CHEST_CLOSE.bukkitSound(), 1.5F, 0.65F);
												    	}
												    }));
												    
													if (pagination.getPage() == 0 && pagination.isLast()) {
														return;
													}
							
												    contents.set(contents.inventory().getSize() - 4, ClickableItem.of(new CleanItem(Material.ARROW).withName("&a&lNEXT PAGE ->").withLore("&7&oGoes to the next page of rewards...").toItemStack(), e -> {
												    	if (pagination.isLast()) {
												    		return;
												    	}
												    	
												    	contents.inventory().open(player, pagination.next().getPage());
												    	player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.5F, 1.5F);
												    }));
												    
												    contents.set(contents.inventory().getSize() - 6, ClickableItem.of(new CleanItem(Material.ARROW).withName("&c&l<- PREVIOUS PAGE").withLore("&7&oGoes back to the previous page of rewards...").toItemStack(), e -> {
												    	if (pagination.isFirst()) {
												    		return;
												    	}
												    	
												    	contents.inventory().open(player, pagination.previous().getPage());
												    	player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.5F, 0.75F);
												    }));
												}

												@Override
												public void update(Player player, InventoryContents contents) {}
											})
											.closeable(false)
											.build();
									
									SmartInventory openedGUI = Main.getInstance().getInvManager().getInventory(p).orElse(null);
									if (openedGUI != null) {
										openedGUI.close(p);
									}
									
									rewards.open(p);
								}
							});

							contents.set((ConfigUtils.getInstance().getSlot(crates) <= 0 ? index : ConfigUtils.getInstance().getSlot(crates)), icon);
						}
						
						contents.set(contents.inventory().getSize() - 1, ClickableItem.of(new CleanItem(Material.IRON_DOOR).withName("&a&lMAIN MENU &7(Click here)").withLore("&7** &fClick here&7 to go back to main menu. **").toItemStack(), e -> {
					    	if (contents.inventory().getParent().isPresent()) {
					    		contents.inventory().getParent().orElse(null).open(p);
					    		p.playSound(p.getLocation(), Sounds.CHEST_CLOSE.bukkitSound(), 1.5F, 0.65F);
					    	}
						}));

						for (int free = 0; free < contents.inventory().getSize(); free++) {
							if (!(contents.get(free).isPresent())) {
								contents.set(free, ClickableItem.empty(ConfigUtils.getInstance().getSpacer()));
							}
						}
					}
					
					@Override
					public void update(Player player, InventoryContents contents) {
						if (ConfigUtils.getInstance().getMenuUpdateSpeed() <= 0) {
							return;
						}

						int state = contents.property("state", 0);
						contents.setProperty("state", state + 1);

						if (state % ConfigUtils.getInstance().getMenuUpdateSpeed() != 0) {
							return;
						}
						
						for (int index = 0; index < ConfigUtils.getInstance().getCrates().size(); index++) {
							final String crates = ConfigUtils.getInstance().getCrates().get(index);
							
							if (!(ConfigUtils.getInstance().isOldKey(crates)) || ConfigUtils.getInstance().hide(crates)) {
								continue;
							}
							
							int slot = (ConfigUtils.getInstance().getSlot(crates) <= 0 ? index : ConfigUtils.getInstance().getSlot(crates));
							
							ClickableItem item = contents.get(slot).orElse(null);
							if (item != null) {
								contents.set(slot, ClickableItem.of(ConfigUtils.getInstance().getCrateKey(p, crates, data.getKey(crates)), item.getConsumer()));
							}
						}
					}
				})
				.closeable(true)
				.build();
		
		menu.open(p);
	}

	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		if (CPManager.hasCP(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (!(CPManager.hasCP(p))) {
			return;
		}
		CratePlayer cp = CPManager.getCP(p);
		if (cp.getCrateInv() != null && e.getInventory().getName().equalsIgnoreCase(cp.getCrateInv().getName())) {
			e.setCancelled(true);
			p.updateInventory();
		}
	}
	
	@EventHandler
	public void onCratePlaceEvent(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		
		if (!(p.hasPermission("crates.place"))) {
			return;
		}
		
		ItemStack beforePlace = e.getItemInHand();
		if (beforePlace == null || beforePlace.getType() != ConfigUtils.getInstance().getBlockType().getType() || !(beforePlace.hasItemMeta())) {
			return;
		}
		
		if (!(beforePlace.getItemMeta().hasDisplayName())) {
			return;
		}

        if (beforePlace.getItemMeta().getDisplayName().equalsIgnoreCase("§bCrate §7(Place Down to Register)")) {
			Message.sendMessage(p, Message.generate("SAVED-TO-CONFIG"));
			Main.getInstance().getCrateLocs().set("Crates." + LocationUtils.locationToString(e.getBlock().getLocation()), UUID.randomUUID().toString());
			Main.getInstance().saveCrateLocs();
			
			Bukkit.getScheduler().cancelTasks(Main.getInstance());
			ConfigUtils.getInstance().setupParticles();
		}
	}
	
	@EventHandler
	public void onCratePlaceEvent(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (!(p.hasPermission("crates.break"))) {
			return;
		}
		
		if (!(Main.getInstance().getCrateLocs().isString("Crates." + LocationUtils.locationToString(e.getBlock().getLocation())))) {
			return;
		}

		if (e.getBlock().getType() == ConfigUtils.getInstance().getBlockType().getType()) {
			if (!(p.isSneaking())) {
				e.setCancelled(true);
				Message.sendMessage(p, "SNEAK-TO-DESTROY");
				return;
			}
			
			Message.sendMessage(p, "DELETED-FROM-CONFIG");
			Main.getInstance().getCrateLocs().set("Crates." + LocationUtils.locationToString(e.getBlock().getLocation()), null);
			Main.getInstance().saveCrateLocs();

			Bukkit.getScheduler().cancelTasks(Main.getInstance());
			ConfigUtils.getInstance().setupParticles();
			CPManager.clearCratePlayers();
		}
	}
}
