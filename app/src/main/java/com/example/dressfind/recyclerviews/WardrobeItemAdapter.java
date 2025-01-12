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

import com.example.dressfind.R;
import com.example.dressfind.activities.ClothingDetailsActivity;
import com.example.dressfind.models.WardrobeItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WardrobeItemAdapter extends RecyclerView.Adapter<WardrobeItemAdapter.WardrobeItemHolder> {

    private Context context;
    private List<WardrobeItem> items;
    private OnCreatePinClickListener pinClickListener;
    public WardrobeItemAdapter(Context context, List<WardrobeItem> items,OnCreatePinClickListener pinClickListener) {
        this.context = context;
        this.items = items;
        this.pinClickListener = pinClickListener;
    }
    public interface OnCreatePinClickListener {
        void onCreatePinClick(WardrobeItem item,String description);
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

        holder.createPinButton.setOnClickListener(v -> {
            showInputDialog(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class WardrobeItemHolder extends RecyclerView.ViewHolder{

        ImageView itemImage;
        TextView itemName;
        View createPinButton;
        public WardrobeItemHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            createPinButton = itemView.findViewById(R.id.createPinCV);
        }
    }
    private void showInputDialog(WardrobeItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Description");

        LayoutInflater inflater = LayoutInflater.from(context);
        EditText input = new EditText(context);
        input.setHint("Enter a description...");
        builder.setView(input);

        builder.setPositiveButton("Create Pin", (dialog, which) -> {
            String description = input.getText().toString().trim();
            if (!description.isEmpty()) {
                pinClickListener.onCreatePinClick(item, description);
            } else {
                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
