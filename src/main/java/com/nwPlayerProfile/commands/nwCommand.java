package com.nwPlayerProfile.commands;

import com.nwPlayerProfile.core.ColorUtils;
import com.nwPlayerProfile.core.Utils;
import com.nwPlayerProfile.profile.profile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.nwPlayerProfile.NwPlayerProfile;

public class nwCommand implements CommandExecutor {
    private final NwPlayerProfile plugin;
    private final profile playerProfile;

    public nwCommand(NwPlayerProfile plugin, profile playerProfile) {
        this.plugin = plugin;
        this.playerProfile = playerProfile;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // ตรวจสอบว่าเป็นคำสั่งใดใน aliases (nwPlayerProfile, nwpp, profile)
        if (command.getName().equalsIgnoreCase("nwPlayerProfile") ||
                command.getName().equalsIgnoreCase("nwpp") ||
                command.getName().equalsIgnoreCase("profile")) {

            // กรณีไม่มี argument
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    // เปิดโปรไฟล์ของตัวเอง
                    playerProfile.openMenu(player, player);
                    return true;
                } else {
                    sender.sendMessage(ColorUtils.translateColorCodes("<red>Only players can use this command!"));
                    return true;
                }
            }

            // กรณีมี 1 argument
            if (args.length == 1) {
                // คำสั่ง reload
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission(Utils.Permission)) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            Utils.loadConfig();
                            ColorUtils.sendMessage(player, Utils.MsgReload);
                        } else {
                            Utils.loadConfig();
                            sender.sendMessage(ColorUtils.translateColorCodes("<green>Config reloaded!"));
                        }
                        return true;
                    } else {
                        sender.sendMessage(ColorUtils.translateColorCodes("<red>You don't have permission!"));
                        return true;
                    }
                }
                // ถ้าไม่ใช่ reload ให้ถือว่าเป็นชื่อผู้เล่น
                else {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (!sender.hasPermission(Utils.PermissionOpen)) {
                            ColorUtils.sendMessage(player, "<red>You don't have permission!");
                            return true;
                        }

                        Player target = Bukkit.getPlayer(args[0]);
                        if (target != null) {
                            playerProfile.openMenu(player, target);
                            return true;
                        } else {
                            ColorUtils.sendMessage(player, "<red>Player not found: " + args[0]);
                            return true;
                        }
                    } else {
                        sender.sendMessage(ColorUtils.translateColorCodes("<red>Only players can use this command!"));
                        return true;
                    }
                }
            }

            // กรณีมีมากกว่า 1 argument
            if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("open")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (!sender.hasPermission(Utils.PermissionOpen)) {
                            ColorUtils.sendMessage(player, "<red>You don't have permission!");
                            return true;
                        }

                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            playerProfile.openMenu(player, target);
                            return true;
                        } else {
                            ColorUtils.sendMessage(player, "<red>Player not found: " + args[1]);
                            return true;
                        }
                    } else {
                        sender.sendMessage(ColorUtils.translateColorCodes("<red>Only players can use this command!"));
                        return true;
                    }
                }
            }

            // แสดงวิธีใช้ถ้าไม่ตรงกับเงื่อนไขใดๆ
            sender.sendMessage(ColorUtils.translateColorCodes(
                    "<yellow>Usage:\n" +
                            "/nwpp <player> - Open player profile\n" +
                            "/nwpp reload - Reload config\n" +
                            "/nwpp open <player> - Open player profile (alternative)"
            ));
            return true;
        }
        return false;
    }
}