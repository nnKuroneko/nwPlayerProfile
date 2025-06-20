package com.nwPlayerProfile.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;

public class CitizensAPI {

    private static boolean citizensEnabled = false;

    // ตรวจสอบว่าปลั๊กอิน Citizens ถูกโหลดหรือไม่
    public static void initialize() {
        Plugin citizensPlugin = Bukkit.getPluginManager().getPlugin("Citizens");
        if (citizensPlugin != null && citizensPlugin.isEnabled()) {
            citizensEnabled = true;
            Bukkit.getLogger().info("Citizens plugin found! Enabling Citizens API integration.");
        } else {
            citizensEnabled = false;
            Bukkit.getLogger().info("Citizens plugin not found. Citizens API integration disabled.");
        }
    }

    // ตรวจสอบว่า Citizens ถูกเปิดใช้งานอยู่หรือไม่
    public static boolean isCitizensEnabled() {
        return citizensEnabled;
    }

    // ตรวจสอบว่าเป็น NPC ของ Citizens หรือไม่
    public static boolean isNPC(Entity entity) {
        if (citizensEnabled) {
            try {
                // เรียกใช้ CitizensAPI.getNPCRegistry().isNPC(entity) โดยตรง
                return net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity);
            } catch (NoClassDefFoundError e) {
                // กรณีที่เกิด NoClassDefFoundError ระหว่างรันไทม์ (ไม่น่าจะเกิดขึ้นถ้า citizensEnabled เป็น true)
                Bukkit.getLogger().warning("Error checking Citizens NPC. Disabling Citizens integration: " + e.getMessage());
                citizensEnabled = false; // ปิดการใช้งานเพื่อป้องกัน error ซ้ำ
            } catch (Throwable e) {
                // ดักจับข้อผิดพลาดอื่น ๆ ที่อาจเกิดขึ้น
                Bukkit.getLogger().warning("Unexpected error during Citizens NPC check: " + e.getMessage());
            }
        }
        return false;
    }
}