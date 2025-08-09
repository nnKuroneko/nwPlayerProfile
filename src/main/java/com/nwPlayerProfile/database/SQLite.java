package com.nwPlayerProfile.database;

import com.nwPlayerProfile.NwPlayerProfile;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map
import java.util.UUID;
import java.util.logging.Level;

public class SQLite extends DatabaseManager {

    private Connection connection;

    public SQLite(NwPlayerProfile plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        // --- ส่วนที่แก้ไข: ตรวจสอบและสร้างโฟลเดอร์ก่อน ---
        File pluginDataFolder = plugin.getDataFolder(); // ได้รับ File object สำหรับ plugins/nwPlayerProfile
        if (!pluginDataFolder.exists()) {
            // หากโฟลเดอร์ยังไม่มี ให้สร้างขึ้นมา
            if (pluginDataFolder.mkdirs()) { // ใช้ mkdirs() เพื่อสร้างโฟลเดอร์แม่ทั้งหมดที่จำเป็น
                plugin.getLogger().log(Level.INFO, "Created plugin data folder: " + pluginDataFolder.getAbsolutePath());
            } else {
                plugin.getLogger().log(Level.SEVERE, "Failed to create plugin data folder: " + pluginDataFolder.getAbsolutePath());
                return; // หากสร้างโฟลเดอร์ไม่ได้ ก็ไม่ต้องไปต่อ
            }
        }
        // --- สิ้นสุดส่วนที่แก้ไข ---

        File databaseFile = new File(pluginDataFolder, "playerdata.db"); // ใช้ pluginDataFolder ที่เพิ่งตรวจสอบ/สร้าง
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create SQLite database file: " + e.getMessage());
                return; // หากสร้างไฟล์ไม่ได้ ก็ไม่ต้องไปต่อ
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath()); // ใช้ absolute path เพื่อความแน่ใจ
            log(Level.INFO, "SQLite database loaded successfully.");
            createTables();
        } catch (SQLException ex) {
            log(Level.SEVERE, "SQLite exception on connection: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            log(Level.SEVERE, "SQLite JDBC driver not found! " + ex.getMessage());
        }
    }

    private void createTables() {
        // เปลี่ยน Primary Key ให้เป็น (uuid, slot_id) เพื่อรองรับหลายช่อง
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_badges (" +
                "uuid VARCHAR(36) NOT NULL," +
                "slot_id VARCHAR(255) NOT NULL," + // เพิ่ม slot_id
                "selected_badge_id VARCHAR(255) NULL," +
                "PRIMARY KEY (uuid, slot_id)" + // ตั้งค่า Primary Key เป็น composite key
                ");";
        try (PreparedStatement ps = connection.prepareStatement(createTableSQL)) {
            ps.executeUpdate();
            log(Level.INFO, "Table 'player_badges' created or already exists.");
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error creating table 'player_badges': " + ex.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log(Level.INFO, "SQLite database closed successfully.");
            }
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error closing SQLite connection: " + ex.getMessage());
        }
    }

    @Override
    public Map<String, String> getAllSelectedBadges(UUID playerUUID) {
        Map<String, String> badges = new HashMap<>();
        String sql = "SELECT slot_id, selected_badge_id FROM player_badges WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { // ใช้ while เพื่ออ่านทุกแถว
                String slotId = rs.getString("slot_id");
                String selectedBadgeId = rs.getString("selected_badge_id");
                badges.put(slotId, selectedBadgeId);
            }
        } catch (SQLException ex) {
            log(Level.SEVERE, "Error getting all badge data for " + playerUUID + ": " + ex.getMessage());
        }
        return badges;
    }

    @Override
    public void saveSelectedBadge(UUID playerUUID, String slotId, String badgeId) {
        String sql = "INSERT OR REPLACE INTO player_badges (uuid, slot_id, selected_badge_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, slotId);
            ps.setString(3, badgeId); // สามารถเป็น null ได้
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
        // Not supported in SQLite
    }

    @Override
    public Map<String, String> getPlayerEquipment(UUID playerUUID) {
        return new HashMap<>();
    }

    @Override
    public void savePlayerCosmetic(UUID playerUUID, String slot, String itemId) {
        // Not supported in SQLite
    }

    @Override
    public Map<String, String> getPlayerCosmetics(UUID playerUUID) {
        return new HashMap<>();
    }

    @Override
    public void setPlayerOffline(UUID playerUUID) {

    }

    @Override
    public void setPlayerOnline(UUID playerUUID, String serverName) {

    }


}