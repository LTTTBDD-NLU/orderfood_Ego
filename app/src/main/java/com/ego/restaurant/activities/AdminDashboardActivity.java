package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    // IDs tồn tại trong activity_admin_dashboard.xml
    private TextView     tvMyName, tvTodayRevenue, tvTodayOrders;
    private LinearLayout navFloorMap, navStaff, navMenu, navAnalytics,
                         navSchedule, navPromotions, navProfile;

    private TextView tvScheduleSubLabel;
    private SessionManager sm;
    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            refreshStats();
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sm = new SessionManager(this);
        String role = sm.getRole();
        if (!PermissionHelper.hasPermission(role, PermissionHelper.MANAGE_STAFF_ACCOUNT)
                && !PermissionHelper.hasPermission(role, PermissionHelper.VIEW_REVENUE_REPORT)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        tvMyName       = findViewById(R.id.tv_my_name);
        tvTodayRevenue = findViewById(R.id.tv_today_revenue);
        tvTodayOrders  = findViewById(R.id.tv_today_orders);
        navFloorMap    = findViewById(R.id.nav_floor_map);
        navStaff       = findViewById(R.id.nav_staff);
        navMenu        = findViewById(R.id.nav_menu);
        navAnalytics   = findViewById(R.id.nav_analytics);
        navSchedule    = findViewById(R.id.nav_schedule);
        navPromotions  = findViewById(R.id.nav_promotions);
        navProfile     = findViewById(R.id.nav_profile);

        if (navSchedule != null && navSchedule.getChildCount() >= 3) {
            View child = navSchedule.getChildAt(2);
            if (child instanceof TextView) tvScheduleSubLabel = (TextView) child;
        }

        tvMyName.setText(sm.getFullName());
        setupNavigation(role);
        handler.post(pollTask);

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            sm.clearSession();
            startActivity(new Intent(this, LandingActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void setupNavigation(String role) {
        navFloorMap.setOnClickListener(v ->
                startActivity(new Intent(this, AdminFloorMapActivity.class)));

        navStaff.setOnClickListener(v ->
                startActivity(new Intent(this, AdminStaffActivity.class)));

        navMenu.setOnClickListener(v -> {
            if (PermissionHelper.hasPermission(role, PermissionHelper.MANAGE_MENU_CATALOG))
                startActivity(new Intent(this, MenuManagementActivity.class));
            else Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
        });

        navAnalytics.setOnClickListener(v -> {
            if (PermissionHelper.hasPermission(role, PermissionHelper.VIEW_REVENUE_REPORT))
                startActivity(new Intent(this, AnalyticsActivity.class));
            else Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
        });

        navSchedule.setOnClickListener(v -> {
            int pending = DatabaseHelper.getInstance(this).getDetailsByStatus("PENDING_CONFIRM").size();
            if (pending > 0 && PermissionHelper.hasPermission(role, PermissionHelper.VERIFY_GUEST_ORDER)) {
                new AlertDialog.Builder(this)
                    .setTitle("Chọn chức năng")
                    .setItems(new String[]{
                            "📅  Lịch làm việc / Chấm công",
                            "⏳  Duyệt " + pending + " đơn chờ xác nhận"
                    }, (d, which) -> {
                        if (which == 0) startActivity(new Intent(this, ScheduleActivity.class));
                        else            startActivity(new Intent(this, WaiterActivity.class));
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
            } else {
                startActivity(new Intent(this, ScheduleActivity.class));
            }
        });

        navPromotions.setOnClickListener(v -> {
            if (PermissionHelper.hasPermission(role, PermissionHelper.MANAGE_MENU_CATALOG))
                startActivity(new Intent(this, PromotionActivity.class));
            else Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Tài khoản quản lý")
                        .setMessage("👤 " + sm.getFullName() + "\n📧 " + sm.getEmail())
                        .setPositiveButton("Đổi mật khẩu", (d, w) ->
                                startActivity(new Intent(this, ChangePasswordActivity.class)))
                        .setNegativeButton("Đóng", null)
                        .show());
    }

    private void refreshStats() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        long now        = System.currentTimeMillis();

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        ArrayList<Order> paidOrders = db.getPaidOrders(todayStart, now);
        double total = 0;
        for (Order o : paidOrders) total += o.getTotalAmount();

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvTodayRevenue.setText(nf.format((long) total) + " đ");
        tvTodayOrders.setText(String.valueOf(paidOrders.size()));

        int pendingCount = db.getDetailsByStatus("PENDING_CONFIRM").size();
        if (tvScheduleSubLabel != null) {
            if (pendingCount > 0) {
                tvScheduleSubLabel.setText("⚠ " + pendingCount + " đơn chờ duyệt");
                tvScheduleSubLabel.setTextColor(0xFFE64A19);
            } else {
                tvScheduleSubLabel.setText("Chấm công nhân viên");
                tvScheduleSubLabel.setTextColor(0xFF9E9E9E);
            }
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }
}
