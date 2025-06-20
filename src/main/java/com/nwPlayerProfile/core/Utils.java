package com.nwPlayerProfile.core;

import com.nwPlayerProfile.NwPlayerProfile;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.nwPlayerProfile.hooks.ItemsAdderHooks;
import com.nwPlayerProfile.hooks.LuckpermsHooks;
import com.nwPlayerProfile.hooks.NexoHooks;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.profile.PlayerProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public static NexoHooks nexoHooks;
    public static ItemsAdderHooks itemsAdderHooks;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();


    static {
        // โหลดค่า Name และ Row จาก config.yml
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        Utils.nexoHooks = new NexoHooks(plugin); // Initialized NexoHooks ที่นี่

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
            return PlaceholderAPI.setPlaceholders(player, rawTitle);
        } else {
            return rawTitle; // Return as is if PlaceholderAPI is not enabled
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

    // ฟังก์ชั่นดึงชุดเกราะจากผู้เล่น
    public static void updateArmorSet(PlayerInventory inventory) {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);

        // ใช้ getItemStackFromString แทน setFromConfig เพื่อให้ดึงจาก Nexo ได้
        ItemStack helmet = inventory.getHelmet();
        if (helmet == null) {
            String materialName = plugin.getConfig().getString("icons.player-helmet.material");
            helmet = getItemStackFromString(materialName, "icons.player-helmet.material");
        }
        armorSet.put("helmet", helmet);

        // ทำแบบเดียวกันกับ armor อื่นๆ
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
                    CustomItems.put(slot, new CustomGUIItem(item, actions));
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

        dev.triumphteam.gui.builder.item.ItemBuilder itemBuilder = dev.triumphteam.gui.builder.item.ItemBuilder.from(itemStack);

        List<String> rawLore;
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            rawLore = itemStack.getItemMeta().getLore();
        } else {
            rawLore = new ArrayList<>();
        }

        List<Component> finalLoreComponents = new ArrayList<>();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && target != null) {
            for (String line : rawLore) {
                String parsedLine = PlaceholderAPI.setPlaceholders(target, line);
                finalLoreComponents.add(
                        ColorUtils.parseWithAutoDetect(parsedLine)
                                .decoration(TextDecoration.ITALIC, false)
                );
            }
        } else {
            for (String line : rawLore) {
                finalLoreComponents.add(
                        MINI_MESSAGE.deserialize(line)
                                .decoration(TextDecoration.ITALIC, false)
                );
            }
        }

        // แปลงชื่อด้วย PlaceholderAPI + MiniMessage
        String rawName = itemStack.getItemMeta().getDisplayName();
        String parsedName = rawName;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && target != null) {
            parsedName = PlaceholderAPI.setPlaceholders(target, rawName);
        }
        Component finalNameComponent = ColorUtils.parseWithAutoDetect(parsedName)
                .decoration(TextDecoration.ITALIC, false);

        itemBuilder.name(finalNameComponent).lore(finalLoreComponents);
        return itemBuilder.build();
    }


    public static dev.triumphteam.gui.builder.item.ItemBuilder setFromConfig(NwPlayerProfile plugin, String path) {
        // ดึงค่าจาก config
        String materialName = plugin.getConfig().getString(path + ".material");
        String displayName = plugin.getConfig().getString(path + ".display");
        int customModelData = plugin.getConfig().getInt(path + ".custom_model_data", 0);

        // ใช้ getItemStackFromString เพื่อรับ ItemStack
        ItemStack itemStack = getItemStackFromString(materialName, path + ".material");

        // ตรวจสอบว่าไอเทมมาจาก Nexo หรือ ItemsAdder หรือไม่
        boolean isCustomItem = false;
        if (nexoHooks != null && nexoHooks.isNexoEnabled()) {
            ItemStack nexoItem = nexoHooks.getItemId(materialName);
            if (nexoItem != null && nexoItem.getType() == itemStack.getType()) {
                isCustomItem = true;
            }
        }

        if (!isCustomItem && itemsAdderHooks != null && itemsAdderHooks.isItemsAdderHooked()) {
            ItemStack iaItem = itemsAdderHooks.getItemId(materialName);
            if (iaItem != null && iaItem.getType() == itemStack.getType()) {
                isCustomItem = true;
            }
        }

        // สร้าง ItemBuilder จาก ItemStack ที่ได้
        dev.triumphteam.gui.builder.item.ItemBuilder itemBuilder =
                dev.triumphteam.gui.builder.item.ItemBuilder.from(itemStack)
                        .name(MINI_MESSAGE.deserialize(displayName));

        // ตั้งค่า custom model data ถ้ามีค่ามากกว่า 0 และไม่ใช่ไอเทมจาก Nexo/ItemsAdder
        if (customModelData > 0 && !isCustomItem) {
            itemBuilder.model(customModelData);
        }

        return itemBuilder;
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


}
