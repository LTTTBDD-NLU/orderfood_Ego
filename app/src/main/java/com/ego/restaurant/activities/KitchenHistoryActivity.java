package com.ego.restaurant.activities;

import androidx.appcompat.app.AppCompatActivity;

public class KitchenHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish(); // Không dùng standalone, xem KitchenActivity tab history
    }

    public static class KitchenHistoryItem {
        public String detailId;   // Bug #36 FIX: cần để gọi rollbackDetailToCoking()
        public String itemName;
        public String tableNum;
        public int    qty;
        public String finishTime;

        public KitchenHistoryItem(String detailId, String itemName,
                                   String tableNum, int qty, String finishTime) {
            this.detailId   = detailId;
            this.itemName   = itemName;
            this.tableNum   = tableNum;
            this.qty        = qty;
            this.finishTime = finishTime;
        }

        public KitchenHistoryItem(String itemName, String tableNum, int qty, String finishTime) {
            this(null, itemName, tableNum, qty, finishTime);
        }
    }
}
