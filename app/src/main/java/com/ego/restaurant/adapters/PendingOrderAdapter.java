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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PendingOrderAdapter extends BaseAdapter {

    public interface OnGroupClickListener {
        void onClick(PendingGroup group);
    }

    public static class PendingGroup {
        public String            orderId;
        public String            claimedTableName; // bàn khách tự khai
        public List<OrderDetail> items = new ArrayList<>();

        public int    getItemCount()    { return items.size(); }
        public String getItemsSummary() {
            StringBuilder sb = new StringBuilder();
            for (OrderDetail d : items)
                sb.append("• ").append(d.getItemName())
                  .append("  x").append(d.getQuantity()).append("\n");
            return sb.toString().trim();
        }
    }

    private final Context             context;
    private final List<PendingGroup>  groups;
    private OnGroupClickListener      clickListener;

    public PendingOrderAdapter(Context ctx, List<PendingGroup> groups) {
        this.context = ctx;
        this.groups  = groups;
    }

    public void setOnGroupClickListener(OnGroupClickListener l) { this.clickListener = l; }

    public static List<PendingGroup> groupByOrder(ArrayList<OrderDetail> rawList) {
        Map<String, PendingGroup> map = new LinkedHashMap<>();
        for (OrderDetail d : rawList) {
            String oid = d.getOrderId();
            if (!map.containsKey(oid)) {
                PendingGroup g = new PendingGroup();
                g.orderId          = oid;
                g.claimedTableName = d.getTableName();
                map.put(oid, g);
            }
            map.get(oid).items.add(d);
        }
        return new ArrayList<>(map.values());
    }

    @Override public int    getCount()         { return groups.size(); }
    @Override public Object getItem(int pos)   { return groups.get(pos); }
    @Override public long   getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView  = LayoutInflater.from(context)
                    .inflate(R.layout.item_pending_order, parent, false);
            h            = new ViewHolder();
            h.tvTable    = convertView.findViewById(R.id.tv_order_table);
            h.tvBadge    = convertView.findViewById(R.id.tv_order_status_badge);
            h.tvSummary  = convertView.findViewById(R.id.tv_order_items_summary);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        PendingGroup g = groups.get(position);

        // Hiển thị bàn khách tự khai
        String table = g.claimedTableName != null && !g.claimedTableName.isEmpty()
                ? g.claimedTableName : "Chưa rõ bàn";
        h.tvTable.setText("📍 " + table);
        h.tvBadge.setText(g.getItemCount() + " món chờ xác nhận");
        h.tvSummary.setText(g.getItemsSummary());

        final PendingGroup group = g;
        convertView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(group);
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tvTable, tvBadge, tvSummary;
    }
}
