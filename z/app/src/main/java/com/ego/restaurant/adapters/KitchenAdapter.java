package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ego.restaurant.R;
import com.ego.restaurant.models.OrderDetail;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KitchenAdapter extends BaseAdapter {

    public interface OnKitchenActionListener {
        void onDone(OrderDetail detail);
        void onOutOfStock(OrderDetail detail);
    }

    private final Context                  context;
    private final ArrayList<OrderDetail>   cookingOrders;
    private final OnKitchenActionListener  listener;
    private final ExecutorService          executor = Executors.newFixedThreadPool(2);

    public KitchenAdapter(Context context, ArrayList<OrderDetail> cookingOrders,
                          OnKitchenActionListener listener) {
        this.context       = context;
        this.cookingOrders = cookingOrders;
        this.listener      = listener;
    }

    @Override public int getCount(){
        return cookingOrders.size();
    }
    @Override public Object getItem(int pos){
        return cookingOrders.get(pos);
    }
    @Override public long getItemId(int pos){
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_kitchen_order, parent, false);
            holder            = new ViewHolder();
            holder.ivImage    = convertView.findViewById(R.id.iv_kitchen_food_img);
            holder.tvTable    = convertView.findViewById(R.id.tv_kitchen_table);
            holder.tvName     = convertView.findViewById(R.id.tv_kitchen_item_name);
            holder.tvQuantity = convertView.findViewById(R.id.tv_kitchen_quantity);
            holder.tvNote     = convertView.findViewById(R.id.tv_kitchen_note);
            holder.btnDone    = convertView.findViewById(R.id.btn_done);
            holder.btnOut     = convertView.findViewById(R.id.btn_out_of_stock);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OrderDetail detail = cookingOrders.get(position);

        holder.tvTable.setText("Bàn: " + (detail.getTableName() != null ? detail.getTableName() : "--"));
        holder.tvName.setText(detail.getItemName());
        holder.tvQuantity.setText("SL: " + detail.getQuantity());

        if (detail.getNote() != null && !detail.getNote().isEmpty()) {
            holder.tvNote.setText("⚠ " + detail.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        if (detail.getImageUrl() != null && !detail.getImageUrl().isEmpty()) {
            final ImageView iv = holder.ivImage;
            executor.execute(() -> {
                try {
                    java.net.URL url = new java.net.URL(detail.getImageUrl());
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(url.openStream());
                    if (context instanceof android.app.Activity)
                        ((android.app.Activity) context).runOnUiThread(() -> iv.setImageBitmap(bmp));
                } catch (Exception ignored) {}
            });
        }

        holder.btnDone.setOnClickListener(v -> { if (listener != null) listener.onDone(detail); });
        holder.btnOut.setOnClickListener(v  -> { if (listener != null) listener.onOutOfStock(detail); });

        return convertView;
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView  tvTable, tvName, tvQuantity, tvNote;
        Button    btnDone, btnOut;
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}