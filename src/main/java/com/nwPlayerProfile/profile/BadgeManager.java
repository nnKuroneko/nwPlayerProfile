package com.nwPlayerProfile.profile;

import com.nwPlayerProfile.NwPlayerProfile;
import com.nwPlayerProfile.core.ColorUtils;
import com.nwPlayerProfile.core.Utils;
import com.nwPlayerProfile.database.DatabaseManager; // Import DatabaseManager
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap; // Import HashMap
import java.util.List;
import java.util.Map; // Import Map
import java.util.Objects;
import java.util.UUID; // Import UUID
import java.util.logging.Level;

public class BadgeManager {

    private final NwPlayerProfile plugin;
    private final DatabaseManager databaseManager; // Add DatabaseManager field
    private File badgeFile;
    private FileConfiguration badgeConfig;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private String guiTitle;
    private int guiRows;
    private int guiPageSize;
    private GuiItem noFillItem;

    private String prevPageMaterial;
    private String prevPageName;
    private int prevPageSlot;

    private String currentPageMaterial;
    private String currentPageNameFormat;
    private int currentPageSlot;

    private String nextPageMaterial;
    private String nextPageName;
    private int nextPageSlot;

    private String msgCurrentlySelected;
    private String msgClickToDeselect;
    private String msgClickToSelect;
    private String msgBadgeSelected;
    private String msgBadgeDeselected;
    private String msgRightClickToRemoveBadge;

    private final Map<String, Badge> badges = new HashMap<>(); // Change to Map for easy lookup by ID

    public BadgeManager(NwPlayerProfile plugin, DatabaseManager databaseManager) { // Add DatabaseManager to constructor
        this.plugin = plugin;
        this.databaseManager = databaseManager; // Initialize databaseManager
        createBadgeFile();
        loadBadgeConfig();
    }

    private void createBadgeFile() {
        badgeFile = new File(plugin.getDataFolder(), "badge.yml");
        if (!badgeFile.exists()) {
            plugin.saveResource("badge.yml", false);
        }
    }

    public void loadBadgeConfig() {
        badgeConfig = YamlConfiguration.loadConfiguration(badgeFile);
        badges.clear(); // Clear existing badges

        // Load GUI settings
        this.guiTitle = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.name", "<red>Badges</red>"));
        this.guiRows = badgeConfig.getInt("badge.gui.rows", 5);
        this.guiPageSize = badgeConfig.getInt("badge.gui.page-size", 28); // Default page size

        // Load no-fill item
        String noFillMat = badgeConfig.getString("badge.gui.no-fill.material", "PAPER");
        int noFillCMD = badgeConfig.getInt("badge.gui.no-fill.custom_model_data", 0);
        String noFillDisplay = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.no-fill.display", "<white/>"));
        ItemStack noFillBaseItem = Utils.getItemStackFromString(noFillMat, "badge.gui.no-fill.material");
        ItemMeta noFillMeta = noFillBaseItem.getItemMeta();
        if (noFillMeta != null) {
            noFillMeta.setDisplayName(noFillDisplay);
            noFillMeta.setCustomModelData(noFillCMD);
            noFillMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // Hide default attributes
            noFillBaseItem.setItemMeta(noFillMeta);
        }
        this.noFillItem = ItemBuilder.from(noFillBaseItem).asGuiItem();


        // Load pagination items
        this.prevPageMaterial = badgeConfig.getString("badge.gui.previous-page-item.material", "ARROW");
        this.prevPageName = badgeConfig.getString("badge.gui.previous-page-item.name", "<green>Previous Page</green>");
        this.prevPageSlot = badgeConfig.getInt("badge.gui.previous-page-item.slot", 39);

        this.currentPageMaterial = badgeConfig.getString("badge.gui.current-page-item.material", "PAPER");
        this.currentPageNameFormat = badgeConfig.getString("badge.gui.current-page-item.name", "<yellow>Page <current>/<total></yellow>");
        this.currentPageSlot = badgeConfig.getInt("badge.gui.current-page-item.slot", 40);

        this.nextPageMaterial = badgeConfig.getString("badge.gui.next-page-item.material", "ARROW");
        this.nextPageName = badgeConfig.getString("badge.gui.next-page-item.name", "<green>Next Page</green>");
        this.nextPageSlot = badgeConfig.getInt("badge.gui.next-page-item.slot", 41);

        this.msgCurrentlySelected = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.messages.currently-selected", "<green><bold>CURRENTLY SELECTED</bold></green>"));
        this.msgClickToDeselect = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.messages.click-to-deselect", "<red>Click to deselect</red>"));
        this.msgClickToSelect = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.messages.click-to-select", "<yellow>Click to select</yellow>"));
        this.msgBadgeSelected = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.messages.badge-selected", "<green>You have selected the '<badge_name>' badge.</green>"));
        this.msgBadgeDeselected = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.messages.badge-deselected", "<green>You have deselected the badge.</green>"));
        this.msgRightClickToRemoveBadge = ColorUtils.translateColorCodes(badgeConfig.getString("badge.gui.messages.right-click-to-remove", "<red>Right-Click to remove badge</red>"));


        ConfigurationSection badgeListSection = badgeConfig.getConfigurationSection("badge.list");
        if (badgeListSection != null) {
            for (String key : badgeListSection.getKeys(false)) {
                String path = "badge.list." + key;
                String permission = badgeConfig.getString(path + ".permission");
                String displayMaterial = badgeConfig.getString(path + ".display-material");
                String displayName = badgeConfig.getString(path + ".display-name");
                int displayCustomModelData = badgeConfig.getInt(path + ".display-custom_model_data", 0);
                List<String> displayLore = badgeConfig.getStringList(path + ".display-lore");

                if (permission == null || displayMaterial == null || displayName == null) {
                    plugin.getLogger().log(Level.WARNING, "Skipping badge '" + key + "' due to missing required fields.");
                    continue;
                }
                // Store in Map using key as ID
                badges.put(key, new Badge(key, permission, displayMaterial, displayName, displayCustomModelData, displayLore));
            }
        }
        plugin.getLogger().log(Level.INFO, "Loaded " + badges.size() + " badges from badge.yml");
    }

