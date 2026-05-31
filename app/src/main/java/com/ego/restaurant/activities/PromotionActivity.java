package com.ego.restaurant.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.PromotionAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PromotionActivity extends AppCompatActivity {

    private ListView lvPromotions;
    private Button   btnAdd;

    private List<PromotionItem>  promos = new ArrayList<>();
    private PromotionAdapter     adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        if (!PermissionHelper.hasPermission(new SessionManager(this).getRole(),
                PermissionHelper.MANAGE_MENU_CATALOG)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        lvPromotions = findViewById(R.id.lv_promotions);
        btnAdd       = findViewById(R.id.btn_add_promotion);

        adapter = new PromotionAdapter(this, promos, item -> {
            DatabaseHelper.getInstance(this).togglePromotion(item.id, !item.active);
            loadPromos();
        });
        lvPromotions.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        loadPromos();
    }

    private void loadPromos() {
        promos.clear();
        Cursor c = DatabaseHelper.getInstance(this).getPromotions();
        while (c.moveToNext()) {
            promos.add(new PromotionItem(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.P_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.P_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.P_DISC)),
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.P_ACTIVE)) == 1));
        }
        c.close();
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Tên chương trình");
        android.widget.EditText etDisc = new android.widget.EditText(this);
        etDisc.setHint("% giảm (vd: 10)");
        etDisc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48,24,48,24);
        layout.addView(etName); layout.addView(etDisc);
        new AlertDialog.Builder(this)
                .setTitle("Tạo khuyến mãi mới")
                .setView(layout)
                .setPositiveButton("Tạo", (d,w)->{
                    String name = etName.getText().toString().trim();
                    String disc = etDisc.getText().toString().trim();
                    if (name.isEmpty() || disc.isEmpty()) { Toast.makeText(this,"Điền đầy đủ",Toast.LENGTH_SHORT).show(); return; }
                    DatabaseHelper.getInstance(this).insertPromotion(name, Double.parseDouble(disc), "MEMBER");
                    loadPromos();
                })
                .setNegativeButton("Huỷ",null).show();
    }

    public static class PromotionItem {
        public int id; public String name; public double discountPercent; public boolean active;
        public PromotionItem(int id, String n, double d, boolean a){this.id=id;name=n;discountPercent=d;active=a;}
    }
}
