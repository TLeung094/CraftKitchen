package com.example.craftkitchen.item;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * CraftEngine 模式物品提供者：以 CraftEngine 自訂物品（{@code craftkitchen:<id>}) 建立與識別。
 *
 * <p>透過 CraftEngine 穩定 API（{@link CraftEngineItems}）運作：
 * <ul>
 *   <li>{@code createItem} → 查 {@link CraftEngineItems#byId} 後呼叫 {@link BukkitItemDefinition#buildBukkitItem()}</li>
 *   <li>{@code getItemId} → {@link CraftEngineItems#getCustomItemId}，限 {@code craftkitchen} namespace</li>
 *   <li>{@code isCustomItem} → 同上 namespace gate</li>
 * </ul>
 *
 * <p>CE 自訂物品與方塊定義於 {@code craftengine-pack/craftkitchen/configuration/}，
 * 部署時複製到 {@code plugins/CraftEngine/resources/} 並執行 {@code /ce reload all}。
 */
public class CraftEngineItemProvider implements ItemProvider {

    private static final String NAMESPACE = "craftkitchen";

    @Override
    public ItemStack createItem(String id, int amount) {
        BukkitItemDefinition def = CraftEngineItems.byId(NAMESPACE + ":" + id);
        if (def == null) {
            // CE 物品尚未載入或 id 不存在；給予紙張避免 null，並由上層決策是否提示玩家
            return new ItemStack(Material.PAPER, Math.max(1, amount));
        }
        ItemStack item = def.buildBukkitItem();
        item.setAmount(Math.max(1, amount));
        return item;
    }

    @Override
    public String getItemId(ItemStack item) {
        Key customId = safeCustomId(item);
        if (customId == null) {
            return null;
        }
        if (!NAMESPACE.equals(customId.namespace())) {
            return null;
        }
        return customId.value();
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        Key customId = safeCustomId(item);
        return customId != null && NAMESPACE.equals(customId.namespace());
    }

    @Override
    public void registerRecipes() {
        // CE 配方在 craftengine-pack/craftkitchen/configuration/ YAML 中定義，
        // 由 CraftEngine 自行載入，無需 Java 端註冊。
    }

    private static Key safeCustomId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        return CraftEngineItems.getCustomItemId(item);
    }
}