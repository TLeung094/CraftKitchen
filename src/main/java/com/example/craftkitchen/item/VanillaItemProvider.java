package com.example.craftkitchen.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VanillaItemProvider implements ItemProvider {
    @Override
    public ItemStack createItem(String id, int amount) {
        ItemStack item = new ItemStack(Material.BREAD, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("CraftKitchen: " + id);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public String getItemId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        return "vanilla:" + item.getType().name().toLowerCase();
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
