package com.ego.restaurant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ego.restaurant.R;
import com.ego.restaurant.models.Staff;

import java.util.ArrayList;

public class StaffAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Staff> staffList;

    public StaffAdapter(Context context, ArrayList<Staff> staffList) {
        this.context   = context;
        this.staffList = staffList;
    }

    @Override public int getCount()          { return staffList.size(); }
    @Override public Object getItem(int pos) { return staffList.get(pos); }
    @Override public long getItemId(int pos) { return staffList.get(pos).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_staff, parent, false);
        }
        Staff staff = staffList.get(position);

        View     dot       = convertView.findViewById(R.id.view_status_dot);
        TextView tvName    = convertView.findViewById(R.id.tv_staff_name);
        TextView tvRole    = convertView.findViewById(R.id.tv_staff_role);
        TextView tvStatus  = convertView.findViewById(R.id.tv_staff_status_label);

        tvName.setText(staff.getName());
        tvRole.setText(staff.getRole());

        if (staff.getStatus() == 1) {
            dot.setBackgroundResource(R.color.staff_active);
            tvStatus.setText("Hoạt động");
            tvStatus.setTextColor(context.getResources().getColor(R.color.staff_active));
            convertView.setAlpha(1f);
        } else {
            dot.setBackgroundResource(R.color.staff_locked);
            tvStatus.setText("Đã khóa");
            tvStatus.setTextColor(context.getResources().getColor(R.color.staff_locked));
            convertView.setAlpha(0.6f);
        }

        return convertView;
    }
}
