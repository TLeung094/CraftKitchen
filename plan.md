# CraftKitchen 專案計畫書（雙模式版）

## 一、專案概述

### 1. 專案名稱
CraftKitchen

### 2. 專案類型
Minecraft 生存伺服器料理系統插件

### 3. 定位
生存伺服器 × 純休閒料理系統

### 4. 核心依賴
- Paper API（必要）
- CraftEngine（可選）

### 5. 目標版本
- 26.2

### 6. 授權
MIT

### 7. 語言
Java 25

### 8. 一句話簡介
用 Paper API 打造的多步驟烹飪、品質評級、調味客製化料理系統。支援兩種物品模式：原版模式（CustomModelData，零依賴）與 CraftEngine 模式（自動生成模型與配方），伺服器主可自由選擇。

---

## 二、核心特色

- 雙模式物品系統
  - 原版模式：CustomModelData
  - CraftEngine 模式：自動生成模型與配方
- 多步驟烹飪
  - 食材 → 預處理 → 烹調 → 調味 → 成品
- 品質與評級
  - 五級品質：失敗 / 普通 / 精良 / 稀有 / 傳說
- 調味與客製化
  - 調味料組合
  - 隱藏食譜
  - 動態 Lore
- 自訂烹飪設備
  - 砧板
  - 爐灶
  - 炒鍋
  - 湯鍋
- 共食系統
  - 附近玩家一起吃，效果延長
- 料理等級
  - 極簡等級系統
  - 解鎖食譜與稱號
- 節日限定
  - 中秋
  - 聖誕
  - 新年
  - 萬聖節
- 向後相容
  - 不安裝 CraftEngine 仍可完整運作

### 8. 非目標（明確排除）
- 不取代原版藥水、金蘋果
- 不做 PVP 料理對決
- 不強制玩家做菜
- 不做餐廳經營系統（預留 v2.0）
- 不強制依賴 CraftEngine

---

## 三、雙模式設計

### 3.1 模式對照

| 項目 | 原版模式 | CraftEngine 模式 |
|---|---|---|
| 物品外觀 | CustomModelData + 手寫資源包 | CraftEngine 自動生成模型 |
| 配方註冊 | Bukkit Recipe API | CraftEngine YAML 配方 |
| 自訂方塊 | 原版方塊偽裝 + 事件監聽 | CraftEngine 真服務端方塊 |
| 資源包需求 | 需手動提供 | CraftEngine 自動打包 |
| 依賴 | Paper API | Paper API + CraftEngine |
| 學習曲線 | 需懂資源包 | 只需寫 YAML |
| 靈活度 | 中 | 高 |
| 推薦對象 | 輕量伺服器、不想裝額外插件 | 大型伺服器、想快速擴展 |

### 3.2 自動偵測機制
插件啟動時將自動偵測 CraftEngine 是否存在：

```java
public enum ItemMode {
    VANILLA,
    CRAFTENGINE
}

public class ModeDetector {
    public static ItemMode detect() {
        if (Bukkit.getPluginManager().getPlugin("CraftEngine") != null) {
            Bukkit.getLogger().info("偵測到 CraftEngine，啟用 CraftEngine 模式");
            return ItemMode.CRAFTENGINE;
        }
        Bukkit.getLogger().info("未偵測到 CraftEngine，啟用原版模式");
        return ItemMode.VANILLA;
    }
}
```

也可在 `config.yml` 中進行強制指定：

```yaml
settings:
  item-mode: auto # auto / vanilla / craftengine
```

### 3.3 抽象層設計
將兩種模式差異抽離至介面：

```java
public interface ItemProvider {
    ItemStack createItem(String id, int amount);
    String getItemId(ItemStack item);
    boolean isCustomItem(ItemStack item);
    void registerRecipes();
}
```

核心邏輯層不依賴實際模式，僅透過 `ItemProvider` 拿取物品與判斷識別碼。

---

## 四、技術架構

### 4.1 整體架構

