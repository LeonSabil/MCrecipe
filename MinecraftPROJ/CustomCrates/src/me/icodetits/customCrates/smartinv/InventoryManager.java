package me.icodetits.customCrates.smartinv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

import me.icodetits.customCrates.smartinv.content.InventoryContents;
import me.icodetits.customCrates.smartinv.opener.ChestInventoryOpener;
import me.icodetits.customCrates.smartinv.opener.InventoryOpener;
import me.icodetits.customCrates.smartinv.opener.SpecialInventoryOpener;

public class InventoryManager {

    private JavaPlugin plugin;
    private PluginManager pluginManager;

    private Map<Player, SmartInventory> inventories;
    private Map<Player, InventoryContents> contents;

    private List<InventoryOpener> defaultOpeners;
    private List<InventoryOpener> openers;

    public InventoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pluginManager = Bukkit.getPluginManager();

        this.inventories = Maps.newConcurrentMap();
        this.contents = Maps.newConcurrentMap();

        this.defaultOpeners = Arrays.asList(
                new ChestInventoryOpener(),
                new SpecialInventoryOpener()
        );

        this.openers = new ArrayList<>();
    }

    public void init() {
        pluginManager.registerEvents(new InvListener(), plugin);

        new InvTask().runTaskTimer(plugin, 1, 1);
    }

    public Optional<InventoryOpener> findOpener(InventoryType type) {
        Optional<InventoryOpener> opInv = this.openers.stream()
                .filter(opener -> opener.supports(type))
                .findAny();

        if(!opInv.isPresent()) {
            opInv = this.defaultOpeners.stream()
                    .filter(opener -> opener.supports(type))
                    .findAny();
        }

        return opInv;
    }

    public void registerOpeners(InventoryOpener... openers) {
        this.openers.addAll(Arrays.asList(openers));
    }

    public List<Player> getOpenedPlayers(SmartInventory inv) {
        List<Player> list = new ArrayList<>();

        this.inventories.forEach((player, playerInv) -> {
            if(inv.equals(playerInv))
                list.add(player);
        });

        return list;
    }

    public Optional<SmartInventory> getInventory(Player p) {
        return Optional.ofNullable(this.inventories.get(p));
    }

    protected void setInventory(Player p, SmartInventory inv) {
        if(inv == null)
            this.inventories.remove(p);
        else
            this.inventories.put(p, inv);
    }

    public Optional<InventoryContents> getContents(Player p) {
        return Optional.ofNullable(this.contents.get(p));
    }

    protected void setContents(Player p, InventoryContents contents) {
        if(contents == null)
            this.contents.remove(p);
        else
            this.contents.put(p, contents);
    }

    @SuppressWarnings("unchecked")
    class InvListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryClick(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();

            if(!inventories.containsKey(p))
                return;

            if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR ||
                    e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                    e.getAction() == InventoryAction.NOTHING) {

                e.setCancelled(true);
                return;
            }

            if(e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
                e.setCancelled(true);

                int row = e.getSlot() / 9;
                int column = e.getSlot() % 9;
                int slot = e.getSlot();
                if (slot < 0)
                	return;

                if(row < 0 || column < 0)
                    return;

                SmartInventory inv = inventories.get(p);
                if (inv.getClickDelay() > 0L && (System.currentTimeMillis() - inv.getLastClickStamp()) <= inv.getClickDelay()) {
                	return;
                }
                
                inv.setLastClickStamp(System.currentTimeMillis());

                if(slot >= inv.getSize())
                    return;

                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == InventoryClickEvent.class)
                        .forEach(listener -> ((InventoryListener<InventoryClickEvent>) listener).accept(e));

                contents.get(p).get(slot).ifPresent(item -> item.run(e));

                p.updateInventory();
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryDrag(InventoryDragEvent e) {
            Player p = (Player) e.getWhoClicked();

            if(!inventories.containsKey(p))
                return;

            SmartInventory inv = inventories.get(p);

            for(int slot : e.getRawSlots()) {
                if(slot >= p.getOpenInventory().getTopInventory().getSize())
                    continue;

                e.setCancelled(true);
                break;
            }

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryDragEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryDragEvent>) listener).accept(e));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryOpen(InventoryOpenEvent e) {
            Player p = (Player) e.getPlayer();

            if(!inventories.containsKey(p))
                return;

            SmartInventory inv = inventories.get(p);

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryOpenEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryOpenEvent>) listener).accept(e));
        }

		@EventHandler(priority = EventPriority.LOW)
		public void onInventoryClose(InventoryCloseEvent e) {
			Player p = (Player) e.getPlayer();

			if (!inventories.containsKey(p))
				return;

			SmartInventory inv = inventories.get(p);

			inv.getListeners().stream().filter(listener -> listener.getType() == InventoryCloseEvent.class)
					.forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener).accept(e));

			if (inv.isCloseable()) {
				e.getInventory().clear();

				inventories.remove(p);
				contents.remove(p);
			} else {
				e.getInventory().clear();

				inventories.remove(p);
				contents.remove(p);
			}
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onPlayerQuit(PlayerQuitEvent e) {
			Player p = e.getPlayer();

            if(!inventories.containsKey(p))
                return;

            SmartInventory inv = inventories.get(p);

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == PlayerQuitEvent.class)
                    .forEach(listener -> ((InventoryListener<PlayerQuitEvent>) listener).accept(e));

            inventories.remove(p);
            contents.remove(p);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onPluginDisable(PluginDisableEvent e) {
            new HashSet<>(inventories.entrySet()).forEach(entry -> {
                SmartInventory inv = entry.getValue();

                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == PluginDisableEvent.class)
                        .forEach(listener -> ((InventoryListener<PluginDisableEvent>) listener).accept(e));

                inv.close(entry.getKey());
            });

            inventories.clear();
            contents.clear();
        }

    }

    class InvTask extends BukkitRunnable {

        @Override
        public void run() {
            inventories.forEach((player, inv) -> inv.getProvider().update(player, contents.get(player)));
        }

    }

}
