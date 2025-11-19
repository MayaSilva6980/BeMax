package com.example.bemax.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.model.domain.Reminder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> listaReminders;
    private OnReminderInteractionListener listener;
    private Context context;

    public interface OnReminderInteractionListener {
        void onReminderClick(Reminder reminder);
        void onEditClick(Reminder reminder);
        void onDeleteClick(Reminder reminder);
    }

    public ReminderAdapter(Context context, List<Reminder> listaReminders, OnReminderInteractionListener listener) {
        this.context = context;
        this.listaReminders = listaReminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = listaReminders.get(position);
        
        // Título
        holder.txtTitle.setText(reminder.getTitle());
        
        // Categoria
        if (reminder.getCategory() != null) {
            holder.txtCategory.setText(reminder.getCategory().getName());
            
            // Ícone da categoria
            String categoryName = reminder.getCategory().getName();
            if (categoryName != null) {
                int iconRes = getCategoryIcon(categoryName.toLowerCase());
                holder.imgCategoryIcon.setImageResource(iconRes);
            }
            
            // Cor da categoria
            String categoryColor = reminder.getCategory().getColor();
            if (categoryColor != null && !categoryColor.isEmpty()) {
                try {
                    int color = Color.parseColor(categoryColor);
                    holder.iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_blue);
                    holder.imgCategoryIcon.setColorFilter(color);
                } catch (IllegalArgumentException e) {
                    // Cor inválida, usar padrão
                }
            }
        } else {
            holder.txtCategory.setText(context.getString(R.string.reminder_category));
            holder.imgCategoryIcon.setImageResource(R.drawable.ic_medicamento);
        }
        
        // Próxima ocorrência
        String nextOccurrence = formatarProximaOcorrencia(reminder.getNextOccurrence());
        holder.txtNextOccurrence.setText(nextOccurrence);
        
        // Frequência
        String frequency = getFrequencyText(reminder.getFrequency());
        holder.chipFrequency.setText(frequency);
        
        // Click listeners
        holder.cardReminder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderClick(reminder);
            }
        });
        
        holder.btnOptions.setOnClickListener(v -> {
            showOptionsMenu(v, reminder);
        });
    }

    @Override
    public int getItemCount() {
        return listaReminders != null ? listaReminders.size() : 0;
    }

    private int getCategoryIcon(String categoryName) {
        switch (categoryName) {
            case "medication":
            case "medicamentos":
                return R.drawable.ic_medicamento;
            case "appointment":
            case "consultas":
                return R.drawable.ic_consulta;
            case "exam":
            case "exames":
                return R.drawable.ic_exame;
            default:
                return R.drawable.ic_medicamento;
        }
    }

    private String formatarProximaOcorrencia(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return context.getString(R.string.reminder_select_time);
        }

        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            Date date = isoFormat.parse(isoDateTime);
            if (date != null) {
                Calendar now = Calendar.getInstance();
                Calendar reminderDate = Calendar.getInstance();
                reminderDate.setTime(date);
                
                // Verificar se é hoje
                if (now.get(Calendar.YEAR) == reminderDate.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == reminderDate.get(Calendar.DAY_OF_YEAR)) {
                    return "Hoje às " + timeFormat.format(date);
                }
                
                // Verificar se é amanhã
                now.add(Calendar.DAY_OF_YEAR, 1);
                if (now.get(Calendar.YEAR) == reminderDate.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == reminderDate.get(Calendar.DAY_OF_YEAR)) {
                    return "Amanhã às " + timeFormat.format(date);
                }
                
                // Data completa
                return dateFormat.format(date) + " às " + timeFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isoDateTime;
    }

    private String getFrequencyText(String frequency) {
        if (frequency == null) return "";
        
        switch (frequency.toLowerCase()) {
            case "once":
                return context.getString(R.string.frequency_once);
            case "daily":
                return context.getString(R.string.frequency_daily);
            case "weekly":
                return context.getString(R.string.frequency_weekly);
            case "monthly":
                return context.getString(R.string.frequency_monthly);
            case "yearly":
                return context.getString(R.string.frequency_yearly);
            default:
                return frequency;
        }
    }

    private void showOptionsMenu(View view, Reminder reminder) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.reminder_options_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                if (listener != null) {
                    listener.onEditClick(reminder);
                }
                return true;
            } else if (id == R.id.action_delete) {
                if (listener != null) {
                    listener.onDeleteClick(reminder);
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    public void updateData(List<Reminder> newReminders) {
        this.listaReminders = newReminders;
        notifyDataSetChanged();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardReminder;
        FrameLayout iconContainer;
        ImageView imgCategoryIcon;
        TextView txtTitle;
        TextView txtCategory;
        ImageView btnOptions;
        TextView txtNextOccurrence;
        Chip chipFrequency;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardReminder = itemView.findViewById(R.id.cardLembrete);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            imgCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            txtTitle = itemView.findViewById(R.id.tvTitle);
            txtCategory = itemView.findViewById(R.id.tvCategory);
            btnOptions = itemView.findViewById(R.id.ivMoreOptions);
            txtNextOccurrence = itemView.findViewById(R.id.tvNextOccurrence);
            chipFrequency = itemView.findViewById(R.id.chipFrequency);
        }
    }
}
