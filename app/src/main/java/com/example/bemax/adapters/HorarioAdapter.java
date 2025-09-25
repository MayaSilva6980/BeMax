package com.example.bemax.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.ViewHolder>
{
    private List<String> items;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public HorarioAdapter(List<String> items) {
        this.items = items;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public String getSelectedItem() {
        if (selectedPosition >= 0 && selectedPosition < items.size()) {
            return items.get(selectedPosition);
        }
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setTextSize(22);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 30, 0, 30);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioAdapter.ViewHolder holder, int position) {
        String text = items.get(position);
        holder.textView.setText(text);

        // destaca item selecionado
        if (position == selectedPosition) {
            holder.textView.setTextColor(Color.BLACK);
            holder.textView.setTextSize(28);
            holder.textView.setTypeface(null, Typeface.BOLD);
        } else {
            holder.textView.setTextColor(Color.GRAY);
            holder.textView.setTextSize(22);
            holder.textView.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
