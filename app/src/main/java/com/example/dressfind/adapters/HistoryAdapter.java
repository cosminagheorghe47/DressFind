package com.example.dressfind.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.ScannedImage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<ScannedImage> scannedImages;

    public HistoryAdapter(Context context, List<ScannedImage> scannedImages) {
        this.context = context;
        this.scannedImages = scannedImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scanned_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedImage scannedImage = scannedImages.get(position);

        holder.textViewDate.setText(formatDate(scannedImage.getScanDate()));

        Picasso.get()
                .load(scannedImage.getImage())
                .into(holder.imageViewScanned);
    }

    @Override
    public int getItemCount() {
        return scannedImages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewScanned;
        TextView textViewDate;

        ViewHolder(View itemView) {
            super(itemView);
            imageViewScanned = itemView.findViewById(R.id.image_view_scanned);
            textViewDate = itemView.findViewById(R.id.text_view_date);
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
