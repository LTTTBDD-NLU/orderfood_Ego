package com.ego.restaurant.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ego.restaurant.R;
import com.ego.restaurant.adapters.ShiftHistoryAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.utils.SessionManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffProfileActivity extends AppCompatActivity {

    private TextView tvStaffName, tvStaffRole, tvTotalDays, tvTotalSalary;
    private ListView lvShiftHistory;
    private List<String> historyList = new ArrayList<>();
    private ShiftHistoryAdapter shiftAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_profile);

        SessionManager sm = new SessionManager(this);
        tvStaffName  = findViewById(R.id.tv_staff_name);
        tvStaffRole  = findViewById(R.id.tv_staff_role);
        tvTotalDays  = findViewById(R.id.tv_total_days);
        tvTotalSalary= findViewById(R.id.tv_total_salary);
        lvShiftHistory= findViewById(R.id.lv_shift_history);

        tvStaffName.setText(sm.getFullName());
        tvStaffRole.setText(getRoleLabel(sm.getRole()));

        shiftAdapter = new ShiftHistoryAdapter(this, historyList);
        lvShiftHistory.setAdapter(shiftAdapter);
        loadData(sm.getUserId());

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
    }

    private void loadData(int userId) {
        int days = DatabaseHelper.getInstance(this).countAttendedDays(userId);
        double salary = days * 300000.0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        tvTotalDays.setText(String.valueOf(days));
        tvTotalSalary.setText(nf.format((long) salary) + "đ");

        historyList.clear();
        Cursor c = DatabaseHelper.getInstance(this).getScheduleForUser(userId);
        while (c.moveToNext()) {
            String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.S_DATE));
            String st   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.S_STATUS));
            boolean m   = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.S_MORN))  == 1;
            boolean a   = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.S_AFTER)) == 1;
            boolean e   = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.S_EVE))   == 1;
            StringBuilder sb = new StringBuilder(date.replace("_","/")).append("  ");
            if (m) sb.append("Sáng ");
            if (a) sb.append("Chiều ");
            if (e) sb.append("Tối ");
            sb.append("— ").append("ATTENDED".equals(st) ? "✓ Đã chấm" : "Đã đăng ký");
            historyList.add(0, sb.toString());
        }
        c.close();
        shiftAdapter.notifyDataSetChanged();
    }

    private String getRoleLabel(String r) {
        if (r == null) return "Nhân viên";
        switch (r.toUpperCase()) {
            case "KITCHEN_STAFF": return "Nhân viên Bếp";
            case "ADMIN":         return "Quản lý";
            case "SUPERADMIN":    return "Quản lý cấp cao";
            default:              return "Phục vụ";
        }
    }
}
