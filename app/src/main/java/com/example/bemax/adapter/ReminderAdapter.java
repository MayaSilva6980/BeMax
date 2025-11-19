package com.example.bemax.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.model.domain.Reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> listaReminders;
    private OnReminderClickListener listener;

    public interface OnReminderClickListener {
        void onReminderClick(Reminder reminder);
    }

    public ReminderAdapter(List<Reminder> listaReminders) {
        this.listaReminders = listaReminders;
    }

    public ReminderAdapter(List<Reminder> listaReminders, OnReminderClickListener listener) {
        this.listaReminders = listaReminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adp_lembrete, parent, false);
        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = listaReminders.get(position);
        
        holder.lblTitulo.setText(reminder.getTitle());
        
        // Formatar data/hora para exibição
        String horarioFormatado = formatarHorario(reminder.getNextOccurrence());
        holder.lblHorario.setText(horarioFormatado);

        // Definir ícone e cor baseado na categoria
        if (reminder.getCategory() != null) {
            String categoryName = reminder.getCategory().getName();
            String categoryColor = reminder.getCategory().getColor();
            
            // Definir ícone baseado no nome da categoria
            if (categoryName != null) {
                switch (categoryName.toLowerCase()) {
                    case "medication":
                        holder.imgLembrete.setImageResource(R.drawable.ic_medicamento);
                        break;
                    case "appointment":
                        holder.imgLembrete.setImageResource(R.drawable.ic_consulta);
                        break;
                    case "exam":
                        holder.imgLembrete.setImageResource(R.drawable.ic_exame);
                        break;
                    default:
                        holder.imgLembrete.setImageResource(R.drawable.ic_medicamento);
                        break;
                }
            }
            
            // Aplicar cor da categoria se disponível
            if (categoryColor != null && !categoryColor.isEmpty()) {
                try {
                    int color = Color.parseColor(categoryColor);
                    holder.cardView.setCardBackgroundColor(Color.argb(30, 
                        Color.red(color), 
                        Color.green(color), 
                        Color.blue(color)));
                } catch (IllegalArgumentException e) {
                    // Cor inválida, usar padrão
                }
            }
        } else {
            holder.imgLembrete.setImageResource(R.drawable.ic_medicamento);
        }

        // Click listener
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onReminderClick(reminder));
        }
    }

    @Override
    public int getItemCount() {
        return listaReminders != null ? listaReminders.size() : 0;
    }

    private String formatarHorario(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "";
        }

        try {
            // Parse ISO 8601 date format
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
            
            Date date = isoFormat.parse(isoDateTime);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isoDateTime;
    }

    public void updateData(List<Reminder> newReminders) {
        this.listaReminders = newReminders;
        notifyDataSetChanged();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView lblTitulo, lblHorario;
        ImageView imgLembrete;
        CardView cardView;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            lblTitulo = itemView.findViewById(R.id.lblTitulo);
            lblHorario = itemView.findViewById(R.id.lblHorario);
            imgLembrete = itemView.findViewById(R.id.imgLembrete);
            cardView = itemView.findViewById(R.id.cardLembrete);
        }
    }
}

