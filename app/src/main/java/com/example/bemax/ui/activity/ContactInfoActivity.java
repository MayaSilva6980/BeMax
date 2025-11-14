package com.example.bemax.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.ui.base.BaseActivity;

import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.adapter.ContatoAdapter;
import com.example.bemax.model.domain.Contato;

import java.util.ArrayList;

public class ContactInfoActivity extends BaseActivity implements  View.OnClickListener
{
    private TextView btnCancelar;
    private RecyclerView recyclerContatos;
    private LinearLayout btnNovoContato;
    private ArrayList<Contato> listaContatos;
    private ContatoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_info_contatos);

            //inicia os controles
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
        recyclerContatos = findViewById(R.id.recyclerContatos);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnNovoContato = findViewById(R.id.btnNovoContato);

        btnCancelar.setOnClickListener(this);
        btnNovoContato.setOnClickListener(this);

        carregaDados();
    }

    @Override
    public void carregaDados() throws Exception
    {
        //prenche lista de contatos
        carregaListaContatos();
    }

    public void carregaListaContatos()
    {
        // Lista de exemplo (poderia vir do banco de dados)
        listaContatos = new ArrayList<>();
        listaContatos.add(new Contato("Maria Souza", "Mãe", "(11) 98888-0000", "maria@email.com", "Diabética"));
        listaContatos.add(new Contato("Carlos Silva", "Irmão", "(11) 97777-1234", "carlos@email.com", "Nenhum"));

        adapter = new ContatoAdapter(listaContatos, contato -> {
            Intent intent = new Intent(this, ContactFormActivity.class);
            intent.putExtra("contato", contato);
            startActivity(intent);
        });

        recyclerContatos.setLayoutManager(new LinearLayoutManager(this));
        recyclerContatos.setAdapter(adapter);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btnNovoContato)
        {
            startActivity(new Intent(this, ContactFormActivity.class));
        }
        else if (view.getId() == R.id.btnCancelar)
        {
            getOnBackPressedDispatcher().onBackPressed();
        }
    }
}
