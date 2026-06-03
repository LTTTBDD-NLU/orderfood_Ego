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

    private String  orderId, tableId, tableName, role;
    private boolean paymentRequested = false;
    private boolean orderAlreadyPaid = false;

    private ArrayList<OrderDetail> orderItems = new ArrayList<>();
    private TrackingAdapter        adapter;
    private SessionManager         sm;

    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            refreshData();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        sm        = new SessionManager(this);
        orderId   = getIntent().getStringExtra("order_id");
        tableId   = getIntent().getStringExtra("table_id");
        tableName = getIntent().getStringExtra("table_name");
        role      = getIntent().getStringExtra("role");
        if (role == null) role = sm.getRole();

        tvTrackingTable    = findViewById(R.id.tv_tracking_table);
        tvOrderPhaseBanner = findViewById(R.id.tv_order_phase_banner);
        tvStatusSummary    = findViewById(R.id.tv_status_summary);
        btnOrderMore       = findViewById(R.id.btn_order_more);
        btnRequestPayment  = findViewById(R.id.btn_request_payment);
        lvOrderItems       = findViewById(R.id.lv_order_items);

        tvTrackingTable.setText(tableName != null ? tableName : "Đơn hàng");
        adapter = new TrackingAdapter();
        lvOrderItems.setAdapter(adapter);

        if (orderId == null && tableId != null) {
            Order o = DatabaseHelper.getInstance(this).getActiveOrderByTable(tableId);
            if (o != null) {
                orderId = o.getOrderId();
                sm.saveActiveOrder(orderId, tableId, tableName);
            }
        }

        btnOrderMore.setOnClickListener(v -> {
            if (paymentRequested || orderAlreadyPaid) {
                Toast.makeText(this, "Đơn hàng đã được gửi thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            if (orderId == null) {
                Toast.makeText(this, "Chưa có đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("table_id",          tableId);
            intent.putExtra("table_name",        tableName);
            intent.putExtra("role",              role);
            intent.putExtra("existing_order_id", orderId);
            startActivity(intent);
        });

        btnRequestPayment.setOnClickListener(v -> {
            if (paymentRequested || orderAlreadyPaid) {
                Toast.makeText(this, "Đã gửi yêu cầu rồi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (orderId == null) {
                Toast.makeText(this, "Chưa có đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentRequestDialog();
        });

        handler.post(pollTask);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String newOrderId = intent.getStringExtra("order_id");
        if (newOrderId != null && !newOrderId.equals(orderId)) {
            orderId   = newOrderId;
            tableId   = intent.getStringExtra("table_id");
            tableName = intent.getStringExtra("table_name");
            if (tableName != null) tvTrackingTable.setText(tableName);
            // Reset trạng thái
            paymentRequested = false;
            orderAlreadyPaid = false;
            btnRequestPayment.setText("Yêu cầu thanh toán");
            btnRequestPayment.setEnabled(true);
            btnOrderMore.setEnabled(true);
        }
        refreshData();
    }

    private void refreshData() {
        if (orderId == null) {
            tvOrderPhaseBanner.setText("⚠ Không tìm thấy đơn hàng");
            tvStatusSummary.setText("Nhập số bàn và thử lại");
            return;
        }

        Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
        if (o == null) return;

        if ("PAID".equals(o.getOrderStatus())) {
            if (!orderAlreadyPaid) {
                orderAlreadyPaid = true;
                sm.clearActiveOrder(); // Bug #42: xóa active order khi đã trả tiền
                runOnUiThread(() -> {
                    tvOrderPhaseBanner.setText("✅ Đơn hàng đã thanh toán. Cảm ơn quý khách!");
                    tvOrderPhaseBanner.setBackgroundColor(0xFF2E7D32);
                    btnRequestPayment.setEnabled(false);
                    btnOrderMore.setEnabled(false);
                });
            }
            return;
        }

        if ("WAITING_PAYMENT".equals(o.getOrderStatus()) && !paymentRequested) {
            paymentRequested = true;
            runOnUiThread(() -> {
                btnRequestPayment.setText("✅ Đã gửi yêu cầu thanh toán");
                btnRequestPayment.setEnabled(false);
                btnOrderMore.setEnabled(false);
            });
        }

        orderItems.clear();
        int pending = 0, cooking = 0, delivering = 0, completed = 0;

        double confirmedTotal = 0; // tiền các món đã vào bếp
        double pendingTotal   = 0; // tiền các món chưa xác nhận

        boolean isMember = "MEMBER".equals(role) || "MEMBER".equals(o.getRoleCode());

        for (OrderDetail d : o.getItems()) {
            if ("CANCELLED".equals(d.getStatus())) continue;
            orderItems.add(d);
            double lineTotal = d.getUnitPrice() * d.getQuantity();
            switch (d.getStatus() != null ? d.getStatus() : "") {
                case "PENDING_CONFIRM":
                    pending++;
                    pendingTotal += lineTotal;
                    break;
                case "COOKING":
                    cooking++;
                    confirmedTotal += lineTotal;
                    break;
                case "DELIVERING":
                    delivering++;
                    confirmedTotal += lineTotal;
                    break;
                case "COMPLETED":
                    completed++;
                    confirmedTotal += lineTotal;
                    break;
            }
        }

        double discountPct = isMember
                ? DatabaseHelper.getInstance(this).getActiveMemberDiscountPercent()
                : 0;
        double discount      = confirmedTotal * discountPct / 100.0;
        double displayTotal  = confirmedTotal - discount;

        adapter.notifyDataSetChanged();
        updateBanner(pending, cooking, delivering, completed);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        StringBuilder sb = new StringBuilder();
        sb.append("Tổng: ").append(nf.format((long) displayTotal)).append(" đ");
        if (isMember && discount > 0)
            sb.append("  (tiết kiệm ").append(nf.format((long) discount)).append(" đ)");
        if (pending > 0)
            sb.append("\n⏳ Chờ xác nhận: ").append(nf.format((long) pendingTotal)).append(" đ (").append(pending).append(" món)");
        sb.append("\n🍳:").append(cooking)
          .append("  🚶:").append(delivering)
          .append("  ✅:").append(completed);
        tvStatusSummary.setText(sb.toString());
    }

    private void updateBanner(int pending, int cooking, int delivering, int completed) {
        if (pending > 0) {
            tvOrderPhaseBanner.setText("⏳ Chờ nhân viên xác nhận bàn (" + pending + " món)");
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
        boolean isMember = "MEMBER".equals(role);
        double confirmedTotal = 0;
        StringBuilder sb = new StringBuilder();

        for (OrderDetail d : orderItems) {
            if ("PENDING_CONFIRM".equals(d.getStatus())) continue; // Bug #38
            double sub = d.getUnitPrice() * d.getQuantity();
            confirmedTotal += sub;
            sb.append("• ").append(d.getItemName())
              .append(" x").append(d.getQuantity())
              .append("  =  ").append(String.format("%,.0f đ\n", sub));
        }

        double discPct  = isMember
                ? DatabaseHelper.getInstance(this).getActiveMemberDiscountPercent()
                : 0;
        double discount = confirmedTotal * discPct / 100.0;
        double total    = confirmedTotal - discount;

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        String summary  = sb.toString()
                + "\n────────────"
                + (isMember && discount > 0
                   ? "\nChiết khấu thành viên (" + (int)discPct + "%): -" + nf.format((long) discount) + " đ"
                   : "")
                + "\nTỔNG: " + nf.format((long) total) + " đ\n\nGửi yêu cầu thanh toán?";

        double finalTotal = total;
        new AlertDialog.Builder(this)
                .setTitle("Hóa đơn — " + tableName)
                .setMessage(summary)
                .setPositiveButton("GỬI", (d, w) -> {
                    DatabaseHelper db = DatabaseHelper.getInstance(this);
                    db.updateOrderStatus(orderId, "WAITING_PAYMENT");
                    db.updateTableStatus(tableId, "WAITING_PAYMENT");
                    paymentRequested = true;
                    btnRequestPayment.setText("✅ Đã gửi yêu cầu thanh toán");
                    btnRequestPayment.setEnabled(false);
                    btnOrderMore.setEnabled(false);
                    Toast.makeText(this, "Nhân viên sẽ đến ngay!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }

    private class TrackingAdapter extends BaseAdapter {
        @Override public int getCount()          { return orderItems.size(); }
        @Override public Object getItem(int pos) { return orderItems.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            ViewHolder h;
            if (cv == null) {
                cv = LayoutInflater.from(OrderTrackingActivity.this)
                        .inflate(R.layout.item_order_tracking, parent, false);
                h = new ViewHolder();
                h.tvName   = cv.findViewById(R.id.tv_track_item_name);
                h.tvQty    = cv.findViewById(R.id.tv_track_qty);
                h.tvPrice  = cv.findViewById(R.id.tv_track_price);
                h.tvStatus = cv.findViewById(R.id.tv_track_status);
                cv.setTag(h);
            } else {
                h = (ViewHolder) cv.getTag();
            }
            OrderDetail d = orderItems.get(pos);
            h.tvName.setText(d.getItemName());
            h.tvQty.setText("x" + d.getQuantity());
            h.tvPrice.setText(String.format("%,.0f đ", d.getUnitPrice() * d.getQuantity()));
            switch (d.getStatus() != null ? d.getStatus() : "") {
                case "PENDING_CONFIRM": h.tvStatus.setText("⏳ Chờ xác nhận"); h.tvStatus.setTextColor(0xFFFB8C00); break;
                case "COOKING":         h.tvStatus.setText("🍳 Đang nấu");     h.tvStatus.setTextColor(0xFF1976D2); break;
                case "DELIVERING":      h.tvStatus.setText("🚶 Đang mang lên");h.tvStatus.setTextColor(0xFF7B1FA2); break;
                case "COMPLETED":       h.tvStatus.setText("✅ Đã giao");       h.tvStatus.setTextColor(0xFF2E7D32); break;
                default:                h.tvStatus.setText(d.getStatus());      h.tvStatus.setTextColor(0xFF9E9E9E);
            }
            return cv;
        }

        class ViewHolder {
            TextView tvName, tvQty, tvPrice, tvStatus;
        }
    }
}
