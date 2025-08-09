package com.nwPlayerProfile.core;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomGUIItem {
    private final ItemStack item;
    private final List<String> actions;
    private final boolean isBadge;
    private final String badgeSlotId; // New field for badge slot ID

    // Modified constructor to include badgeSlotId
    public CustomGUIItem(ItemStack item, List<String> actions, boolean isBadge, String badgeSlotId) {
        this.item = item;
        this.actions = actions;
        this.isBadge = isBadge;
        this.badgeSlotId = isBadge ? badgeSlotId : null; // Only set if it's a badge
    }

    public ItemStack getItem() {
        return item;
    }

    public List<String> getActions() {
        return actions;
    }

    public boolean isBadge() { // Getter for the badge field
        return isBadge;
    }

    public String getBadgeSlotId() {
        return badgeSlotId;
    }
}
