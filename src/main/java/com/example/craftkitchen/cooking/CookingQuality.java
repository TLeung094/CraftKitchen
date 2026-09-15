package com.example.craftkitchen.cooking;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * 料理品質分級。
 *
 * <p>除 {@link #NONE}（未經烹飪流程的物品）外，共有五級，對齊 plan.md §8.2：
 * <ul>
 *   <li>{@link #FAILED} 失敗（灰）— 0.5x 效果，可能附帶負面</li>
 *   <li>{@link #NORMAL} 普通（白）— 1.0x</li>
 *   <li>{@link #FINE} 精良（綠）— 1.25x</li>
 *   <li>{@link #RARE} 稀有（青）— 1.5x</li>
 *   <li>{@link #LEGENDARY} 傳說（金）— 2.0x</li>
 * </ul>
 * 機率與倍率可由 {@code config.yml} 的 {@code quality} 區段覆寫；顏色為固定顯示用。
 */
public enum CookingQuality {
    NONE(NamedTextColor.WHITE, "無", 1.0),
    FAILED(NamedTextColor.GRAY, "失敗", 0.5),
    NORMAL(NamedTextColor.WHITE, "普通", 1.0),
    FINE(NamedTextColor.GREEN, "精良", 1.25),
    RARE(NamedTextColor.AQUA, "稀有", 1.5),
    LEGENDARY(NamedTextColor.GOLD, "傳說", 2.0);

    private final NamedTextColor color;
    private final String displayName;
    private final double defaultMultiplier;

    CookingQuality(NamedTextColor color, String displayName, double defaultMultiplier) {
        this.color = color;
        this.displayName = displayName;
        this.defaultMultiplier = defaultMultiplier;
    }

    public NamedTextColor color() {
        return color;
    }

    public String displayName() {
        return displayName;
    }

    public double defaultMultiplier() {
        return defaultMultiplier;
    }
}
