package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.utils.SessionManager;

public class CustomerActivity extends AppCompatActivity {

    // IDs thực tế trong activity_customer.xml
    private TextView   tvWelcome, tvRoleBadge, tvSwitchAccount, tvChangePwd;
    private EditText   etTableNumber;
    private Button     btnViewMenu, btnMyBill;

    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        sm             = new SessionManager(this);
        tvWelcome      = findViewById(R.id.tv_welcome);
        tvRoleBadge    = findViewById(R.id.tv_role_badge);
        tvSwitchAccount= findViewById(R.id.tv_switch_account);
        tvChangePwd    = findViewById(R.id.tv_change_password);
        etTableNumber  = findViewById(R.id.et_table_number);
        btnViewMenu    = findViewById(R.id.btn_view_menu);
        btnMyBill      = findViewById(R.id.btn_my_bill);

        String role = sm.getRole();
        String name = sm.getFullName();

        if ("MEMBER".equals(role)) {
            tvWelcome.setText("Xin chào, " + (TextUtils.isEmpty(name) ? "thành viên" : name) + "!");
            tvRoleBadge.setText("THÀNH VIÊN");
            btnMyBill.setVisibility(View.VISIBLE);
            tvChangePwd.setVisibility(View.VISIBLE);
            tvSwitchAccount.setText("Đăng xuất");
            tvSwitchAccount.setOnClickListener(v -> logout());
        } else {
            tvRoleBadge.setText("KHÁCH VÃN LAI");
            tvSwitchAccount.setText("Đăng nhập / Đăng ký");
            tvSwitchAccount.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }

        btnViewMenu.setOnClickListener(v -> goToMenu());
        btnMyBill.setOnClickListener(v   -> goToTracking());
        tvChangePwd.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("MEMBER".equals(sm.getRole())) {
            checkActiveOrderAndNotify();
        }
    }

    private void checkActiveOrderAndNotify() {
        String savedOrderId   = sm.getActiveOrderId();
        String savedTableId   = sm.getActiveTableId();
        String savedTableName = sm.getActiveTableName();

        if (savedOrderId == null || savedOrderId.isEmpty()) return;

        Order o = DatabaseHelper.getInstance(this).getOrderById(savedOrderId);
        if (o == null || "PAID".equals(o.getOrderStatus()) || "CANCELLED".equals(o.getOrderStatus())) {
            sm.clearActiveOrder();
            return;
        }

        if (savedTableId != null && savedTableId.length() > 1) {
            try {
                String numStr = savedTableId.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) etTableNumber.setText(numStr);
            } catch (Exception ignored) {}
        }

        String displayTable = savedTableName != null && !savedTableName.isEmpty()
                ? savedTableName : "bàn của bạn";
        new AlertDialog.Builder(this)
                .setTitle("📋 Đơn hàng đang hoạt động")
                .setMessage("Bạn có đơn hàng đang chờ tại " + displayTable
                        + ".\n\nBấm 'Theo dõi đơn' để xem tiến trình.")
                .setPositiveButton("Theo dõi đơn", (d, w) -> {
                    Intent intent = new Intent(this, OrderTrackingActivity.class);
                    intent.putExtra("order_id",   savedOrderId);
                    intent.putExtra("table_id",   savedTableId);
                    intent.putExtra("table_name", savedTableName);
                    intent.putExtra("role",       sm.getRole());
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void goToMenu() {
        String tStr = etTableNumber.getText().toString().trim();
        if (TextUtils.isEmpty(tStr)) { etTableNumber.setError("Nhập số bàn"); return; }
        int num;
        try { num = Integer.parseInt(tStr); }
        catch (NumberFormatException e) { etTableNumber.setError("Không hợp lệ"); return; }
        if (num < 1 || num > 99) { etTableNumber.setError("Số bàn 1–99"); return; }

        String tableId = String.format("T%02d", num);
        if (!DatabaseHelper.getInstance(this).tableExists(tableId)) {
            etTableNumber.setError("Bàn " + num + " không tồn tại");
            Toast.makeText(this, "Bàn " + num + " không có trong sơ đồ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("table_number", num);
        intent.putExtra("table_name",   "Bàn " + String.format("%02d", num));
        intent.putExtra("table_id",     tableId);
        intent.putExtra("role",         sm.getRole());
        startActivity(intent);
    }

    private void goToTracking() {
        String tStr = etTableNumber.getText().toString().trim();
        if (TextUtils.isEmpty(tStr)) { etTableNumber.setError("Nhập số bàn trước"); return; }
        int num;
        try { num = Integer.parseInt(tStr); }
        catch (NumberFormatException e) { etTableNumber.setError("Không hợp lệ"); return; }

        String tableId   = String.format("T%02d", num);
        String tableName = "Bàn " + String.format("%02d", num);

        if (!DatabaseHelper.getInstance(this).tableExists(tableId)) {
            etTableNumber.setError("Bàn " + num + " không tồn tại");
            return;
        }

        Order o = DatabaseHelper.getInstance(this).getActiveOrderByTable(tableId);
        if (o == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Chưa có đơn hàng")
                    .setMessage(tableName + " chưa có đơn nào đang hoạt động.\nBạn muốn đặt món mới?")
                    .setPositiveButton("Xem thực đơn", (d, w) -> goToMenu())
                    .setNegativeButton("Đóng", null)
                    .show();
            return;
        }

        Intent intent = new Intent(this, OrderTrackingActivity.class);
        intent.putExtra("table_id",   tableId);   // Bug #13 FIX: đúng key
        intent.putExtra("table_name", tableName);
        intent.putExtra("role",       sm.getRole());
        intent.putExtra("order_id",   o.getOrderId());
        startActivity(intent);
    }

    private void logout() {
        sm.clearSession();
        startActivity(new Intent(this, LandingActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
