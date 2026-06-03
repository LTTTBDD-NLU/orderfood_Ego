package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.CartAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.MenuItem;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private ListView      lvCart;
    private TextView      tvTotal, tvCartTable, tvSavings, tvCartCountHeader;
    private Button        btnConfirmOrder;
    private EditText      etGuestTable;
    private LinearLayout  layoutGuestTable, layoutSavings;

    private ArrayList<MenuItem> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;
    private String role, tableName, tableId, existingOrderId;
    private int    tableNumber;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        sm              = new SessionManager(this);
        role            = getIntent().getStringExtra("role");
        tableNumber     = getIntent().getIntExtra("table_number", 0);
        tableName       = getIntent().getStringExtra("table_name");
        tableId         = getIntent().getStringExtra("table_id");
        existingOrderId = getIntent().getStringExtra("existing_order_id");
        if (role == null)      role      = sm.getRole();
        if (tableName == null) tableName = tableNumber > 0 ? "Bàn " + tableNumber : "--";

        ArrayList<?> raw = (ArrayList<?>) getIntent().getSerializableExtra("cart_items");
        if (raw != null) for (Object o : raw) if (o instanceof MenuItem) cartItems.add((MenuItem) o);

        lvCart            = findViewById(R.id.lv_cart);
        tvTotal           = findViewById(R.id.tv_total);
        tvCartTable       = findViewById(R.id.tv_cart_table);
        tvSavings         = findViewById(R.id.tv_savings);
        tvCartCountHeader = findViewById(R.id.tv_cart_count_header);
        btnConfirmOrder   = findViewById(R.id.btn_confirm_order);
        etGuestTable      = findViewById(R.id.et_guest_table);
        layoutGuestTable  = findViewById(R.id.layout_guest_table);
        layoutSavings     = findViewById(R.id.layout_savings);

        if ("GUEST".equals(role)) {
            layoutGuestTable.setVisibility(existingOrderId == null ? View.VISIBLE : View.GONE);
            if (tableNumber > 0) etGuestTable.setText(String.valueOf(tableNumber));
            btnConfirmOrder.setText(existingOrderId != null
                    ? "THÊM MÓN VÀO ĐƠN HIỆN TẠI"
                    : "GỬI ĐƠN — CHỜ NHÂN VIÊN XÁC NHẬN");
        }
        if ("MEMBER".equals(role) && layoutSavings != null)
            layoutSavings.setVisibility(View.VISIBLE);

        cartAdapter = new CartAdapter(this, cartItems, role);
        cartAdapter.setOnChangedListener(this::updateTotal);
        lvCart.setAdapter(cartAdapter);

        tvCartTable.setText(existingOrderId != null
                ? "Thêm món cho: " + tableName
                : "Đặt cho: " + tableName);
        updateTotal();
        findViewById(R.id.tv_back_menu).setOnClickListener(v -> finish());
        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void updateTotal() {
        double total = 0, savings = 0;
        for (MenuItem item : cartItems) {
            double p = item.getAppliedPrice(role);
            total   += p * item.getQuantity();
            savings += (item.getGuestPrice() - p) * item.getQuantity();
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvTotal.setText(nf.format((long) total) + " đ");
        tvCartCountHeader.setText(cartItems.size() + " món");
        if (tvSavings != null && savings > 0)
            tvSavings.setText(nf.format((long) savings) + " đ");
    }

    private void confirmOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guest lần đầu: bắt nhập số bàn
        if ("GUEST".equals(role) && existingOrderId == null) {
            String tStr = etGuestTable.getText().toString().trim();
            if (TextUtils.isEmpty(tStr)) { etGuestTable.setError("Nhập số bàn"); return; }
            try {
                tableNumber = Integer.parseInt(tStr);
                tableName   = "Bàn " + tableNumber;
            } catch (NumberFormatException e) { etGuestTable.setError("Không hợp lệ"); return; }
        }

        btnConfirmOrder.setEnabled(false);
        btnConfirmOrder.setText("Đang gửi...");

        String finalTableId = (tableId != null && !tableId.isEmpty())
                ? tableId : String.format("T%02d", tableNumber);

        DatabaseHelper db    = DatabaseHelper.getInstance(this);
        String         userId= sm.getUid();
        // status item: MEMBER → COOKING (thẳng bếp), GUEST → PENDING_CONFIRM
        String itemStatus = "MEMBER".equals(role) ? "COOKING" : "PENDING_CONFIRM";

        String orderId;
        if (existingOrderId != null) {
            orderId = existingOrderId;
        } else {
            orderId = db.createOrder(finalTableId, tableName, userId, role);
        }

        final String finalOrderId = orderId;
        final String fTableId     = finalTableId;

        for (MenuItem item : cartItems) {
            db.insertDetail(
                    orderId,
                    item.getItemId() != null ? item.getItemId() : "",
                    item.getItemName(),
                    item.getImageUrl() != null ? item.getImageUrl() : "",
                    item.getQuantity(),
                    item.getAppliedPrice(role),
                    item.getNote() != null ? item.getNote() : "",
                    fTableId, tableName, itemStatus);
        }
        db.recalcOrderTotal(orderId);

        Toast.makeText(this,
                "MEMBER".equals(role) ? "✅ Đặt thành công! Bếp đang chế biến."
                                      : "⏳ Đã gửi! Nhân viên sẽ xác nhận bàn.",
                Toast.LENGTH_LONG).show();

        if ("GUEST".equals(role) && existingOrderId == null) {
            Intent intent = new Intent(this, WaitingConfirmActivity.class);
            intent.putExtra("order_id",   finalOrderId);
            intent.putExtra("table_id",   fTableId);
            intent.putExtra("table_name", tableName);
            intent.putExtra("cart_items", cartItems);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id",   finalOrderId);
            intent.putExtra("table_id",   fTableId);
            intent.putExtra("table_name", tableName);
            intent.putExtra("role",       role);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
}
