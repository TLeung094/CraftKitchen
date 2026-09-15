package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.cooking.QualityCalculator;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityCalculatorTest {

    @Test
    void shouldReturnConfiguredDefaultMultipliers() {
        QualityCalculator calc = new QualityCalculator();
        assertEquals(0.5, calc.multiplier(CookingQuality.FAILED));
        assertEquals(1.0, calc.multiplier(CookingQuality.NORMAL));
        assertEquals(1.25, calc.multiplier(CookingQuality.FINE));
        assertEquals(1.5, calc.multiplier(CookingQuality.RARE));
        assertEquals(2.0, calc.multiplier(CookingQuality.LEGENDARY));
    }

    @Test
    void shouldReturnFailedForLowRollAndLegendaryForHighRoll() {
        QualityCalculator calc = new QualityCalculator();
        assertEquals(CookingQuality.FAILED, calc.roll(5_000, 30_000, 300_000, 1, 0.0));
        assertEquals(CookingQuality.LEGENDARY, calc.roll(5_000, 30_000, 300_000, 1, 0.999999));
    }

    @Test
    void shouldNeverReturnNone() {
        QualityCalculator calc = new QualityCalculator();
        for (double r = 0.0; r < 1.0; r += 0.01) {
            assertNotEquals(CookingQuality.NONE, calc.roll(5_000, 30_000, 300_000, 1, r));
        }
    }

    @Test
    void shouldFavorHigherTiersWhenFastVersusSlow() {
        QualityCalculator calc = new QualityCalculator();
        Random rnd = new Random(42L);
        int fastHigh = 0;
        int slowHigh = 0;
        int iterations = 50_000;
        for (int i = 0; i < iterations; i++) {
            double r = rnd.nextDouble();
            CookingQuality fast = calc.roll(5_000, 30_000, 300_000, 1, r);
            CookingQuality slow = calc.roll(290_000, 30_000, 300_000, 1, r);
            if (fast == CookingQuality.RARE || fast == CookingQuality.LEGENDARY) {
                fastHigh++;
            }
            if (slow == CookingQuality.RARE || slow == CookingQuality.LEGENDARY) {
                slowHigh++;
            }
        }
        assertTrue(fastHigh > slowHigh,
            "快速完成應產生更多稀有/傳說：fast=" + fastHigh + " slow=" + slowHigh);
    }

    @Test
    void shouldFavorHigherTiersWithHigherLevel() {
        QualityCalculator calc = new QualityCalculator();
        Random rnd = new Random(7L);
        int lowLevelHigh = 0;
        int highLevelHigh = 0;
        int iterations = 50_000;
        for (int i = 0; i < iterations; i++) {
            double r = rnd.nextDouble();
            CookingQuality low = calc.roll(60_000, 30_000, 300_000, 1, r);
            CookingQuality high = calc.roll(60_000, 30_000, 300_000, 20, r);
            if (low == CookingQuality.RARE || low == CookingQuality.LEGENDARY) {
                lowLevelHigh++;
            }
            if (high == CookingQuality.RARE || high == CookingQuality.LEGENDARY) {
                highLevelHigh++;
            }
        }
        assertTrue(highLevelHigh > lowLevelHigh,
            "高廚師等級應產生更多稀有/傳說：low=" + lowLevelHigh + " high=" + highLevelHigh);
    }
}
