package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.utils.SessionManager;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu đã login → nhảy thẳng vào màn tương ứng
        SessionManager sm = new SessionManager(this);
        if (sm.isLoggedIn()) {
            redirectByRole(sm.getRole());
            return;
        }

        setContentView(R.layout.activity_landing);

        Button btnGuest  = findViewById(R.id.btn_guest_order);
        Button btnLogin  = findViewById(R.id.btn_go_login);

        btnGuest.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerActivity.class)));
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void redirectByRole(String role) {
        Intent intent;
        if (role == null) role = "GUEST";
        switch (role.toUpperCase()) {
            case "WAITSTAFF":
                intent = new Intent(this, WaiterActivity.class); break;
            case "KITCHEN_STAFF":
                intent = new Intent(this, KitchenActivity.class); break;
            case "ADMIN": case "SUPERADMIN":
                intent = new Intent(this, AdminDashboardActivity.class); break;
            default:
                intent = new Intent(this, CustomerActivity.class); break;
        }
        startActivity(intent);
        finish();
    }
}
