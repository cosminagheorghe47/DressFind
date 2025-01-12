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

import com.example.dressfind.activities.OutfitDetailsActivity;
import com.example.dressfind.models.Outfit;
import com.example.dressfind.models.WardrobeItem;

import java.util.List;
import com.example.dressfind.R;
import com.squareup.picasso.Picasso;

public class OutfitsAdapter extends RecyclerView.Adapter<OutfitsAdapter.OutfitsHolder> {

    private Context context;
    private List<Outfit> outfits;

    public OutfitsAdapter(Context context, List<Outfit> outfits) {
        this.context = context;
        this.outfits = outfits;
    }

    @NonNull
    @Override
    public OutfitsAdapter.OutfitsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.outfit_list_item, parent, false);
        return new OutfitsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitsAdapter.OutfitsHolder holder, int position) {

        Outfit outfit = outfits.get(position);

        holder.outfitName.setText(outfit.getName());
        Picasso.get().load(outfit.getImage()).into(holder.outfitImage);

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

    public class OutfitsHolder extends RecyclerView.ViewHolder{

        ImageView outfitImage;
        TextView outfitName;


        public OutfitsHolder(@NonNull View itemView) {
            super(itemView);
            outfitImage = itemView.findViewById(R.id.outfitImage);
            outfitName = itemView.findViewById(R.id.outfitName);
        }
    }
}
