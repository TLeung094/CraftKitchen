package com.example.craftkitchen.item;

import org.bukkit.inventory.ItemStack;

public interface ItemProvider {
    ItemStack createItem(String id, int amount);
    String getItemId(ItemStack item);
    boolean isCustomItem(ItemStack item);
    void registerRecipes();
}
