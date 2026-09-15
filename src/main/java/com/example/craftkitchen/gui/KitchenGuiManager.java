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
 * 廚房 GUI 管理器：食譜瀏覽、料理等級、烹飪指南。
 *
 * <p>提供五個視窗：
 * <ul>
 *   <li>{@link GuiHolder.View#MAIN 主目錄} — 食譜大全／等級／指南入口</li>
 *   <li>{@link GuiHolder.View#RECIPE_LIST 食譜清單} — 分頁顯示所有食譜，點擊進入詳情</li>
 *   <li>{@link GuiHolder.View#RECIPE_DETAIL 食譜詳情} — 成品預覽、步驟指示、食材清單</li>
 *   <li>{@link GuiHolder.View#GUIDE 烹飪指南} — 步驟→方塊對應、品質分級、爐灶點燃說明</li>
 *   <li>{@link GuiHolder.View#LEVEL 料理等級} — 等級、稱號、經驗、升級進度</li>
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

    // ---- 步驟顯示 ----
    private static final List<String> STEP_ORDER = List.of("cut", "marinate", "cook", "season");
    private static final Map<String, Material> STEP_MATERIAL = Map.of(
        "cut", Material.STONECUTTER,
        "marinate", Material.BREWING_STAND,
        "cook", Material.CAMPFIRE,
        "season", Material.CRAFTING_TABLE
    );
    private static final Map<String, String> STEP_BLOCK_DESC = Map.of(
        "cut", "壓力板 / 切石機",
        "marinate", "釀造台",
        "cook", "營火 / 靈魂營火（需打火石點燃）",
        "season", "工作台"
    );

    // ============================================================
    //  開啟各視窗
    // ============================================================

    /** 開啟廚房主目錄。 */
    public void openMain(Player player) {
        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_MAIN,
            text("CraftKitchen 廚房目錄", NamedTextColor.GOLD));
        h.view(GuiHolder.View.MAIN);
        fill(inv, Material.GRAY_STAINED_GLASS_PANE, 0, SIZE_MAIN);

        inv.setItem(4, icon(Material.KNOWLEDGE_BOOK,
            text("食譜大全", NamedTextColor.AQUA),
            lore(NamedTextColor.GRAY, "點擊查看所有可用食譜")));
        inv.setItem(13, levelIcon(player));
        inv.setItem(22, icon(Material.WRITTEN_BOOK,
            text("烹飪指南", NamedTextColor.AQUA),
            lore(NamedTextColor.GRAY, "步驟與方塊對應", "品質分級說明")));

        var active = plugin.getCookingService().getTracker().getActiveRecipes(player.getUniqueId());
        if (!active.isEmpty()) {
            inv.setItem(20, icon(Material.CLOCK,
                text("進行中的食譜", NamedTextColor.YELLOW),
                lore(NamedTextColor.GRAY, "正在製作：" + String.join(", ", active), "點擊查看進度")));
        }

        player.openInventory(inv);
    }

    /** 開啟食譜清單（分頁），頁碼會自動夾在合法範圍。 */
    public void openRecipeList(Player player, int page) {
        List<RecipeDefinition> recipes = plugin.getCookingService().getRecipeManager().getRecipes();
        int total = recipes.size();
        page = GuiLayout.clampPage(page, total);

        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_LIST,
            text("食譜大全 (" + (page + 1) + "/" + GuiLayout.pageCount(total) + ")", NamedTextColor.AQUA));
        h.view(GuiHolder.View.RECIPE_LIST).page(page);

        fill(inv, Material.BLACK_STAINED_GLASS_PANE, 0, GuiLayout.ITEMS_PER_PAGE);
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
            text("食譜：" + recipe.getName(), NamedTextColor.AQUA));
        fill(inv, Material.GRAY_STAINED_GLASS_PANE, 0, SIZE_DETAIL);
        h.view(GuiHolder.View.RECIPE_DETAIL).recipeId(recipeId).page(fromPage);

        // 成品預覽（含品質 lore）
        FoodData food = plugin.getFoodRegistry().get(recipeId);
        ItemStack result = plugin.getItemProvider().createItem(recipeId, 1);
        var rMeta = result.getItemMeta();
        if (rMeta != null) {
            rMeta.displayName(text(recipe.getName(), NamedTextColor.AQUA));
            ItemLore.apply(rMeta, CookingQuality.NORMAL, food);
            result.setItemMeta(rMeta);
        }
        inv.setItem(4, result);

        // 步驟指示（cut/marinate/cook/season）
        int stepSlot = 10;
        for (String stepId : STEP_ORDER) {
            boolean used = recipe.getSteps().contains(stepId);
            Material mat = STEP_MATERIAL.getOrDefault(stepId, Material.PAPER);
            NamedTextColor color = used ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY;
            List<Component> l = new ArrayList<>();
            l.add(text(used ? "✓ 本食譜需要" : "✗ 本食譜不需要", color));
            l.add(text("方塊：" + STEP_BLOCK_DESC.get(stepId), NamedTextColor.GRAY));
            inv.setItem(stepSlot, icon(mat, text(stepName(stepId), color), l));
            stepSlot++;
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

        inv.setItem(18, navIcon(Material.ARROW, text("返回", NamedTextColor.RED), List.of()));

        player.openInventory(inv);
    }

    /** 開啟烹飪指南。 */
    public void openGuide(Player player) {
        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_GUIDE, text("烹飪指南", NamedTextColor.AQUA));
        fill(inv, Material.GRAY_STAINED_GLASS_PANE, 0, SIZE_GUIDE);
        h.view(GuiHolder.View.GUIDE);

        inv.setItem(4, icon(Material.WRITTEN_BOOK,
            text("CraftKitchen 烹飪指南", NamedTextColor.GOLD),
            lore(NamedTextColor.GRAY,
                "潛行右鍵方塊觸發烹飪步驟",
                "完成所有步驟即得料理成品")));

        int slot = 10;
        for (String stepId : STEP_ORDER) {
            inv.setItem(slot, icon(STEP_MATERIAL.get(stepId),
                text(stepName(stepId), NamedTextColor.AQUA),
                List.of(text("方塊：" + STEP_BLOCK_DESC.get(stepId), NamedTextColor.GRAY))));
            slot++;
        }

        // 品質分級
        List<Component> qualityLore = new ArrayList<>();
        qualityLore.add(text("完成料理時隨機決定品質", NamedTextColor.GRAY));
        qualityLore.add(text("速度與等級可提升高級機率", NamedTextColor.GRAY));
        qualityLore.add(Component.empty());
        for (CookingQuality q : CookingQuality.values()) {
            if (q == CookingQuality.NONE) {
                continue;
            }
            qualityLore.add(text(q.displayName() + " ×" + q.defaultMultiplier() + " 效果", q.color()));
        }
        inv.setItem(15, icon(Material.NETHER_STAR,
            text("品質分級", NamedTextColor.GOLD), qualityLore));

        inv.setItem(16, icon(Material.FLINT_AND_STEEL,
            text("爐灶點燃", NamedTextColor.GOLD),
            lore(NamedTextColor.GRAY,
                "潛行右鍵營火 + 打火石 → 點燃",
                "潛行右鍵營火 + 水桶 → 澆滅",
                "cook 步驟需爐灶已點燃")));

        inv.setItem(18, navIcon(Material.ARROW, text("返回", NamedTextColor.RED), List.of()));

        player.openInventory(inv);
    }

    /** 開啟料理等級資訊。 */
    public void openLevel(Player player) {
        GuiHolder h = new GuiHolder(player);
        Inventory inv = Bukkit.createInventory(h, SIZE_LEVEL, text("料理等級", NamedTextColor.GOLD));
        fill(inv, Material.GRAY_STAINED_GLASS_PANE, 0, SIZE_LEVEL);
        h.view(GuiHolder.View.LEVEL);

        var lm = plugin.getLevelManager();
        int xp = lm.getExperience(player.getUniqueId());
        int level = lm.getLevel(player.getUniqueId());
        String title = lm.getTitle(player.getUniqueId());
        int nextLevelXp = level * 50;
        int remaining = nextLevelXp - xp;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        var meta = head.getItemMeta();
        if (meta != null) {
            meta.displayName(text("料理等級：" + level, NamedTextColor.GOLD));
            List<Component> l = new ArrayList<>();
            l.add(text("稱號：" + title, NamedTextColor.AQUA));
            l.add(text("經驗：" + xp, NamedTextColor.WHITE));
            l.add(text("距下一級：" + remaining + " 經驗", NamedTextColor.GREEN));
            l.add(text("（每 50 經驗升一級）", NamedTextColor.GRAY));
            meta.lore(l.stream().map(this::noItalic).toList());
            head.setItemMeta(meta);
        }
        inv.setItem(4, head);

        inv.setItem(22, icon(Material.COOKED_BEEF,
            text("如何獲得經驗", NamedTextColor.AQUA),
            lore(NamedTextColor.GRAY,
                "完成料理即可獲得經驗",
                "傳說 +50 / 稀有 +40 / 精良 +30",
                "普通 +20 / 失敗 +5")));

        inv.setItem(18, navIcon(Material.ARROW, text("返回", NamedTextColor.RED), List.of()));

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
                + (done.isEmpty() ? "無" : String.join(", ", done)));
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
            meta.displayName(text(recipe.getName(), NamedTextColor.AQUA));
            List<Component> l = new ArrayList<>();
            l.add(text("步驟：" + String.join(" → ", recipe.getSteps()), NamedTextColor.GRAY));
            l.add(text("食材：" + (recipe.getIngredients().isEmpty()
                ? "無" : String.join(", ", recipe.getIngredients())), NamedTextColor.GRAY));
            if (food != null && food.getEffects() != null && !food.getEffects().isEmpty()) {
                l.add(text("效果：" + summarizeEffects(food), NamedTextColor.WHITE));
            }
            l.add(Component.empty());
            l.add(text("點擊查看詳情", NamedTextColor.YELLOW));
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
            text("料理等級", NamedTextColor.GOLD),
            List.of(
                text("等級 " + level + "（" + title + "）", NamedTextColor.AQUA),
                text("經驗：" + xp, NamedTextColor.WHITE),
                text("點擊查看詳情", NamedTextColor.YELLOW)
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
        fill(inv, Material.BLACK_STAINED_GLASS_PANE, 45, SIZE_LIST);
        int pages = GuiLayout.pageCount(total);
        if (page > 0) {
            inv.setItem(SLOT_PREV, navIcon(Material.ARROW,
                text("上一頁", NamedTextColor.AQUA), List.of()));
        }
        inv.setItem(SLOT_BACK, navIcon(Material.BARRIER,
            text("返回目錄", NamedTextColor.RED), List.of()));
        if (page < pages - 1) {
            inv.setItem(SLOT_NEXT, navIcon(Material.ARROW,
                text("下一頁", NamedTextColor.AQUA), List.of()));
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

    private String stepName(String id) {
        return switch (id) {
            case "cut" -> "切割";
            case "marinate" -> "醃製";
            case "cook" -> "烹煮";
            case "season" -> "調味";
            default -> id;
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
