package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Staff;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail      = findViewById(R.id.et_email);
        etPassword   = findViewById(R.id.et_password);
        btnLogin     = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);
        tvBack       = findViewById(R.id.tv_back_landing);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        tvBack.setOnClickListener(v -> finish());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pwd   = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) { etEmail.setError("Nhập email"); return; }
        if (TextUtils.isEmpty(pwd))   { etPassword.setError("Nhập mật khẩu"); return; }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Staff staff = db.login(email, pwd);

        btnLogin.setEnabled(true);
        btnLogin.setText("ĐĂNG NHẬP");

        if (staff == null) {
            Toast.makeText(this, "Sai email/mật khẩu hoặc tài khoản bị khóa",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new SessionManager(this).saveSession(
                staff.getId(), staff.getRole(), staff.getName(), email);
        navigateByRole(staff.getRole());
    }

    private void navigateByRole(String role) {
        Intent intent;
        switch (PermissionHelper.getHomeActivityForRole(role)) {
            case "WaiterActivity":
                intent = new Intent(this, WaiterActivity.class); break;
            case "KitchenActivity":
                intent = new Intent(this, KitchenActivity.class); break;
            case "AdminDashboardActivity":
                intent = new Intent(this, AdminDashboardActivity.class); break;
            default:
                intent = new Intent(this, CustomerActivity.class); break;
        }
        startActivity(intent);
        finish();
    }
}
