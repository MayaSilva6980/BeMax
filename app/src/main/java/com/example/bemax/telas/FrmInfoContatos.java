package com.example.bemax.telas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.util.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;

import com.example.bemax.adapters.ContatoAdapter;
import com.example.bemax.model.Contato;

import java.util.ArrayList;

public class FrmInfoContatos extends BaseActivity implements  View.OnClickListener
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
            Intent intent = new Intent(this, FrmCadastroContatos.class);
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
            startActivity(new Intent(this, FrmCadastroContatos.class));
        }
        else if (view.getId() == R.id.btnCancelar)
        {
            onBackPressed();
        }
    }
}
