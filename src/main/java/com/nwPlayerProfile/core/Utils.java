package com.nwPlayerProfile.core;

import com.nwPlayerProfile.NwPlayerProfile;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.nwPlayerProfile.database.BadgeData;
import com.nwPlayerProfile.database.DatabaseManager;
import com.nwPlayerProfile.hooks.ItemsAdderHooks;
import com.nwPlayerProfile.hooks.LuckpermsHooks;
import com.nwPlayerProfile.hooks.NexoHooks;
import com.nwPlayerProfile.profile.BadgeManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;


public class Utils {

    public static String Name;
    public static Integer Rows;
    public static String MsgReload;
    public static Boolean isRightPlayer;
    public static String MsgNotFoundUser;
    public static String MsgOpenGuiUser;
    public static final Map<CosmeticSlot, Integer> SlotMap = new HashMap<>();
    public static final Map<String, Integer> ArmorSlotMap = new HashMap<>();
    public static final Map<String, String> GroupPlaceholders = new HashMap<>();

    // เพิ่มการดึงค่าจาก config สำหรับ "no-fill"
    public static String NoFillDisplay;
    public static String Permission;
    public static String PermissionOpen;
    public static Material NoFillMaterial;
    public static int NoFillCustomModelData;
    public static List<String> NoFillLore;
    public static String[] NoFillActions;
    public static String ActionType;

    public static NexoHooks nexoHooks;
    public static ItemsAdderHooks itemsAdderHooks;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static BadgeManager badgeManager; // Declare it static here
    private static final Map<UUID, Map<String, String>> selectedPlayerBadges = new HashMap<>();


