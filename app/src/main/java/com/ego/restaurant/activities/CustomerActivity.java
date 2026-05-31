package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ego.restaurant.R;
import com.ego.restaurant.utils.SessionManager;

public class CustomerActivity extends AppCompatActivity {

    private TextView tvWelcome, tvRoleBadge, tvSwitchAccount, tvChangePwd;
    private EditText etTableNumber;
    private Button   btnViewMenu, btnMyBill;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        sm            = new SessionManager(this);
        tvWelcome     = findViewById(R.id.tv_welcome);
        tvRoleBadge   = findViewById(R.id.tv_role_badge);
        tvSwitchAccount=findViewById(R.id.tv_switch_account);
        tvChangePwd   = findViewById(R.id.tv_change_password);
        etTableNumber = findViewById(R.id.et_table_number);
        btnViewMenu   = findViewById(R.id.btn_view_menu);
        btnMyBill     = findViewById(R.id.btn_my_bill);

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
            tvSwitchAccount.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }

        btnViewMenu.setOnClickListener(v -> goToMenu());
        btnMyBill.setOnClickListener(v   -> goToTracking());
        tvChangePwd.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private void goToMenu() {
        String tStr = etTableNumber.getText().toString().trim();
        if (TextUtils.isEmpty(tStr)) { etTableNumber.setError("Nhập số bàn"); return; }
        int num;
        try { num = Integer.parseInt(tStr); }
        catch (NumberFormatException e) { etTableNumber.setError("Không hợp lệ"); return; }

        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("table_number", num);
        intent.putExtra("table_name",   "Bàn " + String.format("%02d", num));
        intent.putExtra("table_id",     String.format("T%02d", num));
        intent.putExtra("role",         sm.getRole());
        startActivity(intent);
    }

    private void goToTracking() {
        String tStr = etTableNumber.getText().toString().trim();
        if (TextUtils.isEmpty(tStr)) { etTableNumber.setError("Nhập số bàn trước"); return; }
        int num;
        try { num = Integer.parseInt(tStr); }
        catch (NumberFormatException e) { etTableNumber.setError("Không hợp lệ"); return; }

        String tableId = String.format("T%02d", num);
        com.ego.restaurant.models.Order o =
                com.ego.restaurant.helpers.DatabaseHelper.getInstance(this)
                        .getActiveOrderByTable(tableId);

        Intent intent = new Intent(this, OrderTrackingActivity.class);
        intent.putExtra("table_id",   tableId);
        intent.putExtra("table_name", "Bàn " + String.format("%02d", num));
        intent.putExtra("role",       sm.getRole());
        if (o != null) intent.putExtra("order_id", o.getOrderId());
        startActivity(intent);
    }

    private void logout() {
        sm.clearSession();
        startActivity(new Intent(this, LandingActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
