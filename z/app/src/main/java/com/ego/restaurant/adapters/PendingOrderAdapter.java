package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ego.restaurant.R;
import com.ego.restaurant.models.OrderDetail;
import java.util.ArrayList;

public class PendingOrderAdapter extends BaseAdapter {

    public interface OnItemClickListener { void onClick(OrderDetail detail); }

    private final Context context;
    private final ArrayList<OrderDetail> orders;
    private OnItemClickListener clickListener;

    public PendingOrderAdapter(Context ctx, ArrayList<OrderDetail> orders) {
        this.context = ctx;
        this.orders  = orders;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }

    @Override
    public int getCount(){
        return orders.size();
    }
    @Override
    public Object getItem(int pos){
        return orders.get(pos);
    }
    @Override
    public long getItemId(int pos){
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_pending_order, parent, false);

        OrderDetail detail = orders.get(position);

        TextView tvTable   = convertView.findViewById(R.id.tv_order_table);
        TextView tvSummary = convertView.findViewById(R.id.tv_order_items_summary);
        TextView tvBadge   = convertView.findViewById(R.id.tv_order_status_badge);

        String tableName = detail.getTableName() != null ? detail.getTableName() : "--";
        tvTable.setText("Bàn: " + tableName);
        tvSummary.setText(detail.getItemName() + " x" + detail.getQuantity()
                + (detail.getNote() != null && !detail.getNote().isEmpty()
                   ? "\n📝 " + detail.getNote() : ""));
        tvBadge.setText("⏳ CHỜ XÁC NHẬN");

        // orderId phải được set từ WaiterActivity trước khi đưa vào adapter
        convertView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(detail);
        });

        return convertView;
    }
}
