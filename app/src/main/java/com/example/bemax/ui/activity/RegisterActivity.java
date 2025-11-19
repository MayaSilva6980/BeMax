package com.example.bemax.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.FrameLayout;

import com.example.bemax.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.bemax.model.dto.RegisterRequest;
import com.example.bemax.model.dto.RegisterResponse;
import com.example.bemax.repository.RegisterRepository;
import com.example.bemax.util.helper.InputMaskHelper;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.AppConstants;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";
    
    // UI Components
    private MaterialToolbar toolbar;
    private TextInputLayout layoutNome;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutCpf;
    private TextInputLayout layoutTelefone;
    private TextInputLayout layoutDataNasc;
    private TextInputLayout layoutSenha;
    private TextInputLayout layoutConfirmarSenha;
    
    private TextInputEditText txtNome;
    private TextInputEditText txtEmail;
    private TextInputEditText txtSenha;
    private TextInputEditText txtConfirmarSenha;
    private TextInputEditText txtTelefone;
    private TextInputEditText txtCpf;
    private TextInputEditText txtDataNasc;
    
    private MaterialButton btnSalvarUsuario;
    private MaterialButton btnCancelar;
    private FrameLayout lnlAreaProgressBar;

    // Repository
    private RegisterRepository registerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_cadastro);

        registerRepository = new RegisterRepository(this);
        initializeControls();
        obtainParameters();
    }

    @Override
    public void obtainParameters() {
        // Preenche os campos com dados vindos da Intent (se houver)
        if (getIntent().hasExtra("email")) {
            txtEmail.setText(getIntent().getStringExtra("email"));
        }
        if (getIntent().hasExtra("nome")) {
            txtNome.setText(getIntent().getStringExtra("nome"));
        }
        if (getIntent().hasExtra("telefone")) {
            txtTelefone.setText(getIntent().getStringExtra("telefone"));
        }
    }

    @Override
    public void initializeControls() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.user_registration_title);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // TextInputLayouts
        layoutNome = findViewById(R.id.layoutNome);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutCpf = findViewById(R.id.textInputLayoutInfoCpf);
        layoutTelefone = findViewById(R.id.layoutTelefone);
        layoutDataNasc = findViewById(R.id.layoutInfoNascimento);
        layoutSenha = findViewById(R.id.layoutSenha);
        layoutConfirmarSenha = findViewById(R.id.layoutConfirmarSenha);
        
        // EditTexts
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);
        txtConfirmarSenha = findViewById(R.id.txtConfirmarSenha);
        txtTelefone = findViewById(R.id.txtTelefone);
        txtCpf = findViewById(R.id.txtCpf);
        txtDataNasc = findViewById(R.id.txtDataNasc);
        
        // Buttons
        btnSalvarUsuario = findViewById(R.id.btnSalvarUsuario);
        btnCancelar = findViewById(R.id.btnCancelar);
        lnlAreaProgressBar = findViewById(R.id.lnlAreaProgressBar);

        // Apply input masks
        InputMaskHelper.aplicarMascara(txtTelefone, AppConstants.MASK_PHONE);
        InputMaskHelper.aplicarMascara(txtCpf, AppConstants.MASK_CPF);
        InputMaskHelper.aplicarMascara(txtDataNasc, AppConstants.MASK_DATE);

        // Set click listeners
        btnCancelar.setOnClickListener(v -> finish());
        btnSalvarUsuario.setOnClickListener(v -> performRegistration());
    }

    @Override
    public void loadData() {
        // Not needed
    }

    private boolean validateFields() {
        // Clear previous errors
        layoutNome.setError(null);
        layoutEmail.setError(null);
        layoutCpf.setError(null);
        layoutTelefone.setError(null);
        layoutDataNasc.setError(null);
        layoutSenha.setError(null);
        layoutConfirmarSenha.setError(null);
        
        String nome = txtNome.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String cpf = txtCpf.getText().toString().trim();
        String telefone = txtTelefone.getText().toString().trim();
        String senha = txtSenha.getText().toString();
        String senhaConfirma = txtConfirmarSenha.getText().toString();
        String dataNasc = txtDataNasc.getText().toString().trim();

        // Validate Nome
        if (nome.isEmpty()) {
            layoutNome.setError(getString(R.string.error_name_required));
            txtNome.requestFocus();
            return false;
        }
        
        // Validate Email
        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_email_required));
            txtEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_email_invalid));
            txtEmail.requestFocus();
            return false;
        }
        
        // Validate CPF
        if (cpf.isEmpty()) {
            layoutCpf.setError(getString(R.string.error_cpf_required));
            txtCpf.requestFocus();
            return false;
        }
        String cpfClean = cpf.replaceAll("[^0-9]", "");
        if (cpfClean.length() != 11) {
            layoutCpf.setError(getString(R.string.error_cpf_invalid));
            txtCpf.requestFocus();
            return false;
        }
        
        // Validate Data Nascimento
        if (dataNasc.isEmpty()) {
            layoutDataNasc.setError(getString(R.string.error_birthdate_required));
            txtDataNasc.requestFocus();
            return false;
        }
        if (dataNasc.length() != 10) {
            layoutDataNasc.setError(getString(R.string.error_birthdate_invalid));
            txtDataNasc.requestFocus();
            return false;
        }
        
        // Validate Telefone
        if (telefone.isEmpty()) {
            layoutTelefone.setError(getString(R.string.error_phone_required));
            txtTelefone.requestFocus();
            return false;
        }
        // Validate phone length (with mask: (##) #####-#### = 15 chars)
        String telefoneClean = telefone.replaceAll("[^0-9]", "");
        if (telefoneClean.length() < 10 || telefoneClean.length() > 11) {
            layoutTelefone.setError(getString(R.string.error_phone_invalid));
            txtTelefone.requestFocus();
            return false;
        }
        
        // Validate Senha
        if (senha.isEmpty()) {
            layoutSenha.setError(getString(R.string.error_password_required));
            txtSenha.requestFocus();
            return false;
        }
        if (senha.length() < 6) {
            layoutSenha.setError(getString(R.string.error_password_too_short));
            txtSenha.requestFocus();
            return false;
        }
        
        // Validate Confirmar Senha
        if (senhaConfirma.isEmpty()) {
            layoutConfirmarSenha.setError(getString(R.string.error_confirm_password_required));
            txtConfirmarSenha.requestFocus();
            return false;
        }
        if (!senha.equals(senhaConfirma)) {
            layoutConfirmarSenha.setError(getString(R.string.error_passwords_different));
            txtConfirmarSenha.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegistration() {
        if (!validateFields()) {
            Log.e(TAG, "Validation failed!");
            return;
        }

        // Get data (keeping masks for phone and date)
        String email = txtEmail.getText().toString().trim();
        String nome = txtNome.getText().toString().trim();
        String senha = txtSenha.getText().toString();
        String cpfClean = txtCpf.getText().toString().trim().replaceAll("[^0-9]", "");
        String telefone = txtTelefone.getText().toString().trim(); // WITH mask
        String dataNasc = txtDataNasc.getText().toString().trim(); // WITH mask

        // Debug log
        Log.d(TAG, "=== VALIDATION SUCCESS ===");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Nome: " + nome);
        Log.d(TAG, "CPF (cleaned): " + cpfClean);
        Log.d(TAG, "Telefone (with mask): " + telefone);
        Log.d(TAG, "Data Nascimento (with mask): " + dataNasc);

        // Create request
        RegisterRequest request = new RegisterRequest(
            email,
            nome,
            senha,
            cpfClean,    // CPF without mask
            telefone,    // Phone WITH mask (##) #####-####
            dataNasc     // Date WITH mask DD/MM/YYYY
        );

        // Debug request
        Log.d(TAG, "=== SENDING REQUEST ===");
        Log.d(TAG, "Request Email: " + request.getEmail());
        Log.d(TAG, "Request FullName: " + request.getFullName());
        Log.d(TAG, "Request CPF: " + request.getCpf());
        Log.d(TAG, "Request Phone: " + request.getPhone());
        Log.d(TAG, "Request DateBirth: " + request.getDateBirth());

        // Show loading
        btnSalvarUsuario.setEnabled(false);
        btnSalvarUsuario.setText(R.string.auth_registering);
        lnlAreaProgressBar.setVisibility(View.VISIBLE);

        // Call API
        registerRepository.register(request, new RegisterRepository.RegisterCallback() {
            @Override
            public void onSuccess(RegisterResponse response) {
                runOnUiThread(() -> {
                    btnSalvarUsuario.setEnabled(true);
                    btnSalvarUsuario.setText(R.string.create_account);
                    lnlAreaProgressBar.setVisibility(View.GONE);

                    NotificationHelper.showSuccess(
                        RegisterActivity.this,
                        getString(R.string.auth_register_success)
                    );

                    Log.d(TAG, "Registration successful!");

                    // Navigate to main activity
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSalvarUsuario.setEnabled(true);
                    btnSalvarUsuario.setText(R.string.create_account);
                    lnlAreaProgressBar.setVisibility(View.GONE);

                    NotificationHelper.showError(
                        RegisterActivity.this,
                        getString(R.string.auth_register_error, error)
                    );

                    Log.e(TAG, "Registration error: " + error);
                });
            }
        });
    }
}
