package com.nwPlayerProfile;

import com.nwPlayerProfile.commands.nwCommand;
import com.nwPlayerProfile.commands.nwTabComplate;
import com.nwPlayerProfile.core.Utils;
import com.nwPlayerProfile.hooks.ItemsAdderHooks;
import com.nwPlayerProfile.metrics.BstatsManager;
import com.nwPlayerProfile.hooks.NexoHooks;
import com.nwPlayerProfile.profile.profile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class NwPlayerProfile extends JavaPlugin {

    private Logger logger = getLogger();
    private profile playerProfile;
    private BstatsManager bstats; // ไม่ได้ใช้ในตัวอย่างนี้ แต่คงไว้
    private NexoHooks hook;
    private ItemsAdderHooks iahook;

    @Override
    public void onEnable() {
        this.logger = getLogger();

        logger.info("============================nwPlayerProfile============================");
        logger.info("Initializing hooks...");

        // ไม่ต้องตรวจสอบลำดับการโหลดตรงนี้แล้ว เพราะ ItemsAdderHooks จะจัดการเอง
        // checkPluginLoadOrder(); // ลบออกหรือคอมเมนต์

        // Initialize hooks
        // ItemsAdderHooks จะลงทะเบียนตัวเองเป็น Listener ใน Constructor
        this.iahook = new ItemsAdderHooks(this);
        this.hook = new NexoHooks(this);

        Utils.itemsAdderHooks = this.iahook;
        Utils.nexoHooks = this.hook;

        // แสดงสถานะ hook อย่างละเอียด
        logHookStatus(); // จะแสดงสถานะเริ่มต้น, ItemsAdder อาจยังไม่พร้อม

        // ส่วนอื่นๆ ของการเริ่มต้น plugin...
        playerProfile = new profile();
        loadCommand();
        loadEvents();
        loadConfig();
//        Utils.loadCustomItems();
//        Utils.loadConfig();

        logger.info("============================nwPlayerProfile============================");
    }

    // ลบเมธอด checkPluginLoadOrder() ออกไป หรือคอมเมนต์ไว้
    /*
    private void checkPluginLoadOrder() {
        Plugin itemsAdder = getServer().getPluginManager().getPlugin("ItemsAdder");
        if (itemsAdder != null) {
            logger.info("ItemsAdder load state: " + (itemsAdder.isEnabled() ? "ENABLED" : "DISABLED"));
            logger.info("ItemsAdder load order: " + getServer().getPluginManager().getPlugins()[0].getName());
        }
    }
    */

    private void logHookStatus() {
        logger.info("=== Hook Status ===");
        if (iahook != null) {
            // เรียกใช้ getStatus() เพื่อดูสถานะปัจจุบันของ ItemsAdderHook
            logger.info("ItemsAdder Hook Status: " + iahook.getStatus());
            if (!iahook.isItemsAdderHooked()) {
                logger.warning("ItemsAdder integration is NOT fully active yet or failed to hook. Check logs for details!");
            }
        } else {
            logger.warning("ItemsAdderHook object not initialized!");
        }

        if (hook != null) {
            logger.info("Nexo: " + hook.getStatus());
        } else {
            logger.warning("NexoHook not initialized!");
        }
    }

    @Override
    public void onDisable() {
        logger.info("nwPlayerProfile Plugin Disabled");
    }

    public void loadCommand() {
        getCommand("nwPlayerProfile").setExecutor(new nwCommand(this, playerProfile));
        getCommand("nwPlayerProfile").setTabCompleter(new nwTabComplate());
    }

    public void loadEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(playerProfile, this);
    }

    public void loadConfig() {
        getDataFolder().mkdir();
        File config = new File(getDataFolder(), "config.yml");
        if(!config.exists()){
            saveDefaultConfig();
        }
        logger.info("Config reloaded from file!");
    }

    public ItemsAdderHooks getIAHOOK() {
        return iahook;
    }

    public NexoHooks getNEXOHOOK() {
        return hook;
    }

}