package me.icodetits.customCrates.smartinv.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.icodetits.customCrates.smartinv.ClickableItem;
import me.icodetits.customCrates.smartinv.InventoryManager;
import me.icodetits.customCrates.smartinv.SmartInventory;

public interface InventoryContents {

    SmartInventory inventory();
    Pagination pagination();

    Optional<SlotIterator> iterator(String id);

    SlotIterator newIterator(String id, SlotIterator.Type type, int startSlot);
    SlotIterator newIterator(SlotIterator.Type type, int startSlot);

    ClickableItem[] all();

    Optional<Integer> firstEmpty();
    Optional<Integer> lastEmpty();

    Optional<ClickableItem> get(int slot);

    InventoryContents set(int slot, ClickableItem item);

    InventoryContents add(ClickableItem item);

    InventoryContents fill(ClickableItem item);
    InventoryContents fillRow(int row, ClickableItem item);
    InventoryContents fillColumn(int column, ClickableItem item);
    InventoryContents fillBorders(ClickableItem item);
	InventoryContents fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item);

    <T> T property(String name);
    <T> T property(String name, T def);

    InventoryContents setProperty(String name, Object value);
    
    default int rows(InventoryType type) {
    	if (type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST) {
    		return 9;
    	} else if (type == InventoryType.FURNACE || type == InventoryType.ANVIL || type == InventoryType.BREWING || type == InventoryType.WORKBENCH || type == InventoryType.DISPENSER || type == InventoryType.DROPPER) {
			return 3;
		} else if (type == InventoryType.ENCHANTING) {
			return 2;
    	} else if (type == InventoryType.BEACON) {
    		return 1;
    	} else if (type == InventoryType.HOPPER) {
    		return 5;
    	} else {
    		return 0;
    	}
    }
    
	class Impl implements InventoryContents {

		private SmartInventory inv;
		private ClickableItem[] contents;

		private Pagination pagination = new Pagination.Impl();
		private Map<String, SlotIterator> iterators = new HashMap<>();
		private Map<String, Object> properties = new HashMap<>();

		public Impl(SmartInventory inv) {
			this.inv = inv;
			this.contents = new ClickableItem[inv.getSize()];
		}

		@Override
        public SmartInventory inventory() { return inv; }

        @Override
        public Pagination pagination() { return pagination; }

        @Override
        public Optional<SlotIterator> iterator(String id) {
            return Optional.ofNullable(this.iterators.get(id));
        }

		@Override
		public SlotIterator newIterator(String id, SlotIterator.Type type, int startSlot) {
	        int row = startSlot % 9;
	        int column = startSlot / 9;
			
			SlotIterator iterator = new SlotIterator.Impl(this, inv, type, row, column);

			this.iterators.put(id, iterator);
			return iterator;
		}

		@Override
		public SlotIterator newIterator(SlotIterator.Type type, int startSlot) {
	        int row = startSlot % 9;
	        int column = startSlot / 9;
			
			return new SlotIterator.Impl(this, inv, type, row, column);
		}

        @Override
        public ClickableItem[] all() { return contents; }

		@Override
		public Optional<Integer> firstEmpty() {
			for (int slot = 0; slot < contents.length; slot++) {
				if (!this.get(slot).isPresent())
					return Optional.of(slot);
			}

			return Optional.empty();
		}
		
		@Override
		public Optional<Integer> lastEmpty() {
			for (int slot = contents.length - 1; slot >= 0; slot--) {
				if (!this.get(slot).isPresent())
					return Optional.of(slot);
			}

			return Optional.empty();
		}

		@Override
		public Optional<ClickableItem> get(int slot) {
			if (slot >= contents.length)
				return Optional.empty();

			return Optional.ofNullable(contents[slot]);
		}

		@Override
		public InventoryContents set(int slot, ClickableItem item) {
			if (slot >= contents.length)
				return this;

			contents[slot] = item;
			update(slot, item != null ? item.getItem() : null);
			return this;
		}

		@Override
		public InventoryContents add(ClickableItem item) {
			for (int slot = 0; slot < contents.length; slot++) {
				if (contents[slot] == null) {
					set(slot, item);
					return this;
				}
			}

            return this;
        }

        @Override
        public InventoryContents fill(ClickableItem item) {
			for (int slot = 0; slot < contents.length; slot++)
				set(slot, item);

			return this;
		}

		@Override
		public InventoryContents fillRow(int row, ClickableItem item) {
			if (row > contents.length / rows(inv.getType()))
				return this;
			
			int rows = rows(inv.getType()), startSlot = rows * (row - 1), endSlot = rows * row;
						
			for (int slot = startSlot; slot < endSlot; slot++)
				set(slot, item);

            return this;
        }

		@Override
		public InventoryContents fillColumn(int column, ClickableItem item) {
			if (column > rows(inv.getType()))
				return this;
			
			int rows = rows(inv.getType()), columns = inv.getSize() / rows, startSlot = column - 1, endSlot = startSlot + (rows * (columns - 1));
			
			for (int slot = startSlot; slot <= endSlot; slot += rows) {
				set(slot, item);
			}

			return this;
		}

		@Override
		public InventoryContents fillBorders(ClickableItem item) {
			fillRect(0, 0, rows(inv.getType()) - 1, (inv.getSize() / 9) - 1, item);
			return this;
		}

		@Override
		public InventoryContents fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
			for (int row = fromRow; row <= toRow; row++) {
				for (int column = fromColumn; column <= toColumn; column++) {
					if (row != fromRow && row != toRow && column != fromColumn && column != toColumn)
						continue;

					int slot = column * 9 + row;
					set(slot, item);
				}
			}

			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T property(String name) {
			return (T) properties.get(name);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T property(String name, T def) {
			return properties.containsKey(name) ? (T) properties.get(name) : def;
		}

		@Override
		public InventoryContents setProperty(String name, Object value) {
			properties.put(name, value);
			return this;
		}

		private void update(int slot, ItemStack item) {
			InventoryManager manager = inv.getManager();

			manager.getOpenedPlayers(inv).forEach(player -> {
				Inventory topInventory = player.getOpenInventory().getTopInventory();
				topInventory.setItem(slot, item);
			});
		}

	}

}