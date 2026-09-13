package com.example.craftkitchen.config;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.item.ItemMode;
import org.bukkit.Bukkit;

public final class ModeDetector {
    private ModeDetector() {
    }

    public static ItemMode detect(CraftKitchen plugin) {
        String configuredMode = plugin.getConfig().getString("settings.item-mode", "auto");

        if (configuredMode.equalsIgnoreCase("vanilla")) {
            plugin.getLogger().info("已強制使用原版模式");
            return ItemMode.VANILLA;
        }

        if (configuredMode.equalsIgnoreCase("craftengine")) {
            plugin.getLogger().info("已強制使用 CraftEngine 模式");
            return ItemMode.CRAFTENGINE;
        }

        if (Bukkit.getPluginManager().getPlugin("CraftEngine") != null) {
            plugin.getLogger().info("偵測到 CraftEngine，啟用 CraftEngine 模式");
            return ItemMode.CRAFTENGINE;
        }

        plugin.getLogger().info("未偵測到 CraftEngine，啟用原版模式");
        return ItemMode.VANILLA;
    }
}
