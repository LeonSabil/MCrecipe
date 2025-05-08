package me.icodetits.customCrates.task.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.configutils.ConfigUtils;
import me.icodetits.customCrates.crateplayer.CPManager;
import me.icodetits.customCrates.crateplayer.CratePlayer;
import me.icodetits.customCrates.menus.InventoryClickType;
import me.icodetits.customCrates.menus.Menu;
import me.icodetits.customCrates.menus.MenuAPI;
import me.icodetits.customCrates.menus.MenuAPI.MenuCloseBehaviour;
import me.icodetits.customCrates.menus.MenuItem;
import me.icodetits.customCrates.task.OpenCrate;
import me.icodetits.customCrates.task.Task;
import me.icodetits.customCrates.utils.PetUtil;
import me.icodetits.customCrates.utils.RandomCollection;
import me.icodetits.customCrates.utils.RandomCollection.Item;

public class CSGOTask extends BukkitRunnable implements Task {
	
	private int step, clicks;
	private long startTime;
	private Location loc;
	private WeakReference<Player> player;
	private CratePlayer cp;
	private RandomCollection<ItemStack> itemsList;
	private List<Integer> selectedSlots;
	
	private boolean cratePetEdited = false;

	public CSGOTask(Player p, Location loc) {
		this.step = 0;
		this.startTime = 0;
		this.player = new WeakReference<Player>(p);
		this.cp = CPManager.getCP(p);
		this.loc = loc.add(new Vector(0.5, 0.5, 0.5));
		this.clicks = ConfigUtils.getInstance().getSelectionClicks(this.cp.getCrateName(), 9);
		this.selectedSlots = Lists.newArrayList();
	}
	
	@Override
	public void scroll(Inventory i, int end, int start, ItemStack item) {
		for (int n = end; n < start; n++) {
			i.setItem(n, i.getItem(n + 1));
			i.setItem(n + 9, i.getItem((n + 1) + 9));
			i.setItem(n - 9, i.getItem((n + 1) - 9));
		}

		i.setItem(start, item);
		
		ItemStack stack = ConfigUtils.getInstance().getBorder(this.cp.getCrateName(), this.itemsList.getWeight(item));
		i.setItem(start + 9, stack);
		i.setItem(start - 9, stack);
	}
	
