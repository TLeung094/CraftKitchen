package com.example.craftkitchen.gui;

import java.util.List;

/**
 * GUI 分頁純邏輯工具。
 *
 * <p>不碰 Bukkit，可獨立單元測試。所有方法均為無狀態靜態函式，
 * 接收「總數／頁碼／來源清單」並回傳計算結果，與 inventory 渲染解耦。
 *
 * <p>分頁模型：54 格 inventory 扣除底欄 9 格導航列，每頁可放 {@value #ITEMS_PER_PAGE} 個物品。
 */
public final class GuiLayout {
    private GuiLayout() {
    }

    /** 每頁可顯示的物品格數（54 格 inventory 扣除底欄導航列 9 格）。 */
    public static final int ITEMS_PER_PAGE = 45;

    /**
     * 給定總數，回傳所需頁數（至少 1，即便總數為 0）。
     *
     * @param totalItems 物品總數（可為 0 或負數，一律視為至少一頁）
     * @return 頁數，最小 1
     */
    public static int pageCount(int totalItems) {
        if (totalItems <= 0) {
            return 1;
        }
        return (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
    }

    /**
     * 將頁碼夾在有效範圍 {@code [0, pageCount(totalItems)-1]}。
     *
     * @param page       欲使用的頁碼
     * @param totalItems 物品總數
     * @return 合法的頁碼
     */
    public static int clampPage(int page, int totalItems) {
        int max = pageCount(totalItems) - 1;
        if (page < 0) {
            return 0;
        }
        return Math.min(page, max);
    }

    /**
     * 回傳指定頁碼應顯示的子清單視圖。
     *
     * <p>頁碼超出範圍時回傳空清單（不丟例外）。
     * 回傳值為原清單的 {@link List#subList} 視圖，呼叫端不應修改。
     *
     * @param all  來源清單
     * @param page 頁碼（建議先經 {@link #clampPage} 處理）
     * @return 該頁的子清單
     */
    public static <T> List<T> pageOf(List<T> all, int page) {
        int total = all.size();
        int start = Math.min(page * ITEMS_PER_PAGE, total);
        int end = Math.min(start + ITEMS_PER_PAGE, total);
        return all.subList(start, end);
    }
}
