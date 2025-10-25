package com.example.bemax.telas;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.bemax.R;
import com.example.bemax.adapters.HorarioAdapter;
import com.example.bemax.util.BaseActivity;
import com.example.bemax.util.CenterItemScrollListener;

import java.util.ArrayList;

public class FrmCadastroLembretes extends BaseActivity implements  View.OnClickListener
{
    public RecyclerView rcvHoras = null;
    public RecyclerView rcvMinutos = null;
    public TextView cmdCancelar = null;
    public Toolbar toolbar = null;


    // variaveis da classe
    ArrayList<Integer> arrHoras = null;
    ArrayList<Integer> arrMinutos = null;

    SnapHelper snapHelperHoras = new LinearSnapHelper();
    SnapHelper snapHelperMinutos = new LinearSnapHelper();

    HorarioAdapter horasAdapter = null;
    HorarioAdapter minutosAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_cadastro_lembrete);
            iniciaControles();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void obtemParametros()
    {
    }

    @Override
    public void iniciaControles() throws Exception
    {
        rcvHoras = findViewById(R.id.rcvHoras);
        rcvMinutos = findViewById(R.id.rcvMinutos);
        cmdCancelar = findViewById(R.id.cmdCancelar);

        cmdCancelar.setOnClickListener(this);

        preencheListasHorarios();
    }

    @Override
    public void carregaDados() throws Exception
    {
        preencheListasHorarios();
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.cmdCancelar)
        {
            onBackPressed();
        }
    }

    private  void preencheListasHorarios()
    {
        arrHoras = new ArrayList<Integer>();
        arrMinutos = new ArrayList<Integer>();

        // Preenche a lista de minutos
        for (int i = 0; i < 60; i++) {
            arrMinutos.add(i);
        }

        // Preenche a lista de horas
        for (int i = 0; i < 24; i++) {
            arrHoras.add(i);
        }

        horasAdapter = new HorarioAdapter(arrHoras);
        minutosAdapter = new HorarioAdapter(arrMinutos);

        rcvHoras.setLayoutManager(new LinearLayoutManager(this));
        rcvMinutos.setLayoutManager(new LinearLayoutManager(this));

        rcvHoras.setAdapter(horasAdapter);
        rcvMinutos.setAdapter(minutosAdapter);

    // snap helper = trava no item central
        snapHelperHoras = new LinearSnapHelper();
        snapHelperMinutos = new LinearSnapHelper();
        snapHelperHoras.attachToRecyclerView(rcvHoras);
        snapHelperMinutos.attachToRecyclerView(rcvMinutos);

    // listener pra detectar item central
        rcvHoras.addOnScrollListener(new CenterItemScrollListener(snapHelperHoras, horasAdapter));
        rcvMinutos.addOnScrollListener(new CenterItemScrollListener(snapHelperMinutos, minutosAdapter));
    }

}
