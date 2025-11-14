package com.example.bemax.ui;

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
import com.example.bemax.adapters.LembreteAdapter;
import com.example.bemax.model.Lembrete;
import com.example.bemax.model.User;

import java.util.ArrayList;

public class FrmHome extends Fragment implements View.OnClickListener {
    // controles
    private TextView lblSaudacao = null;
    private TextView lblBatimento = null;
    private TextView lblPressao = null;
    private TextView lblSono = null;
    private TextView lblHidratacao = null;
    private RecyclerView rcvLembretes = null;
    private LinearLayout lnlAdicionarLembrete = null;

    FrmPrincipal frmPrincipal = null;
    User currentUser = null;

    public FrmHome(FrmPrincipal principal, User user) {
        frmPrincipal = principal;
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
             lblSaudacao.setText(getGreeting() + ", " + firstName + "!");
         } else {
             lblSaudacao.setText(getGreeting() + "!");
         }

         //preenche a lista de lembretes
         preencheListaLembretes();
     }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lnlAdicionarLembrete) {
            startActivity(new Intent(frmPrincipal, FrmCadastroLembretes.class));
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
        rcvLembretes.setLayoutManager(new LinearLayoutManager(frmPrincipal));
        rcvLembretes.setAdapter(adapter);
    }

    // Retorna a saudação apropriada com base na hora do dia
    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 6) {
            // 00:00 - 05:59 = Madrugada
            return getString(R.string.good_dawn);
        } else if (hour >= 6 && hour < 12) {
            // 06:00 - 11:59 = Manhã
            return getString(R.string.good_morning);
        } else if (hour >= 12 && hour < 18) {
            // 12:00 - 17:59 = Tarde
            return getString(R.string.good_afternoon);
        } else {
            // 18:00 - 23:59 = Noite
            return getString(R.string.good_evening);
        }
    }
}