    static {
        // โหลดค่า Name และ Row จาก config.yml
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        DatabaseManager dbManager = plugin.getDatabaseManager();
        Utils.nexoHooks = new NexoHooks(plugin); // Initialized NexoHooks ที่นี่
        Utils.badgeManager = new BadgeManager(plugin, dbManager);

        ActionType = plugin.getConfig().getString("setting.action", "RIGHT_CLICK").toUpperCase();
        Utils.Name = plugin.getConfig().getString("gui.name");
        Utils.MsgReload = plugin.getConfig().getString("message.msg-reload");
        Utils.isRightPlayer = plugin.getConfig().getBoolean("setting.rightplayer",true);
        Utils.MsgNotFoundUser = plugin.getConfig().getString("message.msg-not-found-user");
        Utils.MsgOpenGuiUser = plugin.getConfig().getString("message.msg-open-gui-user");
        Utils.Permission = plugin.getConfig().getString("setting.permission");
        Utils.PermissionOpen = plugin.getConfig().getString("setting.open");

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

        ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                String placeholder = groupsSection.getString(groupName);
                if (placeholder != null) {
                    GroupPlaceholders.put(groupName.toLowerCase(), placeholder);
                }
            }
        }

        // Setup LuckPerms Hooks
        LuckpermsHooks.setup(GroupPlaceholders);

    }

    public static String getParsedGuiTitle(Player player) {
        // 1. Replace your custom {groups} placeholder first
        String rawTitle = Utils.Name.replace("{groups}", Utils.getPlayerGroupPlaceholder(player));

        // 2. Now, pass the *entire* rawTitle through PlaceholderAPI for any other placeholders
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null) {
            return ColorUtils.translateColorCodes(rawTitle, player);
        } else {
            return ColorUtils.translateColorCodes(rawTitle, player); // Return as is if PlaceholderAPI is not enabled
        }
    }

    public static String getPlayerGroupPlaceholder(Player player) {
        String groupName = LuckpermsHooks.getPlayerGroupPlaceholder(player);
        if (!groupName.isEmpty()) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null) {
                String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, groupName);
                return parsedPlaceholder;
            } else {
                return groupName;
            }
        }
        return ""; // คืนค่าว่างเปล่าถ้าไม่พบกลุ่มหรือกลุ่มใน Config
    }

    public static int getSlot(CosmeticSlot slotType) {
        return SlotMap.getOrDefault(slotType, -1); // คืนค่า -1 ถ้าไม่มีข้อมูล
    }

    public static Map<String, ItemStack> getPlayerEquipmentFromMySQL(UUID playerUUID) {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        DatabaseManager db = plugin.getDatabaseManager();
        Map<String, ItemStack> equipment = new HashMap<>();

        if (db.isMySQLEnabled()) {
            Map<String, String> eqMap = db.getPlayerEquipment(playerUUID);

            if (eqMap != null && !eqMap.isEmpty()) {
                equipment.put("helmet", getItemStackFromString(eqMap.get("helmet"), "equipment.helmet"));
                equipment.put("chestplate", getItemStackFromString(eqMap.get("chestplate"), "equipment.chestplate"));
                equipment.put("leggings", getItemStackFromString(eqMap.get("leggings"), "equipment.leggings"));
                equipment.put("boots", getItemStackFromString(eqMap.get("boots"), "equipment.boots"));
            }
        }

        return equipment;
    }

    // เพิ่มเมธอดสำหรับดึงข้อมูล Cosmetic จาก MySQL
    public static Map<CosmeticSlot, ItemStack> getPlayerCosmeticsFromMySQL(UUID playerUUID) {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        DatabaseManager db = plugin.getDatabaseManager();
        Map<CosmeticSlot, ItemStack> cosmetics = new HashMap<>();

        if (db.isMySQLEnabled()) {
            Map<String, String> cosMap = db.getPlayerCosmetics(playerUUID);

            if (cosMap != null && !cosMap.isEmpty()) {
                for (Map.Entry<String, String> entry : cosMap.entrySet()) {
                    try {
                        CosmeticSlot slot = CosmeticSlot.valueOf(entry.getKey().toUpperCase());
                        ItemStack item = getItemStackFromString(entry.getValue(), "cosmetic." + entry.getKey());
                        cosmetics.put(slot, item);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid cosmetic slot in database: " + entry.getKey());
                    }
                }
            }
        }

        return cosmetics;
    }

    // แก้ไขเมธอด updateArmorSet เพื่อรองรับข้อมูลจาก MySQL
    public static void updateArmorSet(PlayerInventory inventory, Map<String, ItemStack> mysqlEquipment) {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        // ใช้ข้อมูลจาก MySQL ถ้ามี
        if (mysqlEquipment != null && !mysqlEquipment.isEmpty()) {
            armorSet.put("helmet", mysqlEquipment.getOrDefault("helmet",
                    getItemStackFromString(plugin.getConfig().getString("icons.player-helmet.material"),
                            "icons.player-helmet.material")));

            armorSet.put("chestplate", mysqlEquipment.getOrDefault("chestplate",
                    getItemStackFromString(plugin.getConfig().getString("icons.player-chestplate.material"),
                            "icons.player-chestplate.material")));

            armorSet.put("leggings", mysqlEquipment.getOrDefault("leggings",
                    getItemStackFromString(plugin.getConfig().getString("icons.player-leggings.material"),
                            "icons.player-leggings.material")));

            armorSet.put("boots", mysqlEquipment.getOrDefault("boots",
                    getItemStackFromString(plugin.getConfig().getString("icons.player-boots.material"),
                            "icons.player-boots.material")));
        } else {
            // ใช้ข้อมูลจาก inventory ถ้าไม่มีข้อมูลจาก MySQL
            ItemStack helmet = inventory.getHelmet();
            if (helmet == null) {
                String materialName = plugin.getConfig().getString("icons.player-helmet.material");
                helmet = getItemStackFromString(materialName, "icons.player-helmet.material");
            }
            armorSet.put("helmet", helmet);

            ItemStack chestplate = inventory.getChestplate();
            if (chestplate == null) {
                String materialName = plugin.getConfig().getString("icons.player-chestplate.material");
                chestplate = getItemStackFromString(materialName, "icons.player-chestplate.material");
            }
            armorSet.put("chestplate", chestplate);

            ItemStack leggings = inventory.getLeggings();
            if (leggings == null) {
                String materialName = plugin.getConfig().getString("icons.player-leggings.material");
                leggings = getItemStackFromString(materialName, "icons.player-leggings.material");
            }
            armorSet.put("leggings", leggings);

            ItemStack boots = inventory.getBoots();
            if (boots == null) {
                String materialName = plugin.getConfig().getString("icons.player-boots.material");
                boots = getItemStackFromString(materialName, "icons.player-boots.material");
            }
            armorSet.put("boots", boots);
        }
    }

    // ฟังก์ชั่นดึงชุดเกราะจากผู้เล่น

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

        if (slotString == null || slotString.isEmpty()) {
            return slots;
        }

        String cleanedSlotString = slotString.trim();

        // Check for list format: "[1,2,3]" or "1,2,3"
        if (cleanedSlotString.contains(",")) {
            // Remove brackets if present
            if (cleanedSlotString.startsWith("[") && cleanedSlotString.endsWith("]")) {
                cleanedSlotString = cleanedSlotString.substring(1, cleanedSlotString.length() - 1);
            }
            String[] parts = cleanedSlotString.split(",");
            for (String part : parts) {
                try {
                    slots.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Invalid slot in list: '" + part.trim() + "' in '" + slotString + "'.");
                }
            }
        } else if (cleanedSlotString.contains("-")) {
            // If it's a range like "51-54"
            String[] parts = cleanedSlotString.split("-");
            if (parts.length == 2) {
                try {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());

                    if (start <= end) {
                        for (int i = start; i <= end; i++) {
                            slots.add(i);
                        }
                    } else {
                        Bukkit.getLogger().warning("Invalid slot range: Start is greater than end in '" + slotString + "'.");
                    }
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Invalid slot range format in '" + slotString + "'.");
                }
            } else {
                Bukkit.getLogger().warning("Invalid slot range format in '" + slotString + "'. Expected 'start-end'.");
            }
        } else {
            // If it's a single slot like "51"
            try {
                slots.add(Integer.parseInt(cleanedSlotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Invalid single slot format: '" + cleanedSlotString + "' in '" + slotString + "'.");
            }
        }

        return slots;
    }

    public static ItemStack getItemStackFromString(String itemName, String configPath) {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        if (itemName == null || itemName.isEmpty()) {
            return new ItemStack(Material.BARRIER);
        }


        // ตรวจสอบกับ Nexo ก่อน
        if (nexoHooks != null && nexoHooks.isNexoEnabled()) {
            ItemStack nexoItem = nexoHooks.getItemId(itemName);
            if (nexoItem != null) {
                return nexoItem.clone(); // สำคัญ: ต้อง clone เพื่อไม่ให้แก้ไขของเดิม
            }
        }

        // ตรวจสอบกับ ItemsAdder
        if (itemsAdderHooks != null && itemsAdderHooks.isItemsAdderHooked()) {
            ItemStack iaItem = itemsAdderHooks.getItemId(itemName);
            if (iaItem != null) {
                return iaItem.clone();
            }
        }

        // ตรวจสอบ Material ของ Minecraft
        Material material = Material.getMaterial(itemName.toUpperCase());
        if (material != null) {
            return new ItemStack(material);
        }
        return new ItemStack(Material.BARRIER);
    }

    public static final Map<Integer, CustomGUIItem> CustomItems = new HashMap<>();
    public static void loadCustomItems() {
        CustomItems.clear(); // รีเซ็ตก่อนโหลดใหม่
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("icons.item");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = "icons.item." + key;
            try {
                String slotString = plugin.getConfig().getString(path + ".slot", "-1");

                List<Integer> slots = parseSlotRange(slotString);

                if (slots.isEmpty()) continue;

                String display = plugin.getConfig().getString(path + ".display", "&eไอเทมใหม่");
                String materialIdentifier = plugin.getConfig().getString(path + ".material", "STONE");
                // int customModelData = plugin.getConfig().getInt(path + ".custom_model_data", 0); // ไม่ได้ใช้ตรงๆ แล้ว ถ้าเป็น Nexo
                List<String> lore = plugin.getConfig().getStringList(path + ".lore");
                List<String> actions = plugin.getConfig().getStringList(path + ".actions");
                boolean isBadgeItem = plugin.getConfig().getBoolean(path + ".badge", false); // Get the badge field
                //String badgeSlotId = plugin.getConfig().getString(path + ".badge_slot_id", key); // Use key as default slot_id
                String badgeSlotId = null;
                if (isBadgeItem) {
                    // *** แก้ไขตรงนี้ให้ใช้ slot แรกที่พบใน config ***
                    // ใช้ slot ตัวแรกที่กำหนดเป็นตัวระบุ ID
                    if (!slots.isEmpty()) {
                        badgeSlotId = "slot_" + slots.get(0);
                    } else {
                        // ถ้าไม่มี slot กำหนด แต่เป็น badge item ก็ยังควรมี ID
                        // อาจจะใช้ key หรือแสดง error ก็ได้ ขึ้นอยู่กับว่าอยากให้เป็นยังไง
                        // สำหรับวิธีที่ง่ายที่สุดนี้ เราจะยังคงยึดติดกับ slot
                        isBadgeItem = false; // ปิดการทำงาน badge ถ้าไม่มี slot
                    }
                }

                ItemStack baseItem = getItemStackFromString(materialIdentifier, path + ".material");
                boolean isNexoItem = false;
                boolean isIAItem = false;

                if (nexoHooks != null && nexoHooks.isNexoEnabled()) {
                    ItemStack nexoItem = nexoHooks.getItemId(materialIdentifier);
                    isNexoItem = (nexoItem != null && nexoItem.getType() != Material.BARRIER);
                }

                if (itemsAdderHooks != null && itemsAdderHooks.isItemsAdderHooked()) {
                    ItemStack iaItem = itemsAdderHooks.getItemId(materialIdentifier);
                    isIAItem = (iaItem != null && iaItem.getType() != Material.BARRIER);
                }

                com.nwPlayerProfile.core.ItemBuilder itemBuilder = new com.nwPlayerProfile.core.ItemBuilder(baseItem);
                itemBuilder.setDisplayName(ColorUtils.translateColorCodes(display));
                itemBuilder.setLore(ColorUtils.translateColorCodes(lore));

                // <<< จัดการ Custom Model Data ตามเงื่อนไข >>>
                if (!isNexoItem && !isIAItem) {
                    int customModelData = plugin.getConfig().getInt(path + ".custom_model_data", 0);
                    itemBuilder.setCustomModelData(customModelData);
                }
                // ถ้าเป็น Nexo Item: ไม่ต้องเรียก setCustomModelData เลย
                // เพราะ baseItem (Nexo Item) จะมี Custom Model Data ของมันอยู่แล้ว
                // และเราไม่ต้องการให้ config ไปทับมัน

                // ซ่อน Attribute เสมอ
                itemBuilder.hideAttribute();

                ItemStack item = itemBuilder.build();

                for (int slot : slots) {
                    CustomItems.put(slot, new CustomGUIItem(item, actions,isBadgeItem, badgeSlotId));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading item " + key + ": " + e.getMessage());
            }
        }
    }

    public static ItemStack setPlayerHeadSkin(ItemStack headItem, Player target) {
        if (headItem != null && headItem.getType() == Material.PLAYER_HEAD) {
            ItemStack clonedItem = headItem.clone();
            SkullMeta meta = (SkullMeta) clonedItem.getItemMeta();

            // ตั้งค่า owner ก่อน
            meta.setOwner(target.getName());

            // ตั้งค่าชื่อและ lore ที่ถูกแปลงแล้ว
            if (meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    displayName = PlaceholderAPI.setPlaceholders(target, displayName);
                }
                meta.setDisplayName(ColorUtils.translateColorCodes(displayName));
            }

            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                List<String> newLore = new ArrayList<>();

                for (String line : lore) {
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        line = PlaceholderAPI.setPlaceholders(target, line);
                    }
                    newLore.add(ColorUtils.translateColorCodes(line));
                }

                meta.setLore(newLore);
            }

            clonedItem.setItemMeta(meta);
            return clonedItem;
        }
        return headItem;
    }

    public static ItemStack setLorePlaceholder(ItemStack itemStack, Player target) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta originalMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;
        String rawName = (originalMeta != null && originalMeta.hasDisplayName()) ? originalMeta.getDisplayName() : null;
        List<String> rawLore = (originalMeta != null && originalMeta.hasLore()) ? originalMeta.getLore() : new ArrayList<>();

        String finalDisplayName = rawName;
        if (finalDisplayName != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && target != null) {
                finalDisplayName = PlaceholderAPI.setPlaceholders(target, finalDisplayName);
            }
            finalDisplayName = ColorUtils.translateColorCodes(finalDisplayName);
        }

        List<String> finalLore = new ArrayList<>();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && target != null) {
            for (String line : rawLore) {
                String parsedLine = PlaceholderAPI.setPlaceholders(target, line);
                // สำคัญ: แปลงรหัสสีแบบเก่าด้วย ColorUtils
                finalLore.add(ColorUtils.translateColorCodes(parsedLine));
            }
        } else {
            // ถ้า PAPI ไม่เปิดใช้งาน ก็แค่แปลงรหัสสีเท่านั้น
            for (String line : rawLore) {
                finalLore.add(ColorUtils.translateColorCodes(line));
            }
        }
        com.nwPlayerProfile.core.ItemBuilder customItemBuilder = new com.nwPlayerProfile.core.ItemBuilder(itemStack.clone()); // ใช้ .clone() เพื่อไม่แก้ไข ItemStack เดิมโดยตรง

        if (finalDisplayName != null) {
            customItemBuilder.setDisplayName(finalDisplayName);
        }
        customItemBuilder.setLore(finalLore);
        return customItemBuilder.build();
    }


    public static ItemStack setFromConfig(NwPlayerProfile plugin, String path) {
        String materialName = plugin.getConfig().getString(path + ".material");
        String displayName = plugin.getConfig().getString(path + ".display");
        int customModelData = plugin.getConfig().getInt(path + ".custom_model_data", 0);

        ItemStack itemStack = getItemStackFromString(materialName, path + ".material");

        boolean isCustomItem = false;
        if (nexoHooks != null && nexoHooks.isNexoEnabled()) {
            ItemStack nexoItem = nexoHooks.getItemId(materialName);
            if (nexoItem != null) {
                isCustomItem = true;
            }
        }

        if (itemsAdderHooks != null && itemsAdderHooks.isItemsAdderHooked()) {
            ItemStack iaItem = itemsAdderHooks.getItemId(materialName);
            if (iaItem != null) {
                isCustomItem = true;
            }
        }

        // สร้าง ItemBuilder จาก ItemStack ที่ได้
        com.nwPlayerProfile.core.ItemBuilder itemBuilder;
        if (isCustomItem) {
            itemBuilder = new com.nwPlayerProfile.core.ItemBuilder(itemStack)
                    .setDisplayName(ColorUtils.translateColorCodes(displayName))
                    .hideAttribute();
        } else {
            itemBuilder = new com.nwPlayerProfile.core.ItemBuilder(itemStack)
                    .setDisplayName(ColorUtils.translateColorCodes(displayName))
                    .setCustomModelData(customModelData)
                    .hideAttribute();
        }

        return itemBuilder.build();
    }

    public static synchronized void loadConfig() {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        plugin.reloadConfig();

        // โหลดค่า Name และ Row จาก config.yml
        Name = plugin.getConfig().getString("gui.name");
        MsgReload = plugin.getConfig().getString("message.msg-reload");
        isRightPlayer = plugin.getConfig().getBoolean("setting.rightplayer",true);
        MsgNotFoundUser = plugin.getConfig().getString("message.msg-not-found-user");
        MsgOpenGuiUser = plugin.getConfig().getString("message.msg-open-gui-user");
        Permission = plugin.getConfig().getString("setting.permission");
        PermissionOpen = plugin.getConfig().getString("setting.open");
        ActionType = plugin.getConfig().getString("setting.action", "RIGHT_CLICK").toUpperCase();


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

        synchronized (GroupPlaceholders) {
            GroupPlaceholders.clear();
            ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection("groups");
            if (groupsSection != null) {
                for (String groupName : groupsSection.getKeys(false)) {
                    String placeholder = groupsSection.getString(groupName);
                    if (placeholder != null) {
                        GroupPlaceholders.put(groupName.toLowerCase(), placeholder);
                    }
                }
            }
        }
        LuckpermsHooks.setup(GroupPlaceholders);
        loadCustomItems();

        NoFillDisplay = plugin.getConfig().getString("icons.item.no-fill.display", "&e");
        //NoFillMaterial = Material.getMaterial(plugin.getConfig().getString("icons.item.no-fill.material", "SUGAR"));
        String noFillMaterialName = plugin.getConfig().getString("icons.item.no-fill.material", "SUGAR");
        ItemStack noFillItem = getItemStackFromString(noFillMaterialName, "icons.item.no-fill.material");
        NoFillMaterial = noFillItem != null ? noFillItem.getType() : Material.SUGAR;
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


    public static String getSelectedBadgeId(UUID playerUUID, String slotId) {
        // ตรวจสอบจาก cache ก่อน
        if (selectedPlayerBadges.containsKey(playerUUID)) {
            return selectedPlayerBadges.get(playerUUID).get(slotId);
        }

        // ถ้าไม่มีใน cache, โหลดจาก DB โดยใช้ getAllSelectedBadges()
        // DatabaseManager no longer has getBadgeData(UUID), use getAllSelectedBadges()
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        Map<String, String> playerBadges = plugin.getDatabaseManager().getAllSelectedBadges(playerUUID);

        if (playerBadges != null) {
            selectedPlayerBadges.put(playerUUID, playerBadges); // Cache the entire map for this player
            return playerBadges.get(slotId); // Get the specific badge for the slotId
        }
        return null; // ไม่มีข้อมูลสำหรับผู้เล่นนี้หรือ slot นี้
    }

    // แก้ไขเมธอด setSelectedBadgeId ให้รับ slotId
    public static void setSelectedBadgeId(UUID playerUUID, String slotId, String badgeId) {
        Map<String, String> playerBadges = selectedPlayerBadges.getOrDefault(playerUUID, new HashMap<>());
        playerBadges.put(slotId, badgeId);
        selectedPlayerBadges.put(playerUUID, playerBadges); // Update cache
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        plugin.getDatabaseManager().saveSelectedBadge(playerUUID, slotId, badgeId); // ใช้ saveSelectedBadge ใหม่
    }

    // แก้ไขเมธอด removeSelectedBadge ให้รับ slotId
    public static void removeSelectedBadge(UUID playerUUID, String slotId) {
        if (selectedPlayerBadges.containsKey(playerUUID)) {
            selectedPlayerBadges.get(playerUUID).remove(slotId); // Remove from cache
        }
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        plugin.getDatabaseManager().deleteSelectedBadge(playerUUID, slotId); // ใช้ deleteSelectedBadge ใหม่
    }

    // เพิ่มเมธอดสำหรับลบ Badge ทั้งหมดของ Player (ถ้าจำเป็น)
    public static void removeAllBadges(UUID playerUUID) {
        selectedPlayerBadges.remove(playerUUID);
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        plugin.getDatabaseManager().deleteAllBadges(playerUUID);
    }

}
