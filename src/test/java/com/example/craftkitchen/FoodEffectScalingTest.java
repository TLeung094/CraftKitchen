package com.example.craftkitchen;

import com.example.craftkitchen.listener.ConsumeListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodEffectScalingTest {

    @Test
    void shouldScaleDurationByMultiplier() {
        assertEquals(45, ConsumeListener.scaleDuration(30, 1.5));
        assertEquals(30, ConsumeListener.scaleDuration(30, 1.0));
        assertEquals(15, ConsumeListener.scaleDuration(30, 0.5));
        assertEquals(60, ConsumeListener.scaleDuration(30, 2.0));
    }

    @Test
    void shouldNotScaleWhenMultiplierIsOne() {
        assertEquals(30, ConsumeListener.scaleDuration(30, 1.0));
    }

    @Test
    void shouldRoundScaledDuration() {
        assertEquals(50, ConsumeListener.scaleDuration(33, 1.5));
    }
}
