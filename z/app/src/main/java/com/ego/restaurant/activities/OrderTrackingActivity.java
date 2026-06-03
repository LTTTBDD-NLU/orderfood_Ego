package com.ego.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderTrackingActivity extends AppCompatActivity {

    private TextView tvTrackingTable, tvOrderPhaseBanner, tvStatusSummary;
    private Button   btnOrderMore, btnRequestPayment;
    private ListView lvOrderItems;

    private String orderId, tableId, tableName, role;
    private boolean paymentRequested = false;

    private ArrayList<OrderDetail> orderItems = new ArrayList<>();
    private TrackingAdapter        adapter;

    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            refreshData();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderId   = getIntent().getStringExtra("order_id");
        tableId   = getIntent().getStringExtra("table_id");
        tableName = getIntent().getStringExtra("table_name");
        role      = getIntent().getStringExtra("role");
        if (role == null) role = new SessionManager(this).getRole();

        tvTrackingTable    = findViewById(R.id.tv_tracking_table);
        tvOrderPhaseBanner = findViewById(R.id.tv_order_phase_banner);
        tvStatusSummary    = findViewById(R.id.tv_status_summary);
        btnOrderMore       = findViewById(R.id.btn_order_more);
        btnRequestPayment  = findViewById(R.id.btn_request_payment);
        lvOrderItems       = findViewById(R.id.lv_order_items);

        tvTrackingTable.setText(tableName != null ? tableName : "Đơn hàng");
        adapter = new TrackingAdapter();
        lvOrderItems.setAdapter(adapter);

        btnOrderMore.setOnClickListener(v -> {
            if (paymentRequested) { Toast.makeText(this,"Đã yêu cầu thanh toán",Toast.LENGTH_SHORT).show(); return; }
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("table_id",          tableId);
            intent.putExtra("table_name",        tableName);
            intent.putExtra("role",              role);
            intent.putExtra("existing_order_id", orderId);
            startActivity(intent);
        });

        btnRequestPayment.setOnClickListener(v -> {
            if (paymentRequested) { Toast.makeText(this,"Đã gửi rồi",Toast.LENGTH_SHORT).show(); return; }
            showPaymentRequestDialog();
        });

        handler.post(pollTask);
    }

    private void refreshData() {
        if (orderId == null) return;
        Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
        if (o == null) return;
        orderItems.clear();
        int pending=0, cooking=0, delivering=0, completed=0;
        double total = 0;
        for (OrderDetail d : o.getItems()) {
            if ("CANCELLED".equals(d.getStatus())) continue;
            orderItems.add(d);
            total += d.getUnitPrice() * d.getQuantity();
            switch (d.getStatus()) {
                case "PENDING_CONFIRM": pending++;    break;
                case "COOKING":         cooking++;    break;
                case "DELIVERING":      delivering++; break;
                case "COMPLETED":       completed++;  break;
            }
        }
        adapter.notifyDataSetChanged();
        updateBanner(pending, cooking, delivering, completed);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvStatusSummary.setText("Tổng: " + nf.format((long) total) + " đ  ·  "
                + "Chờ:" + pending + " Nấu:" + cooking
                + " Giao:" + delivering + " Xong:" + completed);
    }

    private void updateBanner(int pending, int cooking, int delivering, int completed) {
        if (pending > 0) {
            tvOrderPhaseBanner.setText("⏳ Chờ xác nhận bàn (" + pending + " món)");
            tvOrderPhaseBanner.setBackgroundColor(getResources().getColor(R.color.status_pending));
        } else if (cooking > 0) {
            tvOrderPhaseBanner.setText("🍳 Đang chế biến (" + cooking + " món)");
            tvOrderPhaseBanner.setBackgroundColor(getResources().getColor(R.color.status_cooking));
        } else if (delivering > 0) {
            tvOrderPhaseBanner.setText("🚶 Đang mang lên bàn (" + delivering + " món)");
            tvOrderPhaseBanner.setBackgroundColor(getResources().getColor(R.color.status_delivering));
        } else if (completed > 0) {
            tvOrderPhaseBanner.setText("✅ Đã phục vụ xong — bấm yêu cầu thanh toán");
            tvOrderPhaseBanner.setBackgroundColor(getResources().getColor(R.color.status_completed));
        }
    }

    private void showPaymentRequestDialog() {
        double total = 0;
        StringBuilder sb = new StringBuilder();
        for (OrderDetail d : orderItems) {
            double sub = d.getUnitPrice() * d.getQuantity();
            total += sub;
            sb.append("• ").append(d.getItemName())
              .append(" x").append(d.getQuantity())
              .append("  =  ").append(String.format("%,.0f đ\n", sub));
        }
        double finalTotal = total;
        NumberFormat nf   = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        new AlertDialog.Builder(this)
                .setTitle("Hóa đơn — " + tableName)
                .setMessage(sb + "\n────────────\nTỔNG: " + nf.format((long) finalTotal) + " đ\n\nGửi yêu cầu thanh toán?")
                .setPositiveButton("GỬI", (d, w) -> {
                    DatabaseHelper db = DatabaseHelper.getInstance(this);
                    db.updateOrderStatus(orderId,  "WAITING_PAYMENT");
                    db.updateTableStatus(tableId, "WAITING_PAYMENT");
                    paymentRequested = true;
                    btnRequestPayment.setText("✅ Đã gửi yêu cầu thanh toán");
                    btnRequestPayment.setEnabled(false);
                    btnOrderMore.setEnabled(false);
                    Toast.makeText(this, "Nhân viên sẽ đến ngay!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Huỷ", null).show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }

    private class TrackingAdapter extends BaseAdapter {
        @Override public int getCount() { return orderItems.size(); }
        @Override public Object getItem(int pos) { return orderItems.get(pos); }
        @Override public long getItemId(int pos) { return pos; }
        @Override public View getView(int pos, View cv, ViewGroup parent) {
            if (cv == null)
                cv = LayoutInflater.from(OrderTrackingActivity.this)
                        .inflate(R.layout.item_order_tracking, parent, false);
            OrderDetail d = orderItems.get(pos);
            ((TextView)cv.findViewById(R.id.tv_track_item_name)).setText(d.getItemName());
            ((TextView)cv.findViewById(R.id.tv_track_qty)).setText("x" + d.getQuantity());
            ((TextView)cv.findViewById(R.id.tv_track_price))
                    .setText(String.format("%,.0f đ", d.getUnitPrice()*d.getQuantity()));
            TextView tvSt = cv.findViewById(R.id.tv_track_status);
            switch (d.getStatus() != null ? d.getStatus() : "") {
                case "PENDING_CONFIRM": tvSt.setText("⏳ Chờ xác nhận"); tvSt.setTextColor(0xFFFB8C00); break;
                case "COOKING":         tvSt.setText("🍳 Đang nấu");    tvSt.setTextColor(0xFF1976D2); break;
                case "DELIVERING":      tvSt.setText("🚶 Đang mang lên");tvSt.setTextColor(0xFF7B1FA2); break;
                case "COMPLETED":       tvSt.setText("✅ Đã giao");      tvSt.setTextColor(0xFF2E7D32); break;
                default:                tvSt.setText(d.getStatus());     tvSt.setTextColor(0xFF9E9E9E);
            }
            return cv;
        }
    }
}
