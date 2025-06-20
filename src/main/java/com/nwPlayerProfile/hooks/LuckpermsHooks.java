package com.nwPlayerProfile.hooks;

import com.nwPlayerProfile.NwPlayerProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LuckpermsHooks {

    private static LuckPerms luckPermsAPI;
    private static Map<String, String> groupPlaceholders;

    public static void setup(Map<String, String> groupPlaceholders) {
        LuckpermsHooks.groupPlaceholders = groupPlaceholders;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsAPI = provider.getProvider();
        } else {
            NwPlayerProfile.getPlugin(NwPlayerProfile.class).getLogger().warning("LuckPerms API not found!");
            luckPermsAPI = null;
        }
    }

    public static String getPlayerGroupPlaceholder(Player player) {
        if (player == null || luckPermsAPI == null || groupPlaceholders == null) {
            return "";
        }

        try {
            User user = luckPermsAPI.getPlayerAdapter(Player.class).getUser(player);
            if (user != null) {
                String primaryGroup = user.getPrimaryGroup();
                if (groupPlaceholders.containsKey(primaryGroup.toLowerCase())) {
                    return groupPlaceholders.get(primaryGroup.toLowerCase());
                } else {
                    Bukkit.getLogger().info("[nwPlayerProfile Debug] Group '" + primaryGroup + "' not found in config groups."); // เพิ่ม Log กรณีไม่พบกลุ่มใน config
                }
            }
        } catch (Exception e) {
            NwPlayerProfile.getPlugin(NwPlayerProfile.class).getLogger().warning("Error while fetching LuckPerms group for " + player.getName() + ": " + e.getMessage());
        }

        return "";
    }
}