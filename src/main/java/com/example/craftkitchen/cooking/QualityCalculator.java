package com.example.craftkitchen.cooking;

/**
 * 混合制品質判定引擎（純邏輯，不碰 Bukkit，可單元測試）。
 *
 * <p>判定流程：
 * <ol>
 *   <li>以 {@link QualityConfig} 的基礎機率為底。</li>
 *   <li>計算 {@code speedScore} ∈ [0,1]：完成時間越短（在完美窗口內為 1.0）越高，
 *       逾時為 0。</li>
 *   <li>計算 {@code levelScore} ∈ [0,1]：廚師等級 / 20（封頂 1.0）。</li>
 *   <li>{@code boost = speedScore*speedWeight + levelScore*levelWeight}。</li>
 *   <li>高級（精良/稀有/傳說）機率 ×(1 + boost*加成係數)，低級（失敗/普通）×(1 - boost*衰減係數)。</li>
 *   <li>正規化後以傳入 {@code roll} ∈ [0,1) 累積抽樣決定等級。</li>
 * </ol>
 * 完成速度越快、廚師等級越高，高級出現機率越高——但仍有隨機性，符合 plan.md 混合制精神。
 */
public class QualityCalculator {
    private static final CookingQuality[] TIERS = {
        CookingQuality.FAILED, CookingQuality.NORMAL, CookingQuality.FINE,
        CookingQuality.RARE, CookingQuality.LEGENDARY
    };

    private final QualityConfig config;

    public QualityCalculator() {
        this(QualityConfig.defaults());
    }

    public QualityCalculator(QualityConfig config) {
        this.config = config;
    }

    public QualityConfig config() {
        return config;
    }

    /** 取指定品質的效果倍率（{@link CookingQuality#NONE} 與未設定者回退預設）。 */
    public double multiplier(CookingQuality quality) {
        return config.multiplier(quality);
    }

    /**
     * 依完成時間、完美窗口、逾時、廚師等級與亂數抽樣決定品質。
     *
     * @param elapsedMs       本次烹飪 session 已耗時（毫秒）
     * @param perfectWindowMs 完美窗口（毫秒），此內完成 speedScore=1.0
     * @param timeoutMs       session 逾時（毫秒），作為 speedScore 的分母
     * @param level          廚師等級
     * @param roll           亂數 ∈ [0,1)
     * @return               五級品質之一（永不回傳 {@link CookingQuality#NONE}）
     */
    public CookingQuality roll(long elapsedMs, long perfectWindowMs, long timeoutMs, int level, double roll) {
        double[] base = new double[TIERS.length];
        for (int i = 0; i < TIERS.length; i++) {
            base[i] = config.probability(TIERS[i]);
        }

        double speedScore = computeSpeedScore(elapsedMs, perfectWindowMs, timeoutMs);
        double levelScore = Math.min(1.0, Math.max(0, level) / 20.0);
        double boost = Math.max(0.0, speedScore * config.speedWeight() + levelScore * config.levelWeight());

        // 高級放大、低級衰減；加成係數隨級別遞增
        double[] factor = {
            Math.max(0.0, 1.0 - boost),
            Math.max(0.0, 1.0 - boost * 0.5),
            1.0 + boost,
            1.0 + boost * 1.5,
            1.0 + boost * 2.0
        };

        double[] adjusted = new double[TIERS.length];
        double sum = 0.0;
        for (int i = 0; i < TIERS.length; i++) {
            adjusted[i] = Math.max(0.0, base[i] * factor[i]);
            sum += adjusted[i];
        }
        if (sum <= 0.0) {
            return CookingQuality.NORMAL;
        }

        double r = Math.min(roll, 0.999999999) * sum;
        double acc = 0.0;
        for (int i = 0; i < TIERS.length; i++) {
            acc += adjusted[i];
            if (r < acc) {
                return TIERS[i];
            }
        }
        return TIERS[TIERS.length - 1];
    }

    private double computeSpeedScore(long elapsedMs, long perfectWindowMs, long timeoutMs) {
        if (perfectWindowMs > 0 && elapsedMs <= perfectWindowMs) {
            return 1.0;
        }
        if (timeoutMs <= 0) {
            return 1.0;
        }
        if (elapsedMs >= timeoutMs) {
            return 0.0;
        }
        return 1.0 - ((double) elapsedMs) / timeoutMs;
    }
}
