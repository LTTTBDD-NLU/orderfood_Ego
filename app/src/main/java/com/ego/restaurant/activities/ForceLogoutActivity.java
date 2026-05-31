package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.ego.restaurant.R;
import com.ego.restaurant.utils.SessionManager;

public class ForceLogoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_force_logout);
        new SessionManager(this).clearSession();
        Button btn = findViewById(R.id.btn_go_login);
        btn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }
    @Override
    public void onBackPressed() {

    }
}
