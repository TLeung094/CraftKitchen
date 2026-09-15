# 安裝指南

CraftKitchen 是 Paper 伺服器的料理系統插件，支援「原版模式」（零依賴）與「CraftEngine 模式」（需安裝 CraftEngine）雙模式自動切換。

## 系統需求

| 項目 | 最低版本 | 備註 |
|---|---|---|
| Minecraft 伺服器 | Paper 1.20.4+ | 不支援 Spigot 以下 |
| Java | 17+ | 建置使用 Java 25，運行需 17+ |
| CraftEngine | 可選 | 安裝後自動啟用 CE 模式 |
| SQLite | 內嵌 | 插件已打包 sqlite-jdbc，無需另外安裝 |

## 安裝步驟

1. 從 [GitHub Releases](https://github.com/TLeung094/CraftKitchen/releases) 下載最新 `CraftKitchen-x.x.x.jar`。
2. 將 jar 放入伺服器 `plugins/` 目錄。
3. （原版模式）如需自訂物品貼圖，把 `resourcepack/` 打包成 ZIP 上傳為伺服器資源包，或發給玩家。
4. （CE 模式）安裝 [CraftEngine](https://github.com/momirealms/CraftEngine) 到 `plugins/`，並放入 `craftengine/` 設定檔（見 [MODES.md](MODES.md)）。
5. 啟動伺服器。插件會自動偵測模式並載入 `config.yml`。
6. 確認控制台看到 `CraftKitchen 已啟動，模式：VANILLA`（或 `CRAFTENGINE`）。

## 首次設定

啟動後 `plugins/CraftKitchen/config.yml` 會自動生成。可調整：

- `settings.item-mode`：`auto`（偵測）/ `vanilla` / `craftengine`
- `settings.cooking-timeout-seconds`：進度逾時重置秒數
- `settings.perfect-window-seconds`：完美窗口（影響品質判定的速度因子）
- `quality`：五級品質的機率、倍率、加權（見 [CONFIG.md](CONFIG.md)）

改完後執行 `/kitchen reload`（需 `craftkitchen.admin` 權限）即時生效。

## 權限

| 權限節點 | 預設 | 說明 |
|---|---|---|
| `craftkitchen.use` | 全員 | 使用食譜／等級查詢 |
| `craftkitchen.admin` | OP | reload、give |

## 指令

| 指令 | 說明 |
|---|---|
| `/kitchen recipe` | 列出可用食譜 |
| `/kitchen level` | 查看料理等級與稱號 |
| `/kitchen mode` | 查看當前物品模式 |
| `/kitchen reload` | 重載配置 |
| `/kitchen give <food>` | 給予食物（管理員） |

## 常見問題

- **吃自訂料理沒效果？** 確認 `config.yml` 的 `foods` 區段有定義該食物的 `effects`，且物品帶有 `food_id` PDC（由插件產生的成品才有）。
- **cook 步驟沒反應？** 爐灶（營火）必須先用打火石點燃。潛行右鍵營火 + 打火石 = 點燃；水桶 = 澆滅。
- **模式沒切到 CE？** 確認 CraftEngine 已安裝且 `item-mode` 不是 `vanilla`。
