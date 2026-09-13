package com.example.craftkitchen.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CraftEngineItemProvider implements ItemProvider {
    @Override
    public ItemStack createItem(String id, int amount) {
        return new ItemStack(Material.BREAD, amount);
    }

    @Override
    public String getItemId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        return "craftengine:" + item.getType().name().toLowerCase();
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    @Override
    public void registerRecipes() {
        // 由 RecipeManager 之後補上實作
    }
}
