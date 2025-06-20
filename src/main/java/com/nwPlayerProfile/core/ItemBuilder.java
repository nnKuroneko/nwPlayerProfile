package com.nwPlayerProfile.core;

import com.nwPlayerProfile.NwPlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack item;

    public ItemBuilder() {
        this.item = new ItemStack(Material.AIR); // Default to AIR
    }

    public ItemBuilder(ItemStack baseItem) { // Constructor ใหม่: สร้าง ItemBuilder ด้วย ItemStack ตั้งต้น
        if (baseItem == null) {
            this.item = new ItemStack(Material.AIR); // ป้องกัน NullPointerException หาก baseItem เป็น null
        } else {
            this.item = baseItem.clone(); // Clone เพื่อป้องกันการแก้ไข ItemStack ต้นฉบับโดยตรง
        }
    }

    public ItemBuilder setMaterial(Material material) {
        if (material == null) {
            material = Material.AIR;  // ใช้ AIR หากเป็น null
        }
        item.setType(material);
        return this;
    }


    public ItemBuilder setDisplayName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        return this;
    }



    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }

    // เพิ่ม method hideAttribute
    public ItemBuilder hideAttribute() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES); // เพิ่มการซ่อน attributes
            item.setItemMeta(meta);
        }
        return this;
    }


    public ItemStack build() {
        return item;
    }
}
