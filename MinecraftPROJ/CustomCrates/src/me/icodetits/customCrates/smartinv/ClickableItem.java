package me.icodetits.customCrates.smartinv;

import java.util.function.Consumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClickableItem {

    private ItemStack item;
    private Consumer<InventoryClickEvent> consumer;
    private boolean emptyIcon;

    private ClickableItem(ItemStack item, Consumer<InventoryClickEvent> consumer, boolean emptyIcon) {
        this.item = item;
        this.consumer = consumer;
        this.emptyIcon = emptyIcon;
    }

    public static ClickableItem empty(ItemStack item) {
        return of(item, e -> {}, true);
    }
    
    public static ClickableItem of(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        return new ClickableItem(item, consumer, false);
    }

    private static ClickableItem of(ItemStack item, Consumer<InventoryClickEvent> consumer, boolean emptyIcon) {
        return new ClickableItem(item, consumer, emptyIcon);
    }

    public void run(InventoryClickEvent e) { consumer.accept(e); }

    public ItemStack getItem() { return item; }
    
    public boolean isEmptyIcon() { return emptyIcon; }
    
    public Consumer<InventoryClickEvent> getConsumer() { return consumer; }

}