```text
┌─────────────────────────────────────────┐
│            CraftKitchen 插件             │
├─────────────────────────────────────────┤
│  ItemProvider（抽象層）                  │
│  ├─ VanillaItemProvider（原版）          │
│  └─ CraftEngineItemProvider（CE）        │
├─────────────────────────────────────────┤
│  事件監聽層（邏輯層）                    │
│  ├─ PlayerItemConsumeEvent（吃）         │
│  ├─ PrepareItemCraftEvent（合成）        │
│  ├─ InventoryClickEvent（取出）          │
│  └─ PlayerInteractEvent（方塊互動）      │
├─────────────────────────────────────────┤
│  核心系統層（模式無關）                  │
│  ├─ 品質計算引擎                         │
│  ├─ 調味判斷引擎                         │
│  ├─ 烹飪步驟追蹤                         │
│  ├─ 共食系統                             │
│  ├─ 等級系統                             │
│  └─ 節日系統                             │
├─────────────────────────────────────────┤
│  資料層                                  │
│  ├─ YAML 配置（食譜、效果、調味）        │
│  └─ SQLite（玩家等級、解鎖記錄）         │
└─────────────────────────────────────────┘
```

### 4.2 技術棧層級
- Paper 1.20.4+
- Java 17
- Gradle (Kotlin DSL)
- 依賴：Paper API（compileOnly）+ CraftEngine（compileOnly，可選）
- 物品外觀：
  - 原版：CustomModelData
  - CE：自動生成
- 配方系統：
  - 原版：Bukkit Recipe API
  - CE：YAML
- 方塊系統：
  - 原版：偽裝 + 事件監聽
  - CE：真方塊
- 資料儲存：YAML + SQLite
- 文本渲染：Adventure API
- 測試：JUnit 5 + MockBukkit

### 4.3 專案結構

```text
CraftKitchen/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/craftkitchen/
│   │   │       ├── CraftKitchen.java
│   │   │       ├── config/
│   │   │       │   ├── ConfigManager.java
│   │   │       │   ├── FoodRegistry.java
│   │   │       │   └── ModeDetector.java
│   │   │       ├── item/
│   │   │       │   ├── ItemProvider.java
│   │   │       │   ├── VanillaItemProvider.java
│   │   │       │   ├── CraftEngineItemProvider.java
│   │   │       └── ItemProviderFactory.java
│   │   │       ├── cooking/
│   │   │       │   ├── CookingStep.java
│   │   │       │   ├── CookingTracker.java
│   │   │       │   ├── RecipeManager.java
│   │   │       │   └── BlockManager.java
│   │   │       ├── quality/
│   │   │       │   ├── FoodQuality.java
│   │   │       │   └── QualityCalculator.java
│   │   │       ├── seasoning/
│   │   │       │   ├── Seasoning.java
│   │   │       │   └── SeasoningManager.java
│   │   │       ├── listener/
│   │   │       │   ├── ConsumeListener.java
│   │   │       │   ├── CraftListener.java
│   │   │       │   ├── InventoryListener.java
│   │   │       │   └── BlockInteractListener.java
│   │   │       ├── social/
│   │   │       │   └── SharedMealManager.java
│   │   │       ├── level/
│   │   │       │   ├── LevelManager.java
│   │   │       │   └── PlayerData.java
│   │   │       ├── holiday/
│   │   │       │   └── HolidayManager.java
│   │   │       ├── command/
│   │   │       │   └── KitchenCommand.java
│   │   │       ├── util/
│   │   │       │   ├── ItemUtil.java
│   │   │       │   └── NBTUtil.java
│   │   │       └── database/
│   │   │           └── DatabaseManager.java
│   │   └── resources/
│   │       ├── plugin.yml
│   │       ├── config.yml
│   │       ├── foods.yml
│   │       ├── seasonings.yml
│   │       ├── recipes.yml
│   │       ├── blocks.yml
│   │       ├── messages.yml
│   │       └── craftengine/
│   │           ├── items.yml
│   │           ├── recipes.yml
│   │           └── blocks.yml
│   └── test/
│       └── java/
│           └── com/example/craftkitchen/
│               ├── QualityCalculatorTest.java
│               └── SeasoningManagerTest.java
├── resourcepack/
│   └── assets/
│       └── minecraft/
│           ├── textures/item/
│           └── models/item/
├── docs/
│   ├── PLAN.md
│   ├── README.md
│   ├── INSTALL.md
│   ├── CONFIG.md
│   ├── RECIPES.md
│   ├── BLOCKS.md
│   ├── MODES.md
│   └── API.md
├── .gitignore
├── LICENSE
└── README.md
```

---

## 五、物品系統（雙模式）

### 5.1 原版模式：CustomModelData

