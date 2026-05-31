package com.ego.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.ego.restaurant.R;
import com.ego.restaurant.models.MenuItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuManagementAdapter extends BaseAdapter {

    public interface OnToggleListener { void onToggle(MenuItem item); }
    public interface OnEditListener   { void onEdit(MenuItem item); }

    private final Context context;
    private final List<MenuItem> items;
    private final OnToggleListener toggleListener;
    private final OnEditListener   editListener;

    public MenuManagementAdapter(Context ctx, List<MenuItem> items, OnToggleListener tl, OnEditListener el) {
        this.context        = ctx;
        this.items          = items;
        this.toggleListener = tl;
        this.editListener   = el;
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
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_menu_management, parent, false);

        MenuItem item = items.get(position);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi"));

        TextView tvName   = convertView.findViewById(R.id.tv_item_name);
        TextView tvGuest  = convertView.findViewById(R.id.tv_guest_price);
        TextView tvMember = convertView.findViewById(R.id.tv_member_price);
        Switch   sw       = convertView.findViewById(R.id.sw_available);

        tvName.setText(item.getItemName());
        tvGuest.setText("Vãn lai: " + nf.format((long) item.getGuestPrice()) + "đ");
        tvMember.setText("Member: " + nf.format((long) item.getMemberPrice()) + "đ");
        sw.setOnCheckedChangeListener(null);
        sw.setChecked("AVAILABLE".equals(item.getStatus()));
        sw.setOnCheckedChangeListener((btn, checked) -> toggleListener.onToggle(item));

        convertView.setOnClickListener(v -> editListener.onEdit(item));
        return convertView;
    }
}
