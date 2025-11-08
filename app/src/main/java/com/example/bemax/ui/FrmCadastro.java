package com.example.bemax.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bemax.R;
import com.example.bemax.model.dto.RegisterRequest;
import com.example.bemax.model.dto.RegisterResponse;
import com.example.bemax.repository.RegisterRepository;
import com.example.bemax.util.BaseActivity;

public class FrmCadastro extends BaseActivity implements  View.OnClickListener {
    // Campos de texto
    private EditText txtNome;
    private EditText txtEmail;
    private EditText txtSenha;
    private EditText txtConfirmarSenha;
    private EditText txtTelefone;
    private EditText txtCpf;
    private EditText txtDataNasc;
    private Button btnSalvarUsuario;
    private TextView btnCancelar;

    private LinearLayout lnlAreaProgressBar;

    //Variaveis de classe
    private RegisterRepository registerRepository;
    private RegisterRequest registerRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_cadastro);

            registerRepository = new RegisterRepository(this);

            iniciaControles();
            obtemParametros();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void obtemParametros()
    {
        // Preenche os campos com dados vindos da Intent (se houver)
        if (getIntent().getSerializableExtra("email") != null)
            txtEmail.setText((String) getIntent().getSerializableExtra("email"));
        
        if (getIntent().getSerializableExtra("nome") != null)
            txtNome.setText((String) getIntent().getSerializableExtra("nome"));
        
        if (getIntent().getSerializableExtra("telefone") != null)
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
        txtCpf = findViewById(R.id.txtCpf);
        txtDataNasc = findViewById(R.id.txtDataNasc);
        lnlAreaProgressBar = findViewById(R.id.lnlAreaProgressBar);

        btnSalvarUsuario = findViewById(R.id.btnSalvarUsuario);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnCancelar.setOnClickListener(this);
        btnSalvarUsuario.setOnClickListener(this);

        carregaDados();
    }

    @Override
    public void carregaDados() throws Exception
    {
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btnCancelar)
        {
            getOnBackPressedDispatcher().onBackPressed();
        }
        else if (view.getId() == R.id.btnSalvarUsuario)
        {
            realizarCadastroRetrofit();
        }
    }

    public boolean validaCampos() {
        String sNome = txtNome.getText().toString();
        String sEmail = txtEmail.getText().toString();
        String sCpf = txtCpf.getText().toString();
        String sTelefone = txtTelefone.getText().toString();
        String sSenha = txtSenha.getText().toString();
        String sSenhaConfirma = txtConfirmarSenha.getText().toString();
        String sDataNasc = txtDataNasc.getText().toString();

        if ( sNome.isEmpty())
        {
            txtNome.setError("Nome é obrigatório");
            txtNome.requestFocus();
            return false;
        }
        else if ( sEmail.isEmpty())
        {
            txtEmail.setError("Email é obrigatório");
            txtEmail.requestFocus();
            return false;
        }
        else if ( sCpf.isEmpty() )
        {
            txtCpf.setError("CPF é obrigatório");
            txtCpf.requestFocus();
            return false;
        }
        else if ( sCpf.length() < 11 )
        {
            txtCpf.setError("CPF inválido");
            txtCpf.requestFocus();
            return false;
        }
        else if ( sTelefone.isEmpty())
        {
            txtTelefone.setError("Telefone é obrigatório");
            txtTelefone.requestFocus();
            return false;
        }
        else if ( sTelefone.length() < 11)
        {
            txtTelefone.setError("Telefone é obrigatório");
            txtTelefone.requestFocus();
            return false;
        }
        else if ( sSenha.isEmpty())
        {
            txtSenha.setError("Senha é obrigatório");
            txtSenha.requestFocus();
            return false;
        }
        else if ( !sSenha.equals(sSenhaConfirma))
        {
            txtConfirmarSenha.setError("Senhas diferentes");
            txtConfirmarSenha.requestFocus();
            return false;
        }
        else if ( sDataNasc.isEmpty())
        {
            txtDataNasc.setError("Data de nascimento é obrigatória");
            txtDataNasc.requestFocus();
            return false;
        }

        Toast.makeText(FrmCadastro.this, sTelefone, Toast.LENGTH_SHORT).show();

        registerRequest = new RegisterRequest(sEmail,sNome,sSenha,sCpf,sTelefone,sDataNasc);
        return true;
    }

    private void realizarCadastroRetrofit() {

        if (!validaCampos())
        {
            return;
        }

        btnSalvarUsuario.setEnabled(false);
        btnSalvarUsuario.setText("Cadastrando...");
        lnlAreaProgressBar.setVisibility(View.VISIBLE);

        registerRepository.register(registerRequest, new RegisterRepository.RegisterCallback() {
            @Override
            public void onSuccess(RegisterResponse response) {
                runOnUiThread(() -> {
                    btnSalvarUsuario.setEnabled(true);
                    btnSalvarUsuario.setText("Salvar");
                    lnlAreaProgressBar.setVisibility(View.GONE);

                    Toast.makeText(FrmCadastro.this,
                            "Cadastro realizado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    // Log debug
                    Log.d("FrmCadastro", "Cadastro realizado com sucesso!");
                    Log.d("FrmCadastro", "Response: " + response);

                    Intent intent = new Intent(FrmCadastro.this, FrmPrincipal.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSalvarUsuario.setEnabled(true);
                    btnSalvarUsuario.setText("Continue");
                    lnlAreaProgressBar.setVisibility(View.GONE);

                    Toast.makeText(FrmCadastro.this,
                            "Erro ao fazer cadastro: " + error,
                            Toast.LENGTH_LONG).show();

                    // Log debug
                    Log.e("FrmCadastro", "Erro no cadastro: " + error);
                });
            }
        });
    }
}