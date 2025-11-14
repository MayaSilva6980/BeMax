package com.example.bemax.ui.fragments;

import static com.example.bemax.util.helper.StringHelper.getGreeting;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.bemax.adapter.LembreteAdapter;
import com.example.bemax.model.domain.Lembrete;
import com.example.bemax.model.domain.User;
import com.example.bemax.ui.activity.ReminderFormActivity;
import com.example.bemax.ui.activity.MainActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener {
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

    public HomeFragment(MainActivity principal, User user) {
        mainActivity = principal;
        currentUser = user;
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
         //preenche campos da tela

         // Atualiza saudação com nome do usuário
         if (currentUser != null && currentUser.getFullName() != null) {
             String firstName = currentUser.getFullName().split(" ")[0];
             lblSaudacao.setText(getGreeting(mainActivity) + ", " + firstName + "!");
         } else {
             lblSaudacao.setText(getGreeting(mainActivity) + "!");
         }

         //preenche a lista de lembretes
         preencheListaLembretes();
     }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lnlAdicionarLembrete) {
            startActivity(new Intent(mainActivity, ReminderFormActivity.class));
        }
    }

    public void preencheListaLembretes() {
        ArrayList listaLembretes = null;
        // obter as informacoes da api

        listaLembretes = new ArrayList<>();
        listaLembretes.add(new Lembrete("Medicamento - Omeprazol", "Hoje às 14:00", 1));
        listaLembretes.add(new Lembrete("Consulta - Cardiologista", "Amanhã às 09:30", 2));
        listaLembretes.add(new Lembrete("Exame de Sangue", "Quarta às 08:00", 3));

        LembreteAdapter adapter = new LembreteAdapter(listaLembretes);
        rcvLembretes.setLayoutManager(new LinearLayoutManager(mainActivity));
        rcvLembretes.setAdapter(adapter);
    }
}