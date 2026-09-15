package com.example.craftkitchen.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;

/**
 * 廚房 GUI 的 InventoryHolder 標記。
 *
 * <p>用途有二：
 * <ol>
 *   <li>讓 {@link KitchenGuiManager} 在 {@code InventoryClickEvent} 中以
 *       {@code event.getInventory().getHolder() instanceof GuiHolder} 快速辨識「這是我的 GUI」，
 *       避免干擾玩家身上或第三方介面。</li>
 *   <li>攜帶 GUI 當前狀態（視窗類型、頁碼、選中食譜），供點擊處理判斷導向。</li>
 * </ol>
 *
 * <p>採「標記 holder」模式（非容器 holder）：本類不持有/建立 inventory，
 * {@link #getInventory()} 回傳 null——Bukkit 在建立 inventory 時只用 holder 作為身分標記，
 * 不會反向呼叫此方法，回傳 null是安全且常見做法。
 */
public final class GuiHolder implements InventoryHolder {

    /** GUI 視窗類型。 */
    public enum View {
        /** 廚房主目錄。 */
        MAIN,
        /** 食譜清單（分頁）。 */
        RECIPE_LIST,
        /** 單一食譜詳情。 */
        RECIPE_DETAIL,
        /** 烹飪指南（步驟→方塊、品質說明）。 */
        GUIDE,
        /** 料理等級資訊。 */
        LEVEL
    }

    private View view;
    private int page;
    private String recipeId;
    private final Player player;

    public GuiHolder(Player player) {
        this.player = Objects.requireNonNull(player);
        this.view = View.MAIN;
        this.page = 0;
        this.recipeId = null;
    }

    public Player player() {
        return player;
    }

    public View view() {
        return view;
    }

    public int page() {
        return page;
    }

    public String recipeId() {
        return recipeId;
    }

    public GuiHolder view(View view) {
        this.view = view;
        return this;
    }

    public GuiHolder page(int page) {
        this.page = page;
        return this;
    }

    public GuiHolder recipeId(String recipeId) {
        this.recipeId = recipeId;
        return this;
    }

    /**
     * 一律回傳 null：本 holder 僅作身分標記，不持有 inventory。
     *
     * @return null
     */
    @Override
    public Inventory getInventory() {
        return null;
    }
}
