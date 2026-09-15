package com.example.craftkitchen.cooking;

public enum CookingResult {
    UNKNOWN_RECIPE,
    INVALID_STEP,
    IN_PROGRESS,
    COMPLETED,
    /** 玩家對該方塊操作時，沒有可推進或可啟動的食譜（步驟不吻合或食材不足） */
    NO_APPLICABLE_RECIPE
}
