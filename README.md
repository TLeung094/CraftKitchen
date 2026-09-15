# CraftKitchen

用 Paper API 打造的多步驟烹飪、品質評級、調味客製化料理系統。支援兩種物品模式：原版模式（CustomModelData，零依賴）與 CraftEngine 模式（自動生成模型與配方），伺服器主可自由選擇。

- **目標版本**：26.2
- **語言**：Java 25
- **建置**：Gradle 9.7.0（Kotlin DSL）
- **核心依賴**：Paper API 1.20.4+（必要）、CraftEngine（可選）
- **授權**：MIT

---

## 目前進度

核心玩法迴圈已接通並全數測試通過（64 個單元測試）：

**潛行右鍵方塊 → 食材檢查 → 多步驟進度（個人化、逾時重置）→ 五級品質判定 → 品質成品 → 食用效果加成**

| 系統 | 狀態 |
|---|---|
| 雙模式架構（VANILLA / CRAFTENGINE 抽象層） | ✅ |
| 食物註冊表（config 載入 `foods`） | ✅ |
| 食用效果（動態識別食物 + 藥水套用） | ✅ |
| 多步驟烹飪追蹤 | ✅ |
| 食譜管理（config 載入 + 預設回退） | ✅ |
| 方塊互動（多食譜 + 潛行觸發 + cut/marinate/cook/season） | ✅ |
| 食材檢查與消耗 | ✅ |
| 個人化進度（per-player） | ✅ |
| 進度逾時重置 | ✅ |
| 五級品質（失敗/普通/精良/稀有/傳說，混合制：速度+等級加權） | ✅ |
| 品質影響食物效果（每級倍率 0.5x ~ 2.0x） | ✅ |
| 動態 Lore（品質分級 + 效果摘要） | ✅ |
| 爐灶點燃／澆滅（cook 步驟需已點燃） | ✅ |
| CraftEngine 實際物品生成（API 整合 + content pack） | ✅ |
| 共食系統 | ✅ |
| 料理等級 | ✅ |
| 節日限定 | ✅ |
| 調味與隱藏食譜 | ✅ |
| 廚房 GUI（食譜大全 / 等級 / 烹飪指南，`/kitchen menu`） | ✅ |
| docs/ 文件（INSTALL/MODES/CONFIG/RECIPES/BLOCKS/API/GUI） | ✅ |
| resourcepack/ 骨架（原版模式 pack.mcmeta + 模型 + 佔位貼圖） | ✅ |
| craftengine-pack/（CE 模式 content pack：3 料理 + 4 廚房方塊 + 32×32 貼圖） | ✅ |

---

## 建置與測試

```bash
./gradlew build   # 編譯打包
./gradlew test    # 執行全部單元測試
```

---

## config.yml 設定說明

```yaml
settings:
  item-mode: auto            # auto / vanilla / craftengine：物品模式，auto 會自動偵測 CraftEngine
  language: zh_TW
  shared-radius: 5           # 共食系統：效果延長的判定半徑（格）
  shared-multiplier: 1.5     # 共食系統：效果時間倍率
  level-enabled: true        # 料理等級系統開關
  holiday-enabled: true      # 節日限定開關
  cooking-timeout-seconds: 300   # 烹飪進度逾時（秒），超時自動清空半成品進度
  perfect-window-seconds: 30     # 完美窗口（秒）：此內完成 → 品質判定 speedScore=1.0

quality:                      # 五級品質（混合制：速度+等級加權把機率往高級移動）
  speed-weight: 0.5
  level-weight: 0.15
  tiers:
    failed:    { probability: 0.05, multiplier: 0.5 }   # 灰
    normal:    { probability: 0.40, multiplier: 1.0 }  # 白
    fine:      { probability: 0.35, multiplier: 1.25 } # 綠
    rare:      { probability: 0.15, multiplier: 1.5 }  # 青
    legendary: { probability: 0.05, multiplier: 2.0 }   # 金

foods:                        # 食物定義，ID 為 key，成品以此查 material / 顯示名 / 藥水效果
  smoked_steak:
    name: 煙燻牛排
    material: COOKED_BEEF
    custom-model-data: 10201
    effects:
      - type: STRENGTH
        duration: 60
        amplifier: 0

recipes:                      # 食譜定義，ID 為 key
  smoked_steak:
    name: 煙燻牛排            # 顯示名稱
    steps: [cut, marinate, cook]      # 依序的烹飪步驟
    ingredients: [BEEF, PEPPER]       # 需要的食材（Bukkit Material 名稱）
  herb_steak:
    name: 香草牛排
    steps: [cut, cook, season]
    ingredients: [BEEF, SWEET_BERRIES]

database:
  type: sqlite
  file: data.db
```

