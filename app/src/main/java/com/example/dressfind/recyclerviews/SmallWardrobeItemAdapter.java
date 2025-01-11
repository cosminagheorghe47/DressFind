package com.example.dressfind.recyclerviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.WardrobeItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SmallWardrobeItemAdapter extends RecyclerView.Adapter<SmallWardrobeItemAdapter.SmallWardrobeItemHolder> {

    private Context context;
    private List<WardrobeItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WardrobeItem item);
    }

    public SmallWardrobeItemAdapter(Context context, List<WardrobeItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SmallWardrobeItemAdapter.SmallWardrobeItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.small_wardrobe_item, parent, false);
        return new SmallWardrobeItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmallWardrobeItemAdapter.SmallWardrobeItemHolder holder, int position) {
        WardrobeItem item = items.get(position);

        holder.itemName.setText(item.getName());
        Picasso.get().load(item.getImage()).into(holder.itemImage);

        // Setează click listener pe articol
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SmallWardrobeItemHolder extends RecyclerView.ViewHolder{

        ImageView itemImage;
        TextView itemName;

        public SmallWardrobeItemHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
        }
    }
}
