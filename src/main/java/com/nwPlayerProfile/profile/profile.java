package com.nwPlayerProfile.profile;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.nwPlayerProfile.core.CustomGUIItem;
//import com.nwPlayerProfile.core.ItemBuilder;
import com.nwPlayerProfile.core.Utils;
import com.nwPlayerProfile.NwPlayerProfile;
import com.nwPlayerProfile.database.DatabaseManager;
import com.nwPlayerProfile.inventory.InventoryManager;
import com.nwPlayerProfile.inventory.InventoryStage;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import com.nwPlayerProfile.hooks.CitizensAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import com.nwPlayerProfile.core.ColorUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import org.bukkit.inventory.meta.ItemMeta;

import static com.nwPlayerProfile.core.Utils.*;

public class profile implements Listener {

    private static final HashMap<UUID, UUID> guiOwners = new HashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
    private final DatabaseManager db = plugin.getDatabaseManager();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        if (!Utils.isRightPlayer) return;

        if (CitizensAPI.isCitizensEnabled() && CitizensAPI.isNPC(event.getRightClicked())) return;

        Player target = (Player) event.getRightClicked();
        if (target == null) return;

        Player player = event.getPlayer();
        String action = Utils.ActionType;

        boolean isSneaking = player.isSneaking();

        switch (action) {
            case "RIGHT_CLICK":
                if (event.getHand() == EquipmentSlot.HAND) { // main hand only
                    openMenu(player, target);
                }
                break;

            case "SHIFT_RIGHT_CLICK":
                if (isSneaking && event.getHand() == EquipmentSlot.HAND) {
                    openMenu(player, target);
                }
                break;

//            case "LEFT_CLICK":
//            case "SHIFT_LEFT_CLICK":
//                // ต้องใช้ Listener แยกอีกตัว เช่น EntityDamageByEntityEvent
//                break;

            default:
                break;
        }
    }


    public void openMenu(Player player , Player target) {

        UUID uuid = target.getUniqueId();

        Map<String, String> eqMap = new HashMap<>();
        Map<String, String> cosMap = new HashMap<>();
        if (db.isMySQLEnabled()) {
            eqMap        = db.getPlayerEquipment(uuid);
            cosMap       = db.getPlayerCosmetics(uuid);
        }

        // ดึงข้อมูลของ CosmeticUser
        CosmeticUser cosmeticUser = HMCCosmeticsAPI.getUser(uuid);
        if (cosmeticUser == null) {
            //player.sendMessage(ColorUtils.translateColorCodes(MsgNotFoundUser));
            InventoryManager.removeStage(player);
            return;
        }
        // อัพเดทชุดเกราะที่ผู้เล่นใส่
        Map<String, ItemStack> mysqlEquipment = Utils.getPlayerEquipmentFromMySQL(uuid);
        Map<CosmeticSlot, ItemStack> mysqlCosmetics = Utils.getPlayerCosmeticsFromMySQL(uuid);
        Utils.updateArmorSet(target.getInventory(), mysqlEquipment);

        //String guiTitle = Utils.Name.replace("{groups}", Utils.getPlayerGroupPlaceholder(target));
        String finalGuiTitle = Utils.getParsedGuiTitle(target);

        // สร้าง GUI
        Gui gui = Gui.gui()
                .title(Component.text(finalGuiTitle))
                .rows(Rows)
                .type(GuiType.CHEST)
                .create();

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        gui.setDefaultTopClickAction(event -> {
            event.setCancelled(true);

            int slot = event.getSlot();
            if (CustomItems.containsKey(slot)) {
                CustomGUIItem guiItem = CustomItems.get(slot);

                if (guiItem.isBadge()) {
                    // This is a badge slot, get its specific ID
                    String badgeSlotId = guiItem.getBadgeSlotId(); // Get the slot ID from CustomGUIItem
                    if (badgeSlotId == null) { // Fallback in case badgeSlotId is not set
                        //NwPlayerProfile.getPlugin().getLogger().warning("Badge item at slot " + slot + " has no badge_slot_id defined!");
                        return;
                    }

                    if (!player.equals(target)) {
                        return;
                    }

                    // Check badge for this specific slot
                    String selectedBadgeId = Utils.getSelectedBadgeId(target.getUniqueId(), badgeSlotId);

                    if (event.getClick() == ClickType.RIGHT && selectedBadgeId != null) {
                        // Right-click on a badge to remove it for this specific slot
                        Utils.removeSelectedBadge(target.getUniqueId(), badgeSlotId);
                        // Re-open the main menu to refresh the slot display
                        openMenu(player, target);
                    } else {
                        // Left-click (or right-click if no badge selected) to open badge selection GUI
                        // Pass the specific badgeSlotId to BadgeManager
                        Utils.badgeManager.openBadgeGUI(player, target, badgeSlotId); // **IMPORTANT**: viewer, target, badgeSlotId
                    }
                } else {
                    List<String> actions = guiItem.getActions();
                    if (target != null) {
                        for (String action : actions) {
                            String parsedAction = PlaceholderAPI.setPlaceholders(target, action);
                            player.performCommand(parsedAction);
                        }
                    }
                }
            }
        });

        gui.setOpenGuiAction(event -> {
            // ป้องกันการเปิด GUI ซ้ำถ้ายังอยู่ในสถานะ OPENING
            if (InventoryManager.getStage(player) == InventoryStage.OPENING) {
                return;
            }

            // กำหนดให้สถานะเป็น OPENING เพื่อป้องกันการเปิดซ้ำ
            InventoryManager.setStage(player, InventoryStage.OPENING);

            // เปลี่ยนสถานะเป็น ACTIVE เมื่อเปิดสำเร็จ
            Bukkit.getScheduler().runTaskLater(NwPlayerProfile.getPlugin(NwPlayerProfile.class), () -> {
                if (player.getOpenInventory().getTopInventory().equals(gui)) {
                    InventoryManager.setStage(player, InventoryStage.ACTIVE);
                }
            }, 5L);
        });

        // ใส่ชุดเกราะที่ผู้เล่นใส่ในแต่ละส่วนลงใน GUI
        GuiItem helmetItem = createArmorGuiItem("helmet", "icons.player-helmet.display", eqMap);
        GuiItem chestplateItem = createArmorGuiItem("chestplate", "icons.player-chestplate.display", eqMap);
        GuiItem leggingsItem = createArmorGuiItem("leggings", "icons.player-leggings.display", eqMap);
        GuiItem bootsItem = createArmorGuiItem("boots", "icons.player-boots.display", eqMap);

        gui.setItem(Utils.getArmorSlot("helmet"), helmetItem);
        gui.setItem(Utils.getArmorSlot("chestplate"), chestplateItem);
        gui.setItem(Utils.getArmorSlot("leggings"), leggingsItem);
        gui.setItem(Utils.getArmorSlot("boots"), bootsItem);


        // ดึงเครื่องประดับทั้งหมดและใส่ลง GUI
        for (CosmeticSlot slotType : CosmeticSlot.values().values()) {
            int slotIndex = Utils.getSlot(slotType);
            if (slotIndex == -1) continue; // ถ้า slot ไม่ถูกต้อง ข้ามไป

            ItemStack item = null;

            // ✅ เช็คว่าผู้เล่นมี cosmetic ในช่องนี้ไหม
            if (db.isMySQLEnabled() && cosMap.containsKey(slotType.toString().toLowerCase())) {
                String itemId = cosMap.get(slotType.toString().toLowerCase());
                item = Utils.getItemStackFromString(itemId, "cosmetic." + slotType.toString().toLowerCase());
            } else {
                // ถ้าไม่มีข้อมูลใน MySQL หรือ MySQL ไม่เปิดใช้งาน
                for (Cosmetic cosmetic : cosmeticUser.getCosmetics()) {
                    if (cosmetic != null && cosmetic.getSlot() == slotType) {
                        item = cosmetic.getItem();
                        break;
                    }
                }
            }

            // ✅ ถ้าไม่มี cosmetic หรือไอเท็มเป็น AIR → โหลดจาก config
            if (item == null || item.getType() == Material.AIR) {
                String slotName = slotType.toString().toLowerCase();
                item = Utils.setFromConfig(NwPlayerProfile.getPlugin(NwPlayerProfile.class), "icons." + slotName);
            }

            // แปลง ItemStack เป็น GuiItem ของ Triumph GUI
            GuiItem guiItem = ItemBuilder.from(item).asGuiItem();
            gui.setItem(slotIndex, guiItem);
        }
//
//        // ใส่ไอเทมทั้งหมดที่กำหนดใน config ลงใน GUI
        synchronized(CustomItems) {
            for (Map.Entry<Integer, CustomGUIItem> entry : Utils.CustomItems.entrySet()) {
                CustomGUIItem customGuiItem = entry.getValue();
                ItemStack baseItem = customGuiItem.getItem().clone();
                ItemStack finalItem = baseItem;

                if (customGuiItem.isBadge()) {
                    String badgeSlotId = customGuiItem.getBadgeSlotId();
                    if (badgeSlotId == null) {
                        //NwPlayerProfile.getPlugin().getLogger().warning("Badge item at slot " + entry.getKey() + " has no badge_slot_id defined and will be skipped.");
                        continue; // Skip if no badge_slot_id is defined
                    }
                    // If it's a badge slot, check if the target player has a selected badge
                    String selectedBadgeId = Utils.getSelectedBadgeId(target.getUniqueId(), badgeSlotId); // Pass slotId here

                    if (selectedBadgeId != null) {
                        // Get the actual badge item from BadgeManager
                        BadgeManager.Badge selectedBadge = Utils.badgeManager.getBadgeById(selectedBadgeId);
                        if (selectedBadge != null) {
                            finalItem = Utils.getItemStackFromString(selectedBadge.getDisplayMaterial(), "badge.list." + selectedBadge.getId() + ".display-material");
                            ItemMeta meta = finalItem.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName(ColorUtils.translateColorCodes(selectedBadge.getDisplayName()));
                                meta.setLore(ColorUtils.translateColorCodes(selectedBadge.getDisplayLore()));
                                if (selectedBadge.getDisplayCustomModelData() > 0) {
                                    meta.setCustomModelData(selectedBadge.getDisplayCustomModelData());
                                }
                                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                finalItem.setItemMeta(meta);
                            }
                            List<String> currentLore = new ArrayList<>();
                            currentLore.addAll(ColorUtils.translateColorCodes(selectedBadge.getDisplayLore()));
                            if (player.equals(target)) {
                                currentLore.add(Utils.badgeManager.getMsgRightClickToRemoveBadge()); // เพิ่มข้อความนี้ก่อน
                            }

                            meta.setLore(currentLore);
                            finalItem.setItemMeta(meta);
                        } else {
                            // If selected badge ID is invalid, show the default custom slot item
                            if (baseItem.getItemMeta() != null) {
                                finalItem = Utils.setLorePlaceholder(baseItem, target);
                            }
                            if (baseItem.getType() == Material.PLAYER_HEAD) {
                                finalItem = Utils.setPlayerHeadSkin(baseItem, target);
                            }
                        }
                    } else {
                        // No badge selected, show the default custom slot item
                        if (baseItem.getItemMeta() != null) {
                            finalItem = Utils.setLorePlaceholder(baseItem, target);
                        }
                        if (baseItem.getType() == Material.PLAYER_HEAD) {
                            finalItem = Utils.setPlayerHeadSkin(baseItem, target);
                        }
                    }
                } else {

                    if (baseItem.getItemMeta() != null) {
                        finalItem = Utils.setLorePlaceholder(baseItem, target); // ส่ง UUID
                    }

                    if (baseItem.getType() == Material.PLAYER_HEAD) {
                        finalItem = Utils.setPlayerHeadSkin(baseItem, target); // ส่ง UUID
                    }

                }

                GuiItem guiItem = ItemBuilder.from(finalItem).asGuiItem();
                gui.setItem(entry.getKey(), guiItem);
            }
        }
