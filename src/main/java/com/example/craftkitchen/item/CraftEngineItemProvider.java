package com.example.craftkitchen.item;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.food.FoodData;
import com.example.craftkitchen.food.FoodRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * CraftEngine 模式物品提供者。
 *
 * <p>完整實作應透過 CraftEngineAPI 取得自訂物品與模型，但本專案目前未將 CraftEngine 列為
 * 編譯依賴，故此處先以原版行為 fallback：同樣查 FoodRegistry、標記 PDC(food_id)，
 * 確保 CraftEngine 模式下核心玩法鏈路仍可運作。待正式整合 CraftEngine 時僅需改寫此類。
 */
public class CraftEngineItemProvider implements ItemProvider {
    private final CraftKitchen plugin;
    private final NamespacedKey foodIdKey;

    public CraftEngineItemProvider(CraftKitchen plugin) {
        this.plugin = plugin;
        this.foodIdKey = new NamespacedKey(plugin, "food_id");
    }

    @Override
    public ItemStack createItem(String id, int amount) {
        FoodRegistry registry = plugin.getFoodRegistry();
        FoodData data = registry != null ? registry.get(id) : null;

        Material material = Material.BREAD;
        int customModelData = 0;
        String displayName = "CraftKitchen: " + id;
        if (data != null) {
            Material parsed = Material.matchMaterial(data.getMaterial());
            if (parsed != null) {
                material = parsed;
            }
            customModelData = data.getCustomModelData();
            displayName = data.getName();
        }

        ItemStack item = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName).decoration(TextDecoration.ITALIC, false));
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            meta.getPersistentDataContainer().set(foodIdKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public String getItemId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
            .get(foodIdKey, PersistentDataType.STRING);
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return getItemId(item) != null;
    }

    @Override
    public void registerRecipes() {
        // 待整合 CraftEngine 後改寫
    }
}
