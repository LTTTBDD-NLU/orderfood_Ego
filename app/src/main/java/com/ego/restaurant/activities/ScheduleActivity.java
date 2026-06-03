package com.ego.restaurant.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.ScheduleDayAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Staff;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    // IDs từ activity_schedule.xml
    private TextView tvBack, tvWeekLabel, tvPrevWeek, tvNextWeek, tvPickStaffLabel;
    private Spinner  spinnerStaff;
    private Button   btnConfirmSchedule, btnRequestShiftChange;
    private ListView lvSchedule;

    private SessionManager sm;
    private String  role;
    private boolean isManager;
    private int     weekOffset = 0;
    private Calendar weekStart;

    private int    targetUserId;
    private String targetName;

    private List<ScheduleDayItem> dayItems = new ArrayList<>();
    private ScheduleDayAdapter    scheduleAdapter;

    private List<String>  staffNames = new ArrayList<>();
    private List<Integer> staffIds   = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        sm           = new SessionManager(this);
        role         = sm.getRole();
        targetUserId = sm.getUserId();
        targetName   = sm.getFullName();

        isManager = PermissionHelper.hasPermission(role, PermissionHelper.MANAGE_STAFF_SCHEDULE)
                 || PermissionHelper.hasPermission(role, PermissionHelper.MANAGE_ADMIN_SCHEDULE);

        tvBack               = findViewById(R.id.tv_back);
        tvWeekLabel          = findViewById(R.id.tv_week_label);
        tvPrevWeek           = findViewById(R.id.tv_prev_week);
        tvNextWeek           = findViewById(R.id.tv_next_week);
        btnConfirmSchedule   = findViewById(R.id.btn_confirm_schedule);
        btnRequestShiftChange= findViewById(R.id.btn_request_shift_change);
        lvSchedule           = findViewById(R.id.lv_schedule);
        spinnerStaff         = findViewById(R.id.spinner_staff);
        tvPickStaffLabel     = findViewById(R.id.tv_pick_staff_label);

        if (isManager) {
            btnConfirmSchedule.setVisibility(View.VISIBLE);
            btnRequestShiftChange.setVisibility(View.GONE);
            spinnerStaff.setVisibility(View.VISIBLE);
            tvPickStaffLabel.setVisibility(View.VISIBLE);
            loadStaffSpinner();
        } else {
            spinnerStaff.setVisibility(View.GONE);
            tvPickStaffLabel.setVisibility(View.GONE);
            btnConfirmSchedule.setVisibility(View.GONE);
        }

        // Adapter: truyền isManager, shiftListener (đăng ký), attendListener (chấm công)
        scheduleAdapter = new ScheduleDayAdapter(
                this, dayItems, isManager,
                item -> saveShiftRegistration(item),   // ngày tương lai: đăng ký ca
                item -> saveAttendance(item)            // ngày quá khứ/hôm nay: chấm công
        );
        lvSchedule.setAdapter(scheduleAdapter);

        tvBack.setOnClickListener(v -> finish());

        // Bug #21 FIX: nhân viên thường không lùi về tuần cũ
        tvPrevWeek.setOnClickListener(v -> {
            if (!isManager && weekOffset <= 0) {
                Toast.makeText(this, "Không thể xem tuần cũ", Toast.LENGTH_SHORT).show();
                return;
            }
            weekOffset--;
            updateWeek();
        });
        tvNextWeek.setOnClickListener(v -> { weekOffset++; updateWeek(); });

        // Nút "Chốt lịch": quản lý lưu đăng ký ca tương lai cho tuần này
        btnConfirmSchedule.setOnClickListener(v -> confirmFutureSchedule());
        btnRequestShiftChange.setOnClickListener(v -> showShiftChangeDialog());

        updateWeek();
    }

    private void loadStaffSpinner() {
        staffNames.clear();
        staffIds.clear();
        staffNames.add("— Chọn nhân viên —");
        staffIds.add(-1);

        boolean isSuperAdmin = "SUPERADMIN".equals(role);
        for (Staff s : DatabaseHelper.getInstance(this).getAllStaff()) {
            if ("SUPERADMIN".equals(s.getRole())) continue;
            if (!isSuperAdmin && "ADMIN".equals(s.getRole())) continue; // Bug #35 FIX
            staffNames.add(s.getName() + " (" + getRoleLabel(s.getRole()) + ")");
            staffIds.add(s.getId());
        }

        ArrayAdapter<String> sa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, staffNames);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStaff.setAdapter(sa);
        spinnerStaff.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (pos == 0) return;
                targetUserId = staffIds.get(pos);
                targetName   = staffNames.get(pos);
                loadScheduleForWeek();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void updateWeek() {
        weekStart = Calendar.getInstance();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar end = (Calendar) weekStart.clone();
        end.add(Calendar.DAY_OF_YEAR, 6);
        String label = sdf.format(weekStart.getTime()) + " — " + sdf.format(end.getTime());

        if (weekOffset < 0)
            label += "  ⚠ Tuần cũ";
        else if (weekOffset == 0)
            label += "  (Tuần này)";

        tvWeekLabel.setText(label);

        // Nút "Chốt lịch": chỉ bật cho tuần hiện tại / tương lai
        if (isManager) {
            boolean isPastWeek = weekOffset < 0;
            btnConfirmSchedule.setEnabled(!isPastWeek);
            btnConfirmSchedule.setText(isPastWeek ? "Tuần cũ — chỉ xem" : "Chốt lịch đăng ký");
        }

        loadScheduleForWeek();
    }

    private void loadScheduleForWeek() {
        dayItems.clear();
        String[] dayNames = {"Thứ 2","Thứ 3","Thứ 4","Thứ 5","Thứ 6","Thứ 7","Chủ Nhật"};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM",    Locale.getDefault());
        SimpleDateFormat kf  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Đọc schedule từ DB
        Map<String, ScheduleDayItem> dbMap = new HashMap<>();
        Cursor cur = DatabaseHelper.getInstance(this).getScheduleForUser(targetUserId);
        if (cur != null) {
            while (cur.moveToNext()) {
                String  key = cur.getString(cur.getColumnIndexOrThrow(DatabaseHelper.S_DATE));
                boolean m   = cur.getInt(cur.getColumnIndexOrThrow(DatabaseHelper.S_MORN))  == 1;
                boolean a   = cur.getInt(cur.getColumnIndexOrThrow(DatabaseHelper.S_AFTER)) == 1;
                boolean e   = cur.getInt(cur.getColumnIndexOrThrow(DatabaseHelper.S_EVE))   == 1;
                String  st  = cur.getString(cur.getColumnIndexOrThrow(DatabaseHelper.S_STATUS));
                ScheduleDayItem it = new ScheduleDayItem("", null);
                it.morning = m; it.afternoon = a; it.evening = e; it.status = st;
                dbMap.put(key, it);
            }
            cur.close();
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);      today.set(Calendar.MILLISECOND, 0);

        Calendar day = (Calendar) weekStart.clone();
        for (int i = 0; i < 7; i++) {
            String label   = dayNames[i] + "  " + sdf.format(day.getTime());
            String dateKey = kf.format(day.getTime());
            boolean isPast = day.before(today); // Bug #21

            ScheduleDayItem item = new ScheduleDayItem(label, (Calendar) day.clone());
            item.isPast  = isPast;
            item.dateKey = dateKey;

            if (dbMap.containsKey(dateKey)) {
                ScheduleDayItem saved = dbMap.get(dateKey);
                item.morning   = saved.morning;
                item.afternoon = saved.afternoon;
                item.evening   = saved.evening;
                item.status    = saved.status;
            }
            dayItems.add(item);
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        scheduleAdapter.notifyDataSetChanged();
    }


    private void saveShiftRegistration(ScheduleDayItem item) {
        if (item.isPast) return;
        boolean ok = DatabaseHelper.getInstance(this).saveSchedule(
                targetUserId, item.dateKey, item.morning, item.afternoon, item.evening);
        if (!ok) {
            Toast.makeText(this, "⚠ Không thể sửa ngày đã qua", Toast.LENGTH_SHORT).show();
            loadScheduleForWeek();
        }
    }


    private void saveAttendance(ScheduleDayItem item) {
        if (!item.morning && !item.afternoon && !item.evening) {
            Toast.makeText(this, "Chọn ít nhất 1 ca để chấm công", Toast.LENGTH_SHORT).show();
            return;
        }

        String caText = "";
        if (item.morning)   caText += "• Ca sáng\n";
        if (item.afternoon) caText += "• Ca chiều\n";
        if (item.evening)   caText += "• Ca tối\n";
        String finalCaText = caText;

        new AlertDialog.Builder(this)
                .setTitle("Chấm công: " + item.label)
                .setMessage("Xác nhận nhân viên " + targetName
                        + " đã đến:\n" + finalCaText)
                .setPositiveButton("✅ XÁC NHẬN CHẤM CÔNG", (d, w) -> {
                    // Bug #21 FIX: markAttendanceShifts lưu đúng ca đã đến
                    boolean ok = DatabaseHelper.getInstance(this).markAttendanceShifts(
                            targetUserId, item.dateKey,
                            item.morning, item.afternoon, item.evening);
                    if (ok) {
                        item.status = "ATTENDED";
                        Toast.makeText(this,
                                "✅ Đã chấm công cho " + targetName + " — " + item.label,
                                Toast.LENGTH_LONG).show();
                        scheduleAdapter.notifyDataSetChanged();
                    } else {
                        // Chưa có record → tạo mới rồi chấm
                        DatabaseHelper.getInstance(this).saveSchedule(
                                targetUserId, item.dateKey,
                                item.morning, item.afternoon, item.evening);
                        DatabaseHelper.getInstance(this).markAttendanceShifts(
                                targetUserId, item.dateKey,
                                item.morning, item.afternoon, item.evening);
                        item.status = "ATTENDED";
                        scheduleAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "✅ Đã chấm công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void confirmFutureSchedule() {
        if (weekOffset < 0) {
            Toast.makeText(this, "Không thể chốt lịch tuần đã qua", Toast.LENGTH_SHORT).show();
            return;
        }
        if (targetUserId < 0) {
            Toast.makeText(this, "Chọn nhân viên trước", Toast.LENGTH_SHORT).show();
            return;
        }
        int saved = 0;
        for (ScheduleDayItem d : dayItems) {
            if (d.isPast) continue;
            boolean ok = DatabaseHelper.getInstance(this).saveSchedule(
                    targetUserId, d.dateKey, d.morning, d.afternoon, d.evening);
            if (ok) saved++;
        }
        Toast.makeText(this,
                "✅ Đã lưu lịch " + saved + " ngày cho " + targetName,
                Toast.LENGTH_LONG).show();
        loadScheduleForWeek();
    }

    private void showShiftChangeDialog() {
        String[] reasons = {"Bận việc gia đình","Ốm / Bệnh","Lý do cá nhân","Khác"};
        new AlertDialog.Builder(this)
                .setTitle("Xin đổi ca")
                .setMessage("Chọn lý do — yêu cầu phải gửi trước 24 giờ")
                .setItems(reasons, (d, w) ->
                        Toast.makeText(this, "✅ Đã ghi nhận yêu cầu đổi ca",
                                Toast.LENGTH_LONG).show())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private String getRoleLabel(String r) {
        if (r == null) return "";
        switch (r.toUpperCase()) {
            case "KITCHEN_STAFF": return "Bếp";
            case "ADMIN":         return "Quản lý";
            default:              return "Phục vụ";
        }
    }

    public static class ScheduleDayItem {
        public String   label, status, dateKey;
        public Calendar date;
        public boolean  morning, afternoon, evening;
        public boolean  isPast;

        public ScheduleDayItem(String label, Calendar date) {
            this.label  = label;
            this.date   = date;
            this.status = "REGISTERED";
            this.isPast = false;
        }
    }
}
