package com.example.bemax.ui.fragments;

import static com.example.bemax.util.helper.StringHelper.getGreeting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.adapter.ReminderAdapter;
import com.example.bemax.model.domain.HealthProfile;
import com.example.bemax.model.domain.Reminder;
import com.example.bemax.model.domain.Stats;
import com.example.bemax.model.domain.User;
import com.example.bemax.model.dto.MeResponse;
import com.example.bemax.ui.activity.ReminderFormActivity;
import com.example.bemax.ui.activity.MainActivity;
import com.example.bemax.util.helper.ErrorHelper;
import com.example.bemax.util.helper.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HomeFragment";
    
    // controles
    private TextView lblSaudacao = null;
    private TextView lblBatimento = null;
    private TextView lblPressao = null;
    private TextView lblSono = null;
    private TextView lblHidratacao = null;
    private RecyclerView rcvLembretes = null;
    private LinearLayout lnlAdicionarLembrete = null;
    private View emptyStateReminders = null;
    private com.google.android.material.button.MaterialButton btnAddFirstReminder = null;
    
    // Health Cards
    private com.google.android.material.card.MaterialCardView cardHeartRate = null;
    private com.google.android.material.card.MaterialCardView cardBloodPressure = null;
    private com.google.android.material.card.MaterialCardView cardSleep = null;
    private com.google.android.material.card.MaterialCardView cardHydration = null;

    MainActivity mainActivity = null;
    User currentUser = null;
    private MeResponse meData;
    private List<Reminder> reminders;

    public HomeFragment(MainActivity principal, User user, MeResponse meData, List<Reminder> reminders) {
        mainActivity = principal;
        currentUser = user;
        this.meData = meData;
        this.reminders = reminders != null ? reminders : new ArrayList<>();
    }

    // Sobrecarga para manter compatibilidade
    public HomeFragment(MainActivity principal, User user, MeResponse meData) {
        this(principal, user, meData, null);
    }

    // Sobrecarga para manter compatibilidade
    public HomeFragment(MainActivity principal, User user) {
        this(principal, user, null, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        // Infla o layout do fragmento
        View view = inflater.inflate(R.layout.frm_home, container, false);

        iniciaControles(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarregar dados ao voltar para o fragment
        Log.d(TAG, "onResume - Recarregando dados do usuário...");
        if (mainActivity != null) {
            mainActivity.reloadUserData();
        }
    }

    public void iniciaControles(View view) {
        lblSaudacao = view.findViewById(R.id.lblSaudacao);
        lblBatimento = view.findViewById(R.id.lblBatimento);
        lblPressao = view.findViewById(R.id.lblPressao);
        lblSono = view.findViewById(R.id.lblSono);
        lblHidratacao = view.findViewById(R.id.lblHidratacao);
        rcvLembretes = view.findViewById(R.id.rcvLembretes);
        lnlAdicionarLembrete = view.findViewById(R.id.lnlAdicionarLembrete);
        emptyStateReminders = view.findViewById(R.id.emptyStateReminders);
        btnAddFirstReminder = view.findViewById(R.id.btnAddFirstReminder);
        
        // Health Cards
        cardHeartRate = view.findViewById(R.id.cardHeartRate);
        cardBloodPressure = view.findViewById(R.id.cardBloodPressure);
        cardSleep = view.findViewById(R.id.cardSleep);
        cardHydration = view.findViewById(R.id.cardHydration);

        lnlAdicionarLembrete.setOnClickListener(this);
        btnAddFirstReminder.setOnClickListener(this);
        
        // Health Cards Click Listeners
        cardHeartRate.setOnClickListener(this);
        cardBloodPressure.setOnClickListener(this);
        cardSleep.setOnClickListener(this);
        cardHydration.setOnClickListener(this);

        loadData();
    }

     public void loadData() {
         // Atualiza saudação com nome do usuário
         if (currentUser != null && currentUser.getFullName() != null) {
             String firstName = currentUser.getFullName().split(" ")[0];
             lblSaudacao.setText(getGreeting(mainActivity) + ", " + firstName + "!");
         } else {
             lblSaudacao.setText(getGreeting(mainActivity) + "!");
         }

         //preenche a lista de lembretes
         preencheListaLembretes();

         if (meData != null && meData.getStats() != null) {
             updateStats(meData.getStats());
         }

         if (meData != null && meData.getHealthProfile() != null) {
             updateHealthData(meData.getHealthProfile());
         }
     }

    /**
     * Atualiza dados do fragment quando novos dados chegam do backend
     */
    public void updateData(User user, MeResponse newMeData, List<Reminder> newReminders) {
        if (user != null) {
            this.currentUser = user;
        }
        if (newMeData != null) {
            this.meData = newMeData;
        }
        if (newReminders != null) {
            this.reminders = newReminders;
        }
        
        // Recarregar UI com novos dados
        if (getView() != null) {
            loadData();
            Log.d(TAG, "HomeFragment atualizado com novos dados (" + (reminders != null ? reminders.size() : 0) + " lembretes)");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        
        if (id == R.id.lnlAdicionarLembrete || id == R.id.btnAddFirstReminder) {
            startActivity(new Intent(mainActivity, ReminderFormActivity.class));
        }
        else if (id == R.id.cardHeartRate) {
            // TODO: Abrir tela de detalhes de frequência cardíaca
            NotificationHelper.showInfo(mainActivity, "Funcionalidade em desenvolvimento");
        }
        else if (id == R.id.cardBloodPressure) {
            // TODO: Abrir tela de detalhes de pressão arterial
            NotificationHelper.showInfo(mainActivity, "Funcionalidade em desenvolvimento");
        }
        else if (id == R.id.cardSleep) {
            // TODO: Abrir tela de detalhes de sono
            NotificationHelper.showInfo(mainActivity, "Funcionalidade em desenvolvimento");
        }
        else if (id == R.id.cardHydration) {
            // TODO: Abrir tela de detalhes de hidratação
            NotificationHelper.showInfo(mainActivity, "Funcionalidade em desenvolvimento");
        }
    }

    public void preencheListaLembretes() {
        // Usar dados dos lembretes vindos do endpoint /reminders
        List<Reminder> listaLembretes = reminders != null ? new ArrayList<>(reminders) : new ArrayList<>();
        
        Log.d(TAG, "Carregando " + listaLembretes.size() + " lembretes do endpoint /reminders");

        // Verificar se há lembretes
        if (listaLembretes.isEmpty()) {
            // Mostrar empty state
            rcvLembretes.setVisibility(View.GONE);
            emptyStateReminders.setVisibility(View.VISIBLE);
            Log.d(TAG, "Exibindo empty state - nenhum lembrete encontrado");
        } else {
            // Mostrar lista
            rcvLembretes.setVisibility(View.VISIBLE);
            emptyStateReminders.setVisibility(View.GONE);
            
            ReminderAdapter adapter = new ReminderAdapter(mainActivity, listaLembretes, new ReminderAdapter.OnReminderInteractionListener() {
                @Override
                public void onReminderClick(Reminder reminder) {
                    // Click no card - pode abrir detalhes
                    Log.d(TAG, "Lembrete clicado: " + reminder.getTitle());
                }

                @Override
                public void onEditClick(Reminder reminder) {
                    Log.d(TAG, "Editando lembrete: " + reminder.getTitle());
                    openEditReminder(reminder);
                }

                @Override
                public void onDeleteClick(Reminder reminder) {
                    Log.d(TAG, "Solicitando exclusão de: " + reminder.getTitle());
                    confirmDeleteReminder(reminder);
                }
            });
            
            rcvLembretes.setLayoutManager(new LinearLayoutManager(mainActivity));
            rcvLembretes.setAdapter(adapter);
        }
    }
    // NOVO MÉTODO para atualizar stats
    private void updateStats(Stats stats) {
        // Exibir estatísticas de lembretes
        Log.d(TAG, "Total de lembretes: " + stats.getTotalReminders());
        Log.d(TAG, "Lembretes ativos: " + stats.getActiveReminders());
        Log.d(TAG, "Lembretes de hoje: " + stats.getTodayReminders());
        Log.d(TAG, "Lembretes próximos: " + stats.getUpcomingReminders());
        
        // TODO: Adicionar TextViews na UI para exibir essas stats
        // Por enquanto apenas logando
    }

    // NOVO MÉTODO para atualizar dados de saúde
    private void updateHealthData(HealthProfile healthProfile) {
        // Atualizar os cards de saúde com dados reais
        Log.d(TAG, "=== DADOS DE SAÚDE ===");
        Log.d(TAG, "Tipo sanguíneo: " + healthProfile.getBloodType());
        Log.d(TAG, "Peso: " + healthProfile.getWeight() + " kg");
        Log.d(TAG, "Altura: " + healthProfile.getHeight() + " cm");
        
        if (healthProfile.getAllergies() != null && !healthProfile.getAllergies().isEmpty()) {
            Log.d(TAG, "Alergias: " + String.join(", ", healthProfile.getAllergies()));
        }
        
        if (healthProfile.getMedications() != null && !healthProfile.getMedications().isEmpty()) {
            Log.d(TAG, "Medicações: " + String.join(", ", healthProfile.getMedications()));
        }
        
        if (healthProfile.getNotes() != null && !healthProfile.getNotes().isEmpty()) {
            Log.d(TAG, "Notas: " + healthProfile.getNotes());
        }
        
        // TODO: Adicionar campos na UI para exibir esses dados
        // Por enquanto apenas logando
    }

    /**
     * Abre a activity de edição de lembrete
     */
    private void openEditReminder(Reminder reminder) {
        Intent intent = new Intent(mainActivity, com.example.bemax.ui.activity.ReminderFormActivity.class);
        intent.putExtra("MODE", "EDIT");
        intent.putExtra("REMINDER", reminder);
        startActivity(intent);
    }

    /**
     * Mostra diálogo de confirmação para deletar lembrete
     */
    private void confirmDeleteReminder(Reminder reminder) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(mainActivity)
                .setTitle(R.string.reminder_delete_confirm_title)
                .setMessage(R.string.reminder_delete_confirm_message)
                .setPositiveButton(R.string.reminder_delete_confirm_yes, (dialog, which) -> {
                    deleteReminder(reminder);
                })
                .setNegativeButton(R.string.reminder_delete_confirm_no, null)
                .show();
    }

    /**
     * Deleta um lembrete do backend
     */
    private void deleteReminder(Reminder reminder) {
        com.example.bemax.util.manager.TokenManager tokenManager = com.example.bemax.util.manager.TokenManager.getInstance(mainActivity);
        com.example.bemax.repository.ReminderRepository reminderRepository = new com.example.bemax.repository.ReminderRepository();

        tokenManager.getAccessToken(new com.example.bemax.util.manager.TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                reminderRepository.deleteReminder(token, reminder.getId(), new com.example.bemax.repository.ReminderRepository.DeleteReminderCallback() {
                    @Override
                    public void onSuccess() {
                        mainActivity.runOnUiThread(() -> {
                            NotificationHelper.showSuccess(mainActivity, "Lembrete excluído com sucesso");
                            // Recarregar dados
                            if (mainActivity != null) {
                                mainActivity.reloadUserData();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        mainActivity.runOnUiThread(() -> {
                            NotificationHelper.showError(mainActivity, "Erro ao excluir lembrete: " + error);
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                mainActivity.runOnUiThread(() -> {
                    ErrorHelper.handleAuthError(getView());
                });
            }
        });
    }
}