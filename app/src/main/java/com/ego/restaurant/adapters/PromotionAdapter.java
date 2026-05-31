package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;
import com.ego.restaurant.R;
import com.ego.restaurant.activities.PromotionActivity.PromotionItem;
import java.util.List;

public class PromotionAdapter extends BaseAdapter {
    public interface OnToggleListener { void onToggle(PromotionItem item); }

    private final Context ctx;
    private final List<PromotionItem> items;
    private final OnToggleListener listener;

    public PromotionAdapter(Context c, List<PromotionItem> items, OnToggleListener l) {
        this.ctx = c; this.items = items; this.listener = l;
    }

    @Override
    public int getCount(){
        return items.size();
    }
    @Override
    public Object getItem(int pos){
        return items.get(pos);
    }
    @Override
    public long getItemId(int pos){
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_promotion, parent, false);
        PromotionItem item = items.get(position);
        ((TextView) convertView.findViewById(R.id.tv_promo_name)).setText(item.name);
        ((TextView) convertView.findViewById(R.id.tv_discount_value)).setText("Giảm " + (int)item.discountPercent + "%");
        ((TextView) convertView.findViewById(R.id.tv_promo_period)).setText("Đối tượng: Member");
        Switch sw = convertView.findViewById(R.id.sw_active);
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(item.active);
        sw.setOnCheckedChangeListener((btn, checked) -> listener.onToggle(item));
        return convertView;
    }
}
