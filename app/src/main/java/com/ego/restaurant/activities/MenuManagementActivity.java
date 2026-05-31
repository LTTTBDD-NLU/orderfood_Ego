package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.MenuManagementAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.MenuItem;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MenuManagementActivity extends AppCompatActivity {

    private ListView lvMenuItems;
    private EditText etSearch;
    private Button   btnAdd;

    private ArrayList<MenuItem> allItems      = new ArrayList<>();
    private List<MenuItem>      filteredItems = new ArrayList<>();
    private MenuManagementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_management);

        if (!PermissionHelper.hasPermission(new SessionManager(this).getRole(),
                PermissionHelper.MANAGE_MENU_CATALOG)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        lvMenuItems = findViewById(R.id.lv_menu_items);
        etSearch    = findViewById(R.id.et_search_menu);
        btnAdd      = findViewById(R.id.btn_add_menu_item);

        adapter = new MenuManagementAdapter(this, filteredItems,
                item -> {
                    String newStatus = "AVAILABLE".equals(item.getStatus())
                            ? "OUT_OF_STOCK" : "AVAILABLE";
                    DatabaseHelper.getInstance(this).updateMenuItemStatus(item.getItemId(), newStatus);
                    item.setStatus(newStatus);
                    adapter.notifyDataSetChanged();
                },
                item -> {
                    Intent intent = new Intent(this, AddEditMenuItemActivity.class);
                    intent.putExtra("item_id", item.getItemId());
                    startActivity(intent);
                });
        lvMenuItems.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditMenuItemActivity.class)));

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterItems(s.toString());
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        loadMenu();
    }

    private void loadMenu() {
        allItems.clear();
        allItems.addAll(DatabaseHelper.getInstance(this).getAllMenuAdmin());
        filterItems(etSearch.getText().toString());
    }

    private void filterItems(String q) {
        filteredItems.clear();
        for (MenuItem m : allItems) {
            if (q == null || q.isEmpty()
                    || m.getItemName().toLowerCase().contains(q.toLowerCase()))
                filteredItems.add(m);
        }
        adapter.notifyDataSetChanged();
    }
}
