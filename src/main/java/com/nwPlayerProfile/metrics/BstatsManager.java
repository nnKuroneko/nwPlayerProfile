package com.nwPlayerProfile.metrics;

import com.nwPlayerProfile.NwPlayerProfile;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;

public class BstatsManager {
    private Metrics metrics;
    private final NwPlayerProfile plugin;
    private final boolean enabled;

    public BstatsManager(NwPlayerProfile plugin) {
        this.plugin = plugin;
        this.enabled = setupMetrics();
    }

    private boolean setupMetrics() {
        try {
            int pluginId = 25933; // เปลี่ยนเป็น ID ของคุณที่ได้จาก bStats
            this.metrics = new Metrics(plugin, pluginId);
            loadCharts();
            plugin.getLogger().info("✓ bStats metrics enabled!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Could not initialize bStats metrics: " + e.getMessage());
            return false;
        }
    }

    private void loadCharts() {
        if (metrics == null) return;

        metrics.addCustomChart(new SimplePie("plugin_version", () ->
                plugin.getDescription().getVersion()
        ));

        metrics.addCustomChart(new SimplePie("minecraft_version", () ->
                Bukkit.getVersion()
        ));

        metrics.addCustomChart(new SimplePie("server_software", () ->
                Bukkit.getServer().getName() + " " + Bukkit.getServer().getVersion()
        ));
    }

    public boolean isEnabled() {
        return enabled;
    }
}