### 食譜設定規則

- `recipes` 區段缺漏或留空時，插件會回退到內建預設食譜，伺服器不會因此壞掉。
- 單一食譜缺少 `name` 或 `steps` 時，該條目會被略過（不影響其他食譜）。
- `ingredients` 使用 Bukkit `Material` 列舉名稱；無效名稱在消耗階段會被安全略過。

---

## 玩法流程

所有烹飪操作皆以**潛行右鍵**方塊觸發，避免干擾原版方塊互動（如工作台開合成介面）。

| 步驟 | 觸發方塊 |
|---|---|
| `cut`（切） | 木製壓力板 / 切石機 |
| `marinate`（醃） | 釀造台 |
| `cook`（煮） | 營火 / 靈魂營火（**需先用打火石點燃**） |
| `season`（調味） | 工作台 |

**爐灶點燃／澆滅**：潛行右鍵營火 + 打火石 = 點燃；水桶 = 澆滅。cook 步驟需爐灶已點燃才能進行。

1. 玩家背包備齊食譜所需食材（例如 `BEEF + PEPPER`）。
2. 潛行右鍵對應方塊依序完成食譜步驟；插件會依玩家背包食材自動判定要推進哪道食譜。
3. 所有步驟完成 → 消耗食材、依**混合制**判定五級品質、獲得帶 lore 的成品：
   - 完成速度越快、廚師等級越高 → 高級（精良/稀有/傳說）機率越高
   - 各級效果倍率：失敗 0.5x、普通 1.0x、精良 1.25x、稀有 1.5x、傳說 2.0x
   - 超過逾時時間 → 進度清空，需重頭開始
4. 食用成品 → 套用食譜定義的藥水效果，時長依品質倍率縮放（與共食倍率疊加）。

每位玩家的進度獨立追蹤；品質會寫入成品的 PersistentDataContainer，離線重上或交易後仍保留。

---

## 專案結構

```text
src/main/java/com/example/craftkitchen/
├── CraftKitchen.java          # 插件入口，初始化所有服務
├── config/                    # ConfigManager、ModeDetector（雙模式偵測）
├── item/                      # ItemProvider 抽象層 + Vanilla/CraftEngine 實作 + ItemLore
├── food/                      # FoodData、FoodEffect、FoodRegistry（config 載入）
├── cooking/                   # 核心烹飪域
│   ├── RecipeDefinition.java  # 食譜定義（名稱/步驟/食材）
│   ├── RecipeManager.java     # 食譜註冊與查詢（config 載入）
│   ├── CookingStep.java       # 單一步驟
│   ├── CookingTracker.java    # 進度追蹤（個人化、時鐘注入、逾時）
│   ├── CookingService.java    # 步驟協調、食材驗證、品質評估
│   ├── CookingResult.java     # 步驟結果列舉
│   ├── CookingQuality.java    # 五級品質列舉（NONE/FAILED/NORMAL/FINE/RARE/LEGENDARY）
│   ├── QualityConfig.java     # 品質機率/倍率/加權（config 載入）
│   ├── QualityCalculator.java # 混合制品質判定引擎（純邏輯）
│   └── StoveState.java        # 爐灶點燃狀態追蹤
└── listener/
    ├── ConsumeListener.java       # 食用效果 + 品質倍率
    ├── BlockInteractListener.java # 方塊互動 → 步驟推進 → 爐灶點燃 → 成品發放
    └── QualityKeys.java           # PDC key 管理
```

詳細設計與後續里程碑請見 [plan.md](plan.md)；安裝與設定見 [docs/](docs/)。
