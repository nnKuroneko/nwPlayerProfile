package com.nwPlayerProfile.profile;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.nwPlayerProfile.core.CustomGUIItem;
//import com.nwPlayerProfile.core.ItemBuilder;
import com.nwPlayerProfile.core.Utils;
import com.nwPlayerProfile.NwPlayerProfile;
import com.nwPlayerProfile.inventory.InventoryManager;
import com.nwPlayerProfile.inventory.InventoryStage;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.citizensnpcs.api.CitizensAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.nwPlayerProfile.core.ColorUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;

import static com.nwPlayerProfile.core.Utils.*;

public class profile implements Listener {

    private static final HashMap<UUID, UUID> guiOwners = new HashMap<>();


    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player) || CitizensAPI.getNPCRegistry().isNPC(event.getRightClicked())) {
            return;  // ถ้าเป็น NPC หรือไม่ใช่ Player ก็จะไม่ทำอะไรต่อ
        }

        Player target = (Player) event.getRightClicked();
        if (target == null) {
            return;
        }

        Player player = event.getPlayer();
        UUID targetUUID = target.getUniqueId();

        openMenu(player , target);


    }


    public void openMenu(Player player , Player target) {

        UUID targetUUID = target.getUniqueId();
        // ดึงข้อมูลของ CosmeticUser
        CosmeticUser cosmeticUser = HMCCosmeticsAPI.getUser(targetUUID);
        if (cosmeticUser == null) {
            player.sendMessage(ColorUtils.translateColorCodes(MsgNotFoundUser));
            InventoryManager.removeStage(player);
            return;
        }

        // อัพเดทชุดเกราะที่ผู้เล่นใส่
        Utils.updateArmorSet(target.getInventory());

        // สร้าง GUI
        Gui gui = Gui.gui()
                .title(Component.text(ColorUtils.translateColorCodes(Name)))
                .rows(Rows)
                .type(GuiType.CHEST)
                .create();

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (CustomItems.containsKey(slot)) {
                CustomGUIItem guiItem = CustomItems.get(slot);
                List<String> actions = guiItem.getActions();

                if (target != null) {
                    for (String action : actions) {
                        player.performCommand(action.replace("%player%", target.getName()));
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
        GuiItem helmetItem = ItemBuilder.from(Utils.getArmor("helmet")).asGuiItem();
        GuiItem chestplateItem = ItemBuilder.from(Utils.getArmor("chestplate")).asGuiItem();
        GuiItem leggingsItem = ItemBuilder.from(Utils.getArmor("leggings")).asGuiItem();
        GuiItem bootsItem = ItemBuilder.from(Utils.getArmor("boots")).asGuiItem();
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
            for (Cosmetic cosmetic : cosmeticUser.getCosmetics()) {
                if (cosmetic != null && cosmetic.getSlot() == slotType) {
                    item = cosmetic.getItem();
                    break;
                }
            }

            // ✅ ถ้าไม่มี cosmetic หรือไอเท็มเป็น AIR → โหลดจาก config
            if (item == null) {
                String slotName = slotType.toString().toLowerCase();
                item = Utils.setFromConfig(NwPlayerProfile.getPlugin(NwPlayerProfile.class), "icons." + slotName).build();
            }

            // แปลง ItemStack เป็น GuiItem ของ Triumph GUI
            GuiItem guiItem = ItemBuilder.from(item).asGuiItem();
            gui.setItem(slotIndex, guiItem);
        }

        // ใส่ไอเทมทั้งหมดที่กำหนดใน config ลงใน GUI
        synchronized(CustomItems) {
            for (Map.Entry<Integer, CustomGUIItem> entry : Utils.CustomItems.entrySet()) {
                ItemStack item = entry.getValue().getItem(); // ดึง ItemStack ออกจาก CustomGUIItem
                GuiItem guiItem = ItemBuilder.from(item).asGuiItem();
                gui.setItem(entry.getKey(), guiItem);
            }
        }

        // ใช้ค่าจาก config สำหรับ "no-fill"
        ItemStack itemFill = new com.nwPlayerProfile.core.ItemBuilder()
                .setMaterial(Utils.NoFillMaterial)
                .setDisplayName(ColorUtils.translateColorCodes(Utils.NoFillDisplay))
                .setCustomModelData(Utils.NoFillCustomModelData)
                .setLore(ColorUtils.translateColorCodes(Utils.NoFillLore))
                .hideAttribute()
                .build();

        GuiItem fillerItem = ItemBuilder.from(itemFill).asGuiItem();
        gui.getFiller().fill(fillerItem);


        // เปิด GUI
//        player.sendMessage(ColorUtils.translateColorCodes(MsgOpenGuiUser));
        guiOwners.put(player.getUniqueId(), targetUUID);
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryManager.removeStage(player);
        guiOwners.remove(player.getUniqueId());
    }

}