package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.KitchenAdapter;
import com.ego.restaurant.adapters.KitchenHistoryAdapter;
import com.ego.restaurant.activities.KitchenHistoryActivity.KitchenHistoryItem;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class KitchenActivity extends AppCompatActivity {

    private LinearLayout tabBtnKds, tabBtnHistory, tabBtnKitchenProfile;
    private LinearLayout tabContentKds, tabContentHistory;
    private ScrollView   tabContentKitchenProfile;
    private static final int TAB_KDS=0, TAB_HIST=1, TAB_PROF=2;

    private ListView  lvOrders, lvHistory;
    private TextView  tvCookingCount;
    private ArrayList<OrderDetail>     cookingList = new ArrayList<>();
    private ArrayList<KitchenHistoryItem> histList = new ArrayList<>();
    private KitchenAdapter        kitchenAdapter;
    private KitchenHistoryAdapter histAdapter;

    private TextView tvKName, tvKRole, tvKDays, tvKSalary;
    private Button   btnKChangePwd, btnKLogout;

    private SessionManager sm;
    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            refreshData();
            handler.postDelayed(this, 2000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);

        sm = new SessionManager(this);
        if (!PermissionHelper.hasPermission(sm.getRole(), PermissionHelper.COOKING_COMPLETE)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        tabBtnKds            = findViewById(R.id.tab_btn_kds);
        tabBtnHistory        = findViewById(R.id.tab_btn_history);
        tabBtnKitchenProfile = findViewById(R.id.tab_btn_kitchen_profile);
        tabContentKds        = findViewById(R.id.tab_content_kds);
        tabContentHistory    = findViewById(R.id.tab_content_history);
        tabContentKitchenProfile = findViewById(R.id.tab_content_kitchen_profile);
        lvOrders             = findViewById(R.id.lv_kitchen_orders);
        tvCookingCount       = findViewById(R.id.tv_cooking_count);
        lvHistory            = findViewById(R.id.lv_kitchen_history);
        tvKName              = findViewById(R.id.tv_kitchen_profile_name);
        tvKRole              = findViewById(R.id.tv_kitchen_profile_role);
        tvKDays              = findViewById(R.id.tv_kitchen_profile_days);
        tvKSalary            = findViewById(R.id.tv_kitchen_profile_salary);
        btnKChangePwd        = findViewById(R.id.btn_kitchen_change_password);
        btnKLogout           = findViewById(R.id.btn_kitchen_logout);

        kitchenAdapter = new KitchenAdapter(this, cookingList, new KitchenAdapter.OnKitchenActionListener() {
            @Override public void onDone(OrderDetail d)       { markDone(d); }
            @Override public void onOutOfStock(OrderDetail d) { markOutOfStock(d); }
        });
        lvOrders.setAdapter(kitchenAdapter);

        histAdapter = new KitchenHistoryAdapter(this, histList);
        lvHistory.setAdapter(histAdapter);

        tabBtnKds.setOnClickListener(v            -> switchTab(TAB_KDS));
        tabBtnHistory.setOnClickListener(v        -> switchTab(TAB_HIST));
        tabBtnKitchenProfile.setOnClickListener(v -> switchTab(TAB_PROF));
        switchTab(TAB_KDS);

        btnKLogout.setOnClickListener(v -> {
            kitchenAdapter.shutdown();
            sm.clearSession();
            startActivity(new Intent(this, LandingActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
        btnKChangePwd.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        loadProfile();
        handler.post(pollTask);
    }

    private void switchTab(int tab) {
        tabContentKds.setVisibility(tab==TAB_KDS ?android.view.View.VISIBLE:android.view.View.GONE);
        tabContentHistory.setVisibility(tab==TAB_HIST?android.view.View.VISIBLE:android.view.View.GONE);
        tabContentKitchenProfile.setVisibility(tab==TAB_PROF?android.view.View.VISIBLE:android.view.View.GONE);
        int on=0xFFE64A19, off=0xFF2A2A2A;
        tabBtnKds.setBackgroundColor(tab==TAB_KDS?on:off);
        tabBtnHistory.setBackgroundColor(tab==TAB_HIST?on:off);
        tabBtnKitchenProfile.setBackgroundColor(tab==TAB_PROF?on:off);
    }

    private void loadProfile() {
        tvKName.setText(sm.getFullName().isEmpty() ? "Nhân viên Bếp" : sm.getFullName());
        tvKRole.setText("Nhân viên Bếp");
        int days   = DatabaseHelper.getInstance(this).countAttendedDays(sm.getUserId());
        double sal = days * 300000.0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvKDays.setText(String.valueOf(days));
        tvKSalary.setText(nf.format((long)sal)+"đ");
    }

    private void refreshData() {
        cookingList.clear();
        cookingList.addAll(DatabaseHelper.getInstance(this).getDetailsByStatus("COOKING"));
        tvCookingCount.setText("Đang nấu: " + cookingList.size());
        kitchenAdapter.notifyDataSetChanged();

        histList.clear();
        ArrayList<OrderDetail> delivered = DatabaseHelper.getInstance(this).getDetailsByStatus("DELIVERING");
        ArrayList<OrderDetail> completed = DatabaseHelper.getInstance(this).getDetailsByStatus("COMPLETED");
        delivered.addAll(completed);
        long todayStart = getTodayStart();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (OrderDetail d : delivered) {
            if (d.getOrderTime() >= todayStart) {
                histList.add(new KitchenHistoryItem(
                        d.getItemName(), d.getTableName(), d.getQuantity(),
                        sdf.format(new Date(d.getOrderTime()))));
            }
        }
        histAdapter.notifyDataSetChanged();
    }

    private void markDone(OrderDetail d) {
        DatabaseHelper.getInstance(this).updateDetailStatus(d.getDetailId(), "DELIVERING");
        Toast.makeText(this,"✅ "+d.getItemName()+" → Bàn "+d.getTableName(),Toast.LENGTH_SHORT).show();
    }

    private void markOutOfStock(OrderDetail d) {
        DatabaseHelper.getInstance(this).updateDetailStatus(d.getDetailId(), "CANCELLED");
        Toast.makeText(this,"❌ Hết: "+d.getItemName(),Toast.LENGTH_LONG).show();
    }

    private long getTodayStart() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY,0);
        c.set(java.util.Calendar.MINUTE,0);
        c.set(java.util.Calendar.SECOND,0);
        c.set(java.util.Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        kitchenAdapter.shutdown();
        handler.removeCallbacks(pollTask);
    }
}
