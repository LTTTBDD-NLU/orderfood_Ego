package com.ego.restaurant.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.TopItemAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvBack, tvTotalRevenue, tvRevenuePeriod, tvOrderCount, tvAvgOrder;
    private Button   btnDay, btnWeek, btnMonth;
    private ListView lvTopItems;
    private List<String>    topItems = new ArrayList<>();
    private TopItemAdapter  topAdapter;
    private String          filterMode = "DAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        if (!PermissionHelper.hasPermission(new SessionManager(this).getRole(),
                PermissionHelper.VIEW_REVENUE_REPORT)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        tvBack          = findViewById(R.id.tv_back);
        tvTotalRevenue  = findViewById(R.id.tv_total_revenue);
        tvRevenuePeriod = findViewById(R.id.tv_revenue_period);
        tvOrderCount    = findViewById(R.id.tv_order_count);
        tvAvgOrder      = findViewById(R.id.tv_avg_order);
        btnDay          = findViewById(R.id.btn_filter_day);
        btnWeek         = findViewById(R.id.btn_filter_week);
        btnMonth        = findViewById(R.id.btn_filter_month);
        lvTopItems      = findViewById(R.id.lv_top_items);

        topAdapter = new TopItemAdapter(this, topItems);
        lvTopItems.setAdapter(topAdapter);

        tvBack.setOnClickListener(v -> finish());
        btnDay.setOnClickListener(v   -> { filterMode = "DAY";   tvRevenuePeriod.setText("Hôm nay");   loadRevenue(); });
        btnWeek.setOnClickListener(v  -> { filterMode = "WEEK";  tvRevenuePeriod.setText("Tuần này");  loadRevenue(); });
        btnMonth.setOnClickListener(v -> { filterMode = "MONTH"; tvRevenuePeriod.setText("Tháng này"); loadRevenue(); });

        loadRevenue();
    }

    private void loadRevenue() {
        long[] range  = getRange();
        ArrayList<Order> orders = DatabaseHelper.getInstance(this)
                .getPaidOrders(range[0], range[1]);

        double total = 0;
        Map<String, Integer> itemCount = new HashMap<>();
        for (Order o : orders) {
            total += o.getTotalAmount();
            if (o.getItems() != null)
                for (OrderDetail d : o.getItems())
                    itemCount.merge(d.getItemName(), d.getQuantity(), Integer::sum);
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvTotalRevenue.setText(nf.format((long) total) + " đ");
        tvOrderCount.setText(String.valueOf(orders.size()));
        tvAvgOrder.setText(orders.size() > 0
                ? nf.format((long)(total / orders.size())) + "đ" : "0đ");

        topItems.clear();
        itemCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> topItems.add(e.getKey() + " — " + e.getValue() + " phần"));
        topAdapter.notifyDataSetChanged();
    }

    private long[] getRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if ("WEEK".equals(filterMode))  cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if ("MONTH".equals(filterMode)) cal.set(Calendar.DAY_OF_MONTH, 1);
        return new long[]{cal.getTimeInMillis(), System.currentTimeMillis()};
    }
}
