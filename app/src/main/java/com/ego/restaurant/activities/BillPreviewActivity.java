package com.ego.restaurant.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.BillItemAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillPreviewActivity extends AppCompatActivity {

    private TextView tvBillTable, tvBillTime, tvSubtotal, tvDiscount, tvTotalBill;
    private ListView lvBillItems;
    private Button   btnPaymentSuccess;

    private String        tableId, tableName;
    private String        primaryOrderId; // orderId từ intent (có thể null)
    private List<String>  allOrderIds = new ArrayList<>(); // Bug #3 FIX

    private double currentSubtotal = 0;
    private double currentTotal    = 0;
    private boolean isMemberOrder  = false;

    private ArrayList<OrderDetail> billItems = new ArrayList<>();
    private BillItemAdapter        billAdapter;

    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            loadBill();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_preview);

        primaryOrderId = getIntent().getStringExtra("order_id");
        tableId        = getIntent().getStringExtra("table_id");
        tableName      = getIntent().getStringExtra("table_name");

        tvBillTable       = findViewById(R.id.tv_bill_table);
        tvBillTime        = findViewById(R.id.tv_bill_time);
        tvSubtotal        = findViewById(R.id.tv_subtotal);
        tvDiscount        = findViewById(R.id.tv_discount);
        tvTotalBill       = findViewById(R.id.tv_total_bill);
        lvBillItems       = findViewById(R.id.lv_bill_items);
        btnPaymentSuccess = findViewById(R.id.btn_payment_success);

        String role = new SessionManager(this).getRole();
        if (!"WAITSTAFF".equals(role) && !"ADMIN".equals(role) && !"SUPERADMIN".equals(role))
            btnPaymentSuccess.setVisibility(android.view.View.GONE);

        billAdapter = new BillItemAdapter(this, billItems);
        lvBillItems.setAdapter(billAdapter);

        btnPaymentSuccess.setOnClickListener(v -> confirmPayment());
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        handler.post(pollTask);
    }

    /**
     * Bug #3 FIX: tải TẤT CẢ đơn active của bàn, không chỉ một orderId
     * Bug #22 FIX: đọc chiết khấu từ DB, không hard-code 10%
     */
    private void loadBill() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        // ── Bước 1: Thu thập tất cả đơn active của bàn ───────────────
        ArrayList<Order> activeOrders = new ArrayList<>();

        if (tableId != null && !tableId.isEmpty()) {
            // Lấy tất cả đơn còn active của bàn
            activeOrders = db.getAllActiveOrdersByTable(tableId);
        } else if (primaryOrderId != null) {
            // Chỉ có orderId đơn lẻ (không biết bàn)
            Order single = db.getOrderById(primaryOrderId);
            if (single != null) activeOrders.add(single);
        }

        // Nếu không tìm thấy qua tableId nhưng có primaryOrderId, thêm vào
        if (activeOrders.isEmpty() && primaryOrderId != null) {
            Order single = db.getOrderById(primaryOrderId);
            if (single != null) activeOrders.add(single);
        }

        if (activeOrders.isEmpty()) {
            tvBillTable.setText(tableName != null ? tableName : "Bàn --");
            tvTotalBill.setText("Không tìm thấy đơn hàng");
            return;
        }

        // ── Bước 2: Gộp tất cả items từ mọi đơn ─────────────────────
        billItems.clear();
        allOrderIds.clear();
        double subtotal = 0;
        boolean hasMember = false;
        Order   firstOrder = activeOrders.get(0);

        for (Order o : activeOrders) {
            allOrderIds.add(o.getOrderId());
            if ("MEMBER".equals(o.getRoleCode())) hasMember = true;
            for (OrderDetail d : o.getItems()) {
                if ("CANCELLED".equals(d.getStatus())) continue;
                billItems.add(d);
                subtotal += d.getUnitPrice() * d.getQuantity();
            }
        }

        // primaryOrderId dùng để thanh toán nếu chỉ có một đơn
        if (primaryOrderId == null && !allOrderIds.isEmpty())
            primaryOrderId = allOrderIds.get(allOrderIds.size() - 1);

        isMemberOrder = hasMember;

        // ── Bước 3: Chiết khấu từ DB (Bug #22 FIX) ───────────────────
        double discountPct = hasMember ? db.getActiveMemberDiscountPercent() : 0;
        double discount    = subtotal * discountPct / 100.0;
        double total       = subtotal - discount;

        currentSubtotal = subtotal;
        currentTotal    = total;

        // ── Bước 4: Cập nhật UI ───────────────────────────────────────
        NumberFormat nf  = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        String displayTable = firstOrder.getTableName() != null && !firstOrder.getTableName().isEmpty()
                ? firstOrder.getTableName()
                : (tableName != null ? tableName : "Bàn --");

        tvBillTable.setText("Bàn: " + displayTable);
        tvBillTime.setText("Mở bàn: " + sdf.format(new Date(firstOrder.getCreatedAt())));
        tvSubtotal.setText(nf.format((long) subtotal) + " đ");

        if (discount > 0) {
            tvDiscount.setText("- " + nf.format((long) discount) + " đ"
                    + "  (thành viên " + (int) discountPct + "%)");
        } else {
            tvDiscount.setText("0 đ");
        }
        tvTotalBill.setText(nf.format((long) total) + " đ");
        billAdapter.notifyDataSetChanged();
    }

    private void confirmPayment() {
        if (allOrderIds.isEmpty() && primaryOrderId == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        // Recalc để đảm bảo số mới nhất
        loadBill();

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        String msg = "Tổng: " + nf.format((long) currentTotal) + " đ"
                + (isMemberOrder ? "\n(Đã áp dụng chiết khấu thành viên)" : "")
                + "\n\nKhách đã trả đủ?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage(msg)
                .setPositiveButton("THANH TOÁN THÀNH CÔNG", (d, w) -> processPayment())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /**
     * Bug #17 FIX: lưu số tiền THỰC SỰ thu (sau chiết khấu) vào DB
     * Bug #3  FIX: đánh dấu PAID cho TẤT CẢ các đơn của bàn
     */
    private void processPayment() {
        handler.removeCallbacks(pollTask);
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        if (allOrderIds.size() > 1) {
            // Nhiều đơn → chia đều tổng thực thu
            db.finalizePaymentMultiple(allOrderIds, currentTotal);
        } else {
            // Một đơn → ghi chính xác
            String oid = allOrderIds.isEmpty() ? primaryOrderId : allOrderIds.get(0);
            db.finalizePayment(oid, currentTotal);
        }

        // Giải phóng bàn
        if (tableId != null && !tableId.isEmpty()) {
            db.updateTableStatus(tableId, "EMPTY");
            db.setTableCurrentOrder(tableId, "");
        }

        // Xóa active order trong session (Bug #42)
        new com.ego.restaurant.utils.SessionManager(this).clearActiveOrder();

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        Toast.makeText(this,
                "✅ Thanh toán thành công! " + nf.format((long) currentTotal) + " đ — Bàn đã giải phóng.",
                Toast.LENGTH_LONG).show();
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }
}
