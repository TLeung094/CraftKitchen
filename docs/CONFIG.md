# 配置說明

所有設定集中於 `plugins/CraftKitchen/config.yml`。改完執行 `/kitchen reload` 即時生效。

## settings

```yaml
settings:
  item-mode: auto            # auto / vanilla / craftengine
  language: zh_TW
  shared-radius: 5           # 共食偵測半徑（格）
  shared-multiplier: 1.5     # 共食時效果時長倍率
  level-enabled: true        # 啟用料理等級
  holiday-enabled: true     # 啟用節日限定
  cooking-timeout-seconds: 300   # 進度閒置逾時（秒），超過則重置
  perfect-window-seconds: 30     # 完美窗口（秒）：此內完成 → 品質判定 speedScore=1.0
```

| 鍵 | 預設 | 說明 |
|---|---|---|
| `item-mode` | `auto` | 強制物品模式 |
| `shared-radius` | `5` | 共食半徑 |
| `shared-multiplier` | `1.5` | 共食效果時長倍率 |
| `cooking-timeout-seconds` | `300` | 進度閒置逾時 |
| `perfect-window-seconds` | `30` | 完美窗口（品質判定速度因子用） |

## foods

定義自訂料理成品（玩家最終食用的物品）。

```yaml
foods:
  smoked_steak:
    name: 煙燻牛排
    material: COOKED_BEEF        # 原版材質
    custom-model-data: 10201    # 資源包貼圖索引（原版模式）
    effects:
      - type: STRENGTH          # 藥水效果（見 org.bukkit.potion.PotionEffectType）
        duration: 60            # 秒
        amplifier: 0            # 等級（0 = I）
```

食用時，`ConsumeListener` 依物品 PDC 的 `food_id` 反查此定義，套用 `effects`。效果時長會乘上**品質倍率**（見 `quality`）與**共食倍率**（若有附近玩家）。

## recipes

定義烹飪食譜（步驟鏈 + 食材）。

```yaml
recipes:
  smoked_steak:
    name: 煙燻牛排
    steps: [cut, marinate, cook]   # 步驟順序
    ingredients: [BEEF, PEPPER]    # 完成時消耗的背包食材（Material 名稱）
  herb_steak:
    name: 香草牛排
    steps: [cut, cook, season]
    ingredients: [BEEF, SWEET_BERRIES]
```

步驟→方塊對應見 [BLOCKS.md](BLOCKS.md)。`RecipeManager.fromConfig` 載入；缺漏時回退預設食譜（smoked_steak、herb_steak）。

## seasonings 與 hidden-recipes

```yaml
seasonings:
  chili:
    name: 辣椒
    material: RED_DYE
    effects:
      - { type: SPEED, duration: 30, amplifier: 0 }
  honey:
    name: 蜂蜜
    material: HONEY_BOTTLE
    effects:
      - { type: REGENERATION, duration: 5, amplifier: 0 }

hidden-recipes:
  honey_spicy_steak:
    name: 蜜汁辣牛排
    base: smoked_steak            # 觸發基礎食譜
    seasonings: [chili, honey]    # 玩家背包需同時持有的調味料
```

完成 `smoked_steak` 時若背包同時持有 chili + honey 的材質，額外產出隱藏料理 `honey_spicy_steak`。

## quality（五級品質）

混合制：完成時依機率擲骰，但**完成速度**與**廚師等級**會加權把機率往高級移動。

```yaml
quality:
  enabled: true
  speed-weight: 0.5      # 速度因子加權（0=純機率，1=純速度）
  level-weight: 0.15    # 等級因子加權
  tiers:
    failed:    { probability: 0.05, multiplier: 0.5 }   # 灰
    normal:    { probability: 0.40, multiplier: 1.0 }  # 白
    fine:      { probability: 0.35, multiplier: 1.25 } # 綠
    rare:      { probability: 0.15, multiplier: 1.5 }  # 青
    legendary: { probability: 0.05, multiplier: 2.0 }  # 金
```

- `probability`：各級基礎機率（建議總和 = 1.0；不為 1 會自動正規化）。
- `multiplier`：該級的效果時長倍率（套用於食用時）。
- 計算細節見 [API.md](API.md) 的 `QualityCalculator`。

## database

```yaml
database:
  type: sqlite
  file: data.db      # 存於 plugins/CraftKitchen/
```

儲存玩家料理經驗。啟動時載入、停用時保存。
