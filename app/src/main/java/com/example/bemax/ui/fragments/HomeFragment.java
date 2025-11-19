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

    MainActivity mainActivity = null;
    User currentUser = null;
    private MeResponse meData;

    public HomeFragment(MainActivity principal, User user, MeResponse meData) {
        mainActivity = principal;
        currentUser = user;
        this.meData = meData;
    }

    // Sobrecarga para manter compatibilidade
    public HomeFragment(MainActivity principal, User user) {
        this(principal, user, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        // Infla o layout do fragmento
        View view = inflater.inflate(R.layout.frm_home, container, false);

        iniciaControles(view);
        return view;
    }

    public void iniciaControles(View view) {
        lblSaudacao = view.findViewById(R.id.lblSaudacao);
        lblBatimento = view.findViewById(R.id.lblBatimento);
        lblPressao = view.findViewById(R.id.lblPressao);
        lblSono = view.findViewById(R.id.lblSono);
        lblHidratacao = view.findViewById(R.id.lblHidratacao);
        rcvLembretes = view.findViewById(R.id.rcvLembretes);
        lnlAdicionarLembrete = view.findViewById(R.id.lnlAdicionarLembrete);

        lnlAdicionarLembrete.setOnClickListener(this);

        carregaDados();
    }

     public void carregaDados() {
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
             atualizarStats(meData.getStats());
         }

         if (meData != null && meData.getHealthProfile() != null) {
             atualizarDadosSaude(meData.getHealthProfile());
         }

     }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lnlAdicionarLembrete) {
            startActivity(new Intent(mainActivity, ReminderFormActivity.class));
        }
    }

    public void preencheListaLembretes() {
        List<Reminder> listaLembretes = new ArrayList<>();
        
        // Usar dados reais se disponíveis
        if (meData != null && meData.getReminders() != null && !meData.getReminders().isEmpty()) {
            listaLembretes.addAll(meData.getReminders());
            Log.d(TAG, "Carregados " + listaLembretes.size() + " lembretes do backend");
        } else {
            Log.d(TAG, "Nenhum lembrete disponível do backend");
        }

        ReminderAdapter adapter = new ReminderAdapter(listaLembretes, reminder -> {
            // Click no lembrete - pode implementar depois para abrir detalhes
            Log.d(TAG, "Lembrete clicado: " + reminder.getTitle());
        });
        
        rcvLembretes.setLayoutManager(new LinearLayoutManager(mainActivity));
        rcvLembretes.setAdapter(adapter);
    }
    // NOVO MÉTODO para atualizar stats
    private void atualizarStats(Stats stats) {
        // Exibir estatísticas de lembretes
        Log.d(TAG, "Total de lembretes: " + stats.getTotalReminders());
        Log.d(TAG, "Lembretes ativos: " + stats.getActiveReminders());
        Log.d(TAG, "Lembretes de hoje: " + stats.getTodayReminders());
        Log.d(TAG, "Lembretes próximos: " + stats.getUpcomingReminders());
        
        // TODO: Adicionar TextViews na UI para exibir essas stats
        // Por enquanto apenas logando
    }

    // NOVO MÉTODO para atualizar dados de saúde
    private void atualizarDadosSaude(HealthProfile healthProfile) {
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
}