```java
public class VanillaItemProvider implements ItemProvider {
    @Override
    public ItemStack createItem(String id, int amount) {
        FoodData data = FoodRegistry.get(id);
        ItemStack item = new ItemStack(data.getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(data.getName())
            .decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(data.getCustomModelData());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getItemId(ItemStack item) {
        if (!item.hasItemMeta()) return null;
        if (!item.getItemMeta().hasCustomModelData()) return null;
        return FoodRegistry.getByCmd(item.getItemMeta().getCustomModelData());
    }
}
```

### 5.2 CraftEngine 模式

```java
public class CraftEngineItemProvider implements ItemProvider {
    @Override
    public ItemStack createItem(String id, int amount) {
        return CraftEngineAPI.getItem(id, amount);
    }

    @Override
    public String getItemId(ItemStack item) {
        return CraftEngineAPI.getItemId(item);
    }
}
```

### 5.3 配置分離
- `foods.yml`、`seasonings.yml`：兩種模式共用
- `craftengine/items.yml`：CraftEngine 模式專用
- `resourcepack/`：原版模式專用

---

## 六、食譜系統（雙模式）

### 6.1 原版模式：Bukkit Recipe API

```java
SmokingRecipe smokedSteak = new SmokingRecipe(
    new NamespacedKey(plugin, "smoked_steak"),
    itemProvider.createItem("smoked_steak", 1),
    new RecipeChoice.ExactChoice(itemProvider.createItem("marinated_beef", 1)),
    0.35f,
    150
);
Bukkit.addRecipe(smokedSteak);
```

### 6.2 CraftEngine 模式：YAML 配方

```yaml
recipes:
  craftkitchen:smoked_steak:
    type: smoking
    ingredient:
      item: craftkitchen:marinated_beef
    result:
      item: craftkitchen:smoked_steak
    cooking_time: 150
    experience: 0.35
```

### 6.3 配方管理器抽象

```java
public class RecipeManager {
    private final ItemProvider itemProvider;
    private final ItemMode mode;

    public void registerAll() {
        if (mode == ItemMode.VANILLA) {
            registerVanillaRecipes();
        } else {
            registerCraftEngineRecipes();
        }
    }
}
```

---

## 七、自訂方塊（雙模式）

### 7.1 原版模式：方塊偽裝
- 砧板：木製壓力板 + 右鍵互動
- 爐灶：營火 + 打火石點燃
- 炒鍋：煙燻爐放入食材
- 湯鍋：釀造台右鍵放入食材
- 蒸籠：高爐放入食材

### 7.2 CraftEngine 模式：真服務端方塊
CraftEngine 可註冊真方塊，支援 WorldEdit、`/setblock`，並具備完整碰撞箱。

### 7.3 方塊管理器抽象

```java
public interface BlockHandler {
    void onInteract(PlayerInteractEvent event);
    void register();
}
```

---

## 八、核心系統設計（模式無關）

### 8.1 多步驟烹飪：以「香煎牛排」為例

| 步驟 | 操作 | 輸入 | 輸出 |
|---|---|---|---|
| 1 | 擊殺牛 | — | raw_beef |
| 2 | 砧板 + 鹽 | raw_beef + salt | marinated_beef |
| 3 | 爐灶 + 炒鍋 | marinated_beef | smoked_steak |
| 4 | 工作台 | smoked_steak + herb | herb_steak |
| 5 | 右鍵食用 | herb_steak | 套用 Buff |

### 8.2 品質與評級

| 品質 | 顏色 | 機率 | 效果倍率 |
|---|---|---|---|
| 失敗 | 灰色 | 5% | 0.5x + 負面 |
| 普通 | 白色 | 40% | 1.0x |
| 精良 | 綠色 | 35% | 1.25x |
| 稀有 | 藍色 | 15% | 1.5x |
| 傳說 | 金色 | 5% | 2.0x + 額外效果 |

品質影響因素（加權）：
- 食材新鮮度：25%
- 預處理完整度：20%
- 烹調方式正確：20%
- 調味比例：20%
- 廚師等級：15%

### 8.3 調味與客製化

| 調味料 | 效果 | 適合料理 |
|---|---|---|
| 鹽 | 品質 +10% | 所有 |
| 香草 | 飽食度 +2 | 肉類、魚類 |
| 辣椒 | 速度 I（30秒） | 肉類 |
| 醬油 | 力量時間 +10% | 肉類、米飯 |
| 蜂蜜 | 回復 I（立即） | 甜點、飲品 |
| 起司 | 抗性 I（30秒） | 西式 |
| 松露 | 幸運 I（60秒） | 高級料理 |

