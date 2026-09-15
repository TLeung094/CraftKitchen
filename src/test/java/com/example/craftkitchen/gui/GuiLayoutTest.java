package com.example.craftkitchen.gui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GuiLayout} 分頁純邏輯測試。
 *
 * <p>不涉及 Bukkit，驗證頁數計算、頁碼夾界、子清單切片正確性。
 */
class GuiLayoutTest {

    @Test
    void pageCount_zeroOrFew_isAtLeastOne() {
        assertEquals(1, GuiLayout.pageCount(0));
        assertEquals(1, GuiLayout.pageCount(1));
        assertEquals(1, GuiLayout.pageCount(GuiLayout.ITEMS_PER_PAGE));
    }

    @Test
    void pageCount_overflowsToMultiplePages() {
        assertEquals(2, GuiLayout.pageCount(GuiLayout.ITEMS_PER_PAGE + 1));
        assertEquals(2, GuiLayout.pageCount(GuiLayout.ITEMS_PER_PAGE * 2));
        assertEquals(3, GuiLayout.pageCount(GuiLayout.ITEMS_PER_PAGE * 2 + 1));
    }

    @Test
    void clampPage_boundsToValidRange() {
        assertEquals(0, GuiLayout.clampPage(-1, 50));
        assertEquals(0, GuiLayout.clampPage(0, 50));
        assertEquals(1, GuiLayout.clampPage(1, 50));
        assertEquals(1, GuiLayout.clampPage(99, 50));
    }

    @Test
    void pageOf_slicesCorrectWindow() {
        List<String> all = new ArrayList<>();
        for (int i = 0; i < GuiLayout.ITEMS_PER_PAGE + 5; i++) {
            all.add("r" + i);
        }
        List<String> p0 = GuiLayout.pageOf(all, 0);
        assertEquals(GuiLayout.ITEMS_PER_PAGE, p0.size());
        assertEquals("r0", p0.get(0));
        assertEquals("r" + (GuiLayout.ITEMS_PER_PAGE - 1), p0.get(GuiLayout.ITEMS_PER_PAGE - 1));

        List<String> p1 = GuiLayout.pageOf(all, 1);
        assertEquals(5, p1.size());
        assertEquals("r" + GuiLayout.ITEMS_PER_PAGE, p1.get(0));
    }

    @Test
    void pageOf_emptyList_isEmpty() {
        assertTrue(GuiLayout.pageOf(List.of(), 0).isEmpty());
    }

    @Test
    void pageOf_outOfRange_isEmpty() {
        List<String> all = List.of("a", "b");
        assertTrue(GuiLayout.pageOf(all, 5).isEmpty());
    }
}
