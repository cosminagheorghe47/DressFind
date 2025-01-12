package com.example.dressfind.recyclerviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
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
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.wardrobe_item_details_popup);

            ImageView popupItemImage = dialog.findViewById(R.id.popupWardrobeItemImage);
            TextView popupItemName = dialog.findViewById(R.id.popupWardrobeItemName);
            TextView popupItemColor = dialog.findViewById(R.id.popupWardrobeItemColor);
            TextView popupItemDescription = dialog.findViewById(R.id.popupWardrobeItemDescription);
            TextView popupItemMaterial = dialog.findViewById(R.id.popupWardrobeItemMaterial);
            TextView popupItemCategory = dialog.findViewById(R.id.popupWardrobeItemCategory);

            popupItemName.setText(item.getName());
            SpannableString colorText = new SpannableString("Color: " + item.getColor());
            colorText.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Apply bold to "Color: "
            popupItemColor.setText(colorText);

            SpannableString descriptionText = new SpannableString("Description: " + item.getDescription());
            descriptionText.setSpan(new StyleSpan(Typeface.BOLD), 0, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Apply bold to "Description: "
            popupItemDescription.setText(descriptionText);

            SpannableString materialText = new SpannableString("Material: " + item.getMaterial());
            materialText.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Apply bold to "Material: "
            popupItemMaterial.setText(materialText);

            SpannableString categoryText = new SpannableString("Category: " + item.getCategory());
            categoryText.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Apply bold to "Category: "
            popupItemCategory.setText(categoryText);

            Picasso.get().load(item.getImage()).into(popupItemImage);

            dialog.show();
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
