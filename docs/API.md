# 開發者 API

核心邏輯層刻意與 Bukkit 解耦，多數類別為純 Java（`CookingService`、`QualityCalculator`、`CookingTracker`、`SeasoningManager`、`LevelManager`、`HolidayManager`、`SharedMealManager`、`StoveState`），可直接單元測試。

## 主要進入點

`CraftKitchen.getInstance()`（`org.bukkit.plugin.java.JavaPlugin` 子類）提供下列 getter：

| Getter | 回傳 | 說明 |
|---|---|---|
| `getItemProvider()` | `ItemProvider` | 物品產生／反查（依模式） |
| `getFoodRegistry()` | `FoodRegistry` | 食物定義查詢 |
| `getCookingService()` | `CookingService` | 烹飪步驟推進、品質判定 |
| `getQualityCalculator()` | `QualityCalculator` | 品質機率／倍率查詢 |
| `getStoveState()` | `StoveState` | 爐灶點燃狀態 |
| `getSharedMealManager()` | `SharedMealManager` | 共食半徑／時長縮放 |
| `getLevelManager()` | `LevelManager` | 料理等級／稱號 |
| `getHolidayManager()` | `HolidayManager` | 節日查詢 |
| `getSeasoningManager()` | `SeasoningManager` | 調味料／隱藏食譜 |

## CookingService

烹飪步驟推進的核心。關鍵方法：

- `handleStep(UUID, recipeId, stepId) → CookingResult`：推進單一步驟，完成時一併判定品質並存於 `lastQuality`。
- `resolveStep(UUID, stepId, availableMaterials) → StepResolution`：決定玩家對某步驟方塊操作時應推進哪道食譜（多食譜決策，純邏輯）。
- `nextStep(UUID, recipeId) → String|null`：該食譜下一個未完成步驟。
- `getMissingIngredients(recipeId, availableMaterials) → List<String>`。
- `getLastQuality(UUID) → CookingQuality`：上次完成食譜的品質。

```java
CookingService.StepResolution res =
    cookingService.resolveStep(player.getUniqueId(), "cook", List.of("BEEF","PEPPER"));
if (res.recipeId() != null) {
    // res.recipeId() = 推進的食譜；res.result() = IN_PROGRESS / COMPLETED / NO_APPLICABLE_RECIPE
}
```

## QualityCalculator（混合制品質判定）

`roll(elapsedMs, perfectWindowMs, timeoutMs, level, roll) → CookingQuality`

1. `speedScore ∈ [0,1]`：完美窗口內 = 1.0；逾時 = 0.0；其間線性遞減。
2. `levelScore ∈ [0,1]`：`level / 20`（封頂）。
3. `boost = speedScore * speedWeight + levelScore * levelWeight`。
4. 高級（精良/稀有/傳說）機率 `×(1 + boost*係數)`；低級（失敗/普通）`×(1 - boost*係數)`。
5. 正規化後以 `roll ∈ [0,1)` 累積抽樣。

`multiplier(CookingQuality) → double` 回傳該級效果倍率（供 `ConsumeListener` 縮放食用效果時長）。

## CookingQuality（五級）

| 常數 | 顏色 | 預設倍率 |
|---|---|---|
| `NONE` | 白 | 1.0（未經烹飪流程） |
| `FAILED` | 灰 | 0.5 |
| `NORMAL` | 白 | 1.0 |
| `FINE` | 綠 | 1.25 |
| `RARE` | 青 | 1.5 |
| `LEGENDARY` | 金 | 2.0 |

enum 自帶 `color()`（`NamedTextColor`）與 `displayName()`，供 `ItemLore` 顯示。機率／倍率可由 `config.yml` 覆寫。

## StoveState

`isLit(locationKey)` / `light(key)` / `extinguish(key)`。位置鍵為 `"world:x,y,z"` 字串，由 listener 轉換。純 Java，可單元測試。

## ItemProvider

`createItem(id, amount)` 產生物品並以 PDC `food_id` 標記；`getItemId(item)` 反查；`isCustomItem(item)` 判定。原版模式另以 `CustomModelData` + 顯示名稱呈現；CE 模式委派 CraftEngine。

成品 Lore 由 `ItemLore.apply(meta, quality, food)` 套用（品質行 + 效果摘要），由知道品質的呼叫端（listener／`/kitchen give`）在取 `ItemMeta` 後呼叫。

## 擴充點

- **新食譜／食物**：改 `config.yml`（`recipes`/`foods`），`/kitchen reload`。
- **新調味料／隱藏食譜**：改 `seasonings`/`hidden-recipes`。
- **新步驟方塊**：在 `BlockInteractListener.stepForBlock` 加對應，並視需要在 `StoveState` 增加狀態。
- **新品質級**：`CookingQuality` 加常數 + `config.yml` 的 `quality.tiers` 加機率／倍率。
