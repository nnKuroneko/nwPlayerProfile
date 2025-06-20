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

public class ItemsAdderHooks implements Listener { // ต้อง implements Listener
    private final NwPlayerProfile plugin;
    private Plugin itemsAdderPlugin;
    private boolean itemsAdderApiLoaded = false; // สถานะว่า ItemsAdder API พร้อมใช้งานแล้ว

    public ItemsAdderHooks(NwPlayerProfile plugin) {
        this.plugin = plugin;
        // ลงทะเบียน Listener ทันที
        Bukkit.getPluginManager().registerEvents(this, plugin);
        initialize();
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
        if (itemsAdderApiLoaded) {
            return; // ป้องกันการ enable ซ้ำ
        }
        try {
            // ตรวจสอบว่า CustomStack class พร้อมใช้งานหรือไม่
            Class.forName("dev.lone.itemsadder.api.CustomStack");
            itemsAdderApiLoaded = true;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Utils.loadConfig(); // โหลด config อีกครั้ง ถ้ามีส่วนที่ต้องพึ่งพา ItemsAdder
                Utils.loadCustomItems(); // โหลด CustomItems ที่พึ่งพา ItemsAdder
            }, 1L);
        } catch (ClassNotFoundException e) {
            itemsAdderApiLoaded = false;
        } catch (NoClassDefFoundError e) {
            itemsAdderApiLoaded = false;
        } catch (Exception e) {
            itemsAdderApiLoaded = false;
        }
    }


    public boolean isItemsAdderHooked() {
        // ตรวจสอบว่า ItemsAdder Plugin ถูกโหลดและ API พร้อมใช้งาน
        return itemsAdderApiLoaded && itemsAdderPlugin != null && itemsAdderPlugin.isEnabled();
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
            return null;
        }
    }
}