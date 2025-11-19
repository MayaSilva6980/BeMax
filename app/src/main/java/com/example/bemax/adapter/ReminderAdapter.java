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
import com.example.bemax.util.DateTimeHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

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
        
        // Próxima ocorrência (converted from UTC to local time)
        String nextOccurrence = DateTimeHelper.formatReminderDateTime(context, reminder.getNextOccurrence());
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
