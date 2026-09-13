package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.listener.ConsumeListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodEffectScalingTest {

    @Test
    void shouldScaleDurationForPerfectQuality() {
        assertEquals(45, ConsumeListener.scaleDuration(30, CookingQuality.PERFECT, 1.5));
    }

    @Test
    void shouldNotScaleDurationForNormalQuality() {
        assertEquals(30, ConsumeListener.scaleDuration(30, CookingQuality.NORMAL, 1.5));
    }

    @Test
    void shouldNotScaleDurationForNoneQuality() {
        assertEquals(30, ConsumeListener.scaleDuration(30, CookingQuality.NONE, 1.5));
    }

    @Test
    void shouldRoundScaledDuration() {
        assertEquals(50, ConsumeListener.scaleDuration(33, CookingQuality.PERFECT, 1.5));
    }
}
