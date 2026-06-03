package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ego.restaurant.R;
import com.ego.restaurant.activities.KitchenHistoryActivity.KitchenHistoryItem;
import java.util.List;

public class KitchenHistoryAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<KitchenHistoryItem> items;

    public KitchenHistoryAdapter(Context c, List<KitchenHistoryItem> items) {
        this.ctx = c;
        this.items = items;
    }

    @Override public int getCount(){
        return items.size();
    }
    @Override public Object getItem(int pos){
        return items.get(pos);
    }
    @Override public long getItemId(int pos){
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_kitchen_history, parent, false);
        KitchenHistoryItem item = items.get(position);
        ((TextView) convertView.findViewById(R.id.tv_item_name)).setText(item.itemName);
        ((TextView) convertView.findViewById(R.id.tv_table_num)).setText("Bàn " + item.tableNum);
        ((TextView) convertView.findViewById(R.id.tv_finish_time)).setText(item.finishTime);
        ((TextView) convertView.findViewById(R.id.tv_qty)).setText("x" + item.qty);
        return convertView;
    }
}
