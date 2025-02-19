package com.nwPlayerProfile.core;

import com.nwPlayerProfile.NwPlayerProfile;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static String Name;
    public static Integer Rows;
    public static String MsgReload;
    public static String MsgNotFoundUser;
    public static String MsgOpenGuiUser;
    public static final Map<CosmeticSlot, Integer> SlotMap = new HashMap<>();
    public static final Map<String, Integer> ArmorSlotMap = new HashMap<>();

    // เพิ่มการดึงค่าจาก config สำหรับ "no-fill"
    public static String NoFillDisplay;
    public static String Permission;
    public static Material NoFillMaterial;
    public static int NoFillCustomModelData;
    public static List<String> NoFillLore;
    public static String[] NoFillActions;

    static {
        // โหลดค่า Name และ Row จาก config.yml
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        Utils.Name = plugin.getConfig().getString("gui.name");
        Utils.MsgReload = plugin.getConfig().getString("message.msg-reload");
        Utils.MsgNotFoundUser = plugin.getConfig().getString("message.msg-not-found-user");
        Utils.MsgOpenGuiUser = plugin.getConfig().getString("message.msg-open-gui-user");
        Utils.Permission = plugin.getConfig().getString("setting.permission");

        String rowString = plugin.getConfig().getString("gui.rows");
        if (rowString != null) {
            try {
                Utils.Rows = Integer.parseInt(rowString);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid row value in config: " + rowString);
            }
        } else {
            plugin.getLogger().warning("gui.rows not found in config.");
        }

        // โหลด slot จาก config.yml และเก็บใน SlotMap
        for (CosmeticSlot slotType : CosmeticSlot.values().values()) {
            int slot = plugin.getConfig().getInt("icons." + slotType.getName().toLowerCase() + ".slot", -1); // ค่า default -1 ถ้าไม่มี
            if (slot != -1) {
                SlotMap.put(slotType, slot);
            }
//            else {
//                plugin.getLogger().warning("Missing slot value for: " + slotType.getName());
//            }
        }

        NoFillDisplay = plugin.getConfig().getString("icons.item.no-fill.display", "&e");
        NoFillMaterial = Material.getMaterial(plugin.getConfig().getString("icons.item.no-fill.material", "SUGAR"));
        NoFillCustomModelData = plugin.getConfig().getInt("icons.item.no-fill.custom_model_data", 130);
        NoFillLore = plugin.getConfig().getStringList("icons.item.no-fill.lore");
        NoFillActions = plugin.getConfig().getStringList("icons.item.no-fill.actions").toArray(new String[0]);

        ArmorSlotMap.put("helmet", plugin.getConfig().getInt("icons.player-helmet.slot", 7)); // ค่า default 7 ถ้าไม่มี
        ArmorSlotMap.put("chestplate", plugin.getConfig().getInt("icons.player-chestplate.slot", 8)); // ค่า default 8 ถ้าไม่มี
        ArmorSlotMap.put("leggings", plugin.getConfig().getInt("icons.player-leggings.slot", 9)); // ค่า default 9 ถ้าไม่มี
        ArmorSlotMap.put("boots", plugin.getConfig().getInt("icons.player-boots.slot", 10)); // ค่า default 10 ถ้าไม่มี

    }

    public static int getSlot(CosmeticSlot slotType) {
        return SlotMap.getOrDefault(slotType, -1); // คืนค่า -1 ถ้าไม่มีข้อมูล
    }

    // ฟังก์ชั่นดึงชุดเกราะจากผู้เล่น
    public static void updateArmorSet(PlayerInventory inventory) {
        armorSet.put("helmet", inventory.getHelmet() != null ? inventory.getHelmet() : setFromConfig(NwPlayerProfile.getPlugin(NwPlayerProfile.class), "icons.player-helmet").build());
        armorSet.put("chestplate", inventory.getChestplate() != null ? inventory.getChestplate() : setFromConfig(NwPlayerProfile.getPlugin(NwPlayerProfile.class), "icons.player-chestplate").build());
        armorSet.put("leggings", inventory.getLeggings() != null ? inventory.getLeggings() : setFromConfig(NwPlayerProfile.getPlugin(NwPlayerProfile.class), "icons.player-leggings").build());
        armorSet.put("boots", inventory.getBoots() != null ? inventory.getBoots() : setFromConfig(NwPlayerProfile.getPlugin(NwPlayerProfile.class), "icons.player-boots").build());
    }


    // ฟังก์ชั่นดึงค่า slot จาก ArmorSlotMap
    public static int getArmorSlot(String part) {
        return ArmorSlotMap.getOrDefault(part, -1); // คืนค่า slot สำหรับชุดเกราะ ถ้าไม่มีให้คืนค่า -1
    }

    public static Map<String, ItemStack> armorSet = new HashMap<>();
    public static ItemStack getArmor(String part) {
        return armorSet.getOrDefault(part, null); // คืนค่าชุดเกราะในแต่ละส่วน ถ้าไม่มีให้คืนค่า null
    }

    public static List<Integer> parseSlotRange(String slotString) {
        List<Integer> slots = new ArrayList<>();

        if (slotString.contains("-")) {
            // ถ้าเป็นช่วง เช่น "51-54"
            String[] parts = slotString.split("-");
            if (parts.length == 2) {
                try {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);

                    if (start <= end) {
                        for (int i = start; i <= end; i++) {
                            slots.add(i);
                        }
                    }
                } catch (NumberFormatException e) {
//                    Bukkit.getLogger().warning("Invalid slot range: " + slotString);
                }
            }
        } else {
            // ถ้าเป็น slot เดียว เช่น "51"
            try {
                slots.add(Integer.parseInt(slotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Invalid slot: " + slotString);
            }
        }

        return slots;
    }


    public static final Map<Integer, CustomGUIItem> CustomItems = new HashMap<>();
    public static void loadCustomItems() {
        CustomItems.clear(); // รีเซ็ตก่อนโหลดใหม่
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("icons.item");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = "icons.item." + key;
            String slotString = plugin.getConfig().getString(path + ".slot", "-1");

            List<Integer> slots = parseSlotRange(slotString); // แปลง slot ให้อยู่ในรูป List<Integer>

            if (slots.isEmpty()) continue; // ถ้า slot ไม่ถูกต้องให้ข้าม

            String display = plugin.getConfig().getString(path + ".display", "&eไอเทมใหม่");
            Material material = Material.getMaterial(plugin.getConfig().getString(path + ".material", "STONE"));
            int customModelData = plugin.getConfig().getInt(path + ".custom_model_data", 0);
            List<String> lore = plugin.getConfig().getStringList(path + ".lore");
            List<String> actions = plugin.getConfig().getStringList(path + ".actions"); // ดึง actions จาก config

            // สร้าง ItemStack
            ItemStack item = new ItemBuilder()
                    .setMaterial(material)
                    .setDisplayName(ColorUtils.translateColorCodes(display))
                    .setCustomModelData(customModelData)
                    .setLore(ColorUtils.translateColorCodes(lore))
                    .hideAttribute()
                    .build();

            // ใส่ไอเทมลงใน CustomGUIItem พร้อม actions
            for (int slot : slots) {
                CustomItems.put(slot, new CustomGUIItem(item, actions)); // ส่งทั้ง ItemStack และ actions
            }
        }
    }

    public static ItemBuilder setFromConfig(NwPlayerProfile plugin, String path) {
        String materialName = plugin.getConfig().getString(path + ".material");
        String displayName = plugin.getConfig().getString(path + ".display");
        int customModelData = plugin.getConfig().getInt(path + ".custom_model_data", 0);

        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.STONE; // ตั้งค่าเริ่มต้นถ้า material ไม่ถูกต้อง
        }

        return new ItemBuilder()
                .setMaterial(material)
                .setDisplayName(ColorUtils.translateColorCodes(displayName))
                .setCustomModelData(customModelData);
    }

    public static synchronized void loadConfig() {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        plugin.reloadConfig();

        // โหลดค่า Name และ Row จาก config.yml
        Name = plugin.getConfig().getString("gui.name");
        MsgReload = plugin.getConfig().getString("message.msg-reload");
        MsgNotFoundUser = plugin.getConfig().getString("message.msg-not-found-user");
//        MsgOpenGuiUser = plugin.getConfig().getString("message.msg-open-gui-user");
        Permission = plugin.getConfig().getString("setting.permission");

        String rowString = plugin.getConfig().getString("gui.rows");
        if (rowString != null) {
            try {
                Rows = Integer.parseInt(rowString);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid row value in config: " + rowString);
            }
        } else {
            plugin.getLogger().warning("gui.rows not found in config.");
        }

        // โหลด slot จาก config.yml และเก็บใน SlotMap
        synchronized (SlotMap) {
            SlotMap.clear(); // ล้างข้อมูลเก่า
            for (CosmeticSlot slotType : CosmeticSlot.values().values()) {
                int slot = plugin.getConfig().getInt("icons." + slotType.getName().toLowerCase() + ".slot", -1);
                if (slot != -1) {
                    SlotMap.put(slotType, slot);
                }
            }
        }

        loadCustomItems();

        NoFillDisplay = plugin.getConfig().getString("icons.item.no-fill.display", "&e");
        NoFillMaterial = Material.getMaterial(plugin.getConfig().getString("icons.item.no-fill.material", "SUGAR"));
        NoFillCustomModelData = plugin.getConfig().getInt("icons.item.no-fill.custom_model_data", 130);
        NoFillLore = plugin.getConfig().getStringList("icons.item.no-fill.lore");
        NoFillActions = plugin.getConfig().getStringList("icons.item.no-fill.actions").toArray(new String[0]);

        synchronized (ArmorSlotMap) {
            ArmorSlotMap.clear();
            ArmorSlotMap.put("helmet", plugin.getConfig().getInt("icons.player-helmet.slot", 7));
            ArmorSlotMap.put("chestplate", plugin.getConfig().getInt("icons.player-chestplate.slot", 8));
            ArmorSlotMap.put("leggings", plugin.getConfig().getInt("icons.player-leggings.slot", 9));
            ArmorSlotMap.put("boots", plugin.getConfig().getInt("icons.player-boots.slot", 10));
        }

        plugin.saveConfig();
        plugin.getLogger().warning("Success config: nwPlayerProfile loaded.");
    }


}
