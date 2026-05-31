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
import com.ego.restaurant.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPwd;
    private Button btnRegister;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName       = findViewById(R.id.et_name);
        etEmail      = findViewById(R.id.et_email);
        etPhone      = findViewById(R.id.et_phone);
        etPassword   = findViewById(R.id.et_password);
        etConfirmPwd = findViewById(R.id.et_confirm_password);
        btnRegister  = findViewById(R.id.btn_register);
        tvBack       = findViewById(R.id.tv_back_login);

        tvBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String name    = etName.getText().toString().trim();
        String email   = etEmail.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        String pwd     = etPassword.getText().toString().trim();
        String confirm = etConfirmPwd.getText().toString().trim();

        if (TextUtils.isEmpty(name))  { etName.setError("Nhập họ tên"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Nhập email"); return; }
        if (pwd.length() < 6)         { etPassword.setError("Ít nhất 6 ký tự"); return; }
        if (!pwd.equals(confirm))     { etConfirmPwd.setError("Mật khẩu không khớp"); return; }

        btnRegister.setEnabled(false);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        long id = db.registerMember(name, email, phone, pwd);

        if (id == -2) {
            etEmail.setError("Email này đã được đăng ký");
            btnRegister.setEnabled(true);
            return;
        }
        if (id < 0) {
            Toast.makeText(this, "Lỗi đăng ký, thử lại", Toast.LENGTH_SHORT).show();
            btnRegister.setEnabled(true);
            return;
        }

        new SessionManager(this).saveSession((int) id, "MEMBER", name, email);
        Toast.makeText(this, "Đăng ký thành công! Chào " + name, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, CustomerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
