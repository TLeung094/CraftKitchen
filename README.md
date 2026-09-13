# CraftKitchen

用 Paper API 打造的多步驟烹飪、品質評級、調味客製化料理系統。支援兩種物品模式：原版模式（CustomModelData，零依賴）與 CraftEngine 模式（自動生成模型與配方），伺服器主可自由選擇。

- **目標版本**：26.2
- **語言**：Java 25
- **建置**：Gradle 9.7.0（Kotlin DSL）
- **核心依賴**：Paper API 1.20.4+（必要）、CraftEngine（可選）
- **授權**：MIT

---

## 目前進度

核心玩法迴圈已完成並全數測試通過（21 個單元測試）：

**互動 → 食材檢查 → 多步驟進度（個人化、逾時重置）→ 品質判定 → 品質成品 → 食用效果加成**

| 系統 | 狀態 |
|---|---|
| 雙模式架構（VANILLA / CRAFTENGINE 抽象層） | ✅ 骨架完成 |
| 食物註冊表（config 載入） | ✅ |
| 食用效果（藥水套用） | ✅ |
| 多步驟烹飪追蹤 | ✅ |
| 食譜管理（config 載入 + 預設回退） | ✅ |
| 方塊互動（砧板 / 爐灶） | ✅ |
| 食材檢查與消耗 | ✅ |
| 個人化進度（per-player） | ✅ |
| 進度逾時重置 | ✅ |
| 品質系統（PERFECT / NORMAL） | ✅ |
| 品質影響食物效果（perfect-multiplier） | ✅ |
| CraftEngine 實際物品生成 | ⬜ stub |
| 共食系統 | ⬜ |
| 料理等級 | ⬜ |
| 節日限定 | ⬜ |
| 調味與隱藏食譜 | ⬜ |

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
  perfect-multiplier: 1.5    # PERFECT 品質成品的藥水時長倍率
  level-enabled: true        # 料理等級系統開關
  holiday-enabled: true      # 節日限定開關
  cooking-timeout-seconds: 300   # 烹飪進度逾時（秒），超時自動清空半成品進度
  perfect-window-seconds: 30     # 完美窗口（秒）：從第一步起算，時限內完成 → PERFECT 品質

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

1. 玩家背包備齊食譜所需食材（例如 `BEEF + PEPPER`）。
2. 對 **砧板**（木製壓力板 / 切石機）右鍵 → 完成 `cut` 步驟。
3. 對 **爐灶**（營火）右鍵 → 完成 `cook` 步驟。
4. 所有步驟完成 → 消耗食材、獲得成品：
   - 在完美窗口內完成 → **PERFECT** 品質，食用效果時長 ×1.5
   - 超過完美窗口但在逾時內完成 → **NORMAL** 品質
   - 超過逾時時間 → 進度清空，需重頭開始
5. 食用成品 → 套用食譜定義的藥水效果（PERFECT 品質時長加成）。

每位玩家的進度獨立追蹤；品質會寫入成品的 PersistentDataContainer，離線重上或交易後仍保留。

---

## 專案結構

```text
src/main/java/com/example/craftkitchen/
├── CraftKitchen.java          # 插件入口，初始化所有服務
├── config/                    # ConfigManager、ModeDetector（雙模式偵測）
├── item/                      # ItemProvider 抽象層 + Vanilla/CraftEngine 實作
├── food/                      # FoodData、FoodEffect、FoodRegistry（config 載入）
├── cooking/                   # 核心烹飪域
│   ├── RecipeDefinition.java  # 食譜定義（名稱/步驟/食材）
│   ├── RecipeManager.java     # 食譜註冊與查詢（config 載入）
│   ├── CookingStep.java       # 單一步驟
│   ├── CookingTracker.java    # 進度追蹤（個人化、時鐘注入、逾時）
│   ├── CookingService.java    # 步驟協調、食材驗證、品質評估
│   ├── CookingResult.java     # 步驟結果列舉
│   └── CookingQuality.java    # 品質列舉（NONE/NORMAL/PERFECT）
└── listener/
    ├── ConsumeListener.java       # 食用效果 + 品質時長加成
    ├── BlockInteractListener.java # 方塊互動 → 步驟推進 → 成品發放
    └── QualityKeys.java           # PDC key 管理
```

詳細設計與後續里程碑請見 [plan.md](plan.md)。
