package com.example.dressfind.recyclerviews;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.activities.OutfitDetailsActivity;
import com.example.dressfind.models.DayOutfits;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private List<DayOutfits> calendarDays;
    private Context context;
    public CalendarAdapter(Context context, List<DayOutfits> calendarDays) {
        this.calendarDays = calendarDays;
        this.context = context;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayOutfits dayOutfits = calendarDays.get(position);
        holder.textDay.setText(dayOutfits.getDay());

        HorizontalOutfitsAdapter adapter = new HorizontalOutfitsAdapter(context,dayOutfits.getOutfits());
        holder.recyclerViewOutfits.setLayoutManager(new LinearLayoutManager(holder.recyclerViewOutfits.getContext(), RecyclerView.HORIZONTAL, false));
        holder.recyclerViewOutfits.setAdapter(adapter);


    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;
        RecyclerView recyclerViewOutfits;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.textDay);
            recyclerViewOutfits = itemView.findViewById(R.id.recyclerViewOutfits);
        }
    }
}
