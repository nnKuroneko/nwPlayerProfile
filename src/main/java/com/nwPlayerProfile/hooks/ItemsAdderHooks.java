package com.nwPlayerProfile.hooks;

import com.nwPlayerProfile.NwPlayerProfile;
import com.nwPlayerProfile.core.Utils;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.logging.Logger;

public class ItemsAdderHooks implements Listener { // ต้อง implements Listener
    private final NwPlayerProfile plugin;
    private Plugin itemsAdderPlugin;
    private boolean itemsAdderApiLoaded = false; // สถานะว่า ItemsAdder API พร้อมใช้งานแล้ว

    public ItemsAdderHooks(NwPlayerProfile plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // ตรวจสอบอีกครั้งทันที ว่า ItemsAdder ถูก enable แล้วหรือยัง
        Plugin itemsAdderPlugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        if (itemsAdderPlugin != null && itemsAdderPlugin.isEnabled()) {
            plugin.getLogger().info("[ItemsAdderHook] ItemsAdder already enabled. Trying to hook directly...");
            tryEnableItemsAdderHook();
        } else {
            plugin.getLogger().info("[ItemsAdderHook] ItemsAdder not enabled yet. Will wait for PluginEnableEvent.");
        }
    }


    public void initialize() {
        itemsAdderPlugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");

        if (itemsAdderPlugin != null && itemsAdderPlugin.isEnabled()) {
            // ถ้า ItemsAdder โหลดและเปิดใช้งานแล้ว ให้พยายาม enable ทันที
            tryEnableItemsAdderHook();
        } else {
            // ถ้ายังไม่พร้อม ให้รอ PluginEnableEvent
            plugin.getLogger().info("[ItemsAdderHook] ItemsAdder is not yet enabled or found. Waiting for PluginEnableEvent.");
        }
    }

    @EventHandler // เพิ่ม Event Listener สำหรับ PluginEnableEvent
    public void onPluginEnable(PluginEnableEvent event) {
        if (!itemsAdderApiLoaded && event.getPlugin().getName().equals("ItemsAdder")) {
            // เมื่อ ItemsAdder โหลดและเปิดใช้งานแล้ว
            tryEnableItemsAdderHook();
        }
    }

    private void tryEnableItemsAdderHook() {
        if (itemsAdderApiLoaded) return;
        try {
            plugin.getLogger().info("[ItemsAdderHook] Trying to hook into ItemsAdder...");
            Class.forName("dev.lone.itemsadder.api.CustomStack");
            itemsAdderApiLoaded = true;
            plugin.getLogger().info("[ItemsAdderHook] ItemsAdder hooked successfully!");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Utils.loadConfig();
                Utils.loadCustomItems();
            }, 1L);
        } catch (Exception e) {
            plugin.getLogger().warning("[ItemsAdderHook] Failed to hook: " + e.getMessage());
            itemsAdderApiLoaded = false;
        }
    }


    public boolean isItemsAdderHooked() {
        Logger logger = Bukkit.getLogger();
        try {
            boolean result = itemsAdderApiLoaded;
            return result;
        } catch (Exception e) {
            logger.warning("[nwPlayerProfile] [Debug] Exception occurred in isItemsAdderHooked: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    public String getStatus() {
        if (itemsAdderPlugin == null) {
            return "ItemsAdder plugin not found.";
        } else if (!itemsAdderPlugin.isEnabled()) {
            return "ItemsAdder plugin found but not enabled yet.";
        } else if (!itemsAdderApiLoaded) {
            return "ItemsAdder plugin enabled, but API classes not loaded/accessible.";
        } else {
            return "ItemsAdder hooked successfully.";
        }
    }


    public ItemStack getItemId(String name) {
        if (!isItemsAdderHooked()) {
            return null;
        }
        try {
            CustomStack customStack = CustomStack.getInstance(name);
            if (customStack == null) {
                return null;
            }

            ItemStack item = customStack.getItemStack();
            if (item == null) {
                return null;
            }
            return item.clone();
        } catch (Exception e) {
            Bukkit.getLogger().severe("[ItemsAdderHook] Exception while getting item: " + name);
            e.printStackTrace();
            return null;
        }
    }


}