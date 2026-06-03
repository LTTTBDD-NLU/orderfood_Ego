package com.ego.restaurant.adapters;

import android.content.Context;
import android.graphics.Typeface;
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

import java.util.List;


public class ScheduleDayAdapter extends BaseAdapter {

    public interface OnShiftChangedListener  { void onChanged(ScheduleDayItem item); }
    public interface OnAttendanceListener    { void onAttend(ScheduleDayItem item); }

    private final Context               ctx;
    private final List<ScheduleDayItem> items;
    private final boolean               isManager;
    private final OnShiftChangedListener shiftListener;
    private final OnAttendanceListener   attendListener;

    public ScheduleDayAdapter(Context ctx, List<ScheduleDayItem> items,
                               boolean isManager,
                               OnShiftChangedListener shiftListener,
                               OnAttendanceListener   attendListener) {
        this.ctx            = ctx;
        this.items          = items;
        this.isManager      = isManager;
        this.shiftListener  = shiftListener;
        this.attendListener = attendListener;
    }

    @Override public int    getCount()         { return items.size(); }
    @Override public Object getItem(int pos)   { return items.get(pos); }
    @Override public long   getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView     = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_schedule_day, parent, false);
            h               = new ViewHolder();
            h.tvLabel       = convertView.findViewById(R.id.tv_day_label);
            h.cbMorning     = convertView.findViewById(R.id.cb_morning);
            h.cbAfternoon   = convertView.findViewById(R.id.cb_afternoon);
            h.cbEvening     = convertView.findViewById(R.id.cb_evening);
            h.btnAttendance = convertView.findViewById(R.id.btn_attendance);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        ScheduleDayItem item = items.get(position);

        // Xóa listener cũ trước khi set state (tránh trigger khi recycle)
        h.cbMorning.setOnCheckedChangeListener(null);
        h.cbAfternoon.setOnCheckedChangeListener(null);
        h.cbEvening.setOnCheckedChangeListener(null);

        // ── Label ngày ──────────────────────────────────────────────
        h.tvLabel.setText(item.label);
        if ("ATTENDED".equals(item.status)) {
            h.tvLabel.setTextColor(0xFF2E7D32);
            h.tvLabel.setTypeface(null, Typeface.BOLD);
            h.tvLabel.setText(item.label + "  ✅");
        } else if (item.isPast) {
            h.tvLabel.setTextColor(0xFF9E9E9E);
            h.tvLabel.setTypeface(null, Typeface.NORMAL);
        } else {
            h.tvLabel.setTextColor(0xFF212121);
            h.tvLabel.setTypeface(null, Typeface.BOLD);
        }

        // ── Checkbox state ───────────────────────────────────────────
        h.cbMorning.setChecked(item.morning);
        h.cbAfternoon.setChecked(item.afternoon);
        h.cbEvening.setChecked(item.evening);

        if (item.isPast) {
            // Ngày qua: Manager thấy checkbox để chấm, Staff chỉ xem
            boolean editable = isManager && !"ATTENDED".equals(item.status);
            h.cbMorning.setEnabled(editable);
            h.cbAfternoon.setEnabled(editable);
            h.cbEvening.setEnabled(editable);
            convertView.setAlpha(editable ? 1f : 0.7f);
        } else {
            // Ngày tương lai / hôm nay: mọi người đăng ký được
            h.cbMorning.setEnabled(true);
            h.cbAfternoon.setEnabled(true);
            h.cbEvening.setEnabled(true);
            convertView.setAlpha(1f);
        }

        // ── Listener: cập nhật model khi checkbox thay đổi ──────────
        h.cbMorning.setOnCheckedChangeListener((btn, checked) -> {
            item.morning = checked;
            if (!item.isPast && shiftListener != null) shiftListener.onChanged(item);
        });
        h.cbAfternoon.setOnCheckedChangeListener((btn, checked) -> {
            item.afternoon = checked;
            if (!item.isPast && shiftListener != null) shiftListener.onChanged(item);
        });
        h.cbEvening.setOnCheckedChangeListener((btn, checked) -> {
            item.evening = checked;
            if (!item.isPast && shiftListener != null) shiftListener.onChanged(item);
        });

        // ── Nút Chấm công: chỉ Manager, chỉ ngày hôm nay / đã qua ──
        if (isManager && item.isPast && !"ATTENDED".equals(item.status)) {
            h.btnAttendance.setVisibility(View.VISIBLE);
            h.btnAttendance.setText("Chấm\ncông");
            h.btnAttendance.setOnClickListener(v -> {
                if (!item.morning && !item.afternoon && !item.evening) {
                    Toast.makeText(ctx, "Chọn ít nhất 1 ca để chấm", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (attendListener != null) attendListener.onAttend(item);
                item.status = "ATTENDED";
                notifyDataSetChanged();
            });
        } else if (isManager && "ATTENDED".equals(item.status)) {
            h.btnAttendance.setVisibility(View.VISIBLE);
            h.btnAttendance.setText("Đã\nchấm");
            h.btnAttendance.setEnabled(false);
            h.btnAttendance.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        } else {
            h.btnAttendance.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView  tvLabel;
        CheckBox  cbMorning, cbAfternoon, cbEvening;
        Button    btnAttendance;
    }
}
