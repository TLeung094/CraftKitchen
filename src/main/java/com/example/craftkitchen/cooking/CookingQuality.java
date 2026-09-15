package com.example.craftkitchen.cooking;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * 料理品質分級 — 森羅物語版本。
 *
 * <p>除 {@link #NONE}（未經烹飪流程的物品）外，共有五級，對齊 plan.md §8.2：
 * <ul>
 *   <li>{@link #FAILED} 凋零（灰）— 0.5x 效果，食材枯萎失味</li>
 *   <li>{@link #NORMAL} 常木（白）— 1.0x，平凡而穩定</li>
 *   <li>{@link #FINE} 新芽（綠）— 1.25x，初生嫩芽般清新</li>
 *   <li>{@link #RARE} 古木（青）— 1.5x，深林老木稀有</li>
 *   <li>{@link #LEGENDARY} 森羅（金）— 2.0x，森林之心至味</li>
 * </ul>
 * 機率與倍率可由 {@code config.yml} 的 {@code quality} 區段覆寫；顏色為固定顯示用。
 *
 * <p>命名取自「森羅物語：廚房」童話森林主題：
 * 五級對應森林生命週期 — 凋零（衰敗）→ 常木（平常）→ 新芽（萌發）→ 古木（歷練）→ 森羅（圓融）。
 */
public enum CookingQuality {
    NONE(NamedTextColor.WHITE, "無", 1.0),
    FAILED(NamedTextColor.GRAY, "凋零", 0.5),
    NORMAL(NamedTextColor.WHITE, "常木", 1.0),
    FINE(NamedTextColor.GREEN, "新芽", 1.25),
    RARE(NamedTextColor.AQUA, "古木", 1.5),
    LEGENDARY(NamedTextColor.GOLD, "森羅", 2.0);

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