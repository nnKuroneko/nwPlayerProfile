package com.nwPlayerProfile.hooks;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nwPlayerProfile.NwPlayerProfile;
import com.nwPlayerProfile.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class NexoHooks implements Listener {
    private final NwPlayerProfile plugin;
    private boolean nexoEnabled = false;
    private String statusMessage = "Not initialized";

    public NexoHooks(NwPlayerProfile plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin); // ลงทะเบียน event listener
        initialize(); // เรียกเริ่มต้น
    }

    public void initialize() {
        if (checkPluginEnabled("Nexo")) {
            enableHook();
        } else {
            plugin.getLogger().info("[NexoHook] Nexo not loaded yet, waiting for PluginEnableEvent...");
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (!nexoEnabled && event.getPlugin().getName().equalsIgnoreCase("Nexo")) {
            enableHook();
        }
    }

    private void enableHook() {
        try {
            Class.forName("com.nexomc.nexo.api.NexoItems");
            nexoEnabled = true;
            statusMessage = "Hooked successfully";
            plugin.getLogger().info("[NexoHook] " + statusMessage);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Utils.loadConfig();
                Utils.loadCustomItems();
            }, 1L);
        } catch (Exception e) {
            nexoEnabled = false;
            statusMessage = "Error initializing Nexo hook: " + e.getMessage();
            plugin.getLogger().warning("[NexoHook] " + statusMessage);
        }
    }

    private boolean checkPluginEnabled(String name) {
        Plugin target = plugin.getServer().getPluginManager().getPlugin(name);
        return target != null && target.isEnabled();
    }

    public boolean isNexoEnabled() {
        return nexoEnabled;
    }

    public String getStatus() {
        return statusMessage;
    }

    public ItemStack getItemId(String name) {
        if (!nexoEnabled) {
            return null;
        }

        try {
            ItemBuilder nexoItemBuilder = NexoItems.itemFromId(name);
            return nexoItemBuilder != null ? nexoItemBuilder.build() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
