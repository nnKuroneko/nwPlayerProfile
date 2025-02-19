package com.nwPlayerProfile.commands;

import com.nwPlayerProfile.core.ColorUtils;
import com.nwPlayerProfile.core.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.nwPlayerProfile.NwPlayerProfile;

import java.io.File;

import static com.nwPlayerProfile.core.Utils.*;

public class nwCommand implements CommandExecutor {
    private final NwPlayerProfile plugin;

    public nwCommand(NwPlayerProfile plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nwPlayerProfile")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission(Permission)) {
                    Utils.loadConfig();
                    sender.sendMessage(ColorUtils.translateColorCodes(MsgReload));
                    return true;
                }
            } else {
                sender.sendMessage("§cUsage: /nwPlayerProfile reload");
            }
        }
        return false;
    }


}
