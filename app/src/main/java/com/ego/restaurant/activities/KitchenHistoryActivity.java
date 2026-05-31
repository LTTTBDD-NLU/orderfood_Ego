package com.ego.restaurant.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ego.restaurant.R;

public class KitchenHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish(); // Không dùng standalone nữa, mở KitchenActivity tab history
    }
    public static class KitchenHistoryItem {
        public String itemName, tableNum, finishTime;
        public int qty;
        public KitchenHistoryItem(String name, String table, int qty, String time) {
            this.itemName   = name;
            this.tableNum   = table;
            this.qty        = qty;
            this.finishTime = time;
        }
    }
}
