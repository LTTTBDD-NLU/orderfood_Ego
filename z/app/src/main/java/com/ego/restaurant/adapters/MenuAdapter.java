package com.ego.restaurant.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ego.restaurant.R;
import com.ego.restaurant.helpers.ImageLoadRunnable;
import com.ego.restaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<MenuItem> menuItems;
    private final ArrayList<MenuItem> cartItems;   // giỏ hàng dùng chung
    private final String roleCode;                 // để hiển thị đúng giá
    private final OnCartChangedListener listener;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public interface OnCartChangedListener {
        void onCartChanged(int count);
    }

    public MenuAdapter(Context context, ArrayList<MenuItem> menuItems,
                       ArrayList<MenuItem> cartItems, String roleCode,
                       OnCartChangedListener listener) {
        this.context   = context;
        this.menuItems = menuItems;
        this.cartItems = cartItems;
        this.roleCode  = roleCode;
        this.listener  = listener;
    }

    @Override
    public int getCount()              { return menuItems.size(); }
    @Override
    public Object getItem(int pos)     { return menuItems.get(pos); }
    @Override
    public long getItemId(int pos)     { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_menu, parent, false);
            holder = new ViewHolder();
            holder.ivImage    = convertView.findViewById(R.id.iv_food_image);
            holder.tvName     = convertView.findViewById(R.id.tv_item_name);
            holder.tvPrice    = convertView.findViewById(R.id.tv_item_price);
            holder.tvStatus   = convertView.findViewById(R.id.tv_item_status);
            holder.btnAdd     = convertView.findViewById(R.id.btn_add);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MenuItem item = menuItems.get(position);

        holder.tvName.setText(item.getItemName());

        double price = item.getAppliedPrice(roleCode);
        holder.tvPrice.setText(String.format("%,.0f đ", price));

        if ("AVAILABLE".equals(item.getStatus())) {
            holder.tvStatus.setText("Còn hàng");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.staff_active));
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setAlpha(1f);
        } else {
            holder.tvStatus.setText("Hết hàng");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.staff_locked));
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setAlpha(0.5f);
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery); // placeholder
            executor.execute(new ImageLoadRunnable(item.getImageUrl(),
                    holder.ivImage, (Activity) context));
        }

        holder.btnAdd.setOnClickListener(v -> {
            boolean found = false;
            for (MenuItem cartItem : cartItems) {
                if (cartItem.getItemId().equals(item.getItemId())) {
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                MenuItem copy = new MenuItem(item.getItemId(), item.getItemName(),
                        item.getImageUrl(), item.getGuestPrice(),
                        item.getMemberPrice(), item.getStatus());
                copy.setQuantity(1);
                cartItems.add(copy);
            }
            Toast.makeText(context, "Đã thêm: " + item.getItemName(), Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onCartChanged(cartItems.size());
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvStatus;
        Button btnAdd;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
