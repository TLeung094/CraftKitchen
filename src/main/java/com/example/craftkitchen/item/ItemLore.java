package com.example.craftkitchen.item;

import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.food.FoodData;
import com.example.craftkitchen.food.FoodEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 料理成品動態 Lore 產生器（森羅物語版）。
 *
 * <p>將森羅品質分級（含顏色）與效果摘要寫入物品 lore，讓玩家在滑鼠懸停時可見。
 * 與 {@link ItemProvider#createItem} 解耦：由知道品質的呼叫端（listener／指令）
 * 在取得 {@link ItemMeta} 後呼叫 {@link #apply} 套用。
 *
 * <p>品質行格式：「✦ 森羅之味：{品質名}」，呈現森林物語調性。
 */
public final class ItemLore {
    private ItemLore() {
    }

    /**
     * 將品質與效果 lore 寫入 meta。
     *
     * @param meta    物品 meta（不可為 null）
     * @param quality 品質（{@link CookingQuality#NONE} 時不寫品質行）
     * @param food    對應食物定義（可能為 null）
     */
    public static void apply(ItemMeta meta, CookingQuality quality, FoodData food) {
        if (meta == null) {
            return;
        }
        List<Component> lore = new ArrayList<>();
        if (quality != null && quality != CookingQuality.NONE) {
            lore.add(
                    Component.text("✦ 森羅之味：" + quality.displayName(), quality.color())
                        .decoration(TextDecoration.ITALIC, false)
                );
        }
        if (food != null && food.getEffects() != null && !food.getEffects().isEmpty()) {
            lore.add(
                    Component.text(summarizeEffects(food))
                        .decoration(TextDecoration.ITALIC, false)
                );
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
    }

    private static String summarizeEffects(FoodData food) {
        StringBuilder sb = new StringBuilder("✦ 效果：");
        for (int i = 0; i < food.getEffects().size(); i++) {
            FoodEffect effect = food.getEffects().get(i);
            if (i > 0) {
                sb.append("、");
            }
            sb.append(effect.getType())
                .append(' ').append(effect.getDuration()).append('s');
            if (effect.getAmplifier() > 0) {
                sb.append(" (").append(effect.getAmplifier() + 1).append(')');
            }
        }
        return sb.toString();
    }
}