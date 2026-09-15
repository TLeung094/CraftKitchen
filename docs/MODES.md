# 雙模式說明

CraftKitchen 透過抽象層 `ItemProvider` 把「物品外觀／配方／方塊」的差異隔離，核心邏輯（品質、調味、烹飪追蹤、等級、節日）對兩種模式完全一致。

## 模式對照

| 項目 | 原版模式（VANILLA） | CraftEngine 模式（CRAFTENGINE） |
|---|---|---|
| 物品外觀 | CustomModelData + 資源包 | CraftEngine 自動生成模型 |
| 配方註冊 | Bukkit Recipe API | CraftEngine YAML 配方 |
| 自訂方塊 | 原版方塊偽裝 + 事件監聯 | CraftEngine 真服務端方塊 |
| 資源包需求 | 需手動提供 | CraftEngine 自動打包 |
| 依賴 | 僅 Paper API | Paper API + CraftEngine |
| 推薦對象 | 輕量伺服器、不想裝額外插件 | 大型伺服器、想快速擴展 |

## 自動偵測

啟動時 `ModeDetector` 檢查 `Bukkit.getPluginManager().getPlugin("CraftEngine")`：

- 存在 → `CRAFTENGINE`
- 不存在 → `VANILLA`

可在 `config.yml` 用 `settings.item-mode` 強制指定（`auto` / `vanilla` / `craftengine`），覆寫自動偵測。

## 抽象層

```java
public interface ItemProvider {
    ItemStack createItem(String id, int amount);
    String getItemId(ItemStack item);
    boolean isCustomItem(ItemStack item);
    void registerRecipes();
}
```

- `VanillaItemProvider`：以 `Material` + `CustomModelData` + 顯示名稱產生物品，並用 PDC（`food_id`）標記，使 `getItemId` 可反查。
- `CraftEngineItemProvider`：委派 CraftEngine API 產生物品（需安裝 CE；未安裝時降為原版行為）。

由 `ItemProviderFactory.create(itemMode, plugin)` 依模式產生實例。核心系統只透過 `ItemProvider` 存取物品，不依賴具體模式。

## 選擇建議

- **小伺服器 / 不想碰資源包** → 原版模式。仍需資源包才能看到自訂貼圖，但不用裝 CE。沒有資源包時物品會顯示原版材質（如 COOKED_BEEF），玩法功能完全正常。
- **大型伺服器 / 想快速新增大量自訂物品與方塊** → CraftEngine 模式。只需寫 YAML，CE 自動生成模型與配方。
