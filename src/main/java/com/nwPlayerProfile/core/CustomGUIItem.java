package com.nwPlayerProfile.core;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomGUIItem {
    private final ItemStack item;
    private final List<String> actions;

    public CustomGUIItem(ItemStack item, List<String> actions) {
        this.item = item;
        this.actions = actions;
    }

    public ItemStack getItem() {
        return item;
    }

    public List<String> getActions() {
        return actions;
    }
}
