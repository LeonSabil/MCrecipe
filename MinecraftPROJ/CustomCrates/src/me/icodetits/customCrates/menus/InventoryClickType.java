package me.icodetits.customCrates.menus;

import org.bukkit.event.inventory.InventoryAction;

public enum InventoryClickType {
    LEFT(true, false), 
    SHIFT_LEFT(true, true), 
    RIGHT(false, false), 
    OTHER(false, false);
    
    private boolean leftClick;
    private boolean shiftClick;
    
    private InventoryClickType(final boolean leftClick, final boolean shiftClick) {
        this.leftClick = leftClick;
        this.shiftClick = shiftClick;
    }
    
    public boolean isLeftClick() {
        return this.leftClick && this != InventoryClickType.OTHER;
    }
    
    public boolean isRightClick() {
        return !this.leftClick && this != InventoryClickType.OTHER;
    }
    
    public boolean isShiftClick() {
        return this.shiftClick;
    }
    
    public static InventoryClickType fromInventoryAction(final InventoryAction action) {
        switch (action) {
            case PICKUP_ALL:
            case PLACE_SOME:
            case PLACE_ALL:
            case SWAP_WITH_CURSOR: {
                return InventoryClickType.LEFT;
            }
            case PICKUP_HALF:
            case PLACE_ONE: {
                return InventoryClickType.RIGHT;
            }
            case MOVE_TO_OTHER_INVENTORY: {
                return InventoryClickType.SHIFT_LEFT;
            }
            default: {
                return InventoryClickType.OTHER;
            }
        }
    }
}
