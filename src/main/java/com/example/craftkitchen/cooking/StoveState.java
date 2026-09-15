package com.example.craftkitchen.cooking;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 爐灶點燃狀態追蹤器（純邏輯，可單元測試）。
 *
 * <p>cook 步驟需爐灶已點燃才可進行。狀態以「位置鍵」（world:x,y,z 字串）為索引，
 * 純 Java 字串運算不依賴 Bukkit，方便測試；listener 負責方塊 ↔ 位置鍵轉換。
 *
 * <p>狀態存於記憶體，伺服器重啟後重置（v1 行為）；未來可持久化至 SQLite。
 */
public class StoveState {
    private final Set<String> lit = ConcurrentHashMap.newKeySet();

    public boolean isLit(String locationKey) {
        return locationKey != null && lit.contains(locationKey);
    }

    public void light(String locationKey) {
        if (locationKey != null) {
            lit.add(locationKey);
        }
    }

    public void extinguish(String locationKey) {
        if (locationKey != null) {
            lit.remove(locationKey);
        }
    }

    public int litCount() {
        return lit.size();
    }

    public void clear() {
        lit.clear();
    }
}
