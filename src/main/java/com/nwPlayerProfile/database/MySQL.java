package com.nwPlayerProfile.database;

import com.nwPlayerProfile.NwPlayerProfile;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MySQL extends DatabaseManager {
    private Connection connection;

    public MySQL(NwPlayerProfile plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        if (!plugin.getConfig().getBoolean("database.enabled")) {
            plugin.getLogger().warning("MySQL is disabled in config.yml.");
            return;
        }

        String host = plugin.getConfig().getString("database.host");
        String port = plugin.getConfig().getString("database.port", "3306");
        String db = plugin.getConfig().getString("database.name");
        String user = plugin.getConfig().getString("database.username");
        String pass = plugin.getConfig().getString("database.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // เพิ่ม connection timeout และพารามิเตอร์อื่นๆ
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                    "?useSSL=false" +
                    "&autoReconnect=true" +
                    "&createDatabaseIfNotExist=true" +
                    "&connectTimeout=5000" +
                    "&socketTimeout=30000";


            connection = DriverManager.getConnection(url, user, pass);
            log(Level.INFO, "MySQL connected successfully.");
            createTables();
        } catch (ClassNotFoundException e) {
            log(Level.SEVERE, "MySQL JDBC driver not found!");
        } catch (SQLException e) {
            log(Level.SEVERE, "MySQL connection failed: " + e.getMessage());
        } catch (Exception e) {
            log(Level.SEVERE, "Unexpected error during MySQL connection: " + e.getMessage());
        }
    }

    private void createTables() {
        String badgeTableSQL =
                "CREATE TABLE IF NOT EXISTS player_badges (" +
                        " uuid VARCHAR(36)," +
                        " slot_id VARCHAR(255)," +
                        " selected_badge_id VARCHAR(255)," +
                        " server_name VARCHAR(255)," +
                        " online_status BOOLEAN DEFAULT FALSE," +
                        " updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        " PRIMARY KEY (uuid, slot_id)" +
                        ");";

        String equipmentTableSQL =
                "CREATE TABLE IF NOT EXISTS player_equipment (" +
                        " uuid VARCHAR(36) PRIMARY KEY," +
                        " helmet VARCHAR(255)," +
                        " chestplate VARCHAR(255)," +
                        " leggings VARCHAR(255)," +
                        " boots VARCHAR(255)," +
                        " server_name VARCHAR(255)," +
                        " online_status BOOLEAN DEFAULT FALSE," +
                        " updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ");";

        String cosmeticTableSQL =
                "CREATE TABLE IF NOT EXISTS player_cosmetics (" +
                        " uuid VARCHAR(36)," +
                        " slot VARCHAR(255)," +
                        " item_id VARCHAR(255)," +
                        " server_name VARCHAR(255)," +
                        " online_status BOOLEAN DEFAULT FALSE," +
                        " updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        " PRIMARY KEY (uuid, slot)" +
                        ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(badgeTableSQL);
            stmt.executeUpdate(equipmentTableSQL);
            stmt.executeUpdate(cosmeticTableSQL);
            log(Level.INFO, "All MySQL tables created successfully.");
        } catch (SQLException e) {
            log(Level.SEVERE, "Failed to create tables: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> getAllSelectedBadges(UUID playerUUID) {
        Map<String, String> badges = new HashMap<>();
        String sql = "SELECT slot_id, selected_badge_id FROM player_badges WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String slotId = rs.getString("slot_id");
                String selectedBadgeId = rs.getString("selected_badge_id");
                String serverName = rs.getString("server_name");
                boolean onlineStatus = rs.getBoolean("online_status");
                badges.put(slotId, selectedBadgeId);
                badges.put(slotId + "_server_name", serverName);
                badges.put(slotId + "_online_status", String.valueOf(onlineStatus));
            }
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error getting all badge data for " + playerUUID + ": " + ex.getMessage());
        }
        return badges;
    }

    @Override
    public void saveSelectedBadge(UUID playerUUID, String slotId, String badgeId) {
        String sql = "INSERT INTO player_badges (uuid, slot_id, selected_badge_id, server_name, online_status) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE selected_badge_id = VALUES(selected_badge_id), " +
                " server_name = VALUES(server_name), online_status = VALUES(online_status), " +
                " updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, slotId);
            ps.setString(3, badgeId);
            ps.setString(4, plugin.getConfig().getString("server.name", "default_server"));
            ps.setBoolean(5, true); // Example: set online_status to true
            ps.setString(6, badgeId); // For UPDATE
            ps.setString(7, plugin.getConfig().getString("server.name", "default_server"));
            ps.setBoolean(8, true);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error saving badge data for " + playerUUID + ", slot " + slotId + ": " + ex.getMessage());
        }
    }

    @Override
    public void deleteSelectedBadge(UUID playerUUID, String slotId) {
        String sql = "DELETE FROM player_badges WHERE uuid = ? AND slot_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, slotId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error deleting badge data for " + playerUUID + ", slot " + slotId + ": " + ex.getMessage());
        }
    }

    @Override
    public void deleteAllBadges(UUID playerUUID) {
        String sql = "DELETE FROM player_badges WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error deleting all badge data for " + playerUUID + ": " + ex.getMessage());
        }
    }

    @Override
    public void savePlayerEquipment(UUID playerUUID,
                                    String helmet, String chestplate,
                                    String leggings, String boots,
                                    String serverName, boolean onlineStatus) {
        String sql = "INSERT INTO player_equipment " +
                "(uuid, helmet, chestplate, leggings, boots, server_name, online_status) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                " helmet=VALUES(helmet), chestplate=VALUES(chestplate), leggings=VALUES(leggings), boots=VALUES(boots), " +
                " server_name=VALUES(server_name), online_status=VALUES(online_status), updated_at=CURRENT_TIMESTAMP";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, helmet);
            ps.setString(3, chestplate);
            ps.setString(4, leggings);
            ps.setString(5, boots);
            ps.setString(6, serverName);
            ps.setBoolean(7, onlineStatus);
            ps.executeUpdate();
        } catch (SQLException e) {
            log(Level.SEVERE, "Failed to save equipment for " + playerUUID + ": " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> getPlayerEquipment(UUID playerUUID) {
        Map<String, String> equipment = new HashMap<>();
        String sql = "SELECT helmet, chestplate, leggings, boots, server_name, online_status FROM player_equipment WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                equipment.put("helmet", rs.getString("helmet"));
                equipment.put("chestplate", rs.getString("chestplate"));
                equipment.put("leggings", rs.getString("leggings"));
                equipment.put("boots", rs.getString("boots"));
                equipment.put("server_name", rs.getString("server_name"));
                equipment.put("online_status", String.valueOf(rs.getBoolean("online_status")));
            }
        } catch (SQLException e) {
            log(Level.SEVERE, "Failed to load equipment for " + playerUUID + ": " + e.getMessage());
        }
        return equipment;
    }

    @Override
    public void savePlayerCosmetic(UUID playerUUID, String slot, String itemId) {
        String sql = "INSERT INTO player_cosmetics (uuid, slot, item_id, server_name, online_status) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE item_id = VALUES(item_id), server_name = VALUES(server_name), " +
                " online_status = VALUES(online_status), updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, slot);
            ps.setString(3, itemId);
            ps.setString(4, plugin.getConfig().getString("server.name", "default_server"));
            ps.setBoolean(5, true);
            ps.executeUpdate();
        } catch (SQLException e) {
            log(Level.SEVERE, "Failed to save cosmetic for " + playerUUID + ", slot: " + slot + ": " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> getPlayerCosmetics(UUID playerUUID) {
        Map<String, String> cosmetics = new HashMap<>();
        String sql = "SELECT slot, item_id, server_name, online_status FROM player_cosmetics WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String slot = rs.getString("slot");
                cosmetics.put(slot, rs.getString("item_id"));
                cosmetics.put(slot + "_server_name", rs.getString("server_name"));
                cosmetics.put(slot + "_online_status", String.valueOf(rs.getBoolean("online_status")));
            }
        } catch (SQLException e) {
            log(Level.SEVERE, "Failed to load cosmetics for " + playerUUID + ": " + e.getMessage());
        }
        return cosmetics;
    }

    /**
     * Mark user offline in all 3 tables
     */
    @Override
    public void setPlayerOffline(UUID playerUUID) {
        String[] tables = {"player_equipment", "player_cosmetics", "player_badges"};
        for (String table : tables) {
            String sql = "UPDATE " + table +
                    " SET online_status = FALSE, updated_at = CURRENT_TIMESTAMP WHERE uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUUID.toString());
                ps.executeUpdate();
            } catch (SQLException ex) {
                log(Level.SEVERE, "Failed to set offline in " + table + " for " + playerUUID + ": " + ex.getMessage());
            }
        }
    }


    public void setPlayerOnline(UUID playerUUID, String serverName) {
        // ensure equipment row exists + set server/online
        String upsertEq = "INSERT INTO player_equipment (uuid, server_name, online_status) " +
                "VALUES (?, ?, TRUE) " +
                "ON DUPLICATE KEY UPDATE server_name=VALUES(server_name), online_status=TRUE, updated_at=CURRENT_TIMESTAMP";
        try (PreparedStatement ps = connection.prepareStatement(upsertEq)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, serverName);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log(Level.SEVERE, "Failed to set online in equipment for " + playerUUID + ": " + ex.getMessage());
        }

        // touch badges/cosmetics (เฉพาะที่มีแถวอยู่)
        String updBdg = "UPDATE player_badges SET server_name=?, online_status=TRUE, updated_at=CURRENT_TIMESTAMP WHERE uuid=?";
        String updCos = "UPDATE player_cosmetics SET server_name=?, online_status=TRUE, updated_at=CURRENT_TIMESTAMP WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(updBdg)) {
            ps.setString(1, serverName);
            ps.setString(2, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            log(Level.SEVERE, "Failed to set online in badges for " + playerUUID + ": " + ex.getMessage());
        }
        try (PreparedStatement ps = connection.prepareStatement(updCos)) {
            ps.setString(1, serverName);
            ps.setString(2, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            log(Level.SEVERE, "Failed to set online in cosmetics for " + playerUUID + ": " + ex.getMessage());
        }
    }



    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log(Level.INFO, "MySQL connection closed successfully.");
            }
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error closing MySQL connection: " + ex.getMessage());
        }
    }
}