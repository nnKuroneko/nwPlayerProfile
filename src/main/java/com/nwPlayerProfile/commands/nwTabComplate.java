package com.nwPlayerProfile.commands;

import com.nwPlayerProfile.core.Utils; // Import Utils เพื่อเข้าถึง PermissionOpen
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class nwTabComplate implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("nwPlayerProfile")) {
            if (args.length == 1) {
                // เพิ่ม 'reload' เสมอ (หรือตาม permission ถ้ามี)
                if (sender.hasPermission(Utils.Permission)) { // ถ้า reload มี permission เฉพาะ
                    completions.add("reload");
                }
                // *** เพิ่ม 'open' เฉพาะเมื่อผู้เล่นมี PermissionOpen ***
                if (sender.hasPermission(Utils.PermissionOpen)) {
                    completions.add("open");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
                // แนะนำชื่อผู้เล่นที่ออนไลน์เมื่อพิมพ์ /nwPlayerProfile open
                // และผู้ส่งคำสั่งต้องมี permission open ด้วย
                if (sender.hasPermission(Utils.PermissionOpen)) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                }
            }
        }

        return completions;
    }
}