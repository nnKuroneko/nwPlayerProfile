package com.nwPlayerProfile.listener;

import com.nwPlayerProfile.NwPlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginListener implements Listener {
    private final NwPlayerProfile plugin;

    public PluginListener(NwPlayerProfile plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("ItemsAdder")) {
            plugin.getLogger().info("ItemsAdder has been enabled, initializing hook...");
            plugin.getIAHOOK().initialize();
        }
    }
}