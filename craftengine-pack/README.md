# CraftEngine Content Pack — CraftKitchen

本資料夾是 CraftKitchen 插件附帶的 **CraftEngine content pack**，定義了自訂料理食物與廚房方塊。

## 內容

```
craftkitchen/
├── pack.yml                      # pack 識別（namespace: craftkitchen）
├── configuration/
│   └── items.yml                 # 3 道料理 + 4 個廚房方塊定義
└── resourcepack/
    └── assets/craftkitchen/
        ├── models/item/          # 物品模型 JSON（item/generated parent）
        └── textures/
            ├── item/             # 3 道料理 32×32 貼圖
            └── block/            # 4 個方塊 top/side 32×32 貼圖
```

### 料理食物（`material: cooked_beef`，自訂模型與名稱）
| ID | 顯示名 | 說明 |
|---|---|---|
| `craftkitchen:smoked_steak` | 煙燻牛排 | 原汁原味，香氣四溢 |
| `craftkitchen:herb_steak` | 香草牛排 | 清新香草，入口即化 |
| `craftkitchen:honey_spicy_steak` | 蜜汁辣牛排 | 甜辣交織，隱藏食譜 |

### 廚房方塊（`block_item`，可放置）
| ID | 用途 | 觸發步驟 |
|---|---|---|
| `craftkitchen:cutting_board` | 砧板 | `cut`（切） |
| `craftkitchen:stove` | 爐灶 | `cook`（煮，需點燃） |
| `craftkitchen:marinating_stand` | 醃漬架 | `marinate`（醃） |
| `craftkitchen:seasoning_table` | 調味台 | `season`（調味） |

## 部署步驟

1. **安裝 CraftEngine**（Community Edition 可，[Modrinth](https://modrinth.com/plugin/craftengine)），放入伺服器 `plugins/`，啟動一次讓它生成資料夾。
2. 將本 `craftkitchen` 資料夾（或解壓 `craftkitchen-pack.zip`）複製到：
   ```
   plugins/CraftEngine/resources/craftkitchen/
   ```
3. 遊戲內執行：
   ```
   /ce reload all
   ```
   CraftEngine 會自動打包資源包並發給線上玩家。
4. 確認 `config.yml` 的 `settings.item-mode: auto`（或 `craftengine`），CraftKitchen 會自動偵測 CE 並切換至 CE 模式。
5. 測試：`/ce item get craftkitchen:smoked_steak` 取得料理；放置 `craftkitchen:cutting_board` 後潛行右鍵開始烹飪。

## 替換貼圖

`textures/` 下的 PNG 為 32×32 佔位像素風貼圖，可直接替換為正式美術素材（保持相同檔名與解析度即可，CE 會重新打包）。

替換後執行 `/ce reload all` 重新產生資源包。
