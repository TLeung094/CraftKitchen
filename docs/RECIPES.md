# 食譜與烹飪流程

## 烹飪迴圈

```
潛行右鍵方塊 → 食材檢查 → 多步驟推進（個人化、逾時重置）→ 品質判定 → 成品 → 食用效果加成
```

## 觸發方式

**所有烹飪操作一律「潛行右鍵」對應方塊觸發**（避免開啟原版介面）。插件依背包食材自動判定要推進哪道食譜：

1. 優先推進玩家「已進行中且下一步吻合」的食譜。
2. 若無，嘗試啟動「第一步吻合且食材齊全」的新食譜。

## 步驟 → 方塊對應

| 步驟 | 方塊 | 備註 |
|---|---|---|
| `cut`（切） | 木製壓力板 / 切石機 | — |
| `marinate`（醃） | 釀造台 | — |
| `cook`（煮） | 營火 / 靈魂營火 | **需先用打火石點燃** |
| `season`（調味） | 工作台 | — |

爐灶點燃／澆滅見 [BLOCKS.md](BLOCKS.md)。

## 內建食譜

### 煙燻牛排（smoked_steak）

| 步驟 | 操作 | 輸入 |
|---|---|---|
| 1 | 潛行右鍵壓力板／切石機 | — |
| 2 | 潛行右鍵釀造台 | — |
| 3 | 潛行右鍵**已點燃**營火 | — |

完成消耗：`BEEF` + `PEPPER`。成品效果：力量 I（60s）。

### 香草牛排（herb_steak）

步驟：`cut → cook → season`。消耗：`BEEF` + `SWEET_BERRIES`。效果：回復（10s）。

### 蜜汁辣牛排（honey_spicy_steak，隱藏）

非獨立食譜，而是完成 `smoked_steak` 時若背包同時持有 `chili`（RED_DYE）與 `honey`（HONEY_BOTTLE）額外產出。效果：力量 II（60s）+ 速度 I（30s）。

## 品質

完成最後一步時判定品質（五級：失敗／普通／精良／稀有／傳說）。成品 lore 顯示品質與顏色；食用時依品質倍率縮放效果時長（失敗 0.5x、傳說 2.0x）。判定細節見 [API.md](API.md)。

## 新增食譜

在 `config.yml` 的 `recipes` 區段加一筆，並在 `foods` 定義對應成品即可：

```yaml
foods:
  grilled_salmon:
    name: 烤鮭魚
    material: COOKED_SALMON
    custom-model-data: 10301
    effects:
      - { type: WATER_BREATHING, duration: 60, amplifier: 0 }

recipes:
  grilled_salmon:
    name: 烤鮭魚
    steps: [cut, cook, season]
    ingredients: [SALMON, KELP]
```

執行 `/kitchen reload` 後新食譜立即可用。記得在資源包加入對應 `custom-model-data` 的貼圖。
