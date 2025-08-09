package com.nwPlayerProfile.core;

// Import ที่จำเป็น
import org.bukkit.Bukkit; // ต้องมีสำหรับการใช้ Bukkit.getItemFactory()
import org.bukkit.ChatColor; // ต้องมีสำหรับการใช้ ChatColor.translateAlternateColorCodes
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // ยังคงมีอยู่ถ้าคุณใช้ในเมธอดอื่น

public class ItemBuilder {
    private final ItemStack item; // เก็บ ItemStack ที่กำลังถูก build
    private ItemMeta currentMeta; // เก็บ ItemMeta ที่กำลังถูกแก้ไข

    public ItemBuilder() {
        this(new ItemStack(Material.AIR)); // เรียก constructor ที่รับ ItemStack
    }

    public ItemBuilder(ItemStack baseItem) {
        if (baseItem == null) {
            this.item = new ItemStack(Material.AIR);
        } else {
            this.item = baseItem.clone(); // Clone เพื่อป้องกันการแก้ไข ItemStack ต้นฉบับโดยตรง
        }
        // ดึง ItemMeta ออกมาเพื่อแก้ไข และเก็บไว้ใน currentMeta
        this.currentMeta = this.item.hasItemMeta() ? this.item.getItemMeta().clone() : Bukkit.getItemFactory().getItemMeta(this.item.getType());

        // ตรวจสอบอีกครั้งว่า currentMeta ไม่เป็น null เผื่อกรณีที่ไม่สามารถดึง meta ได้
        if (this.currentMeta == null) {
            this.currentMeta = Bukkit.getItemFactory().getItemMeta(this.item.getType());
        }
    }

    public ItemBuilder setMaterial(Material material) {
        if (material == null) {
            material = Material.AIR;
        }
        item.setType(material);
        // เนื่องจาก Material เปลี่ยน, ItemMeta อาจต้องถูกสร้างใหม่หรือดึงใหม่
        // แต่ในกรณีทั่วไป การเปลี่ยน Material จะ reset meta ด้วย
        // เราจะเชื่อว่า ItemMeta ที่ถูก set ทีหลังจะทับซ้อน
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        if (currentMeta != null) {
            // *** สำคัญ: แปลงรหัสสีที่นี่! ***
            currentMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            // ไม่ต้อง setItemMeta ที่นี่ เพราะเราจะ set ทีเดียวใน build()
        }
        return this;
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        if (currentMeta != null) {
            currentMeta.setCustomModelData(customModelData);
            // ไม่ต้อง setItemMeta ที่นี่
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (currentMeta != null) {
            List<String> translatedLore = new ArrayList<>();
            if (lore != null) {
                for (String line : lore) {
                    // *** สำคัญ: แปลงรหัสสีที่นี่สำหรับแต่ละบรรทัดใน Lore! ***
                    translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            currentMeta.setLore(translatedLore);
            // ไม่ต้อง setItemMeta ที่นี่
        }
        return this;
    }

    public ItemBuilder hideAttribute() {
        if (currentMeta != null) {
            currentMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            // ไม่ต้อง setItemMeta ที่นี่
        }
        return this;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        // หากมีการ set ItemMeta โดยตรงจากภายนอก เราจะใช้ตัวนั้น
        this.currentMeta = meta;
        // ไม่ต้อง setItemMeta ที่นี่
        return this;
    }

    public ItemMeta getItemMeta() {
        return currentMeta; // คืนค่า ItemMeta ที่เรากำลังแก้ไขอยู่
    }

    // เมธอด build() จะเป็นตัวที่นำ ItemMeta ที่ถูกแก้ไขทั้งหมดกลับไปใส่ใน ItemStack
    public ItemStack build() {
        // *** สำคัญ: นำ ItemMeta ที่แก้ไขแล้วกลับไป set ให้กับ ItemStack ก่อนคืนค่า ***
        if (currentMeta != null) {
            item.setItemMeta(currentMeta);
        }
        return item;
    }
}