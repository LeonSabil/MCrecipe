package me.icodetits.customCrates.smartinv;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import me.icodetits.customCrates.smartinv.content.InventoryContents;
import me.icodetits.customCrates.smartinv.content.InventoryProvider;
import me.icodetits.customCrates.smartinv.opener.InventoryOpener;

@SuppressWarnings("unchecked")
public class SmartInventory {

    private String id;
    private String title;
    private InventoryType type;
    private int size;
    private long clickDelay, lastClickStamp;
    private boolean closeable;

    private InventoryProvider provider;
    private SmartInventory parent;

    private List<InventoryListener<? extends Event>> listeners;
    private InventoryManager manager;

    private SmartInventory(InventoryManager manager) {
        this.manager = manager;
    }

    public Inventory open(Player player) { return open(player, 0); }
    public Inventory open(Player player, int page) {
        Optional<SmartInventory> oldInv = this.manager.getInventory(player);

        oldInv.ifPresent(inv -> {
            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
                            .accept(new InventoryCloseEvent(player.getOpenInventory())));

            this.manager.setInventory(player, null);
        });

        InventoryContents contents = new InventoryContents.Impl(this);
        contents.pagination().page(page);

        this.manager.setContents(player, contents);
        provider.init(player, contents);

        InventoryOpener opener = this.manager.findOpener(type)
                .orElseThrow(() -> new IllegalStateException("No opener found for the inventory type " + type.name()));
        Inventory handle = opener.open(this, player);

        this.manager.setInventory(player, this);

        return handle;
    }

    public void close(Player player) {
        listeners.stream()
                .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
                        .accept(new InventoryCloseEvent(player.getOpenInventory())));

        this.manager.setInventory(player, null);
        player.closeInventory();

        this.manager.setContents(player, null);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public long getLastClickStamp() { return lastClickStamp; }
    public long getClickDelay() { return clickDelay; }
    public InventoryType getType() { return type; }
    public int getSize() { return size; }
    public boolean isCloseable() { return closeable; }
    
    public void setLastClickStamp(long lastClickStamp) { this.lastClickStamp = lastClickStamp; }

    public InventoryProvider getProvider() { return provider; }
    public Optional<SmartInventory> getParent() { return Optional.ofNullable(parent); }

    public InventoryManager getManager() { return manager; }

    List<InventoryListener<? extends Event>> getListeners() { return listeners; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private String id = "unknown";
        private String title = "";
        private InventoryType type = InventoryType.CHEST;
        private int size = 54;
        private long clickDelay = -1L;
        private boolean closeable = true;

        private InventoryManager manager;
        private InventoryProvider provider;
        private SmartInventory parent;

        private List<InventoryListener<? extends Event>> listeners = new ArrayList<>();

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder type(InventoryType type) {
            this.type = type;
            return this;
        }
        
        public Builder delay(long clickDelay) {
            this.clickDelay = clickDelay;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder closeable(boolean closeable) {
            this.closeable = closeable;
            return this;
        }

        public Builder provider(InventoryProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder parent(SmartInventory parent) {
            this.parent = parent;
            return this;
        }

        public Builder listener(InventoryListener<? extends Event> listener) {
            this.listeners.add(listener);
            return this;
        }

        public Builder manager(InventoryManager manager) {
            this.manager = manager;
            return this;
        }

        public SmartInventory build() {
            if(this.provider == null)
                throw new IllegalStateException("The provider of the SmartInventory.Builder must be set.");

            InventoryManager manager = this.manager;

            if(manager == null)
                throw new IllegalStateException("The manager of the SmartInventory.Builder must be set, "
                        + "or the SmartInvs should be loaded as a plugin.");

            SmartInventory inv = new SmartInventory(manager);
            inv.id = this.id;
            inv.title = this.title;
            inv.type = this.type;
            inv.clickDelay = this.clickDelay;
            inv.size = this.size;
            inv.closeable = this.closeable;
            inv.provider = this.provider;
            inv.parent = this.parent;
            inv.listeners = this.listeners;

            return inv;
        }
    }

}