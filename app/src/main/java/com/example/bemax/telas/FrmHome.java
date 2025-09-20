package com.example.bemax.telas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FrmHome extends Fragment
{
    // controles
    private Toolbar toolbar = null;
    private TextView lblSaudacao = null;
    private TextView lblBatimento = null;
    private TextView lblPressao = null;
    private TextView lblSono = null;
    private TextView lblHidratacao = null;
    private RecyclerView rcvLembretes = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState)
    {
        // Infla o layout do fragmento
        View view = inflater.inflate(R.layout.frm_home, container, false);

        iniciaControles(view);
        return view;
    }

    public void iniciaControles(View view)
    {
        toolbar = view.findViewById(R.id.toolbar);
        lblSaudacao = view.findViewById(R.id.lblSaudacao);
        lblBatimento = view.findViewById(R.id.lblBatimento);
        lblPressao = view.findViewById(R.id.lblPressao);
        lblSono = view.findViewById(R.id.lblSono);
        lblHidratacao = view.findViewById(R.id.lblHidratacao);
        rcvLembretes = view.findViewById(R.id.rcvLembretes);


        carregaDados();
    }

     public void carregaDados()
     {
         // obter as informacoes da api


     }
}