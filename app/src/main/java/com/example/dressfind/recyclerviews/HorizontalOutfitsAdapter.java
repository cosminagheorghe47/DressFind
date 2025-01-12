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
import com.example.dressfind.activities.OutfitDetailsActivity;
import com.example.dressfind.models.Outfit;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HorizontalOutfitsAdapter extends RecyclerView.Adapter<HorizontalOutfitsAdapter.OutfitViewHolder> {

    private List<Outfit> outfits;
    private Context context;
    public HorizontalOutfitsAdapter(Context context, List<Outfit> outfits) {
        this.outfits = outfits;
        this.context=context;
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit outfit = outfits.get(position);
        holder.textTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(outfit.getScheduledDate()));
        holder.name.setText(outfits.get(position).getName());
        Picasso.get().load(outfit.getImage()).into(holder.imageOutfit);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OutfitDetailsActivity.class);
            intent.putExtra("outfitId", outfit.getOutfitId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return outfits.size();
    }

    public static class OutfitViewHolder extends RecyclerView.ViewHolder {
        ImageView imageOutfit;
        TextView textTime;
        TextView name;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            imageOutfit = itemView.findViewById(R.id.imageOutfit);
            textTime = itemView.findViewById(R.id.textTime);
            name=itemView.findViewById(R.id.textOutfitName);
        }
    }
}
