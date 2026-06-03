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

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPwd, etNewPwd, etConfirmPwd;
    private Button   btnUpdate;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etCurrentPwd = findViewById(R.id.et_current_password);
        etNewPwd     = findViewById(R.id.et_new_password);
        etConfirmPwd = findViewById(R.id.et_confirm_password);
        btnUpdate    = findViewById(R.id.btn_update_password);
        tvBack       = findViewById(R.id.tv_back);

        tvBack.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> attemptChange());
    }

    private void attemptChange() {
        String cur  = etCurrentPwd.getText().toString().trim();
        String nw   = etNewPwd.getText().toString().trim();
        String conf = etConfirmPwd.getText().toString().trim();

        if (TextUtils.isEmpty(cur)) { etCurrentPwd.setError("Nhập mật khẩu hiện tại"); return; }
        if (nw.length() < 6)        { etNewPwd.setError("Mật khẩu mới ít nhất 6 ký tự"); return; }
        if (!nw.equals(conf))        { etConfirmPwd.setError("Xác nhận không khớp"); return; }

        SessionManager sm = new SessionManager(this);
        int userId = sm.getUserId();

        boolean ok = DatabaseHelper.getInstance(this).changePassword(userId, cur, nw);
        if (!ok) {
            etCurrentPwd.setError("Mật khẩu hiện tại không đúng");
            return;
        }

        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
        sm.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
