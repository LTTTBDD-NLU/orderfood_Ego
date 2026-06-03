package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.MenuItem;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WaitingConfirmActivity extends AppCompatActivity {

    private TextView    tvTableInfo, tvOrderSummary, tvOrderTotal, tvStatusUpdate;
    private ProgressBar progressWaiting;
    private Button      btnCancelOrder, btnViewTracking;

    private String              orderId, tableId, tableName;
    private ArrayList<MenuItem> cartItems = new ArrayList<>();
    private boolean             confirmed = false;

    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            if (confirmed) return;
            checkConfirmationStatus();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_confirm);

        orderId   = getIntent().getStringExtra("order_id");
        tableId   = getIntent().getStringExtra("table_id");
        tableName = getIntent().getStringExtra("table_name");

        ArrayList<?> raw = (ArrayList<?>) getIntent().getSerializableExtra("cart_items");
        if (raw != null)
            for (Object o : raw)
                if (o instanceof MenuItem) cartItems.add((MenuItem) o);

        tvTableInfo    = findViewById(R.id.tv_table_info);
        tvOrderSummary = findViewById(R.id.tv_order_summary);
        tvOrderTotal   = findViewById(R.id.tv_order_total);
        tvStatusUpdate = findViewById(R.id.tv_status_update);
        progressWaiting= findViewById(R.id.progress_waiting);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnViewTracking= findViewById(R.id.btn_view_tracking);

        tvTableInfo.setText(tableName != null ? tableName : "Bàn --");
        buildOrderSummary();

        btnViewTracking.setOnClickListener(v -> goToTracking());
        btnCancelOrder.setOnClickListener(v  -> confirmCancel());

        if (orderId != null) {
            Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
            if (o != null && "CANCELLED".equals(o.getOrderStatus())) {
                Toast.makeText(this, "Đơn hàng đã bị hủy", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        handler.post(pollTask);
    }

    private void buildOrderSummary() {
        // Ưu tiên dùng cartItems từ intent (nhanh hơn)
        if (!cartItems.isEmpty()) {
            StringBuilder sb  = new StringBuilder();
            double        tot = 0;
            for (MenuItem item : cartItems) {
                sb.append("• ").append(item.getItemName())
                  .append("  x").append(item.getQuantity()).append("\n");
                tot += item.getAppliedPrice("GUEST") * item.getQuantity();
            }
            tvOrderSummary.setText(sb.toString().trim());
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
            tvOrderTotal.setText(nf.format((long) tot) + " đ");
            return;
        }
        // Fallback: load từ DB (khi resume sau khi app bị kill)
        if (orderId != null) {
            Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
            if (o != null && o.getItems() != null) {
                StringBuilder sb  = new StringBuilder();
                double        tot = 0;
                NumberFormat  nf  = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
                for (OrderDetail d : o.getItems()) {
                    if ("CANCELLED".equals(d.getStatus())) continue;
                    sb.append("• ").append(d.getItemName())
                      .append("  x").append(d.getQuantity()).append("\n");
                    tot += d.getUnitPrice() * d.getQuantity();
                }
                tvOrderSummary.setText(sb.toString().trim());
                tvOrderTotal.setText(nf.format((long) tot) + " đ");
            }
        }
    }

    private void checkConfirmationStatus() {
        if (orderId == null) return;
        Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
        if (o == null || o.getItems() == null) return;

        if ("CANCELLED".equals(o.getOrderStatus())) {
            confirmed = true;
            handler.removeCallbacks(pollTask);
            tvStatusUpdate.setText("❌ Đơn hàng đã bị huỷ.");
            progressWaiting.setVisibility(View.GONE);
            handler.postDelayed(() -> {
                new SessionManager(this).clearActiveOrder();
                finish();
            }, 1500);
            return;
        }

        boolean hasPending = false;
        boolean hasActive  = false;
        for (OrderDetail d : o.getItems()) {
            if ("CANCELLED".equals(d.getStatus())) continue;
            hasActive = true;
            if ("PENDING_CONFIRM".equals(d.getStatus())) { hasPending = true; break; }
        }

        if (!hasActive) return;

        if (!hasPending) {
            // Waiter đã xác nhận tất cả items → sang OTA
            confirmed = true;
            handler.removeCallbacks(pollTask);
            tvStatusUpdate.setText("✅ Bàn đã được xác nhận! Đang chuyển...");
            progressWaiting.setVisibility(View.GONE);
            handler.postDelayed(this::goToTracking, 1000);
        } else {
            tvStatusUpdate.setText("⏳ Đang chờ nhân viên xác nhận bàn...");
        }
    }

    private void goToTracking() {
        Intent intent = new Intent(this, OrderTrackingActivity.class);
        intent.putExtra("order_id",   orderId);
        intent.putExtra("table_id",   tableId);
        intent.putExtra("table_name", tableName);
        intent.putExtra("role",       "GUEST");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc muốn hủy?")
                .setPositiveButton("Hủy đơn", (d, w) -> {
                    if (orderId != null)
                        DatabaseHelper.getInstance(this).updateOrderStatus(orderId, "CANCELLED");
                    new SessionManager(this).clearActiveOrder();
                    Toast.makeText(this, "Đã hủy đơn", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Giữ đơn", null)
                .show();
    }

    @Override
    public void onBackPressed() { confirmCancel(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }
}
