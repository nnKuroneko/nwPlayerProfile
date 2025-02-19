package com.nwPlayerProfile.inventory;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;

public class InventoryManager {
    private static final HashMap<UUID, InventoryStage> playerStages = new HashMap<>();

    public static void setStage(Player player, InventoryStage stage) {
        playerStages.put(player.getUniqueId(), stage);
    }

    public static InventoryStage getStage(Player player) {
        return playerStages.getOrDefault(player.getUniqueId(), InventoryStage.ACTIVE);
    }

    public static void removeStage(Player player) {
        playerStages.remove(player.getUniqueId());
    }
}
