package com.example.craftkitchen.listener;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.food.FoodData;
import com.example.craftkitchen.food.FoodEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConsumeListener implements Listener {
    private final CraftKitchen plugin;

    public ConsumeListener(CraftKitchen plugin) {
        this.plugin = plugin;
    }

    public static int scaleDuration(int durationSeconds, CookingQuality quality, double perfectMultiplier) {
        if (quality != CookingQuality.PERFECT) {
            return durationSeconds;
        }
        return (int) Math.round(durationSeconds * perfectMultiplier);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        FoodData food = plugin.getFoodRegistry().get("smoked_steak");
        if (food == null) {
            return;
        }

        CookingQuality quality = readQuality(event.getItem());
        double multiplier = plugin.getConfig().getDouble("settings.perfect-multiplier", 1.5);
        boolean sharing = plugin.getSharedMealManager().hasNearbyPlayers(player);

        for (FoodEffect effect : food.getEffects()) {
            PotionEffectType type = PotionEffectType.getByName(effect.getType());
            if (type != null) {
                int scaled = scaleDuration(effect.getDuration(), quality, multiplier);
                scaled = plugin.getSharedMealManager().scaleDuration(scaled, sharing);
                player.addPotionEffect(new PotionEffect(type, scaled * 20, effect.getAmplifier()));
            }
        }

        if (sharing) {
            player.sendMessage("你與附近的玩家一起享用了美食，效果時間延長！");
        }
    }

    private CookingQuality readQuality(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return CookingQuality.NONE;
        }
        String value = item.getItemMeta().getPersistentDataContainer()
            .get(QualityKeys.qualityKey(plugin), PersistentDataType.STRING);
        if (value == null) {
            return CookingQuality.NONE;
        }
        try {
            return CookingQuality.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CookingQuality.NONE;
        }
    }
}