隱藏食譜範例：
- 煙燻牛排 + 辣椒 + 蜂蜜 → 蜜汁辣牛排
- 烤鱈魚 + 香草 + 鹽 → 香草烤魚
- 白飯 + 醬油 + 海苔 → 壽司

### 8.4 共食系統
半徑 5 格內有其他玩家時：
- 效果時間 +50%
- 顯示溫馨提示

### 8.5 料理等級

| 等級 | 稱號 | 解鎖內容 |
|---|---|---|
| 1 | 見習廚師 | 基礎食物效果 |
| 5 | 家常廚師 | 完美機率 +10% |
| 10 | 宴會廚師 | 共食範圍 +2 格 |
| 20 | 傳奇廚神 | 完美效果時間 +100% |

### 8.6 節日限定
- 中秋：月餅，飽食 + 幸運 II
- 聖誕：聖誕布丁，飽食 + 抗寒 I
- 新年：年菜，飽食 + 力量 I + 幸運 I
- 萬聖節：南瓜湯，飽食 + 夜視 I

---

## 九、開發里程碑

### Phase 1：基礎建設（第 1–2 週）
- [x] 建立 Gradle 專案
- [x] 設定 Paper API + CraftEngine（compileOnly）
- [x] 主類與 `plugin.yml`
- [x] 配置管理器
- [x] `ModeDetector`（雙模式偵測）
- [x] `ItemProvider` 介面與兩個實作
- [x] GitHub Actions CI

交付物：可載入空插件 + 雙模式切換 ✅（2026-09-13）

### Phase 2：核心料理（第 3–4 週）
- [x] 食物註冊表
- [x] `PlayerItemConsumeEvent` 監聽
- [x] 基礎效果套用
- [x] 手動消耗物品
- [x] 共食系統

交付物：吃自訂食物會套用 Buff（兩種模式都通）— 部分完成（2026-09-13）

### Phase 3：自訂方塊（第 5–6 週）
- [x] 原版模式方塊互動
- [ ] CraftEngine 模式方塊定義
- [x] 爐灶點燃／澆滅
- [x] `BlockInteractListener`

交付物：核心烹飪設備可運作（兩種模式）— 原版模式完成（2026-09-13）

### Phase 4：多步驟烹飪（第 7–8 週）
- [x] 烹飪步驟追蹤
- [x] `RecipeManager`（雙模式）
- [x] 配方註冊（config.yml 載入 + 預設回退）
- [x] 步驟完成度記錄（個人化 per-player + 逾時重置）

交付物：完整多步驟鏈路可運作 ✅（2026-09-13）

### Phase 5：品質系統（第 9 週）
- [x] 品質計算引擎（完美窗口時間制）
- [x] 五級品質映射（失敗/普通/精良/稀有/傳說，混合制：速度+等級加權）
- [x] Lore 動態修改（品質分級 + 效果摘要）
- [x] 品質效果倍率（每級 0.5x ~ 2.0x）

交付物：料理有品質分級

### Phase 6：調味客製化（第 10 週）
- [x] 調味料註冊
- [x] 動態配方結果修改
- [x] 隱藏食譜觸發
- [x] 調味效果對照

交付物：玩家可自訂調味組合

### Phase 7：等級與節日（第 11 週）
- [x] 玩家等級系統
- [x] SQLite 資料儲存
- [x] 稱號系統
- [x] 節日限定食譜

交付物：完整等級與節日功能

### Phase 8：指令、權限、文件（第 12 週）
- [x] 指令系統
- [x] 權限節點
- [ ] PlaceholderAPI 支援（可選）
- [x] 完整文件（含 `MODES.md`）

交付物：可發布的 v1.0.0

### Phase 9：測試與發布（第 13–14 週）
- [x] 單元測試（雙模式）
- [ ] 整合測試
- [x] 資源包打包
- [ ] GitHub Release

交付物：GitHub 正式發布

---

## 十、GitHub 專案規劃

### 10.1 Repository 設定
- Repo 名稱：CraftKitchen
- 描述：生存伺服器專用的多步驟烹飪料理系統插件（支援原版 / CraftEngine 雙模式）
- 授權：MIT
- 預設分支：main
- 開發分支：dev
- 功能分支：feature/xxx
- 修復分支：fix/xxx

### 10.2 分支策略

```text
main          ← 穩定發布版
  ↑
dev           ← 開發整合
  ↑
feature/xxx   ← 功能開發
fix/xxx       ← 修復
```

