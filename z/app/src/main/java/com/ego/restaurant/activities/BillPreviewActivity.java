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
import java.util.Locale;

public class BillPreviewActivity extends AppCompatActivity {

    private TextView tvBillTable, tvBillTime, tvSubtotal, tvDiscount, tvTotalBill;
    private ListView lvBillItems;
    private Button   btnPaymentSuccess;

    private String tableId, tableName;
    private String orderId;

    private ArrayList<OrderDetail> billItems = new ArrayList<>();
    private BillItemAdapter        billAdapter;

    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            loadBill();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_preview);

        orderId   = getIntent().getStringExtra("order_id");
        tableId   = getIntent().getStringExtra("table_id");
        tableName = getIntent().getStringExtra("table_name");

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

        if (orderId == null && tableId != null) {
            Order o = DatabaseHelper.getInstance(this).getActiveOrderByTable(tableId);
            if (o != null) orderId = o.getOrderId();
        }

        btnPaymentSuccess.setOnClickListener(v -> confirmPayment());
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        handler.post(pollTask);
    }

    private void loadBill() {
        if (orderId == null) {
            tvBillTable.setText(tableName != null ? tableName : "Bàn --");
            tvTotalBill.setText("Không tìm thấy đơn");
            return;
        }

        Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
        if (o == null) return;

        billItems.clear();
        double subtotal = 0;
        for (OrderDetail d : o.getItems()) {
            if ("CANCELLED".equals(d.getStatus())) continue;
            billItems.add(d);
            subtotal += d.getUnitPrice() * d.getQuantity();
        }

        boolean isMember = "MEMBER".equals(o.getRoleCode());
        double discount  = isMember ? subtotal * 0.10 : 0;
        double total     = subtotal - discount;

        NumberFormat nf  = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        tvBillTable.setText("Bàn: " + (o.getTableName() != null ? o.getTableName() : tableName));
        tvBillTime.setText("Mở bàn: " + sdf.format(new Date(o.getCreatedAt())));
        tvSubtotal.setText(nf.format((long) subtotal) + " đ");
        tvDiscount.setText("- " + nf.format((long) discount) + " đ");
        tvTotalBill.setText(nf.format((long) total) + " đ");

        billAdapter.notifyDataSetChanged();
    }

    private void confirmPayment() {
        if (orderId == null) return;
        Order o = DatabaseHelper.getInstance(this).getOrderById(orderId);
        if (o == null) return;

        double subtotal = 0;
        for (OrderDetail d : o.getItems()) {
            if ("CANCELLED".equals(d.getStatus())) continue;
            subtotal += d.getUnitPrice() * d.getQuantity();
        }
        boolean isMember = "MEMBER".equals(o.getRoleCode());
        double  total    = isMember ? subtotal * 0.90 : subtotal;
        NumberFormat nf  = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Tổng: " + nf.format((long) total) + " đ\n\nKhách đã trả đủ?")
                .setPositiveButton("THANH TOÁN THÀNH CÔNG", (d, w) -> processPayment(total))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void processPayment(double total) {
        handler.removeCallbacks(pollTask);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.updateOrderStatus(orderId, "PAID");
        db.updateTableStatus(tableId != null ? tableId : "", "EMPTY");
        db.setTableCurrentOrder(tableId != null ? tableId : "", "");
        Toast.makeText(this, "✅ Thanh toán thành công! Bàn đã được giải phóng.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }
}
