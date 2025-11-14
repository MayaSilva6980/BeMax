package com.example.bemax.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.model.domain.Lembrete;
import com.example.bemax.util.AppConstants;

import java.util.List;

public class LembreteAdapter extends RecyclerView.Adapter<LembreteAdapter.LembreteViewHolder>
{

    private List<Lembrete> listaLembretes;

    public LembreteAdapter(List<Lembrete> listaLembretes)
    {
        this.listaLembretes = listaLembretes;
    }

    @NonNull
    @Override
    public LembreteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adp_lembrete, parent, false); // seu XML
        return new LembreteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LembreteViewHolder holder, int position)
    {
        Lembrete lembrete = listaLembretes.get(position);
        holder.lblTitulo.setText(lembrete.DsTitulo);
        holder.lblHorario.setText(lembrete.DsHorario);

        if (lembrete.FlTipo == AppConstants.REMINDER_TYPE_MEDICATION)
        {
            holder.imgLembrete.setImageResource(R.drawable.ic_medicamento);
        }
        else if (lembrete.FlTipo == AppConstants.REMINDER_TYPE_APPOINTMENT)
        {
            holder.imgLembrete.setImageResource(R.drawable.ic_consulta);
        }
        else if (lembrete.FlTipo == AppConstants.REMINDER_TYPE_EXAM)
        {
            holder.imgLembrete.setImageResource(R.drawable.ic_exame);
        }
    }

    @Override
    public int getItemCount() {
        return listaLembretes.size();
    }

    public static class LembreteViewHolder extends RecyclerView.ViewHolder
    {
        TextView lblTitulo, lblHorario;
        ImageView imgLembrete;

        public LembreteViewHolder(@NonNull View itemView)
        {
            super(itemView);
            lblTitulo = itemView.findViewById(R.id.lblTitulo); // ajustar IDs certos
            lblHorario = itemView.findViewById(R.id.lblHorario);
            imgLembrete = itemView.findViewById(R.id.imgLembrete);
        }
    }
}