	@Override
	public void start() {
		Player p = this.player.get();
		if (p == null) {
			return;
		}
		
		final Menu menu = MenuAPI.getInstance().createMenu(ConfigUtils.getInstance().getSelectionName(this.cp.getCrateName(), 9), 1);
		menu.setMenuCloseBehaviour(new MenuCloseBehaviour() {
			
			@Override
			public void onClose(final Player p, final Menu menu, boolean bypass) {
				if (!(bypass)) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

						@Override
						public void run() {
							if (!(menu.bypassMenuCloseBehaviour())) {
								menu.openMenu(p);
							}
						}
					}, 5L);
				}
			}
		});
		
		for (int i = 0; i < menu.getInventory().getSize(); i++) {
			final int slot = (i + 1);
			
			menu.addMenuItem(new MenuItem() {
				
				@Override
				public void onClick(Player p, InventoryClickType click) {
					if (CSGOTask.this.selectedSlots.contains(Integer.valueOf(slot + 8))) {
						return;
					}
					
					if ((CSGOTask.this.selectedSlots.size() + 1) > CSGOTask.this.clicks) {
						return;
					}

					CSGOTask.this.selectedSlots.add(Integer.valueOf(slot + 8));
					menu.updateMenu(false);
					
					if (CSGOTask.this.selectedSlots.size() >= CSGOTask.this.clicks) {
						Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
							
							@Override
							public void run() {
								Player p = CSGOTask.this.player.get();
								if (p != null && Bukkit.getPlayer(p.getName()) != null) {
									menu.setBypassMenuCloseBehaviour(true);
									p.closeInventory();
									startTask();
								}
							}
						}, 10L);
					}
				}
				
				@Override
				public ItemStack getItemStack() {
					if (CSGOTask.this.selectedSlots.contains(Integer.valueOf(slot + 8))) {
						return ConfigUtils.getInstance().getSelectedIcon(slot);
					} else {
						return ConfigUtils.getInstance().getSelectIcon(slot);
					}
				}
			}, i);
		}
		
		menu.openMenu(p);
	}
	
	private void startTask() {
		this.startTime = System.currentTimeMillis();
		if (!(this.cratePetEdited)) {
			this.itemsList = ConfigUtils.getInstance().getCrateRands(this.cp.getCrateName());
		}

		Player p = this.player.get();

		if (this.itemsList == null) {
			if (p.isOp()) {
				Message.sendMessage(p, "ADMIN-ERROR-WHILE-OPENING");
			} else {
				Message.sendMessage(p, "ERROR-WHILE-OPENING");
			}
			
			CPManager.getCP(p).destroy(p);
			p.closeInventory();
			clean();
			return;
		}
		
		if (Main.getInstance().externalPetHook()) {
			Object petData = PetUtil.petMatchCrate(p);
			if (petData != null && !this.cratePetEdited) {
				int petLevel = PetUtil.petLevel(petData);
				double addChance = getIncreaseRange(petLevel);
				int maxIncrease = 1, maxDecrease = Math.max(1, getMaxDecreases(petLevel));
				List<Item<ItemStack>> decreaseChances = Lists.newArrayList(), increaseChances = Lists.newArrayList();
				
				final Menu menu = MenuAPI.getInstance().createMenu(ChatColor.translateAlternateColorCodes('&', "&8&lSELECT &n" + maxDecrease + "&8&l TO REMOVE"), ConfigUtils.getInstance().fit(this.itemsList.getWeightMap().size()));
				menu.setMenuCloseBehaviour(new MenuCloseBehaviour() {
					
					@Override
					public void onClose(final Player p, final Menu menu, boolean bypass) {
						if (!(bypass)) {
							Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
	
								@Override
								public void run() {
									if (!(menu.bypassMenuCloseBehaviour())) {
										menu.openMenu(p);
									}
								}
							}, 5L);
						}
					}
				});
				
				for (Item<ItemStack> itemKey : this.itemsList.getWeightMap()) {
					final Item<ItemStack> finalItemKey = itemKey;
					final ItemStack item = finalItemKey.getType();
					
					if (menu.getInventory().firstEmpty() == -1) {
						continue;
					}
	
					menu.addMenuItem(new MenuItem() {
						
						@Override
						public void onClick(Player p, InventoryClickType click) {
							if (decreaseChances.contains(finalItemKey)) {
								return;
							}
	
							if ((decreaseChances.size() + 1) > maxDecrease) {
								return;
							}
	
							decreaseChances.add(finalItemKey);
							CSGOTask.this.itemsList.remove(item);
							menu.updateMenu(false);
							
							if (decreaseChances.size() >= maxDecrease) {
								menu.setBypassMenuCloseBehaviour(true);
								p.closeInventory();
								
								decreaseChances.clear();
								
								final Menu menu = MenuAPI.getInstance().createMenu(ChatColor.translateAlternateColorCodes('&', "&8&lSELECT &n" + maxIncrease + "&8&l TO INCREASE %"), ConfigUtils.getInstance().fit(CSGOTask.this.itemsList.getWeightMap().size()));
								menu.setMenuCloseBehaviour(new MenuCloseBehaviour() {
									
									@Override
									public void onClose(final Player p, final Menu menu, boolean bypass) {
										if (!(bypass)) {
											Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
	
												@Override
												public void run() {
													if (!(menu.bypassMenuCloseBehaviour())) {
														menu.openMenu(p);
													}
												}
											}, 5L);
										}
									}
								});
								
								for (Item<ItemStack> itemKey : CSGOTask.this.itemsList.getWeightMap()) {
									final Item<ItemStack> finalItemKey = itemKey;
									final ItemStack item = finalItemKey.getType();
									
									if (menu.getInventory().firstEmpty() == -1) {
										continue;
									}
	
									menu.addMenuItem(new MenuItem() {
										
										@Override
										public void onClick(Player p, InventoryClickType click) {
											if (increaseChances.contains(finalItemKey)) {
												return;
											}
	
											if ((increaseChances.size() + 1) > maxIncrease) {
												return;
											}
	
											increaseChances.add(finalItemKey);
											finalItemKey.setChance(finalItemKey.getChance() * addChance);
											menu.updateMenu(false);
											
											if (increaseChances.size() >= maxIncrease) {
												Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
													
													@Override
													public void run() {
														Player p = CSGOTask.this.player.get();
														if (p != null && Bukkit.getPlayer(p.getName()) != null) {
															menu.setBypassMenuCloseBehaviour(true);
															p.closeInventory();
															
															increaseChances.clear();
															
															CSGOTask.this.cratePetEdited = true;
															startTask();
														}
													}
												}, 10L);
											}
										}
										
										@Override
										public ItemStack getItemStack() {
											if (increaseChances.contains(finalItemKey)) {
												ItemStack stack = item.clone();
												ItemMeta meta = stack.getItemMeta();
												meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&l&nSELECTED"));
												stack.setItemMeta(meta);
												
												return stack;
											} else {
												ItemStack stack = item.clone();
												ItemMeta meta = stack.getItemMeta();
												List<String> lore = (meta.hasLore() ? meta.getLore() : Lists.newArrayList());
												lore.add(" ");
												lore.add(ChatColor.translateAlternateColorCodes('&', "&7** Once &fincreased&7 item will be easier to &fwin&7. **"));
												meta.setLore(lore);
												stack.setItemMeta(meta);
												
												return stack;
											}
										}
									}, menu.getInventory().firstEmpty());
								}
								
								menu.openMenu(p);
							}
						}
						
						@Override
						public ItemStack getItemStack() {
							if (decreaseChances.contains(finalItemKey)) {
								ItemStack stack = item.clone();
								ItemMeta meta = stack.getItemMeta();
								meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&l&nSELECTED"));
								stack.setItemMeta(meta);
								
								return stack;
							} else {
								ItemStack stack = item.clone();
								ItemMeta meta = stack.getItemMeta();
								List<String> lore = (meta.hasLore() ? meta.getLore() : Lists.newArrayList());
								lore.add(" ");
								lore.add(ChatColor.translateAlternateColorCodes('&', "&7** Once &fremoved&7 item will not be &fwinnable&7. **"));
								meta.setLore(lore);
								stack.setItemMeta(meta);
								
								return stack;
							}
						}
					}, menu.getInventory().firstEmpty());
				}
				
				menu.openMenu(p);
				PetUtil.petActivate(petData, p);
				return;
			}
		}
		
		Inventory crate = this.cp.getCrateInv();

		crate.setItem(9, this.itemsList.next());
		crate.setItem(10, this.itemsList.next());
		crate.setItem(11, this.itemsList.next());
		crate.setItem(12, this.itemsList.next());
		crate.setItem(13, this.itemsList.next());
		crate.setItem(14, this.itemsList.next());
		crate.setItem(15, this.itemsList.next());
		crate.setItem(16, this.itemsList.next());
		crate.setItem(17, this.itemsList.next());
		
		for (int i = 9; i < 18; i++) {
			ItemStack item = crate.getItem(i);
			if (item == null) {
				continue;
			}

			ItemStack stack = ConfigUtils.getInstance().getBorder(this.cp.getCrateName(), this.itemsList.getWeight(item));

			crate.setItem(i + 9, stack);
			crate.setItem(i - 9, stack);
		}

		p.openInventory(crate);
		p.updateInventory();

		ConfigUtils.getInstance().playFirework(this.loc, this.cp.getCrateName(), false);
		runTaskTimer(Main.getInstance(), 1L, 1L);
	}

	@Override
	public void setRainbowContents(String crateName, boolean random) {
		RandomCollection<Integer> rand = ConfigUtils.getInstance().getBorderDatas(crateName);

		int data = rand.next();
		Inventory inv = this.cp.getCrateInv();

		INV_LOOP: for (int i = 0; i < inv.getSize(); i++) {
			
			if (i == 9 || i == 10 || i == 11 || i == 12 || i == 13 || i == 14 || i == 15 || i == 16|| i == 17) {
				continue;
			}

			if (random) {
				int randColor = rand.next();
				inv.setItem(i, ConfigUtils.getInstance().getBorder(crateName, randColor));
			} else {
				for (Integer slot : this.selectedSlots) {
					if (i == (slot - 9) || i == (slot + 9)) {
						continue INV_LOOP;
					}
				}
				
				inv.setItem(i, ConfigUtils.getInstance().getBorder(crateName, data));
			}
		}
	}
	
	@Override
	public void clean() {
		if (this.step != 0) {
			this.step = 0;
		}
		if (this.startTime != 0) {
			this.startTime = 0;
		}
		if (this.cp != null) {
			this.cp = null;
		}
		if (this.loc != null) {
			this.loc = null;
		}
		if (this.player != null) {
			this.player.clear();
			this.player = null;
		}
		if (this.itemsList != null) {
			this.itemsList.destroy();
			this.itemsList = null;
		}
		if (this.clicks != 0) {
			this.clicks = 0;
		}
		if (this.selectedSlots != null) {
			this.selectedSlots.clear();
			this.selectedSlots = null;
		}
	}

	@Override
	public void cancel() {
		clean();
		super.cancel();
	}
	
	@Override
	public void run() {
		if (this.player == null || this.player.get() == null || Bukkit.getPlayer(this.player.get().getName()) == null) {
			cancel();
			return;
		}

		String crateName = this.cp.getCrateName();
		Inventory crate = this.cp.getCrateInv();
		OpenCrate openCrate = this.cp.getOpenCrate();
		Player p = this.player.get();

		if (crate == null) {
			cancel();
			return;
		}
		
		if (p.getOpenInventory() == null 
			|| p.getOpenInventory().getTopInventory() == null 
			|| p.getOpenInventory().getTopInventory().getTitle() == null 
			|| !(p.getOpenInventory().getTopInventory().getTitle().equalsIgnoreCase(crate.getTitle()))){
			p.closeInventory();
			p.openInventory(crate);
		}

		long timeElapsed = System.currentTimeMillis() - this.startTime;

		if (this.step == 0) {
			if (timeElapsed < 6000L) {
				ConfigUtils.getInstance().playParticles(this.loc, crateName);

				openCrate.check();

				if (openCrate.should()) {
					ConfigUtils.getInstance().playSound(p, crateName, "movingSound");
					
					scroll(crate, 9, 17, this.itemsList.next());
					p.updateInventory();
				}
			} else if (timeElapsed >= 6500L) {
				ConfigUtils.getInstance().playSound(p, crateName, "prizeSound");
				
				for (int i = 9; i < 18; i++) {
					if (!(this.selectedSlots.contains(Integer.valueOf(i)))) {
						crate.setItem(i, new ItemStack(Material.AIR, 1));
					}
				}
				
				for (Integer slot : this.selectedSlots) {
					ItemStack i = crate.getItem(slot);
					if (i != null) {
						crate.setItem(slot - 9, ConfigUtils.getInstance().getPrizeItem(this.cp.getCrateName()));
						crate.setItem(slot + 9, ConfigUtils.getInstance().getPrizeItem(this.cp.getCrateName()));
					}
				}

				this.step += 1;
			}
		} else if (this.step == 1) {
			if (timeElapsed < 9000L) {
				setRainbowContents(crateName, false);
			} else {
				try {
					for (Integer slot : this.selectedSlots) {
						ItemStack i = crate.getItem(slot);
						if (i != null) {
							ConfigUtils.getInstance().finish(p, crateName, i);
						}
					}
					ConfigUtils.getInstance().playFirework(this.loc, crateName, true);
					ConfigUtils.getInstance().playSound(p, crateName, "closeInventorySound");
				} finally {
					cancel();
					p.closeInventory();
					CPManager.getCP(p).destroy(p);
				}
			}
		}
	}
}