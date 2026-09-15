package com.example.craftkitchen;

import com.example.craftkitchen.cooking.StoveState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoveStateTest {

    @Test
    void shouldTrackLitAndExtinguished() {
        StoveState stove = new StoveState();
        String key = "world:10,64,20";

        assertFalse(stove.isLit(key));
        stove.light(key);
        assertTrue(stove.isLit(key));
        stove.extinguish(key);
        assertFalse(stove.isLit(key));
    }

    @Test
    void shouldNotAffectOtherLocations() {
        StoveState stove = new StoveState();
        stove.light("world:1,2,3");

        assertFalse(stove.isLit("world:4,5,6"));
        assertEquals(1, stove.litCount());
    }

    @Test
    void shouldCountAndClearLitStoves() {
        StoveState stove = new StoveState();
        stove.light("a:1,1,1");
        stove.light("b:2,2,2");
        assertEquals(2, stove.litCount());

        stove.clear();
        assertEquals(0, stove.litCount());
    }

    @Test
    void shouldIgnoreNullKeys() {
        StoveState stove = new StoveState();
        stove.light(null);
        stove.extinguish(null);
        assertFalse(stove.isLit(null));
        assertEquals(0, stove.litCount());
    }
}
