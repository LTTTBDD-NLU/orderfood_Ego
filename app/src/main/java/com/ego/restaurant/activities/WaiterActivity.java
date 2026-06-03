package com.ego.restaurant.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.DeliveryAdapter;
import com.ego.restaurant.adapters.PendingOrderAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.models.Table;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaiterActivity extends AppCompatActivity {

    // IDs từ activity_waiter.xml
    private LinearLayout tabBtnTables, tabBtnFood, tabBtnConfirm, tabBtnProfile;
    private LinearLayout tabContentTables, tabContentFood, tabContentConfirm;
    private ScrollView   tabContentProfile;
    private TextView     tvPendingBadge;
    private GridLayout   gridTables;
    private ListView     lvDelivering, lvPending;
    private TextView     tvProfileName, tvProfileRole, tvProfileDays, tvProfileSalary;
    private Button       btnMySchedule, btnChangePwd, btnLogout;

    private static final int TAB_TABLES=0, TAB_FOOD=1, TAB_CONFIRM=2, TAB_PROFILE=3;

    private ArrayList<OrderDetail>       deliveringList = new ArrayList<>();
    private List<PendingOrderAdapter.PendingGroup> pendingGroups = new ArrayList<>();
    private DeliveryAdapter      deliveryAdapter;
    private PendingOrderAdapter  pendingAdapter;

    // Bug #2: theo dõi số bàn WAITING_PAYMENT
    private int lastWaitingPaymentCount = 0;

    private SessionManager sm;
    private final Handler  handler  = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            refreshAll();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter);

        sm = new SessionManager(this);
        if (!PermissionHelper.hasPermission(sm.getRole(), PermissionHelper.VERIFY_GUEST_ORDER)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        bindViews();
        setupAdapters();
        setupBottomNav();
        loadProfileData();
        handler.post(pollTask);

        btnLogout.setOnClickListener(v -> {
            sm.clearSession();
            startActivity(new Intent(this, LandingActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
        btnMySchedule.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class)));
        btnChangePwd.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private void bindViews() {
        tabBtnTables   = findViewById(R.id.tab_btn_tables);
        tabBtnFood     = findViewById(R.id.tab_btn_food);
        tabBtnConfirm  = findViewById(R.id.tab_btn_confirm);
        tabBtnProfile  = findViewById(R.id.tab_btn_profile);
        tabContentTables   = findViewById(R.id.tab_content_tables);
        tabContentFood     = findViewById(R.id.tab_content_food);
        tabContentConfirm  = findViewById(R.id.tab_content_confirm);
        tabContentProfile  = findViewById(R.id.tab_content_profile);
        gridTables         = findViewById(R.id.grid_tables);
        lvDelivering       = findViewById(R.id.lv_delivering_orders);
        lvPending          = findViewById(R.id.lv_pending_orders);
        tvPendingBadge     = findViewById(R.id.tv_pending_badge);
        tvProfileName      = findViewById(R.id.tv_profile_name);
        tvProfileRole      = findViewById(R.id.tv_profile_role);
        tvProfileDays      = findViewById(R.id.tv_profile_days);
        tvProfileSalary    = findViewById(R.id.tv_profile_salary);
        btnMySchedule      = findViewById(R.id.btn_my_schedule_from_profile);
        btnChangePwd       = findViewById(R.id.btn_change_password_profile);
        btnLogout          = findViewById(R.id.btn_waiter_logout);
    }

    private void setupAdapters() {
        deliveryAdapter = new DeliveryAdapter(this, deliveringList, this::markDelivered);
        lvDelivering.setAdapter(deliveryAdapter);

        pendingAdapter = new PendingOrderAdapter(this, pendingGroups);
        // Yêu cầu mới: click vào 1 group → hiện dialog xác nhận CẢ BÀN
        pendingAdapter.setOnGroupClickListener(this::showConfirmTableDialog);
        lvPending.setAdapter(pendingAdapter);
    }

    private void setupBottomNav() {
        tabBtnTables.setOnClickListener(v  -> switchTab(TAB_TABLES));
        tabBtnFood.setOnClickListener(v    -> switchTab(TAB_FOOD));
        tabBtnConfirm.setOnClickListener(v -> switchTab(TAB_CONFIRM));
        tabBtnProfile.setOnClickListener(v -> switchTab(TAB_PROFILE));
        switchTab(TAB_TABLES);
    }

    private void switchTab(int tab) {
        tabContentTables.setVisibility(tab  == TAB_TABLES  ? View.VISIBLE : View.GONE);
        tabContentFood.setVisibility(tab    == TAB_FOOD     ? View.VISIBLE : View.GONE);
        tabContentConfirm.setVisibility(tab == TAB_CONFIRM  ? View.VISIBLE : View.GONE);
        tabContentProfile.setVisibility(tab == TAB_PROFILE  ? View.VISIBLE : View.GONE);
        int on = 0xFFE64A19, off = 0xFFFFFFFF;
        tabBtnTables.setBackgroundColor(tab  == TAB_TABLES  ? on : off);
        tabBtnFood.setBackgroundColor(tab    == TAB_FOOD     ? on : off);
        tabBtnConfirm.setBackgroundColor(tab == TAB_CONFIRM  ? on : off);
        tabBtnProfile.setBackgroundColor(tab == TAB_PROFILE  ? on : off);
    }

    private void loadProfileData() {
        tvProfileName.setText(sm.getFullName().isEmpty() ? "Nhân viên" : sm.getFullName());
        tvProfileRole.setText(getRoleLabel(sm.getRole()));
        int days   = DatabaseHelper.getInstance(this).countAttendedDays(sm.getUserId());
        double sal = days * 300_000.0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvProfileDays.setText(String.valueOf(days));
        tvProfileSalary.setText(nf.format((long) sal) + "đ");
    }

    private void refreshAll() {
        refreshTables();
        refreshPending();
        refreshDelivering();
        checkWaitingPaymentAlert(); // Bug #2
    }

    private void checkWaitingPaymentAlert() {
        int cur = DatabaseHelper.getInstance(this).countWaitingPaymentTables();
        if (cur > lastWaitingPaymentCount) {
            Toast.makeText(this,
                    "💳 " + (cur - lastWaitingPaymentCount) + " bàn yêu cầu thanh toán!",
                    Toast.LENGTH_LONG).show();
            switchTab(TAB_TABLES);
        }
        lastWaitingPaymentCount = cur;
    }

    private void refreshTables() {
        ArrayList<Table> tables = DatabaseHelper.getInstance(this).getAllTables();
        gridTables.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;
        int sz = (int)(90*dp), mg = (int)(6*dp);

        for (Table t : tables) {
            Button btn = new Button(this);
            btn.setText(t.getTableName());
            btn.setTextColor(0xFFFFFFFF);
            btn.setTextSize(11f);
            int color;
            switch (t.getStatus() != null ? t.getStatus() : "EMPTY") {
                case "OCCUPIED":        color = getResources().getColor(R.color.table_occupied);        break;
                case "WAITING_PAYMENT": color = getResources().getColor(R.color.table_waiting_payment); break;
                default:                color = getResources().getColor(R.color.table_empty);
            }
            btn.setBackgroundTintList(ColorStateList.valueOf(color));
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = sz; p.height = sz; p.setMargins(mg, mg, mg, mg);
            btn.setLayoutParams(p);
            btn.setOnClickListener(v -> onTableClick(t));
            gridTables.addView(btn);
        }
    }

    private void onTableClick(Table t) {
        String st = t.getStatus() != null ? t.getStatus() : "EMPTY";
        if ("WAITING_PAYMENT".equals(st) || "OCCUPIED".equals(st)) {
            Intent i = new Intent(this, BillPreviewActivity.class);
            i.putExtra("table_id",   t.getTableId());
            i.putExtra("table_name", t.getTableName());
            startActivity(i);
        } else {
            Toast.makeText(this, t.getTableName() + " — Trống", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshPending() {
        ArrayList<OrderDetail> raw = DatabaseHelper.getInstance(this)
                .getDetailsByStatus("PENDING_CONFIRM");
        pendingGroups.clear();
        pendingGroups.addAll(PendingOrderAdapter.groupByOrder(raw));
        pendingAdapter.notifyDataSetChanged();

        int cnt = pendingGroups.size();
        tvPendingBadge.setText(cnt > 0 ? "Xác nhận (" + cnt + " bàn)" : "Xác nhận");
        tvPendingBadge.setTextColor(cnt > 0 ? 0xFFE64A19 : 0xFF757575);
    }

    private void refreshDelivering() {
        deliveringList.clear();
        deliveringList.addAll(DatabaseHelper.getInstance(this).getDetailsByStatus("DELIVERING"));
        deliveryAdapter.notifyDataSetChanged();
    }

    private void showConfirmTableDialog(PendingOrderAdapter.PendingGroup group) {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_table, null);
        TextView tvInfo  = dv.findViewById(R.id.tv_dialog_order_info);
        EditText etTable = dv.findViewById(R.id.et_dialog_new_table);

        // Điền thông tin bàn và tất cả món
        String claimedTable = group.claimedTableName != null
                ? group.claimedTableName.replaceAll("[^0-9]", "").trim()
                : "";
        etTable.setText(claimedTable);

        // Hiển thị toàn bộ danh sách món của bàn này
        tvInfo.setText(group.getItemCount() + " món — khách khai: "
                + (group.claimedTableName != null ? group.claimedTableName : "chưa rõ")
                + "\n\n" + group.getItemsSummary());

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận bàn — " + group.getItemCount() + " món")
                .setView(dv)
                .setPositiveButton("✅ XÁC NHẬN BÀN → GỬI BẾP", (d, w) -> {
                    String tn = etTable.getText().toString().trim();
                    if (TextUtils.isEmpty(tn)) {
                        Toast.makeText(this, "Nhập số bàn thực tế", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int tableNum;
                    try { tableNum = Integer.parseInt(tn); }
                    catch (Exception e) {
                        Toast.makeText(this, "Số bàn không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String tid   = String.format("T%02d", tableNum);
                    String tname = "Bàn " + String.format("%02d", tableNum);

                    // confirmOrderTable chuyển TẤT CẢ PENDING_CONFIRM của order này → COOKING
                    DatabaseHelper.getInstance(this)
                            .confirmOrderTable(group.orderId, tid, tname);
                    Toast.makeText(this,
                            "✅ Đã xác nhận " + tname + " — " + group.getItemCount()
                            + " món đang vào bếp!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void markDelivered(OrderDetail d) {
        DatabaseHelper.getInstance(this).updateDetailStatus(d.getDetailId(), "COMPLETED");
        Toast.makeText(this, "✅ Đã giao: " + d.getItemName(), Toast.LENGTH_SHORT).show();
    }

    private String getRoleLabel(String r) {
        if (r == null) return "Nhân viên";
        switch (r.toUpperCase()) {
            case "KITCHEN_STAFF": return "Nhân viên Bếp";
            case "ADMIN":         return "Quản lý";
            default:              return "Phục vụ";
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollTask);
    }
}
