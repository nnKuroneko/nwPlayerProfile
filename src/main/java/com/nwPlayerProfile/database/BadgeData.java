package com.nwPlayerProfile.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BadgeData {
    private final UUID playerUUID;
    // Map: slotId -> selectedBadgeId (สามารถเป็น null ได้ถ้ายังไม่เลือก)
    private final Map<String, String> selectedBadges;

    public BadgeData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.selectedBadges = new HashMap<>();
    }

    public BadgeData(UUID playerUUID, Map<String, String> selectedBadges) {
        this.playerUUID = playerUUID;
        this.selectedBadges = selectedBadges != null ? new HashMap<>(selectedBadges) : new HashMap<>();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    // ดึง Badge ID สำหรับ slot ที่เฉพาะเจาะจง
    public String getSelectedBadgeId(String slotId) {
        return selectedBadges.get(slotId);
    }

    // ดึง Map ของ Badge ทั้งหมด
    public Map<String, String> getAllSelectedBadges() {
        return new HashMap<>(selectedBadges); // คืนค่าสำเนาเพื่อป้องกันการแก้ไขโดยตรง
    }

    // ตั้งค่า Badge ID สำหรับ slot ที่เฉพาะเจาะจง
    public void setSelectedBadgeId(String slotId, String badgeId) {
        if (badgeId == null) {
            selectedBadges.remove(slotId); // ลบถ้าเป็น null
        } else {
            selectedBadges.put(slotId, badgeId);
        }
    }
}