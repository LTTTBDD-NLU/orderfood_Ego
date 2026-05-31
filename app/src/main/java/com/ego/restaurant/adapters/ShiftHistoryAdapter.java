package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ego.restaurant.R;

import java.util.List;

public class ShiftHistoryAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<String> items;

    public ShiftHistoryAdapter(Context c, List<String> items) {
        this.ctx = c;
        this.items = items;
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
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(items.get(position));
        tv.setTextSize(14f);
        tv.setPadding(24, 16, 24, 16);
        return convertView;
    }
}