//
//        // ใช้ค่าจาก config สำหรับ "no-fill"
//
        String noFillMaterialName = NwPlayerProfile.getPlugin(NwPlayerProfile.class)
                .getConfig().getString("icons.item.no-fill.material", "SUGAR");

// ดึง ItemStack จากชื่อ material โดยตรง
        ItemStack fillerItemStack = getItemStackFromString(noFillMaterialName, "icons.item.no-fill.material");

        com.nwPlayerProfile.core.ItemBuilder customItemBuilder = new com.nwPlayerProfile.core.ItemBuilder(fillerItemStack);

        customItemBuilder.setDisplayName(ColorUtils.translateColorCodes(Utils.NoFillDisplay != null ? Utils.NoFillDisplay : ""));

        customItemBuilder.setLore(ColorUtils.translateColorCodes(Utils.NoFillLore)); // สมมติว่า setLore รับ List<String> ที่แปลงแล้ว

        customItemBuilder.hideAttribute();


//// ตรวจสอบว่าเป็นไอเทมจาก Nexo หรือไม่
        boolean isCustomItem = false;
        if (nexoHooks != null && nexoHooks.isNexoEnabled()) {
            ItemStack nexoItem = nexoHooks.getItemId(noFillMaterialName);
            isCustomItem = (nexoItem != null && nexoItem.getType() == fillerItemStack.getType());

            // Debug log
        }

        if (itemsAdderHooks != null && itemsAdderHooks.isItemsAdderHooked()) {
            ItemStack iaItem = itemsAdderHooks.getItemId(noFillMaterialName);
            isCustomItem = (iaItem != null && iaItem.getType() == fillerItemStack.getType());

            // Debug log
        }

