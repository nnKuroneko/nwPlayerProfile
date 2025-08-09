package com.nwPlayerProfile.database;

import com.nwPlayerProfile.NwPlayerProfile;
import java.util.UUID;
import java.util.Map; // Import Map
import java.util.logging.Level;

public abstract class DatabaseManager {

    protected NwPlayerProfile plugin;

    public DatabaseManager(NwPlayerProfile plugin) {
        this.plugin = plugin;
    }

    public abstract void load();
    public abstract void close();

    // REMOVE THIS LINE: public abstract BadgeData getBadgeData(UUID playerUUID);
    // It's no longer needed as we have getAllSelectedBadges() now.

    public abstract Map<String, String> getAllSelectedBadges(UUID playerUUID);
    public abstract void saveSelectedBadge(UUID playerUUID, String slotId, String badgeId);
    public abstract void deleteSelectedBadge(UUID playerUUID, String slotId);
    public abstract void deleteAllBadges(UUID playerUUID);

    // Equipment
    public abstract void savePlayerEquipment(UUID playerUUID,
                                    String helmet, String chestplate,
                                    String leggings, String boots,
                                    String serverName, boolean onlineStatus);
    public abstract Map<String, String> getPlayerEquipment(UUID playerUUID);

    // Cosmetic
    public abstract void savePlayerCosmetic(UUID playerUUID, String slot, String itemId);
    public abstract Map<String, String> getPlayerCosmetics(UUID playerUUID);

    public boolean isMySQLEnabled() {
        return plugin.getConfig().getBoolean("database.enabled", false);
    }
    public abstract void setPlayerOnline(UUID playerUUID, String serverName);
    public abstract void setPlayerOffline(UUID playerUUID);


    public void log(Level level, String msg) {
        plugin.getLogger().log(level, msg);
    }

}