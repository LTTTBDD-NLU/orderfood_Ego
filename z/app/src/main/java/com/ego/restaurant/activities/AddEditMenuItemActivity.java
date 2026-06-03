package com.ego.restaurant.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.MenuItem;

import java.util.ArrayList;

public class AddEditMenuItemActivity extends AppCompatActivity {

    private TextView tvTitle, tvBack;
    private EditText etName, etDesc, etCat, etGuestPrice, etMemberPrice, etImageUrl;
    private Button   btnSave;

    private String editItemId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_menu_item);

        tvTitle      = findViewById(R.id.tv_title);
        tvBack       = findViewById(R.id.tv_back);
        etName       = findViewById(R.id.et_item_name);
        etDesc       = findViewById(R.id.et_item_desc);
        etCat        = findViewById(R.id.et_category);
        etImageUrl   = findViewById(R.id.et_image_url);
        etGuestPrice = findViewById(R.id.et_guest_price);
        etMemberPrice= findViewById(R.id.et_member_price);
        btnSave      = findViewById(R.id.btn_save_item);

        editItemId = getIntent().getStringExtra("item_id");
        if (editItemId != null) {
            tvTitle.setText("Sửa món ăn");
            loadExistingItem(editItemId);
        }

        tvBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveItem());
        findViewById(R.id.btn_pick_image).setOnClickListener(v ->
                Toast.makeText(this, "Nhập URL ảnh vào ô bên dưới", Toast.LENGTH_SHORT).show());
    }

    private void loadExistingItem(String itemId) {
        ArrayList<MenuItem> all = DatabaseHelper.getInstance(this).getAllMenuAdmin();
        for (MenuItem m : all) {
            if (itemId.equals(m.getItemId())) {
                etName.setText(m.getItemName());
                etCat.setText(m.getCategoryId());
                etImageUrl.setText(m.getImageUrl());
                etGuestPrice.setText(String.valueOf((long)m.getGuestPrice()));
                etMemberPrice.setText(String.valueOf((long)m.getMemberPrice()));
                break;
            }
        }
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String cat  = etCat.getText().toString().trim();
        String img  = etImageUrl.getText().toString().trim();
        String gpS  = etGuestPrice.getText().toString().trim();
        String mpS  = etMemberPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Nhập tên món"); return; }
        if (TextUtils.isEmpty(gpS))  { etGuestPrice.setError("Nhập giá vãn lai"); return; }
        if (TextUtils.isEmpty(mpS))  { etMemberPrice.setError("Nhập giá member"); return; }

        double gp = Double.parseDouble(gpS);
        double mp = Double.parseDouble(mpS);
        if (mp >= gp) { etMemberPrice.setError("Giá member phải thấp hơn giá vãn lai"); return; }

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        if (editItemId != null) {
            db.updateMenuItem(editItemId, name, cat, img, gp, mp);
            Toast.makeText(this, "Đã cập nhật món ăn", Toast.LENGTH_SHORT).show();
        } else {
            db.insertMenuItem(name, cat, img, gp, mp);
            Toast.makeText(this, "Đã thêm món mới", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
