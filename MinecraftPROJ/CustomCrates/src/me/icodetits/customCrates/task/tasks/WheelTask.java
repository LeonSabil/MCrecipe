package me.icodetits.customCrates.task.tasks;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ListIterator;

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
import me.icodetits.customCrates.task.TaskType;
import me.icodetits.customCrates.utils.PetUtil;
import me.icodetits.customCrates.utils.RandomCollection;
import me.icodetits.customCrates.utils.RandomCollection.Item;

public class WheelTask extends BukkitRunnable implements Task {

	private int step, currentSlot, repeats, maxRepeats;
	private long startTime;
	private Location loc;
	private WeakReference<Player> player;
	private CratePlayer cp;
	private RandomCollection<ItemStack> itemsList;
	private List<Integer> itemSlots;
	private ListIterator<Integer> slotIter;
	
	private boolean cratePetEdited = false;

	public WheelTask(Player p, Location loc) {
		this.step = 0;
		this.startTime = 0;
		this.currentSlot = 0;
		this.repeats = 0;
		this.player = new WeakReference<Player>(p);
		this.cp = CPManager.getCP(p);
		this.maxRepeats = ConfigUtils.getInstance().getSelectionClicks(this.cp.getCrateName(), 1000);
		this.loc = loc.add(new Vector(0.5, 0.5, 0.5));
	}
	
	public WheelTask(Player p, Location loc, int repeats) {
		this.step = 0;
		this.startTime = 0;
		this.currentSlot = 0;
		this.repeats = repeats;
		this.player = new WeakReference<Player>(p);
		this.cp = CPManager.getCP(p);
		this.maxRepeats = ConfigUtils.getInstance().getSelectionClicks(this.cp.getCrateName(), 1000);
		this.loc = loc.add(new Vector(0.5, 0.5, 0.5));
	}
	
	@Override
	public void scroll(Inventory i, int end, int start, ItemStack item) {
		if (end != 0 || start != 0) {
			return;
		}

		if (!(this.slotIter.hasNext())) {
			this.slotIter = this.itemSlots.listIterator();
		}

		if (this.currentSlot != 0) {
			i.setItem(this.currentSlot, this.itemsList.next());
		}
		
		i.setItem(this.currentSlot = this.slotIter.next(), item);
	}
	
	@Override
	public void start() {
		this.startTime = System.currentTimeMillis();
		if (!(this.cratePetEdited)) {
			this.itemsList = ConfigUtils.getInstance().getCrateRands(this.cp.getCrateName());
		}
		this.itemSlots = Lists.newArrayList(13, 14, 15, 16, 25, 34, 43, 42, 41, 40, 39, 38, 37, 28, 19, 10, 11, 12);
		this.slotIter = this.itemSlots.listIterator();
		
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
							WheelTask.this.itemsList.remove(item);
							menu.updateMenu(false);
							
							if (decreaseChances.size() >= maxDecrease) {
								menu.setBypassMenuCloseBehaviour(true);
								p.closeInventory();
								
								decreaseChances.clear();
								
								final Menu menu = MenuAPI.getInstance().createMenu(ChatColor.translateAlternateColorCodes('&', "&8&lSELECT &n" + maxIncrease + "&8&l TO INCREASE %"), ConfigUtils.getInstance().fit(WheelTask.this.itemsList.getWeightMap().size()));
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
								
								for (Item<ItemStack> itemKey : WheelTask.this.itemsList.getWeightMap()) {
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
														Player p = WheelTask.this.player.get();
														if (p != null && Bukkit.getPlayer(p.getName()) != null) {
															menu.setBypassMenuCloseBehaviour(true);
															p.closeInventory();
															
															increaseChances.clear();
															
															WheelTask.this.cratePetEdited = true;
															start();
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

		this.itemSlots.forEach(i -> crate.setItem(i, this.itemsList.next()));

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

		for (int i = 0; i < inv.getSize(); i++) {
			if (this.itemSlots.contains(i)) {
				continue;
			}
			
			if (random) {
				int randColor = rand.next();
				inv.setItem(i, ConfigUtils.getInstance().getBorder(crateName, randColor));
			} else {
				inv.setItem(i, ConfigUtils.getInstance().getBorder(crateName, data));
			}
		}
	}
	
	@Override
	public void clean() {
		if (this.step != 0) {
			this.step = 0;
		}
		if (this.currentSlot != 0) {
			this.currentSlot = 0;
		}
		if (this.repeats != 0) {
			this.repeats = 0;
		}
		if (this.maxRepeats != 0) {
			this.maxRepeats = 0;
		}
		if (this.startTime != 0) {
			this.startTime = 0;
		}
		if (this.loc != null) {
			this.loc = null;
		}
		if (this.cp != null) {
			this.cp = null;
		}
		if (this.player != null) {
			this.player.clear();
			this.player = null;
		}
		if (this.itemSlots != null) {
			this.itemSlots = null;
		}
		if (this.slotIter != null) {
			this.slotIter = null;
		}
		if (this.itemsList != null) {
			this.itemsList.destroy();
			this.itemsList = null;
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

				setRainbowContents(crateName, true);
				ConfigUtils.getInstance().playParticles(this.loc, crateName);

				openCrate.check();

				if (openCrate.should()) {
					ConfigUtils.getInstance().playSound(p, crateName, "movingSound");
					scroll(crate, 0, 0, ConfigUtils.getInstance().getPrizeItem(this.cp.getCrateName()));
					p.updateInventory();
				}
			} else if (timeElapsed >= 6500L) {
				ConfigUtils.getInstance().playSound(p, crateName, "prizeSound");
				
				this.itemSlots.forEach(i -> {
					if (i == this.currentSlot) {
						return;
					}
					
					crate.setItem(i, new ItemStack(Material.AIR, 1));
				});
				
				crate.setItem(this.currentSlot, this.itemsList.next());

				this.step += 1;
			}
		} else if (this.step == 1) {
			if (timeElapsed < 9000L) {
				setRainbowContents(crateName, false);
			} else {
				try {
					ItemStack i = crate.getItem(this.currentSlot);
					ConfigUtils.getInstance().finish(p, crateName, i);
					ConfigUtils.getInstance().playFirework(this.loc, crateName, true);
					ConfigUtils.getInstance().playSound(p, crateName, "closeInventorySound");
					
					this.repeats++;
					if (this.repeats < this.maxRepeats) {
						Inventory inv = Bukkit.createInventory(p, TaskType.WHEEL.getInventorySize(), ConfigUtils.getInstance().getOpenInvName(crateName));

						CratePlayer cp = CPManager.makeCP(p);
						cp.setCrateName(crateName);
						cp.setCrateInv(inv);
						cp.setOpenCrate(new OpenCrate());
						
						Task task = new WheelTask(this.player.get(), this.loc, this.repeats);
						task.start();
					}
				} finally {
					cancel();
					p.closeInventory();
					CPManager.getCP(p).destroy(p);
				}
			}
		}
	}
}