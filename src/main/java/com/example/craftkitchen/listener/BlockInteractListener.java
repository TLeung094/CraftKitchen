package com.example.craftkitchen.listener;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.RecipeDefinition;
import com.example.craftkitchen.cooking.StoveState;
import com.example.craftkitchen.food.FoodData;
import com.example.craftkitchen.item.ItemLore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 方塊互動 → 烹飪步驟推進。
 *
 * <p>為避免干擾原版方塊行為（工作台/釀造台會開介面），烹飪一律以「潛行右鍵」觸發。
 * 步驟 → 方塊對應：
 * <ul>
 *   <li>cut（切）→ 木製壓力板 / 切石機</li>
 *   <li>marinate（醃）→ 釀造台</li>
 *   <li>cook（煮）→ 營火 / 靈魂營火（<b>需已點燃</b>）</li>
 *   <li>season（調味）→ 工作台</li>
 * </ul>
 *
 * <p>爐灶點燃／澆滅（僅營火）：
 * <ul>
 *   <li>潛行右鍵營火 + 打火石 → 點燃爐灶</li>
 *   <li>潛行右鍵營火 + 水桶 → 澆滅爐灶</li>
 * </ul>
 * 實際推進哪道食譜由 {@link CookingService#resolveStep} 決定（支援多食譜）。
 */
public class BlockInteractListener implements Listener {
    private final CraftKitchen plugin;
    private final CookingService cookingService;
    private final StoveState stoveState;

    public BlockInteractListener(CraftKitchen plugin, CookingService cookingService) {
        this.plugin = plugin;
        this.cookingService = cookingService;
        this.stoveState = plugin.getStoveState();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Material blockType = clicked.getType();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // 爐灶點燃 / 澆滅（營火）
        if (isStove(blockType)) {
            if (hand.getType() == Material.FLINT_AND_STEEL) {
                event.setCancelled(true);
                stoveState.light(locationKey(clicked));
                player.sendMessage("爐灶已點燃");
                return;
            }
            if (hand.getType() == Material.WATER_BUCKET) {
                event.setCancelled(true);
                stoveState.extinguish(locationKey(clicked));
                player.sendMessage("爐灶已熄滅");
                return;
            }
        }

        String stepId = stepForBlock(blockType);
        if (stepId == null) {
            return;
        }

        // cook 步驟需爐灶已點燃
        if (stepId.equals("cook") && isStove(blockType) && !stoveState.isLit(locationKey(clicked))) {
            player.sendMessage("爐灶尚未點燃，請用打火石點燃後再烹調。");
            return;
        }

        List<String> available = Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() != Material.AIR)
            .map(item -> item.getType().name())
            .toList();

        CookingService.StepResolution resolution =
            cookingService.resolveStep(player.getUniqueId(), stepId, available);
        if (resolution.recipeId() == null) {
            player.sendMessage("此處沒有可進行的食譜（步驟不對或缺少食材）。");
            return;
        }
        event.setCancelled(true);
        handleResult(player, resolution.recipeId(), resolution.result());
    }

    private boolean isStove(Material type) {
        return type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE;
    }

    private String locationKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + "," + block.getY() + "," + block.getZ();
    }

    private String stepForBlock(Material type) {
        if (type == Material.OAK_PRESSURE_PLATE || type == Material.STONECUTTER) {
            return "cut";
        }
        if (type == Material.BREWING_STAND) {
            return "marinate";
        }
        if (type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE) {
            return "cook";
        }
        if (type == Material.CRAFTING_TABLE) {
            return "season";
        }
        return null;
    }

    private void handleResult(Player player, String recipeId, CookingResult result) {
        if (result == CookingResult.COMPLETED) {
            consumeIngredients(player, recipeId);
            ItemStack reward = plugin.getItemProvider().createItem(recipeId, 1);
            CookingQuality quality = cookingService.getLastQuality(player.getUniqueId());
            FoodData food = plugin.getFoodRegistry().get(recipeId);
            var meta = reward.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(
                    QualityKeys.qualityKey(plugin),
                    PersistentDataType.STRING,
                    quality.name()
                );
                ItemLore.apply(meta, quality, food);
                reward.setItemMeta(meta);
            }
            player.getInventory().addItem(reward);
            player.sendMessage("料理完成：" + recipeName(recipeId) + "，品質：" + quality.displayName());

            checkHiddenRecipe(player, recipeId);

            int xp = xpForQuality(quality);
            var levelUps = plugin.getLevelManager().addExperience(player.getUniqueId(), xp);
            for (int level : levelUps) {
                player.sendMessage("升級！料理等級達到 " + level
                    + "（" + plugin.getLevelManager().getTitle(player.getUniqueId()) + "）");
            }
        } else if (result == CookingResult.IN_PROGRESS) {
            var done = cookingService.getTracker().getCompletedSteps(player.getUniqueId(), recipeId);
            player.sendMessage(recipeName(recipeId) + " 進度：已完成 "
                + (done.isEmpty() ? "無" : String.join(", ", done)));
        } else if (result == CookingResult.NO_APPLICABLE_RECIPE) {
            player.sendMessage("此處沒有可進行的食譜。");
        }
    }

    private int xpForQuality(CookingQuality quality) {
        return switch (quality) {
            case LEGENDARY -> 50;
            case RARE -> 40;
            case FINE -> 30;
            case NORMAL -> 20;
            case FAILED -> 5;
            default -> 0;
        };
    }

    private void consumeIngredients(Player player, String recipeId) {
        var recipe = cookingService.getRecipeManager().getRecipe(recipeId);
        if (recipe == null) {
            return;
        }
        for (String ingredient : recipe.getIngredients()) {
            Material material = Material.matchMaterial(ingredient);
            if (material != null) {
                player.getInventory().removeItem(new ItemStack(material, 1));
            }
        }
    }

    private void checkHiddenRecipe(Player player, String baseRecipeId) {
        var seasoningManager = plugin.getSeasoningManager();
        List<String> held = Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() != Material.AIR)
            .map(item -> item.getType().name())
            .toList();

        List<String> applied = new ArrayList<>();
        for (var entry : List.of("chili", "honey")) {
            var seasoning = seasoningManager.getSeasoning(entry);
            if (seasoning.isPresent() && held.contains(seasoning.get().getMaterial())) {
                applied.add(entry);
            }
        }

        seasoningManager.findHiddenRecipe(baseRecipeId, applied).ifPresent(hiddenId -> {
            ItemStack bonus = plugin.getItemProvider().createItem(hiddenId, 1);
            FoodData bonusFood = plugin.getFoodRegistry().get(hiddenId);
            var meta = bonus.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(
                    QualityKeys.qualityKey(plugin),
                    PersistentDataType.STRING,
                    CookingQuality.NORMAL.name()
                );
                ItemLore.apply(meta, CookingQuality.NORMAL, bonusFood);
                bonus.setItemMeta(meta);
            }
            player.getInventory().addItem(bonus);
            player.sendMessage("發現隱藏食譜！獲得：" + recipeName(hiddenId));
        });
    }

    private String recipeName(String recipeId) {
        RecipeDefinition recipe = cookingService.getRecipeManager().getRecipe(recipeId);
        return recipe != null ? recipe.getName() : recipeId;
    }
}
