package com.example.dressfind.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.MatchingImage;
import com.squareup.picasso.Picasso;

import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class MatchingImageAdapter extends RecyclerView.Adapter<MatchingImageAdapter.ViewHolder> {
    private List<MatchingImage> matchingImages;
    private Context context;

    public MatchingImageAdapter(Context context) {
        this.context = context;
        this.matchingImages = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_matching_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchingImage matchingImage = matchingImages.get(position);

        Picasso.get()
                .load(matchingImage.getImageUrl())
                .into(holder.imageView);

        holder.pageTitle.setText(matchingImage.getPageTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(matchingImage.getPageUrl()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return matchingImages.size();
    }

    public void updateData(List<MatchingImage> newMatchingImages) {
        this.matchingImages.clear();
        this.matchingImages.addAll(newMatchingImages);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView pageTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            pageTitle = itemView.findViewById(R.id.pageTitle);
        }
    }
}
