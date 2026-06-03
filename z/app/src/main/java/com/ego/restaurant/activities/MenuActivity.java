package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.MenuAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.MenuItem;
import com.ego.restaurant.utils.SessionManager;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private ListView  lvMenu;
    private TextView  tvCartCount, tvTableInfo;
    private EditText  etSearch;
    private Button    btnViewCart;

    private ArrayList<MenuItem> allItems    = new ArrayList<>();
    private ArrayList<MenuItem> displayItems= new ArrayList<>();
    private ArrayList<MenuItem> cartItems   = new ArrayList<>();
    private MenuAdapter menuAdapter;

    private String tableId, tableName, role, existingOrderId;
    private int    tableNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        role            = getIntent().getStringExtra("role");
        tableNumber     = getIntent().getIntExtra("table_number", 0);
        tableName       = getIntent().getStringExtra("table_name");
        tableId         = getIntent().getStringExtra("table_id");
        existingOrderId = getIntent().getStringExtra("existing_order_id");

        SessionManager sm = new SessionManager(this);
        if (role == null) role = sm.getRole();
        if (tableName == null) tableName = tableNumber > 0 ? "Bàn " + tableNumber : "--";

        lvMenu      = findViewById(R.id.lv_menu);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvTableInfo = findViewById(R.id.tv_table_info);
        btnViewCart = findViewById(R.id.btn_view_cart);
        etSearch    = findViewById(R.id.et_search_menu);

        String label = "MEMBER".equals(role) ? "Giá Member ✦" : "Giá vãn lai";
        String extra = existingOrderId != null ? " (Gọi thêm)" : "";
        tvTableInfo.setText(tableName + extra + "  —  " + label);

        menuAdapter = new MenuAdapter(this, displayItems, cartItems, role,
                count -> tvCartCount.setText("Giỏ: " + count));
        lvMenu.setAdapter(menuAdapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    filterMenu(s.toString());
                }
            });
        }

        btnViewCart.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Chưa chọn món nào", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("cart_items",        cartItems);
            intent.putExtra("table_number",      tableNumber);
            intent.putExtra("table_name",        tableName);
            intent.putExtra("table_id",          tableId);
            intent.putExtra("role",              role);
            if (existingOrderId != null)
                intent.putExtra("existing_order_id", existingOrderId);
            startActivity(intent);
        });

        TextView tvBack = findViewById(R.id.tv_back_from_menu);
        if (tvBack != null) tvBack.setOnClickListener(v -> finish());

        loadMenuFromDb();
    }

    private void loadMenuFromDb() {
        allItems = DatabaseHelper.getInstance(this).getAvailableMenu();
        if (allItems.isEmpty()) {
            Toast.makeText(this, "Thực đơn trống", Toast.LENGTH_SHORT).show();
        }
        filterMenu(null);
    }

    private void filterMenu(String query) {
        displayItems.clear();
        if (query == null || query.trim().isEmpty()) {
            displayItems.addAll(allItems);
        } else {
            String lq = query.toLowerCase().trim();
            for (MenuItem item : allItems)
                if (item.getItemName() != null
                        && item.getItemName().toLowerCase().contains(lq))
                    displayItems.add(item);
        }
        menuAdapter.notifyDataSetChanged();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (menuAdapter != null) menuAdapter.shutdown();
    }
}
