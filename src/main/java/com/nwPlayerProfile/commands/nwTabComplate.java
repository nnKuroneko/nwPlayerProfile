package com.nwPlayerProfile.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class nwTabComplate implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("nwPlayerProfile")) {
            if (args.length == 1) {
                completions.add("reload"); // เพิ่ม reload เป็นตัวเลือก
            }
        }

        return completions;
    }
}