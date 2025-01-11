package com.example.dressfind.recyclerviews;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.activities.ClothingDetailsActivity;
import com.example.dressfind.activities.OutfitDetailsActivity;
import com.example.dressfind.models.WardrobeItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WardrobeItemAdapter extends RecyclerView.Adapter<WardrobeItemAdapter.WardrobeItemHolder> {

    private Context context;
    private List<WardrobeItem> items;

    public WardrobeItemAdapter(Context context, List<WardrobeItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public WardrobeItemAdapter.WardrobeItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wardrobe_item, parent, false);
        return new WardrobeItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WardrobeItemAdapter.WardrobeItemHolder holder, int position) {

        WardrobeItem item = items.get(position);

        holder.itemName.setText(item.getName());
        Picasso.get().load(item.getImage()).into(holder.itemImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClothingDetailsActivity.class);
            intent.putExtra("currentItem", item);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class WardrobeItemHolder extends RecyclerView.ViewHolder{

        ImageView itemImage;
        TextView itemName;

        public WardrobeItemHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);

        }
    }
}
