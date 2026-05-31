package com.ego.restaurant.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.models.Table;

import java.util.ArrayList;

public class AdminFloorMapActivity extends AppCompatActivity {

    private GridLayout gridTables;
    private TextView   tvBack, tvTableStats;

    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            refreshTables();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_floor_map);

        tvBack       = findViewById(R.id.tv_back);
        tvTableStats = findViewById(R.id.tv_table_stats);
        gridTables   = findViewById(R.id.grid_admin_tables);

        tvBack.setOnClickListener(v -> finish());
        handler.post(pollTask);
    }

    private void refreshTables() {
        ArrayList<Table> tables = DatabaseHelper.getInstance(this).getAllTables();
        gridTables.removeAllViews();

        int dp = (int)(90 * getResources().getDisplayMetrics().density);
        int mg = (int)(6  * getResources().getDisplayMetrics().density);
        int empty=0, occupied=0, waiting=0;

        for (Table t : tables) {
            String st = t.getStatus() != null ? t.getStatus() : "EMPTY";
            switch (st) {
                case "OCCUPIED":        occupied++; break;
                case "WAITING_PAYMENT": waiting++;  break;
                default:                empty++;
            }

            Button btn = new Button(this);
            btn.setText(t.getTableName());
            btn.setTextColor(0xFFFFFFFF);
            btn.setTextSize(12f);

            int color;
            switch (st) {
                case "OCCUPIED":        color = getResources().getColor(R.color.table_occupied);        break;
                case "WAITING_PAYMENT": color = getResources().getColor(R.color.table_waiting_payment); break;
                default:                color = getResources().getColor(R.color.table_empty);
            }
            btn.setBackgroundTintList(ColorStateList.valueOf(color));

            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width  = dp; p.height = dp;
            p.setMargins(mg, mg, mg, mg);
            btn.setLayoutParams(p);

            btn.setOnClickListener(v -> showTableOptions(t));
            gridTables.addView(btn);
        }

        tvTableStats.setText(empty + " trống · " + occupied + " khách · " + waiting + " chờ TT");
    }

    private void showTableOptions(Table t) {
        String st = t.getStatus() != null ? t.getStatus() : "EMPTY";
        String label;
        switch (st) {
            case "OCCUPIED":        label = "🟢 Đang có khách"; break;
            case "WAITING_PAYMENT": label = "🔴 Chờ thanh toán"; break;
            default:                label = "⚪ Trống";
        }

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(t.getTableName())
                        .setMessage(label)
                        .setNegativeButton("Đóng", null);

        if ("WAITING_PAYMENT".equals(st) || "OCCUPIED".equals(st)) {
            builder.setPositiveButton("📄 Xem hóa đơn", (d, w) -> {
                Order o = DatabaseHelper.getInstance(this).getActiveOrderByTable(t.getTableId());
                Intent intent = new Intent(this, BillPreviewActivity.class);
                intent.putExtra("table_id",   t.getTableId());
                intent.putExtra("table_name", t.getTableName());
                if (o != null) intent.putExtra("order_id", o.getOrderId());
                startActivity(intent);
            });
        }
        builder.show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }
}
