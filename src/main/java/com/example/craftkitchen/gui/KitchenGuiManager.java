package com.example.craftkitchen.gui;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.cooking.RecipeDefinition;
import com.example.craftkitchen.food.FoodData;
import com.example.craftkitchen.food.FoodEffect;
import com.example.craftkitchen.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 森羅物語 · 廚房 GUI 管理器。
 *
 * <p>提供五個視窗，承襲童話森林廚房美學 — 苔綠面板、蜜糖琥珀、金葉書頁：
 * <ul>
 *   <li>{@link GuiHolder.View#MAIN 主目錄} — 森羅之門，三徑交匯</li>
 *   <li>{@link GuiHolder.View#RECIPE_LIST 森羅食譜} — 分頁展示所有食譜</li>
 *   <li>{@link GuiHolder.View#RECIPE_DETAIL 食譜詳情} — 食材、步驟、成品</li>
 *   <li>{@link GuiHolder.View#GUID 烹飪指南} — 步驟方塊對應、五級品質、爐灶點燃</li>
 *   <li>{@link GuiHolder.View#LEVEL 廚階} — 料理人稱號、經驗累積</li>
 * </ul>
 *
 * <p>本類為純資訊瀏覽介面，<b>不改動</b>既有的「潛行右鍵方塊觸發烹飪」核心玩法。
 * GUI 內所有點擊一律取消（不允許拿取物品），避免玩家從介面取得免費成品。
 */
public final class KitchenGuiManager implements Listener {
    private final CraftKitchen plugin;

    public KitchenGuiManager(CraftKitchen plugin) {
        this.plugin = plugin;
    }

    // ---- 視窗大小 ----
    private static final int SIZE_MAIN = 27;
    private static final int SIZE_LIST = 54;
    private static final int SIZE_DETAIL = 27;
    private static final int SIZE_GUIDE = 27;
    private static final int SIZE_LEVEL = 27;

    // ---- 分頁導航格（54 格底列）----
    private static final int SLOT_PREV = 45;
    private static final int SLOT_BACK = 49;
    private static final int SLOT_NEXT = 53;

    // ---- 主題色 ----
    // 森羅物語色板 — 苔綠 / 蜜糖 / 古木
    private static final NamedTextColor THEME_PRIMARY = NamedTextColor.DARK_GREEN;
    private static final NamedTextColor THEME_ACCENT  = NamedTextColor.GOLD;
    private static final NamedTextColor THEME_QUIET   = NamedTextColor.GRAY;
    private static final NamedTextColor THEME_BARK    = NamedTextColor.DARK_PURPLE;

    // ---- 森羅物語填充板 ----
    private static final Material PANE_FERN   = Material.FERN;          // 主背景 — 蕨葉
    private static final Material PANE_LEAF   = Material.OAK_LEAVES;    // 邊框 — 橡葉
    private static final Material PANE_MOSS   = Material.MOSS_BLOCK;    // 內容區 — 苔石
    private static final Material PANE_BOOK   = Material.BOOKSHELF;     // 底部導航 — 古書架
    private static final Material PANE_BARK   = Material.BARRIER;       // 罕用 — 古木欄

    // ---- 步驟顯示 ----
    private static final List<String> STEP_ORDER = List.of("cut", "marinate", "cook", "season");
    private static final Map<String, Material> STEP_MATERIAL = Map.of(
        "cut", Material.STONECUTTER,
        "marinate", Material.BREWING_STAND,
        "cook", Material.CAMPFIRE,
        "season", Material.CRAFTING_TABLE
    );
    private static final Map<String, String> STEP_NAME = Map.of(
        "cut", "伐枝",
        "marinate", "浸露",
        "cook", "燒柴",
        "season", "點芳"
    );
    private static final Map<String, String> STEP_BLOCK_DESC = Map.of(
        "cut", "菜板（壓力板／切石機）",
        "marinate", "搪瓷盆子（釀造台）",
        "cook", "炉灶（營火，需打火石點燃）",
        "season", "厨具架（工作台）"
    );

    // ============================================================
    //  開啟各視窗
    // ============================================================

    /** 開啟森羅之門（主目錄）。 */
    public void openMain(Player player) {
        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_MAIN,
            text("❦ 森羅物語 · 廚房 ❦", THEME_ACCENT));
        h.view(GuiHolder.View.MAIN);
        fill(inv, PANE_MOSS, 0, SIZE_MAIN);

        // 上方裝飾（葉飾邊）
        inv.setItem(0, icon(PANE_LEAF, text(""), List.of()));
        inv.setItem(8, icon(PANE_LEAF, text(""), List.of()));
        inv.setItem(18, icon(PANE_LEAF, text(""), List.of()));
        inv.setItem(26, icon(PANE_LEAF, text(""), List.of()));

        inv.setItem(4, icon(Material.KNOWLEDGE_BOOK,
            text("森羅食譜", THEME_PRIMARY),
            lore(THEME_QUIET, "「萬物有靈，皆可成饌」", "翻開這頁，覽盡森羅菜譜")));
        inv.setItem(13, levelIcon(player));
        inv.setItem(22, icon(Material.WRITABLE_BOOK,
            text("烹飪指南", THEME_PRIMARY),
            lore(THEME_QUIET, "「循四時之序，烹森羅真味」", "步驟方塊對應", "森羅品質分級")));

        var active = plugin.getCookingService().getTracker().getActiveRecipes(player.getUniqueId());
        if (!active.isEmpty()) {
            inv.setItem(20, icon(Material.CLOCK,
                text("進行中的食譜", NamedTextColor.YELLOW),
                lore(THEME_QUIET, "正在料理：" + String.join("、", active), "翻開這頁，續森羅之約")));
        }

        player.openInventory(inv);
    }

    /** 開啟森羅食譜（分頁），頁碼會自動夾在合法範圍。 */
    public void openRecipeList(Player player, int page) {
        List<RecipeDefinition> recipes = plugin.getCookingService().getRecipeManager().getRecipes();
        int total = recipes.size();
        page = GuiLayout.clampPage(page, total);

        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_LIST,
            text("❦ 森羅食譜 (" + (page + 1) + "/" + GuiLayout.pageCount(total) + ")", THEME_PRIMARY));
        h.view(GuiHolder.View.RECIPE_LIST).page(page);

        fill(inv, PANE_FERN, 0, GuiLayout.ITEMS_PER_PAGE);
        fillBottomNav(inv, page, total);

        List<RecipeDefinition> pageItems = GuiLayout.pageOf(recipes, page);
        int slot = 0;
        for (RecipeDefinition recipe : pageItems) {
            inv.setItem(slot, recipeIcon(recipe));
            slot++;
        }

        player.openInventory(inv);
    }

    /** 開啟單一食譜詳情；{@code fromPage} 用於「返回」時回到正確的清單頁。 */
    public void openRecipeDetail(Player player, String recipeId, int fromPage) {
        RecipeDefinition recipe = plugin.getCookingService().getRecipeManager().getRecipe(recipeId);
        if (recipe == null) {
            player.sendMessage("找不到食譜：" + recipeId);
            return;
        }

        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_DETAIL,
            text("❦ " + recipe.getName() + " · 詳情", THEME_PRIMARY));
        fill(inv, PANE_MOSS, 0, SIZE_DETAIL);
        h.view(GuiHolder.View.RECIPE_DETAIL).recipeId(recipeId).page(fromPage);

        // 邊框葉飾
        for (int s : new int[]{0, 1, 7, 8, 9, 17, 18, 19, 25, 26}) {
            inv.setItem(s, icon(PANE_LEAF, text(""), List.of()));
        }

        // 成品預覽（含品質 lore）
        FoodData food = plugin.getFoodRegistry().get(recipeId);
        ItemStack result = plugin.getItemProvider().createItem(recipeId, 1);
        var rMeta = result.getItemMeta();
        if (rMeta != null) {
            rMeta.displayName(text(recipe.getName(), THEME_ACCENT));
            ItemLore.apply(rMeta, CookingQuality.NORMAL, food);
            result.setItemMeta(rMeta);
        }
        inv.setItem(4, result);

        // 步驟指示（cut/marinate/cook/season）
        int stepSlot = 11;
        for (String stepId : STEP_ORDER) {
            boolean used = recipe.getSteps().contains(stepId);
            Material mat = STEP_MATERIAL.getOrDefault(stepId, Material.PAPER);
            NamedTextColor color = used ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY;
            String mark = used ? "✦ 本食譜需要" : "✧ 本食譜無需";
            List<Component> l = new ArrayList<>();
            l.add(text(mark, color));
            l.add(text("方塊：" + STEP_BLOCK_DESC.get(stepId), THEME_QUIET));
            inv.setItem(stepSlot, icon(mat, text(STEP_NAME.get(stepId), color), l));
            stepSlot += 2;
        }

        // 食材清單
        int ingSlot = 20;
        for (String ing : recipe.getIngredients()) {
            if (ingSlot > 23) {
                break;
            }
            Material mat = Material.matchMaterial(ing);
            ItemStack ingItem = mat != null ? new ItemStack(mat)
                : icon(Material.BARRIER, text(ing, NamedTextColor.RED), List.of());
            var iMeta = ingItem.getItemMeta();
            if (iMeta != null) {
                iMeta.displayName(text(ing, NamedTextColor.WHITE));
                ingItem.setItemMeta(iMeta);
            }
            inv.setItem(ingSlot, ingItem);
            ingSlot++;
        }

        inv.setItem(18, navIcon(Material.ARROW, text("歸去", NamedTextColor.GREEN), List.of()));

        player.openInventory(inv);
    }

    /** 開啟森羅烹飪指南。 */
    public void openGuide(Player player) {
        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_GUIDE,
            text("❦ 森羅烹飪指南", THEME_PRIMARY));
        fill(inv, PANE_FERN, 0, SIZE_GUIDE);
        h.view(GuiHolder.View.GUIDE);

        // 邊框
        for (int s : new int[]{0, 1, 7, 8, 9, 17, 18, 19, 25, 26}) {
            inv.setItem(s, icon(PANE_LEAF, text(""), List.of()));
        }

        inv.setItem(4, icon(Material.WRITABLE_BOOK,
            text("森羅烹飪指南", THEME_ACCENT),
            lore(THEME_QUIET,
                "「循四時之序，烹森羅真味」",
                "潛行右鍵方塊，觸發烹飪步驟",
                "完成四步，方得成品")));

        // 步驟指引
        int slot = 10;
        for (String stepId : STEP_ORDER) {
            inv.setItem(slot, icon(STEP_MATERIAL.get(stepId),
                text(STEP_NAME.get(stepId), THEME_PRIMARY),
                List.of(text("方塊：" + STEP_BLOCK_DESC.get(stepId), THEME_QUIET))));
            slot++;
        }

        // 品質分級
        List<Component> qualityLore = new ArrayList<>();
        qualityLore.add(text("「森林有五境，菜亦有五品」", NamedTextColor.DARK_GREEN));
        qualityLore.add(text("完成料理時隨機決定品質", THEME_QUIET));
        qualityLore.add(text("速度與等級可提升高級機率", THEME_QUIET));
        qualityLore.add(Component.empty());
        for (CookingQuality q : CookingQuality.values()) {
            if (q == CookingQuality.NONE) {
                continue;
            }
            qualityLore.add(text(q.displayName() + " ×" + q.defaultMultiplier() + " 效果", q.color()));
        }
        inv.setItem(15, icon(Material.NETHER_STAR,
            text("森羅品質分級", THEME_ACCENT), qualityLore));

        inv.setItem(16, icon(Material.FLINT_AND_STEEL,
            text("爐灶點燃", THEME_ACCENT),
            lore(THEME_QUIET,
                "潛行右鍵營火 + 打火石 → 點燃",
                "潛行右鍵營火 + 水桶 → 澆滅",
                "「燒柴」步驟需爐灶已點燃")));

        inv.setItem(18, navIcon(Material.ARROW, text("歸去", NamedTextColor.GREEN), List.of()));

        player.openInventory(inv);
    }

    /** 開啟森羅廚階。 */
    public void openLevel(Player player) {
        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_LEVEL,
            text("❦ 森羅廚階", THEME_ACCENT));
        fill(inv, PANE_MOSS, 0, SIZE_LEVEL);
        h.view(GuiHolder.View.LEVEL);

        // 邊框
        for (int s : new int[]{0, 1, 7, 8, 9, 17, 18, 19, 25, 26}) {
            inv.setItem(s, icon(PANE_LEAF, text(""), List.of()));
        }

        var lm = plugin.getLevelManager();
        int xp = lm.getExperience(player.getUniqueId());
        int level = lm.getLevel(player.getUniqueId());
        String title = lm.getTitle(player.getUniqueId());
        int nextLevelXp = level * 50;
        int remaining = nextLevelXp - xp;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        var meta = head.getItemMeta();
        if (meta != null) {
            meta.displayName(text("森羅廚階 · " + level + " 階", THEME_ACCENT));
            List<Component> l = new ArrayList<>();
            l.add(text("稱號：" + title, THEME_PRIMARY));
            l.add(text("「" + flavorForLevel(level) + "」", NamedTextColor.DARK_GREEN));
            l.add(text("經驗：" + xp, NamedTextColor.WHITE));
            l.add(text("距下一階：" + remaining + " 經驗", NamedTextColor.GREEN));
            l.add(text("（每 50 經驗升一階）", THEME_QUIET));
            meta.lore(l.stream().map(this::noItalic).toList());
            head.setItemMeta(meta);
        }
        inv.setItem(4, head);

        inv.setItem(22, icon(Material.COOKED_BEEF,
            text("如何積累經驗", THEME_PRIMARY),
            lore(THEME_QUIET,
                "完成料理即得森羅眷顧",
                "森羅 +50 / 古木 +40 / 新芽 +30",
                "常木 +20 / 凋零 +5")));

        inv.setItem(18, navIcon(Material.ARROW, text("歸去", NamedTextColor.GREEN), List.of()));

        player.openInventory(inv);
    }

    // ============================================================
    //  事件處理
    // ============================================================

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder h)) {
            return;
        }
        // 一律取消：GUI 僅供瀏覽，玩家不得取走任何物品
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.equals(h.player())) {
            return;
        }

        int slot = event.getRawSlot();
        switch (h.view()) {
            case MAIN -> handleMainClick(player, slot);
            case RECIPE_LIST -> handleListClick(player, h, slot);
            case RECIPE_DETAIL -> handleDetailClick(player, h, slot);
            case GUIDE, LEVEL -> handleSimpleBack(player, slot);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHolder) {
            event.setCancelled(true);
        }
    }

    // ============================================================
    //  點擊分派
    // ============================================================

    private void handleMainClick(Player player, int slot) {
        switch (slot) {
            case 4 -> openRecipeList(player, 0);
            case 13 -> openLevel(player);
            case 22 -> openGuide(player);
            case 20 -> showActiveProgress(player);
            default -> {
            }
        }
    }

    private void handleListClick(Player player, GuiHolder h, int slot) {
        if (slot == SLOT_PREV) {
            openRecipeList(player, h.page() - 1);
            return;
        }
        if (slot == SLOT_NEXT) {
            openRecipeList(player, h.page() + 1);
            return;
        }
        if (slot == SLOT_BACK) {
            openMain(player);
            return;
        }
        if (slot < 0 || slot >= GuiLayout.ITEMS_PER_PAGE) {
            return;
        }
        List<RecipeDefinition> pageItems = GuiLayout.pageOf(
            plugin.getCookingService().getRecipeManager().getRecipes(), h.page());
        if (slot < pageItems.size()) {
            openRecipeDetail(player, pageItems.get(slot).getId(), h.page());
        }
    }

    private void handleDetailClick(Player player, GuiHolder h, int slot) {
        if (slot == 18) {
            openRecipeList(player, h.page());
        }
    }

    private void handleSimpleBack(Player player, int slot) {
        if (slot == 18) {
            openMain(player);
        }
    }

    /** 關閉 GUI 並以聊天訊息列出玩家進行中食譜的步驟進度。 */
    private void showActiveProgress(Player player) {
        var tracker = plugin.getCookingService().getTracker();
        var active = tracker.getActiveRecipes(player.getUniqueId());
        if (active.isEmpty()) {
            return;
        }
        player.closeInventory();
        var rm = plugin.getCookingService().getRecipeManager();
        for (String rid : active) {
            var done = tracker.getCompletedSteps(player.getUniqueId(), rid);
            var recipe = rm.getRecipe(rid);
            String name = recipe != null ? recipe.getName() : rid;
            player.sendMessage(name + " 進度：已完成 "
                + (done.isEmpty() ? "無" : String.join("、", done)));
        }
    }

    // ============================================================
    //  圖示建構工具
    // ============================================================

    private ItemStack icon(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(noItalic(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(this::noItalic).toList());
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack navIcon(Material mat, Component name, List<Component> lore) {
        return icon(mat, name, lore);
    }

    private ItemStack recipeIcon(RecipeDefinition recipe) {
        FoodData food = plugin.getFoodRegistry().get(recipe.getId());
        ItemStack item = plugin.getItemProvider().createItem(recipe.getId(), 1);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(text(recipe.getName(), THEME_ACCENT));
            List<Component> l = new ArrayList<>();
            l.add(text("「" + STEP_NAME.get(recipe.getSteps().get(0)) + "」",
                NamedTextColor.DARK_GREEN));
            l.add(text("步驟：" + String.join(" → ", recipe.getSteps().stream()
                .map(STEP_NAME::get).toList()), THEME_QUIET));
            l.add(text("食材：" + (recipe.getIngredients().isEmpty()
                ? "無" : String.join("、", recipe.getIngredients())), THEME_QUIET));
            if (food != null && food.getEffects() != null && !food.getEffects().isEmpty()) {
                l.add(text("效果：" + summarizeEffects(food), NamedTextColor.WHITE));
            }
            l.add(Component.empty());
            l.add(text("✦ 翻開這頁，覽森羅詳情 ✦", NamedTextColor.YELLOW));
            meta.lore(l.stream().map(this::noItalic).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack levelIcon(Player player) {
        var lm = plugin.getLevelManager();
        int level = lm.getLevel(player.getUniqueId());
        String title = lm.getTitle(player.getUniqueId());
        int xp = lm.getExperience(player.getUniqueId());
        return icon(Material.PLAYER_HEAD,
            text("森羅廚階", THEME_ACCENT),
            List.of(
                text(level + " 階 · " + title, THEME_PRIMARY),
                text("「" + flavorForLevel(level) + "」", NamedTextColor.DARK_GREEN),
                text("經驗：" + xp, NamedTextColor.WHITE),
                text("✦ 翻開這頁，覽森羅廚道 ✦", NamedTextColor.YELLOW)
            ));
    }

    // ============================================================
    //  版面填充
    // ============================================================

    private void fill(Inventory inv, Material mat, int from, int to) {
        ItemStack pane = icon(mat, text(""), List.of());
        for (int i = from; i < to; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, pane);
            }
        }
    }

    private void fillBottomNav(Inventory inv, int page, int total) {
        // 底部導航列用古書架，象徵森羅古書
        ItemStack navBg = icon(PANE_BOOK, text(""), List.of());
        for (int i = 45; i < SIZE_LIST; i++) {
            inv.setItem(i, navBg);
        }
        int pages = GuiLayout.pageCount(total);
        if (page > 0) {
            inv.setItem(SLOT_PREV, navIcon(Material.ARROW,
                text("前章", NamedTextColor.YELLOW), List.of()));
        }
        inv.setItem(SLOT_BACK, navIcon(Material.BARRIER,
            text("歸森羅之門", NamedTextColor.GREEN), List.of()));
        if (page < pages - 1) {
            inv.setItem(SLOT_NEXT, navIcon(Material.ARROW,
                text("後章", NamedTextColor.YELLOW), List.of()));
        }
    }

    // ============================================================
    //  文字工具
    // ============================================================

    private Component text(String s) {
        return s.isEmpty() ? Component.empty() : Component.text(s).decoration(TextDecoration.ITALIC, false);
    }

    private Component text(String s, NamedTextColor c) {
        return Component.text(s, c).decoration(TextDecoration.ITALIC, false);
    }

    private Component noItalic(Component c) {
        return c.decoration(TextDecoration.ITALIC, false);
    }

    private List<Component> lore(NamedTextColor color, String... lines) {
        List<Component> list = new ArrayList<>();
        for (String l : lines) {
            list.add(text(l, color));
        }
        return list;
    }

    private String flavorForLevel(int level) {
        // 森羅物語各階詩句
        return switch (level) {
            case 0 -> "森之門外，學步新芽";
            case 1, 2, 3 -> "林間漫步，拾薪溫火";
            case 4, 5, 6 -> "溪畔炊煙，識味知節";
            case 7, 8, 9 -> "古木成蔭，烹飪有方";
            case 10, 11, 12 -> "森羅深處，得窺真味";
            case 13, 14, 15 -> "萬物有靈，皆可成饌";
            default -> "森羅圓融，調味天成";
        };
    }

    private String summarizeEffects(FoodData food) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < food.getEffects().size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            FoodEffect e = food.getEffects().get(i);
            sb.append(e.getType()).append(' ').append(e.getDuration()).append('s');
            if (e.getAmplifier() > 0) {
                sb.append(" (").append(e.getAmplifier() + 1).append(')');
            }
        }
        return sb.toString();
    }
}