package com.example.bemax.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.model.Contato;
import com.example.bemax.util.BaseActivity;

public class FrmCadastroContatos extends BaseActivity implements  View.OnClickListener {
    // Campos de texto
    private EditText txtNomeContato;
    private EditText txtParentesco;
    private EditText txtTelefoneContato;
    private EditText txtEmailContato;
    private EditText txtObservacoesContato;

    // Botões e TextViews
    private Button btnSalvarContato;
    private TextView cmdCancelar;

    public Contato contatoEditar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_cadastro_contatos);

            obtemParametros();
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
        contatoEditar = (Contato) getIntent().getSerializableExtra("contato");
    }

    @Override
    public void iniciaControles() throws Exception
    {
        txtTelefoneContato = findViewById(R.id.txtTelefoneContato);
        txtNomeContato = findViewById(R.id.txtNomeContato);
        txtEmailContato = findViewById(R.id.txtEmailContato);
        txtParentesco = findViewById(R.id.txtParentesco);
        txtObservacoesContato = findViewById(R.id.txtObservacoesContato);

        btnSalvarContato = findViewById(R.id.btnSalvarContato);
        cmdCancelar = findViewById(R.id.cmdCancelar);

        cmdCancelar.setOnClickListener(this);

        carregaDados();
    }

    @Override
    public void carregaDados() throws Exception
    {
        if (contatoEditar != null)
        {
            txtTelefoneContato.setText(contatoEditar.getTelefone());
            txtEmailContato.setText(contatoEditar.getEmail());
            txtNomeContato.setText(contatoEditar.getNome());
            txtParentesco.setText(contatoEditar.getParentesco());
            txtObservacoesContato.setText(contatoEditar.getObservacoes());
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.cmdCancelar)
        {
            getOnBackPressedDispatcher().onBackPressed();
        }
    }
}