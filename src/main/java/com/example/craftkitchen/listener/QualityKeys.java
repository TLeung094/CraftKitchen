package com.example.craftkitchen.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class QualityKeys {
    private QualityKeys() {
    }

    public static NamespacedKey qualityKey(Plugin plugin) {
        return new NamespacedKey(plugin, "cooking_quality");
    }
}
