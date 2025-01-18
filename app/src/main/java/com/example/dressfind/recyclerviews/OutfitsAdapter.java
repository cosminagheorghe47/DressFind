package com.example.dressfind.recyclerviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private OutfitsAdapter.OnCreatePinClickListener pinClickListener;
    public OutfitsAdapter(Context context, List<Outfit> outfits, OutfitsAdapter.OnCreatePinClickListener pinClickListener) {
        this.context = context;
        this.outfits = outfits;
        this.pinClickListener = pinClickListener;
    }
    public interface OnCreatePinClickListener {
        void onCreatePinClick(Outfit outfit,String description);
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
        holder.createPinButton.setOnClickListener(v -> {
            showInputDialog(outfit);
        });
    }

    @Override
    public int getItemCount() {
        return outfits.size();
    }

    public class OutfitsHolder extends RecyclerView.ViewHolder{

        ImageView outfitImage;
        TextView outfitName;
        View createPinButton;

        public OutfitsHolder(@NonNull View itemView) {
            super(itemView);
            outfitImage = itemView.findViewById(R.id.outfitImage);
            outfitName = itemView.findViewById(R.id.outfitName);
            createPinButton = itemView.findViewById(R.id.createPinCV);
        }
    }
    private void showInputDialog(Outfit outfit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Description");

        LayoutInflater inflater = LayoutInflater.from(context);
        EditText input = new EditText(context);
        input.setHint("Enter a description...");
        builder.setView(input);

        builder.setPositiveButton("Create Pin", (dialog, which) -> {
            String description = input.getText().toString().trim();
            if (!description.isEmpty()) {
                pinClickListener.onCreatePinClick(outfit, description);
            } else {
                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
