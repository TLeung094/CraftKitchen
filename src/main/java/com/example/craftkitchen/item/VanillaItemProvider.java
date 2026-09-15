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
 * 原版模式物品提供者：以 CustomModelData + 顯示名稱 + PDC(food_id) 產生物品。
 * 透過持有的 {@link CraftKitchen} 動態查詢 FoodRegistry，使 reload 配置後即時生效。
 */
public class VanillaItemProvider implements ItemProvider {
    private final CraftKitchen plugin;
    private final NamespacedKey foodIdKey;

    public VanillaItemProvider(CraftKitchen plugin) {
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
        // 雙模式配方註冊由 RecipeManager 統一處理
    }

    public NamespacedKey foodIdKey() {
        return foodIdKey;
    }
}
