package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ego.restaurant.R;
import com.ego.restaurant.models.OrderDetail;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BillItemAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<OrderDetail> items;

    public BillItemAdapter(Context c, List<OrderDetail> items) {
        this.ctx = c; this.items = items;
    }

    @Override public int getCount()          { return items.size(); }
    @Override public Object getItem(int pos) { return items.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_bill, parent, false);
        OrderDetail d   = items.get(position);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));
        ((TextView) convertView.findViewById(R.id.tv_item_name)).setText(d.getItemName());
        ((TextView) convertView.findViewById(R.id.tv_item_qty)).setText("x" + d.getQuantity());
        ((TextView) convertView.findViewById(R.id.tv_item_total))
                .setText(nf.format((long)(d.getUnitPrice() * d.getQuantity())) + " đ");
        return convertView;
    }
}
