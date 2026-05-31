package com.ego.restaurant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.ego.restaurant.R;
import com.ego.restaurant.activities.ScheduleActivity.ScheduleDayItem;
import com.ego.restaurant.helpers.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScheduleDayAdapter extends BaseAdapter {

    public interface OnSaveListener {
        void onSave(ScheduleDayItem item);
    }

    private final Context ctx;
    private final List<ScheduleDayItem> items;
    private final String role;
    private int targetUserId;
    private final boolean isManager;
    private final OnSaveListener saveListener;

    public ScheduleDayAdapter(Context c, List<ScheduleDayItem> items,
                              String role, String targetUid,
                              boolean isManager, OnSaveListener sl) {
        this.ctx = c;
        this.items = items;
        this.role = role;
        this.isManager = isManager;
        this.saveListener = sl;
        try {
            this.targetUserId = Integer.parseInt(targetUid);
        } catch (Exception e) {
            this.targetUserId = -1;
        }
    }

    public void setTargetUid(String uid) {
        try {
            this.targetUserId = Integer.parseInt(uid);
        } catch (Exception e) {
            this.targetUserId = -1;
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int pos) {
        return items.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_schedule_day, parent, false);

        ScheduleDayItem day = items.get(position);
        TextView tvDay = convertView.findViewById(R.id.tv_day_label);
        CheckBox cbMorn = convertView.findViewById(R.id.cb_morning);
        CheckBox cbAfter = convertView.findViewById(R.id.cb_afternoon);
        CheckBox cbEve = convertView.findViewById(R.id.cb_evening);
        Button btnAttend = convertView.findViewById(R.id.btn_attendance);

        tvDay.setText(day.label);

        // Màu nền theo trạng thái chấm công
        convertView.setBackgroundColor(
                "ATTENDED".equals(day.status) ? 0xFFE8F5E9 : Color.WHITE);

        // Reset listener trước setChecked
        cbMorn.setOnCheckedChangeListener(null);
        cbAfter.setOnCheckedChangeListener(null);
        cbEve.setOnCheckedChangeListener(null);
        cbMorn.setChecked(day.morning);
        cbAfter.setChecked(day.afternoon);
        cbEve.setChecked(day.evening);

        if (isManager) {
            // Quản lý: checkbox chỉ đọc, nút Chấm công dùng SQLite
            cbMorn.setEnabled(false);
            cbAfter.setEnabled(false);
            cbEve.setEnabled(false);
            btnAttend.setVisibility(View.VISIBLE);

            boolean attended = "ATTENDED".equals(day.status);
            btnAttend.setText(attended ? "✓ Đã chấm" : "Chấm công");
            btnAttend.setEnabled(!attended);
            btnAttend.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            attended ? 0xFF9E9E9E : 0xFFE64A19));

            btnAttend.setOnClickListener(v -> {
                if (targetUserId < 0) {
                    Toast.makeText(ctx, "Chọn nhân viên trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                String dateKey = getDateKey(day);
                // ✅ Dùng SQLite thay Firebase
                DatabaseHelper.getInstance(ctx).markAttendance(targetUserId, dateKey);
                day.status = "ATTENDED";
                notifyDataSetChanged();
                Toast.makeText(ctx, "✅ Đã chấm công: " + day.label, Toast.LENGTH_SHORT).show();
            });

        } else {
            // Nhân viên: checkbox chỉnh được, nút ẩn
            cbMorn.setEnabled(true);
            cbAfter.setEnabled(true);
            cbEve.setEnabled(true);
            btnAttend.setVisibility(View.GONE);

            cbMorn.setOnCheckedChangeListener((btn, checked) -> {
                day.morning = checked;
                saveListener.onSave(day);
            });
            cbAfter.setOnCheckedChangeListener((btn, checked) -> {
                day.afternoon = checked;
                saveListener.onSave(day);
            });
            cbEve.setOnCheckedChangeListener((btn, checked) -> {
                day.evening = checked;
                saveListener.onSave(day);
            });
        }
        return convertView;
    }

    private String getDateKey(ScheduleDayItem day) {
        return new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
                .format(day.date.getTime());
    }
}