//// ตั้งค่า custom model data ถ้าไม่ใช่ไอเทมจาก Nexo
        if (Utils.NoFillCustomModelData > 0 && !isCustomItem) {
            customItemBuilder.setCustomModelData(Utils.NoFillCustomModelData);
        }

        GuiItem noFillGuiItem = new GuiItem(customItemBuilder.build());

// เติมลง GUI
        gui.getFiller().fill(noFillGuiItem);


        // เปิด GUI
//        player.sendMessage(ColorUtils.translateColorCodes(MsgOpenGuiUser));
        guiOwners.put(player.getUniqueId(), uuid);
        gui.open(player);

        gui.setCloseGuiAction(event -> {
            // ป้องกันการลบสถานะก่อนเปิด GUI เสร็จ
            if (InventoryManager.getStage(player) == InventoryStage.OPENING) {
                Bukkit.getScheduler().runTaskLater(NwPlayerProfile.getPlugin(NwPlayerProfile.class), () -> {
                    if (player.getOpenInventory().getTopInventory() == null) {
                        InventoryManager.removeStage(player);
                        guiOwners.remove(player.getUniqueId());
                    }
                }, 5L);
            } else {
                InventoryManager.setStage(player, InventoryStage.CLOSING);
                Bukkit.getScheduler().runTaskLater(NwPlayerProfile.getPlugin(NwPlayerProfile.class), () -> {
                    InventoryManager.removeStage(player);
                    guiOwners.remove(player.getUniqueId());
                }, 5L);
            }
        });
    }


    private GuiItem createArmorGuiItem(String part, String configPath, Map<String, String> eqMap) {
        NwPlayerProfile plugin = NwPlayerProfile.getPlugin(NwPlayerProfile.class);
        ItemStack armorPiece = null;

        // ถ้า MySQL เปิดใช้งานและมีข้อมูล equipment ในฐานข้อมูล
        if (db.isMySQLEnabled() && eqMap.containsKey(part)) {
            String itemId = eqMap.get(part);
            armorPiece = Utils.getItemStackFromString(itemId, "equipment." + part);
        } else {
            // ถ้าไม่มีข้อมูลใน MySQL หรือ MySQL ไม่เปิดใช้งาน
            armorPiece = Utils.getArmor(part);
        }

        String basePath = "icons.player-" + part;

        if (armorPiece == null || armorPiece.getType() == Material.AIR) {
            String defaultMaterialName = plugin.getConfig().getString(configPath.replace(".display", ".material"), "BARRIER");
            String displayName = plugin.getConfig().getString(basePath + ".display", "&cMissing " + part);
            int customModelData = plugin.getConfig().getInt(basePath + ".custom_model_data", 0);
            ItemStack defaultItem = Utils.getItemStackFromString(defaultMaterialName, configPath.replace(".display", ".material"));
            if (defaultItem == null || defaultItem.getType() == Material.AIR) {
                defaultItem = new ItemStack(Material.BARRIER);
            }

            com.nwPlayerProfile.core.ItemBuilder itemBuilder = new com.nwPlayerProfile.core.ItemBuilder(defaultItem)
                    .setDisplayName(ColorUtils.translateColorCodes(displayName));

            if (nexoHooks != null && nexoHooks.isNexoEnabled()) {
                ItemStack nexoItem = nexoHooks.getItemId(defaultMaterialName);
                if (nexoItem == null) {
                    itemBuilder.setCustomModelData(customModelData);
                }
            }

            if (itemsAdderHooks != null && itemsAdderHooks.isItemsAdderHooked()) {
                ItemStack iaItem = itemsAdderHooks.getItemId(defaultMaterialName);
                if (iaItem == null) {
                    itemBuilder.setCustomModelData(customModelData);
                }
            }

            return new GuiItem(itemBuilder.build());
        }

        return new GuiItem(armorPiece);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryManager.removeStage(player);
        guiOwners.remove(player.getUniqueId());
    }

}