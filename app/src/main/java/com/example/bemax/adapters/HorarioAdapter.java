package com.example.bemax.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;

import java.util.ArrayList;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.AdapterHorarioHolder>
{
    private ArrayList<Integer> items;
    private int PosicaoSelecionada = RecyclerView.NO_POSITION;

    public HorarioAdapter(ArrayList<Integer> items) {
        this.items = items;
    }

    public void setPosicaoSelecionada(int position)
    {
        PosicaoSelecionada = position;
        notifyDataSetChanged();
    }

    public String getSelectedItem() {
        if (PosicaoSelecionada >= 0 && PosicaoSelecionada < items.size())
        {
            return items.get(PosicaoSelecionada).toString();
        }
        return null;
    }

    @Override
    public AdapterHorarioHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_horario, parent, false); // seu XML
        return new AdapterHorarioHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterHorarioHolder holder, int position)
    {
        String text = items.get(position).toString();
        holder.lblNumero.setText(String.format("%02d", text));


        // destaca item selecionado
        if (position == PosicaoSelecionada)
        {
            holder.lblNumero.setTextColor(Color.BLACK);
            holder.lblNumero.setTextSize(28);
            holder.lblNumero.setTypeface(null, Typeface.BOLD);
        }
        else
        {
            holder.lblNumero.setTextColor(Color.GRAY);
            holder.lblNumero.setTextSize(22);
            holder.lblNumero.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class AdapterHorarioHolder extends RecyclerView.ViewHolder {
        TextView lblNumero;

        public AdapterHorarioHolder(View itemView) {
            super(itemView);
            lblNumero = (TextView) itemView;
        }
    }
}
