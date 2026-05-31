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

    private final Context context;
    private final ArrayList<OrderDetail> orders;
    private final OnDeliveryDoneListener listener;

    public DeliveryAdapter(Context context, ArrayList<OrderDetail> orders,
                           OnDeliveryDoneListener listener) {
        this.context  = context;
        this.orders   = orders;
        this.listener = listener;
    }

    @Override
    public int getCount()          {
        return orders.size();
    }

    @Override
    public Object getItem(int pos) {
        return orders.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_delivery_order, parent, false);
        }

        OrderDetail detail = orders.get(position);

        TextView tvTable = convertView.findViewById(R.id.tv_delivery_table);
        TextView tvItem  = convertView.findViewById(R.id.tv_delivery_item);
        TextView tvQty   = convertView.findViewById(R.id.tv_delivery_qty);
        Button btnDone   = convertView.findViewById(R.id.btn_delivery_done);

        tvTable.setText("Bàn: " + detail.getTableName());
        tvItem.setText(detail.getItemName());
        tvQty.setText("x" + detail.getQuantity());

        // Nhân viên xác nhận đã mang lên bàn -> COMPLETED
        btnDone.setOnClickListener(v -> {
            if (listener != null) listener.onDelivered(detail);
        });

        return convertView;
    }
}
