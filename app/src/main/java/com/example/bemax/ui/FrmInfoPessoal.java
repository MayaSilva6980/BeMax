package com.example.bemax.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.util.BaseActivity;

public class FrmInfoPessoal extends BaseActivity implements  View.OnClickListener
{

    // TextInputEditText
    private EditText txtInfoNome;
    private EditText txtInfoNascimento;
    private EditText txtInfoGenero;
    private EditText txtInfoEndereco;

    // Buttons e TextViews
    private Button btnSalvar;
    private TextView btnCancelar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_info_pessoal);
            iniciaControles();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void obtemParametros() {

    }

    @Override
    public void iniciaControles() throws Exception
    {
        txtInfoNome = findViewById(R.id.txtInfoNome);
        txtInfoNascimento = findViewById(R.id.txtInfoNascimento);
        txtInfoGenero = findViewById(R.id.txtInfoGenero);
        txtInfoEndereco = findViewById(R.id.txtInfoEndereco);

        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnCancelar.setOnClickListener(this);
    }

    @Override
    public void carregaDados() throws Exception {

    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btnCancelar)
        {
            getOnBackPressedDispatcher().onBackPressed();
        }
    }

}
