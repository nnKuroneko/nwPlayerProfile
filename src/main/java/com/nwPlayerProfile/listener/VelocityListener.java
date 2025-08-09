package com.nwPlayerProfile.listener;

import com.hibiscusmc.hmccosmetics.util.HMCCPlayerUtils;
import com.nwPlayerProfile.database.DatabaseManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;


import java.util.Map;
import java.util.UUID;

public class VelocityListener {

    private final DatabaseManager db;

    public VelocityListener(DatabaseManager db) {
        this.db = db;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent e) {
        Player player = e.getPlayer();
        String serverName = e.getServer().getServerInfo().getName();
        UUID uuid = player.getUniqueId();

        db.setPlayerOnline(uuid, serverName);

        Map<String, String> eqMap  = db.getPlayerEquipment(uuid);
        Map<String, String> cosMap = db.getPlayerCosmetics(uuid);
        Map<String, String> bdgMap = db.getAllSelectedBadges(uuid);

        db.savePlayerEquipment(
                uuid,
                eqMap.getOrDefault("helmet", ""),
                eqMap.getOrDefault("chestplate", ""),
                eqMap.getOrDefault("leggings", ""),
                eqMap.getOrDefault("boots", ""),
                serverName,
                true
        );
        cosMap.forEach((slot, itemId) -> db.savePlayerCosmetic(uuid, slot, itemId));
        bdgMap.forEach((key, val) -> {
            if (!key.endsWith("_server_name") && !key.endsWith("_online_status")) {
                db.saveSelectedBadge(uuid, key, val);
            }
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        db.setPlayerOffline(uuid);
    }
}