package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.ego.restaurant.R;
import com.ego.restaurant.models.MenuItem;
import java.util.ArrayList;

public class CartAdapter extends BaseAdapter {

    public interface OnCartChangedListener { void onChanged(); }

    private final Context context;
    private final ArrayList<MenuItem> cartItems;
    private final String roleCode;
    private OnCartChangedListener changeListener;

    public CartAdapter(Context context, ArrayList<MenuItem> cartItems, String roleCode) {
        this.context   = context;
        this.cartItems = cartItems;
        this.roleCode  = roleCode;
    }

    public void setOnChangedListener(OnCartChangedListener l) { this.changeListener = l; }

    @Override public int getCount()           { return cartItems.size(); }
    @Override public Object getItem(int pos)  { return cartItems.get(pos); }
    @Override public long getItemId(int pos)  { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_cart, parent, false);

        MenuItem item = cartItems.get(position);

        TextView tvName  = convertView.findViewById(R.id.tv_cart_item_name);
        TextView tvNote  = convertView.findViewById(R.id.tv_cart_item_note);
        TextView tvPrice = convertView.findViewById(R.id.tv_cart_item_price);
        TextView tvQty   = convertView.findViewById(R.id.tv_cart_item_qty);
        Button   btnPlus = convertView.findViewById(R.id.btn_plus);
        Button   btnMinus= convertView.findViewById(R.id.btn_minus);

        tvName.setText(item.getItemName());
        tvQty.setText(String.valueOf(item.getQuantity()));

        double price = item.getAppliedPrice(roleCode) * item.getQuantity();
        tvPrice.setText(String.format("%,.0f đ", price));

        if (item.getNote() != null && !item.getNote().isEmpty()) {
            tvNote.setText("Ghi chú: " + item.getNote());
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvNote.setVisibility(View.GONE);
        }

        btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyDataSetChanged();
            if (changeListener != null) changeListener.onChanged();
        });

        btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                cartItems.remove(position);
            }
            notifyDataSetChanged();
            if (changeListener != null) changeListener.onChanged();
        });

        return convertView;
    }
}
