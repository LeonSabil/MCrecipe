package me.icodetits.customCrates.smartinv.content;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.event.inventory.InventoryType;

import me.icodetits.customCrates.smartinv.ClickableItem;
import me.icodetits.customCrates.smartinv.SmartInventory;

public interface SlotIterator {

    enum Type {
        HORIZONTAL,
        ORDER,
        VERTICAL
    }

    Optional<ClickableItem> get();
    SlotIterator set(ClickableItem item);

    SlotIterator previous();
    SlotIterator next();

    SlotIterator blacklist(int slot);

    int row();
    SlotIterator row(int row);

    int column();

	SlotIterator column(int column);

	boolean started();

	boolean ended();

	boolean doesAllowOverride();

	SlotIterator allowOverride(boolean override);

	class Impl implements SlotIterator {

		private InventoryContents contents;
		private SmartInventory inv;

		private Type type;
		private boolean started = false;
		private boolean allowOverride = true;
		private int row, column;

		private Set<Integer> blacklisted = new HashSet<>();

		public Impl(InventoryContents contents, SmartInventory inv, Type type, int startRow, int startColumn) {

			this.contents = contents;
			this.inv = inv;

			this.type = type;

			this.row = startRow;
			this.column = startColumn;
		}

		public Impl(InventoryContents contents, SmartInventory inv, Type type) {

			this(contents, inv, type, 0, 0);
		}

		@Override
		public Optional<ClickableItem> get() {
			int slot = column * 9 + row;

			return contents.get(slot);
		}

		@Override
		public SlotIterator set(ClickableItem item) {
			if (canPlace()) {
				int slot = column * 9 + row;

				contents.set(slot, item);
			}

			return this;
        }

        @Override
        public SlotIterator previous() {
            if(row == 0 && column == 0) {
                this.started = true;
                return this;
            }
            
            do {
                if(!this.started) {
                    this.started = true;
                }
                else {
                    switch(type) {
                        case VERTICAL:
                            column--;

                            if(column == -1) {
                                column = (inv.getSize() / 9) - 1;
                                row--;
                            }
                            break;
                        case ORDER:
                        	int slot = contents.lastEmpty().orElse(-1);
                        	if (slot == -1) {
                        		row = 0;
                        		column = 0;
                        		break;
                        	}
                        	
                	        row = slot % 9;
                	        column = slot / 9;
                	        break;
                        case HORIZONTAL:
                            row--;

                            if(row == -1) {
                                row = rows(inv.getType()) - 1;
                                column--;
                            }
                            break;
                    }
                }
            }
            while(!canPlace() && (row != 0 || column != 0));

            return this;
        }

        @Override
        public SlotIterator next() {
			if (ended()) {
				this.started = true;
				return this;
			}

            do {
                if(!this.started) {
                    this.started = true;
                }
                else {
                    switch(type) {
                        case VERTICAL:
                            column = ++column % (inv.getSize() / 9);

                            if(column == 0)
                                row++;
                            break;
                        case ORDER:
                        	int slot = contents.firstEmpty().orElse(-1);
                        	if (slot == -1) {
                        		row = 0;
                        		column = 0;
                        		break;
                        	}
                        	
                	        row = slot % 9;
                	        column = slot / 9;
                	        break;
                        case HORIZONTAL:
                            row = ++row % rows(inv.getType());

                            if(row == 0)
                                column++;
                            break;
                    }
                }
            }
            while(!canPlace() && !ended());

            return this;
        }

        @Override
        public SlotIterator blacklist(int slot) {
            this.blacklisted.add(slot);
            return this;
        }

        @Override
        public int row() { return row; }

        @Override
        public SlotIterator row(int row) {
            this.row = row;
            return this;
        }

        @Override
        public int column() { return column; }

        @Override
        public SlotIterator column(int column) {
            this.column = column;
            return this;
        }

        @Override
        public boolean started() {
            return this.started;
        }

        @Override
        public boolean ended() {
            return row == rows(inv.getType()) - 1
                    && column == (inv.getSize() / 9) - 1;
        }

        @Override
        public boolean doesAllowOverride() { return allowOverride; }

        @Override
        public SlotIterator allowOverride(boolean override) {
            this.allowOverride = override;
            return this;
        }

        private boolean canPlace() {
        	int slot = column * 9 + row;
        	
            return !blacklisted.contains(slot) && (allowOverride || !this.get().isPresent());
        }

        private int rows(InventoryType type) {
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
        
    }

}