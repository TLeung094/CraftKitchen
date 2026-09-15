# CraftKitchen 資源包（原版模式）

本目錄為原版模式的自訂物品貼圖。CraftEngine 模式不需此包（由 CE 自動生成模型）。

## 內容

- `pack.mcmeta`：資源包宣告（pack_format 18 = MC 1.20.x）。
- `assets/minecraft/models/item/cooked_beef.json`：以 `CustomModelData` override 原版烤牛肉，指向自訂模型。
- `assets/craftkitchen/models/item/*.json`：各料理模型（parent = `item/generated`）。
- `assets/craftkitchen/textures/item/*.png`：16×16 佔位貼圖（可替換為正式美術）。

## 自訂物品編號對應

| CustomModelData | 物品 | 貼圖 |
|---|---|---|
| 10201 | 煙燻牛排 | smoked_steak.png |
| 10202 | 香草牛排 | herb_steak.png |
| 10203 | 蜜汁辣牛排 | honey_spicy_steak.png |

## 打包與安裝

1. 將本目錄內所有檔案壓成 ZIP（**pack.mcmeta 必須在 ZIP 根目錄**）：
   ```bash
   cd resourcepack && zip -r ../CraftKitchen-ResourcePack.zip assets pack.mcmeta
   ```
2. 伺服器：上傳為伺服器資源包（`server.yml` 的 `resource-pack` 設定），玩家進服自動下載。
   - 或放給玩家手動放進 `.minecraft/resourcepacks/`。
3. 佔位貼圖可替換為正式美術（檔名不變即可）。

## 新增物品貼圖

1. 在 `config.yml` 的 `foods` 加新食物並指定新的 `custom-model-data`（例如 10401）。
2. 在 `assets/craftkitchen/textures/item/` 放對應 PNG（例如 `grilled_salmon.png`）。
3. 在 `assets/craftkitchen/models/item/grilled_salmon.json` 建模型（parent = `item/generated`，layer0 指向貼圖）。
4. 在 `cooked_beef.json`（或該食物使用的原版材質）的 `overrides` 加一筆 predicate。
5. 重打包 ZIP。
