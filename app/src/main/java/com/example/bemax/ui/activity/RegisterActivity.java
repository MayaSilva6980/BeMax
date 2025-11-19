package com.example.bemax.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.model.dto.RegisterRequest;
import com.example.bemax.model.dto.RegisterResponse;
import com.example.bemax.repository.RegisterRepository;
import com.example.bemax.util.helper.ErrorHelper;
import com.example.bemax.util.helper.InputMaskHelper;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.AppConstants;

public class RegisterActivity extends BaseActivity implements  View.OnClickListener {
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
    protected void onCreate(Bundle savedInstanceState) {
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
    public void obtemParametros() {
        // Preenche os campos com dados vindos da Intent (se houver)
        if (getIntent().getSerializableExtra("email") != null)
            txtEmail.setText((String) getIntent().getSerializableExtra("email"));
        
        if (getIntent().getSerializableExtra("nome") != null)
            txtNome.setText((String) getIntent().getSerializableExtra("nome"));
        
        if (getIntent().getSerializableExtra("telefone") != null)
            txtTelefone.setText((String) getIntent().getSerializableExtra("telefone"));
    }

    @Override
    public void iniciaControles() throws Exception {
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

        InputMaskHelper.aplicarMascara(txtTelefone, AppConstants.MASK_PHONE);
        InputMaskHelper.aplicarMascara(txtCpf, AppConstants.MASK_CPF);
        InputMaskHelper.aplicarMascara(txtDataNasc, AppConstants.MASK_DATE);

        btnCancelar.setOnClickListener(this);
        btnSalvarUsuario.setOnClickListener(this);

        carregaDados();
    }

    @Override
    public void carregaDados() throws Exception {
    }

    @Override
    public void onClick(View view) {
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
            txtNome.setError(getString(R.string.error_name_required));
            txtNome.requestFocus();
            return false;
        }
        else if ( sEmail.isEmpty())
        {
            txtEmail.setError(getString(R.string.error_email_required));
            txtEmail.requestFocus();
            return false;
        }
        else if (sCpf.isEmpty())
        {
            txtCpf.setError(getString(R.string.error_cpf_required));
            txtCpf.requestFocus();
            return false;
        }
        else if ( sCpf.length() < 11 )
        {
            txtCpf.setError(getString(R.string.error_cpf_invalid));
            txtCpf.requestFocus();
            return false;
        }
        else if ( sTelefone.isEmpty())
        {
            txtTelefone.setError(getString(R.string.error_phone_required));
            txtTelefone.requestFocus();
            return false;
        }
        else if ( sTelefone.length() < 11)
        {
            txtTelefone.setError(getString(R.string.error_phone_required));
            txtTelefone.requestFocus();
            return false;
        }
        else if ( sSenha.isEmpty())
        {
            txtSenha.setError(getString(R.string.error_password_required));
            txtSenha.requestFocus();
            return false;
        }
        else if ( !sSenha.equals(sSenhaConfirma))
        {
            txtConfirmarSenha.setError(getString(R.string.error_passwords_different));
            txtConfirmarSenha.requestFocus();
            return false;
        }
        else if ( sDataNasc.isEmpty())
        {
            txtDataNasc.setError(getString(R.string.error_birthdate_required));
            txtDataNasc.requestFocus();
            return false;
        }

        registerRequest = new RegisterRequest(sEmail,sNome,sSenha,sCpf,sTelefone,sDataNasc);
        return true;
    }

    private void realizarCadastroRetrofit() {

        if (!validaCampos())
        {
            return;
        }

        btnSalvarUsuario.setEnabled(false);
        btnSalvarUsuario.setText(R.string.register_registering);
        lnlAreaProgressBar.setVisibility(View.VISIBLE);

        registerRepository.register(registerRequest, new RegisterRepository.RegisterCallback() {
            @Override
            public void onSuccess(RegisterResponse response) {
                runOnUiThread(() -> {
                    btnSalvarUsuario.setEnabled(true);
                    btnSalvarUsuario.setText(R.string.register_save);
                    lnlAreaProgressBar.setVisibility(View.GONE);

                    NotificationHelper.showSuccess(
                        RegisterActivity.this,
                        getString(R.string.register_success)
                    );

                    // Log debug
                    Log.d("FrmCadastro", "Cadastro realizado com sucesso!");
                    Log.d("FrmCadastro", "Response: " + response);

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSalvarUsuario.setEnabled(true);
                    btnSalvarUsuario.setText(R.string.register_save);
                    lnlAreaProgressBar.setVisibility(View.GONE);

                    ErrorHelper.handleRegistrationError(
                        findViewById(android.R.id.content),
                        error
                    );

                    // Log debug
                    Log.e("FrmCadastro", "Erro no cadastro: " + error);
                });
            }
        });
    }
}