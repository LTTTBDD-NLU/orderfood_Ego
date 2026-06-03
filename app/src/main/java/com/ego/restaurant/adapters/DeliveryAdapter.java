package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ego.restaurant.R;
import com.ego.restaurant.models.OrderDetail;

import java.util.ArrayList;


public class DeliveryAdapter extends BaseAdapter {

    public interface OnDeliveryDoneListener {
        void onDelivered(OrderDetail detail);
    }

    private final Context                context;
    private final ArrayList<OrderDetail> orders;
    private final OnDeliveryDoneListener listener;

    public DeliveryAdapter(Context context, ArrayList<OrderDetail> orders,
                           OnDeliveryDoneListener listener) {
        this.context  = context;
        this.orders   = orders;
        this.listener = listener;
    }

    @Override public int    getCount()         { return orders.size(); }
    @Override public Object getItem(int pos)   { return orders.get(pos); }
    @Override public long   getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_delivery_order, parent, false);
            h           = new ViewHolder();
            h.tvTable   = convertView.findViewById(R.id.tv_delivery_table);
            h.tvItem    = convertView.findViewById(R.id.tv_delivery_item);
            h.tvQty     = convertView.findViewById(R.id.tv_delivery_qty);
            h.btnDone   = convertView.findViewById(R.id.btn_delivery_done);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        OrderDetail d = orders.get(position);
        h.tvTable.setText("Bàn: " + (d.getTableName() != null ? d.getTableName() : "--"));

        // Gộp tên món + ghi chú vào tv_delivery_item (không có tv_delivery_note trong layout)
        String itemText = d.getItemName();
        if (d.getNote() != null && !d.getNote().isEmpty())
            itemText += "\n📝 " + d.getNote();
        h.tvItem.setText(itemText);
        h.tvQty.setText("x" + d.getQuantity());

        final OrderDetail detail = d;
        h.btnDone.setOnClickListener(v -> {
            if (listener != null) listener.onDelivered(detail);
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tvTable, tvItem, tvQty;
        Button   btnDone;
    }
}
