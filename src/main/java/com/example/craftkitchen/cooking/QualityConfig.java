package com.example.craftkitchen.cooking;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

/**
 * 品質系統設定：各級機率、效果倍率，以及混合制加權權重。
 *
 * <p>混合制（{@link QualityCalculator}）以「完成速度」與「廚師等級」兩因子加權，
 * 將機率質量由低級（失敗/普通）往高級（精良/稀有/傳說）移動。
 * 純資料物件，可單元測試。
 */
public class QualityConfig {
    private final Map<CookingQuality, Double> probabilities = new EnumMap<>(CookingQuality.class);
    private final Map<CookingQuality, Double> multipliers = new EnumMap<>(CookingQuality.class);
    private double speedWeight = 0.5;
    private double levelWeight = 0.15;

    private QualityConfig() {
    }

    /** 以 plan.md §8.2 預設值建立設定。 */
    public static QualityConfig defaults() {
        QualityConfig qc = new QualityConfig();
        qc.probabilities.put(CookingQuality.FAILED, 0.05);
        qc.probabilities.put(CookingQuality.NORMAL, 0.40);
        qc.probabilities.put(CookingQuality.FINE, 0.35);
        qc.probabilities.put(CookingQuality.RARE, 0.15);
        qc.probabilities.put(CookingQuality.LEGENDARY, 0.05);
        for (CookingQuality q : CookingQuality.values()) {
            qc.multipliers.put(q, q.defaultMultiplier());
        }
        qc.speedWeight = 0.5;
        qc.levelWeight = 0.15;
        return qc;
    }

    /** 從 {@code config.yml} 的 {@code quality} 區段載入，缺漏回退預設。 */
    public static QualityConfig fromConfig(FileConfiguration config) {
        QualityConfig qc = defaults();
        if (config == null) {
            return qc;
        }
        ConfigurationSection section = config.getConfigurationSection("quality");
        if (section == null) {
            return qc;
        }
        qc.speedWeight = section.getDouble("speed-weight", qc.speedWeight);
        qc.levelWeight = section.getDouble("level-weight", qc.levelWeight);
        ConfigurationSection tiers = section.getConfigurationSection("tiers");
        if (tiers != null) {
            for (String key : tiers.getKeys(false)) {
                try {
                    CookingQuality q = CookingQuality.valueOf(key.toUpperCase());
                    ConfigurationSection tier = tiers.getConfigurationSection(key);
                    if (tier == null) {
                        continue;
                    }
                    qc.probabilities.put(q, tier.getDouble("probability", qc.probabilities.get(q)));
                    qc.multipliers.put(q, tier.getDouble("multiplier", qc.multipliers.get(q)));
                } catch (IllegalArgumentException ignored) {
                    // 未知 tier 名稱，忽略
                }
            }
        }
        return qc;
    }

    public double probability(CookingQuality q) {
        return probabilities.getOrDefault(q, 0.0);
    }

    public double multiplier(CookingQuality q) {
        return multipliers.getOrDefault(q, q.defaultMultiplier());
    }

    public double speedWeight() {
        return speedWeight;
    }

    public double levelWeight() {
        return levelWeight;
    }
}
