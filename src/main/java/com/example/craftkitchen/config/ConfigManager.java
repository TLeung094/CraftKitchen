package com.example.craftkitchen.config;

import com.example.craftkitchen.CraftKitchen;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigManager {
    private final CraftKitchen plugin;

    public ConfigManager(CraftKitchen plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfiguration() {
        return plugin.getConfig();
    }

    public String getItemModeSetting() {
        return getConfiguration().getString("settings.item-mode", "auto");
    }

    public boolean isAutoMode() {
        return getItemModeSetting().equalsIgnoreCase("auto");
    }
}
