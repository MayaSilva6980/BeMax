package com.example.bemax.telas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.model.Contato;
import com.example.bemax.util.BaseActivity;

public class FrmCadastro extends BaseActivity implements  View.OnClickListener {
    // Campos de texto
    private EditText txtNome;
    private EditText txtEmail;
    private EditText txtSenha;
    private EditText txtConfirmarSenha;
    private EditText txtTelefone;
    private Button btnSalvarUsuario;
    private TextView cmdCancelar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_cadastro);

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
        txtEmail.setText((String) getIntent().getSerializableExtra("email"));
        txtNome.setText((String) getIntent().getSerializableExtra("nome"));
        txtTelefone.setText((String) getIntent().getSerializableExtra("telefone"));
    }

    @Override
    public void iniciaControles() throws Exception
    {
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);
        txtConfirmarSenha = findViewById(R.id.txtConfirmarSenha);
        txtTelefone = findViewById(R.id.txtTelefone);

        btnSalvarUsuario = findViewById(R.id.btnSalvarUsuario);
        cmdCancelar = findViewById(R.id.cmdCancelar);

        cmdCancelar.setOnClickListener(this);

        carregaDados();
    }

    @Override
    public void carregaDados() throws Exception
    {
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.cmdCancelar)
        {
            onBackPressed();
        }
    }
}