    public String getMsgRightClickToRemoveBadge() {
        return msgRightClickToRemoveBadge;
    }


    public FileConfiguration getBadgeConfiguration() {
        return badgeConfig;
    }

    public Badge getBadgeById(String id) {
        return badges.get(id);
    }

    public void openBadgeGUI(Player viewer, Player target, String badgeSlotId) {
        PaginatedGui gui = dev.triumphteam.gui.guis.Gui.paginated()
                .title(miniMessage.deserialize(guiTitle))
                .rows(guiRows)
                .pageSize(guiPageSize)
                .create();

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        gui.setDefaultTopClickAction(event -> {
            event.setCancelled(true); // Cancel the default click action for all slots

            int clickedSlot = event.getSlot();
            String action = badgeConfig.getString("badge.gui.current-page-item.action", "");

            if (clickedSlot == currentPageSlot && viewer != null && action != null && !action.isEmpty()) {
                if (action.equalsIgnoreCase("close")) { // Check if the command is "close"
                    gui.close(viewer); // Close the GUI directly
                } else if (action.equalsIgnoreCase("back")) { // Check if the command is "close"
                    plugin.getProfileManager().openMenu(viewer, target);
                }
                else {
                    if (target != null) {
                        String parsedAction = PlaceholderAPI.setPlaceholders(target, action);
                        viewer.performCommand(parsedAction.replace("%player%", viewer.getName()));
                    }
                }
            }
            // If another slot is clicked, nothing happens (due to event.setCancelled(true))
        });

        String currentlySelectedBadgeId = Utils.getSelectedBadgeId(target.getUniqueId(), badgeSlotId);

        // Add badges to the GUI
        for (Badge badge : badges.values()) {
            if (viewer.hasPermission(badge.getPermission())) {
                ItemStack badgeItem = Utils.getItemStackFromString(badge.getDisplayMaterial(), "badge.list." + badge.getId() + ".display-material");
                ItemMeta meta = badgeItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColorUtils.translateColorCodes(badge.getDisplayName()));
                    List<String> lore = new ArrayList<>(badge.getDisplayLore()); // Create mutable list
//                    if (badge.getDisplayCustomModelData() > 0) {
//                        meta.setCustomModelData(badge.getDisplayCustomModelData());
//                    }
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                    // Add selection status to lore
                    if (Objects.equals(currentlySelectedBadgeId, badge.getId())) {
                        lore.add(msgCurrentlySelected);
                        lore.add(msgClickToDeselect);
                    } else {
                        lore.add(msgClickToSelect);
                    }
                    meta.setLore(ColorUtils.translateColorCodes(lore)); // Set translated lore
                    badgeItem.setItemMeta(meta);
                }
                String badgeId = badge.getId(); // Capture badgeId for lambda
                gui.addItem(ItemBuilder.from(badgeItem).asGuiItem(event -> {
                    event.setCancelled(true);
                    String clickedBadgeId = badge.getId();
                    if (Objects.equals(currentlySelectedBadgeId, badgeId)) {
                        Utils.removeSelectedBadge(target.getUniqueId(), badgeSlotId);
                        viewer.sendMessage(msgBadgeDeselected); // Use loaded message
                    } else {
                        Utils.setSelectedBadgeId(target.getUniqueId(), badgeSlotId, clickedBadgeId);
                        viewer.sendMessage(msgBadgeSelected.replace("<badge_name>", ColorUtils.translateColorCodes(badge.getDisplayName()))); // Use loaded message
                    }
                    // Re-open the main profile GUI to reflect changes
                    plugin.getProfileManager().openMenu(viewer, target); // Assumes profile manager exists and has openMenu method
                }));
            }
        }

        // Pagination controls
        ItemStack previousPageItem = Utils.getItemStackFromString(prevPageMaterial, "badge.gui.previous-page-item.material");
        ItemMeta prevMeta = previousPageItem.getItemMeta();
        if(prevMeta != null) {
            prevMeta.setDisplayName(ColorUtils.translateColorCodes(prevPageName));
            prevMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            previousPageItem.setItemMeta(prevMeta);
        }
        gui.setItem(prevPageSlot, ItemBuilder.from(previousPageItem).asGuiItem(event -> {
            gui.previous();
            updateCurrentPageItem(gui);
            event.setCancelled(true);
        }));

        ItemStack nextPageItem = Utils.getItemStackFromString(nextPageMaterial, "badge.gui.next-page-item.material");
        ItemMeta nextMeta = nextPageItem.getItemMeta();
        if(nextMeta != null) {
            nextMeta.setDisplayName(ColorUtils.translateColorCodes(nextPageName));
            nextMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextPageItem.setItemMeta(nextMeta);
        }
        gui.setItem(nextPageSlot, ItemBuilder.from(nextPageItem).asGuiItem(event -> {
            gui.next();
            updateCurrentPageItem(gui);
            event.setCancelled(true);
        }));

        // Initial setup for current page item
        //gui.getFiller().;
        updateCurrentPageItem(gui);

        gui.open(viewer);
    }

    private void updateCurrentPageItem(PaginatedGui gui) {
        ItemStack currentMaterial = Utils.getItemStackFromString(currentPageMaterial, "badge.gui.current-page-item.material");
        String formattedName = currentPageNameFormat
                .replace("<current>", String.valueOf(gui.getCurrentPageNum()))
                .replace("<total>", String.valueOf(gui.getPagesNum()));

        ItemMeta currentMeta = currentMaterial.getItemMeta();
        if(currentMeta != null) {
            currentMeta.setDisplayName(ColorUtils.translateColorCodes(formattedName));
            currentMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            currentMaterial.setItemMeta(currentMeta);
        }
        gui.updateItem(currentPageSlot, ItemBuilder.from(currentMaterial).asGuiItem(event -> event.setCancelled(true)));
    }


    // Helper class for Badge data
    public static class Badge {
        private final String id;
        private final String permission;
        private final String displayMaterial;
        private final String displayName;
        private final int displayCustomModelData;
        private final List<String> displayLore;

        public Badge(String id, String permission, String displayMaterial, String displayName, int displayCustomModelData, List<String> displayLore) {
            this.id = id;
            this.permission = permission;
            this.displayMaterial = displayMaterial;
            this.displayName = displayName;
            this.displayCustomModelData = displayCustomModelData;
            this.displayLore = displayLore;
        }

        public String getId() {
            return id;
        }

        public String getPermission() {
            return permission;
        }

        public String getDisplayMaterial() {
            return displayMaterial;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDisplayCustomModelData() {
            return displayCustomModelData;
        }

        public List<String> getDisplayLore() {
            return displayLore;
        }
    }
}