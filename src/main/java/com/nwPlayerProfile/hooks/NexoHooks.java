package com.nwPlayerProfile.hooks;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nwPlayerProfile.NwPlayerProfile;
import com.nwPlayerProfile.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class NexoHooks {
    private final NwPlayerProfile plugin;
    private boolean nexoEnabled = false;
    private String statusMessage = "Not initialized";

    public NexoHooks(NwPlayerProfile plugin) {
        this.plugin = plugin;
        try {
            this.nexoEnabled = checkPluginEnabled("Nexo");
            this.statusMessage = nexoEnabled ? "Hooked successfully" : "Nexo not found or disabled";
            plugin.getLogger().info("[NexoHook] " + statusMessage);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Utils.loadConfig(); // โหลด config อีกครั้ง ถ้ามีส่วนที่ต้องพึ่งพา ItemsAdder
                Utils.loadCustomItems(); // โหลด CustomItems ที่พึ่งพา ItemsAdder
            }, 1L);
        } catch (Exception e) {
            this.statusMessage = "Error initializing Nexo hook: " + e.getMessage();
            plugin.getLogger().warning("[NexoHook] " + statusMessage);
        }
    }

    private boolean checkPluginEnabled(String name) {
        Plugin target = plugin.getServer().getPluginManager().getPlugin(name);
        if (target == null) {
            statusMessage = name + " plugin not found";
            return false;
        }
        if (!target.isEnabled()) {
            statusMessage = name + " plugin found but disabled";
            return false;
        }
        try {
            Class.forName("com.nexomc.nexo.api.NexoItems");
            return true;
        } catch (ClassNotFoundException e) {
            statusMessage = "Nexo API classes not found";
            return false;
        }
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
            if (nexoItemBuilder != null) {
                return nexoItemBuilder.build();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}