### 10.3 Commit 規範
- `feat:` 新增 CraftEngine 模式支援
- `feat:` 新增砧板方塊
- `fix:` 修正 CustomModelData 物品讀取錯誤
- `docs:` 更新 `MODES.md`
- `refactor:` 重構 `ItemProvider` 抽象層
- `test:` 新增雙模式單元測試
- `chore:` 更新 Gradle 依賴

### 10.4 Issue 標籤
- `enhancement`：新功能
- `bug`：錯誤
- `documentation`：文件
- `good first issue`：適合新手
- `help wanted`：需要協助
- `phase-1` ~ `phase-9`：對應里程碑
- `mode-vanilla`：原版模式相關
- `mode-craftengine`：CraftEngine 模式相關
- `block`：自訂方塊相關
- `system`：核心系統相關
- `resourcepack`：資源包相關

### 10.5 GitHub Actions CI

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build
        run: ./gradlew build
      - name: Test
        run: ./gradlew test
      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: CraftKitchen
          path: build/libs/*.jar
```

### 10.6 Release 流程
- 從 `dev` 合併到 `main`
- 打 tag：`v1.0.0`
- GitHub Actions 自動建置
- 建立 Release，附上：
  - 插件 JAR
  - 資源包 ZIP（原版模式用）
  - CraftEngine 配置範例（CE 模式用）
  - 更新日誌
  - 安裝說明

---

## 十一、配置檔案規劃

### 11.1 `config.yml`

```yaml
settings:
  item-mode: auto
  replace-vanilla: true
  shared-radius: 5
  shared-multiplier: 1.5
  perfect-multiplier: 1.5
  level-enabled: true
  holiday-enabled: true
  language: zh_TW

database:
  type: sqlite
  file: data.db
```

### 11.2 `foods.yml`

```yaml
foods:
  smoked_steak:
    vanilla:
      material: COOKED_BEEF
      custom-model-data: 10201
    craftengine:
      item-id: craftkitchen:smoked_steak
    name: "煙燻牛排"
    effects:
      - type: STRENGTH
        duration: 60
        amplifier: 0
    quality-multiplier: true
```

### 11.3 `seasonings.yml`

```yaml
seasonings:
  salt:
    vanilla:
      material: SUGAR
      custom-model-data: 10501
    craftengine:
      item-id: craftkitchen:salt
    quality-bonus: 0.10
    compatible: all
```

### 11.4 `blocks.yml`

```yaml
blocks:
  cutting_board:
    vanilla:
      block: OAK_PRESSURE_PLATE
      function: preprocess
      sound: BLOCK_WOOD_BREAK
    craftengine:
      block-id: craftkitchen:cutting_board
```

### 11.5 `messages.yml`

```yaml
messages:
  shared-meal: "&lt;yellow&gt;你與 &lt;white&gt;%player%&lt;/white&gt; 一起享用了 %food%"
  quality-legendary: "&lt;gold&gt;★ 傳說中的料理！"
  block-lit: "&lt;red&gt;爐灶已點燃"
  block-extinguished: "&lt;gray&gt;爐灶已熄滅"
  mode-detected: "&lt;green&gt;已啟用 %mode% 模式"
```

---

## 十二、指令與權限

| 指令 | 說明 | 權限 |
|---|---|---|
| `/kitchen` | 主指令 | `craftkitchen.use` |
| `/kitchen recipe` | 查看食譜 | `craftkitchen.use` |
| `/kitchen level` | 查看等級 | `craftkitchen.use` |
| `/kitchen mode` | 查看當前模式 | `craftkitchen.use` |
| `/kitchen reload` | 重載配置 | `craftkitchen.admin` |
| `/kitchen give <food>` | 給予食物 | `craftkitchen.admin` |
| `/kitchen block <name>` | 取得方塊 | `craftkitchen.admin` |

### 權限節點
```text
craftkitchen.use
craftkitchen.admin
craftkitchen.recipe.unlock
craftkitchen.quality.bypass
craftkitchen.block.use
```

---

## 十三、文件規劃（docs/）

- `PLAN.md`：本計畫書
- `README.md`：專案簡介、功能、截圖、安裝
- `INSTALL.md`：安裝步驟、依賴、資源包設定
- `MODES.md`：雙模式說明與選擇指南
- `CONFIG.md`：所有配置檔案說明
- `RECIPES.md`：食譜鏈路與配方範例
- `BLOCKS.md`：自訂方塊清單與用途
- `API.md`：開發者 API
- `CONTRIBUTING.md`：貢獻指南
- `CHANGELOG.md`：版本更新記錄

---

## 十四、測試規劃

### 單元測試
- `QualityCalculatorTest`：品質計算公式
- `SeasoningManagerTest`：調味判斷
- `FoodRegistryTest`：配置讀取
- `CookingTrackerTest`：步驟追蹤
- `VanillaItemProviderTest`：原版物品生成
- `CraftEngineItemProviderTest`：CE 物品生成

### 整合測試（MockBukkit）
- 吃食物觸發事件（雙模式）
- 合成觸發品質修改
- 共食半徑判斷
- 方塊互動觸發烹飪（雙模式）

### 手動測試清單
- [ ] 原版模式：多步驟鏈路完整走通
- [ ] CraftEngine 模式：多步驟鏈路完整走通
- [ ] 模式自動偵測正確
- [ ] 五級品質都能觸發
- [ ] 調味組合正確
- [ ] 共食效果正常
- [ ] 等級提升與稱號
- [ ] 節日限定食譜
- [ ] 自訂方塊互動正常
- [ ] 爐灶點燃／澆滅正常
- [ ] 資源包貼圖正確顯示（原版模式）
- [ ] CraftEngine 模型正確顯示（CE 模式）

---

## 十五、風險與應對

| 風險 | 影響 | 應對 |
|---|---|---|
| CustomModelData 衝突 | 中 | 統一編號規劃，文件記錄 |
| CraftEngine API 變動 | 高 | 鎖定版本、封裝在 `CraftEngineItemProvider` |
| 資源包載入失敗 | 中 | 提供範例包，測試驗證 |
| 雙模式行為不一致 | 高 | 抽象層 + 雙模式測試 |
| 自訂方塊被誤用 | 中 | 事件中檢查玩家權限 |
| 原版方塊被破壞 | 中 | 監聽 `BlockBreakEvent`，保護方塊 |
| 效能問題（大量事件） | 低 | 快取、非同步處理 |
| 品質計算太複雜 | 中 | 提供配置開關 |
| 食譜註冊衝突 | 低 | 使用 `NamespacedKey` 唯一識別 |

---

## 十六、未來擴展（v2.0+）
- 餐廳經營系統
- NPC 顧客
- 料理比賽活動
- 跨伺服器食譜同步
- Web 儀表板
- 多語言支援（英文、日文）
- 資料包版本（完全脫離插件）

---

## 十七、立即行動清單

1. 建立 GitHub Repo：CraftKitchen
2. 初始化 Gradle 專案（Paper API + CraftEngine compileOnly）
3. 建立目錄結構（依專案結構）
4. 寫 `plugin.yml` 與主類實作
5. 實作 `ModeDetector`：雙模式偵測
6. 實作 `ItemProvider` 介面 + 兩個實作
7. 實作 `ItemUtil.java`：CustomModelData 物品生成
8. 實作 `RecipeManager.java`：雙模式配方註冊
9. 實作 `ConsumeListener.java`：吃食物觸發效果
10. 實作 `BlockInteractListener.java`：方塊互動
11. 建立資源包（原版模式） + CE 配置（CE 模式）
12. 提交第一個 commit：`chore: initial project setup`
13. 建立 GitHub Actions CI
14. 開始 Phase 1

---

## 十八、附錄：`build.gradle.kts`

```kotlin
plugins {
    java
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.momirealms:craftengine:VERSION")
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    build {
        dependsOn("processResources")
    }
    test {
        useJUnitPlatform()
    }
}
```

### `plugin.yml`（軟依賴宣告）

```yaml
name: CraftKitchen
version: 1.0.0
main: com.example.craftkitchen.CraftKitchen
api-version: '1.20'
softdepend:
  - CraftEngine
  - PlaceholderAPI
commands:
  kitchen:
    description: CraftKitchen 主指令
    usage: /kitchen <subcommand>
permissions:
  craftkitchen.use:
    default: true
  craftkitchen.admin:
    default: op
```

---

## 十九、結論

CraftKitchen 的核心價值在於：在不強制依賴 CraftEngine 的前提下，提供一套完整、可延展、可維護的 Minecraft 烹飪系統。它同時兼顧輕量伺服器的可用性與大型伺服器的擴展性，透過雙模式抽象層、穩定的配置結構與分階段開發計畫，能夠以較低風險逐步完成 v1.0.0 的發布目標。

本計畫書是一份可執行的工程藍圖，後續開發將依照 Phase 1 至 Phase 9 的順序實作，確保功能、測試與文件同步完成。
