package com.nwPlayerProfile;

import com.nwPlayerProfile.commands.nwCommand;
import com.nwPlayerProfile.commands.nwTabComplate;
import com.nwPlayerProfile.core.Utils;
import com.nwPlayerProfile.profile.profile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class NwPlayerProfile extends JavaPlugin {

    private Logger logger = getLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger.info("nwPlayerProfile Plugin Enabled");
        logger.info("Created by NN#7999");
        logger.info("& Newworld Server");
        loadCommand();
        loadEvents();
        loadConfig();
        Utils.loadCustomItems();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("nwPlayerProfile Plugin Disabled");
    }

    public void loadCommand() {
        getCommand("nwPlayerProfile").setExecutor(new nwCommand(this));
        getCommand("nwPlayerProfile").setTabCompleter(new nwTabComplate());
    }


    public void loadEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(new profile(), this);
    }

    public void loadConfig() {
        getDataFolder().mkdir();
        File config = new File(getDataFolder(), "config.yml");
        if(!config.exists()){
            saveDefaultConfig();
        }
        logger.info("Config reloaded from file!");
    }

}
