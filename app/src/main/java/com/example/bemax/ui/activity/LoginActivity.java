package com.example.bemax.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bemax.R;
import com.example.bemax.model.dto.LoginResponse;
import com.example.bemax.repository.AuthRepository;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.security.SecureBiometricManager;
import com.example.bemax.util.storage.SecureStorage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    
    // Controles do Layout
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextSenha;
    private Button btnContinue;
    private LinearLayout btnGoogle;
    private TextView txtSignup;
    private ProgressBar progressBar;

    // Variáveis da classe
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private AuthRepository authRepository;
    
    // Segurança com Biometria
    private SecureBiometricManager biometricManager;
    private SecureStorage secureStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_login);

        authRepository = new AuthRepository();
        secureStorage = new SecureStorage(this);
        secureStorage.setBiometricManager(this); // Configurar biometria
        biometricManager = new SecureBiometricManager(this);
        
        iniciaControles();
        
        // Verificar se há login salvo com biometria
        checkSavedLogin();
    }

    @Override
    public void obtemParametros() {
    }

    @Override
    public void iniciaControles() {
        editTextEmail = findViewById(R.id.textInputEditTextLoginEmail);
        editTextSenha = findViewById(R.id.textInputEditTextLoginSenha);
        btnContinue = findViewById(R.id.btnContinue);
        btnGoogle = findViewById(R.id.btnGoogle);
        progressBar = findViewById(R.id.progressBar);
        txtSignup = findViewById(R.id.txtSignup);

        btnGoogle.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        txtSignup.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        // Configura opções de login Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void carregaDados() throws Exception {
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnContinue) {
            realizarLoginRetrofit();
        }
        else if (view.getId() == R.id.btnGoogle) {
            signIn();
        }
        else if (view.getId() == R.id.txtSignup) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    /**
     * Verifica se há login salvo e oferece biometria
     */
    private void checkSavedLogin() {
        if (secureStorage.isBiometricEnabled() && secureStorage.hasValidToken()) {
            String savedEmail = secureStorage.getUserEmail();
            
            if (savedEmail != null) {
                editTextEmail.setText(savedEmail);

                Toast.makeText(this,
                        R.string.auth_biometric_quick_login,
                        Toast.LENGTH_LONG).show();
                
                editTextEmail.postDelayed(this::authenticateWithBiometric, 500);
            }
        }
    }

    /**
     * Autentica usando biometria para recuperar token
     */
    private void authenticateWithBiometric() {
        progressBar.setVisibility(View.VISIBLE);

        // Recuperar token com biometria
        secureStorage.getAccessToken(new SecureStorage.TokenCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            R.string.biometric_success,
                            Toast.LENGTH_SHORT).show();

                    // Verificar se token expirou ou está expirando
                    if (secureStorage.isTokenExpired() || secureStorage.isTokenExpiringSoon()) {
                        // Tentar renovar com refresh token
                        refreshTokenWithBiometric();
                    } else {
                        // Token válido, pode entrar direto
                        progressBar.setVisibility(View.GONE);
                        goToPrincipal();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (error.contains("cancelada")) {
                        // Usuário cancelou
                        editTextSenha.requestFocus();
                    } else {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Renova o token usando refresh token (com biometria)
     */
    private void refreshTokenWithBiometric() {
        secureStorage.getRefreshToken(new SecureStorage.TokenCallback() {
            @Override
            public void onTokenRetrieved(String refreshToken) {
                secureStorage.getAccessToken(new SecureStorage.TokenCallback() {
                    @Override
                    public void onTokenRetrieved(String oldAccessToken) {
                        // Chamar API de refresh
                        authRepository.refreshToken(oldAccessToken, refreshToken, new AuthRepository.AuthCallback() {
                            @Override
                            public void onSuccess(LoginResponse response) {
                                runOnUiThread(() -> {
                                    // Salvar novos tokens
                                    saveTokensWithBiometric(response, () -> {
                                        progressBar.setVisibility(View.GONE);
                                        goToPrincipal();
                                    });
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    secureStorage.clearTokens();
                                    Toast.makeText(LoginActivity.this,
                                            "Sessão expirada. Faça login novamente.",
                                            Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    secureStorage.clearTokens();
                    Toast.makeText(LoginActivity.this,
                            "Sessão expirada. Faça login novamente.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void realizarLoginRetrofit() {
        String email = editTextEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError(getString(R.string.error_email_required));
            editTextEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            editTextSenha.setError(getString(R.string.error_password_required));
            editTextSenha.requestFocus();
            return;
        }

        if (senha.length() < 6) {
            editTextSenha.setError(getString(R.string.error_password_min_length));
            editTextSenha.requestFocus();
            return;
        }

        btnContinue.setEnabled(false);
        btnContinue.setText(R.string.auth_logging_in);
        progressBar.setVisibility(View.VISIBLE);

        authRepository.login(email, senha, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            R.string.auth_login_success,
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Login realizado com sucesso!");

                    // Salvar email e expiração
                    secureStorage.saveUserEmail(email);
                    long expirationTimeMs = System.currentTimeMillis() + (response.getExpiresIn() * 1000);
                    secureStorage.saveTokenExpiration(expirationTimeMs);

                    // Salvar tokens com biometria
                    saveTokensWithBiometric(response, () -> {
                        btnContinue.setEnabled(true);
                        btnContinue.setText(R.string.auth_continue);
                        progressBar.setVisibility(View.GONE);

                        // Perguntar se quer ativar biometria
                        if (!secureStorage.isBiometricEnabled() && 
                            biometricManager.checkBiometricAvailability() == SecureBiometricManager.BiometricStatus.AVAILABLE) {
                            askToEnableBiometric(response);
                        } else {
                            goToPrincipal();
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnContinue.setEnabled(true);
                    btnContinue.setText(R.string.auth_continue);
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(LoginActivity.this,
                            getString(R.string.auth_login_error, error),
                            Toast.LENGTH_LONG).show();

                    Log.e(TAG, "Erro no login: " + error);
                });
            }
        });
    }

    /**
     * Salva tokens com ou sem biometria
     */
    private void saveTokensWithBiometric(LoginResponse response, Runnable onComplete) {
        Log.d(TAG, "Salvando AMBOS os tokens com UMA ÚNICA biometria...");
        
        // NOVO: Usa saveTokens() que solicita biometria apenas 1x
        secureStorage.saveTokens(
            response.getAccessToken(),
            response.getRefreshToken(),
            new SecureStorage.TokenCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Ambos os tokens salvos com sucesso!");
                    runOnUiThread(() -> {
                        onComplete.run();
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Erro ao salvar tokens: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, 
                            "Erro ao salvar sessão: " + error, 
                            Toast.LENGTH_LONG).show();
                        
                        // Re-habilitar botões para tentar novamente
                        btnContinue.setEnabled(true);
                        btnGoogle.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    });
                }
            }
        );
    }

    private void askToEnableBiometric(LoginResponse response) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.biometric_enable_title)
                .setMessage(R.string.biometric_enable_message)
                .setPositiveButton(R.string.biometric_enable_positive, (dialog, which) -> {
                    secureStorage.setBiometricEnabled(true);
                    
                    Log.d(TAG, "Biometria ativada, re-criptografando tokens...");
                    
                    // Re-salvar tokens COM criptografia biométrica
                    // Isso é NECESSÁRIO porque tokens foram salvos SEM biometria
                    saveTokensWithBiometric(response, () -> {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Tokens re-criptografados com sucesso");
                            Toast.makeText(this, R.string.auth_biometric_activated, Toast.LENGTH_SHORT).show();
                            goToPrincipal();
                        });
                    });
                })
                .setNegativeButton(R.string.biometric_enable_negative, (dialog, which) -> {
                    // Usuário não quer biometria, vai para principal
                    goToPrincipal();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Navega para MainActivity - dados do usuário serão buscados via /me
     */
    private void goToPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("SKIP_BIOMETRIC", true); // Flag: acabou de fazer login
        startActivity(intent);
        finish();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google SignIn successful: " + account.getEmail());

                progressBar.setVisibility(View.VISIBLE);
                btnGoogle.setEnabled(false);

                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                Toast.makeText(this, getString(R.string.auth_google_signin_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro no Google SignIn", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String googleIdToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Log.d(TAG, "Firebase auth successful");

                user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                    if (tokenTask.isSuccessful()) {
                        String firebaseIdToken = tokenTask.getResult().getToken();
                        Log.d(TAG, "Firebase ID Token obtido com sucesso");
                        loginWithFirebaseToken(firebaseIdToken, user);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnGoogle.setEnabled(true);
                        Toast.makeText(this, R.string.auth_firebase_token_error, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao obter Firebase token", tokenTask.getException());
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                btnGoogle.setEnabled(true);
                Toast.makeText(this, R.string.auth_firebase_auth_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro no Firebase Auth", task.getException());
            }
        });
    }

    private void loginWithFirebaseToken(String firebaseToken, FirebaseUser firebaseUser) {
        authRepository.loginWithFirebase(firebaseToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            R.string.auth_google_login_success,
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Backend login successful");

                    // Salvar email e foto
                    secureStorage.saveUserEmail(firebaseUser.getEmail());
                    if (firebaseUser.getPhotoUrl() != null) {
                        secureStorage.saveUserPhotoUrl(firebaseUser.getPhotoUrl().toString());
                    }
                    
                    long expirationTimeMs = System.currentTimeMillis() + (response.getExpiresIn() * 1000);
                    secureStorage.saveTokenExpiration(expirationTimeMs);

                    // Salvar tokens com biometria
                    saveTokensWithBiometric(response, () -> {
                        progressBar.setVisibility(View.GONE);
                        btnGoogle.setEnabled(true);

                        // Perguntar sobre biometria
                        if (!secureStorage.isBiometricEnabled() && 
                            biometricManager.checkBiometricAvailability() == SecureBiometricManager.BiometricStatus.AVAILABLE) {
                            askToEnableBiometric(response);
                        } else {
                            goToPrincipal();
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGoogle.setEnabled(true);

                    if (error.contains("404") || error.contains("not registered")) {
                        Toast.makeText(LoginActivity.this,
                                R.string.auth_complete_registration,
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        intent.putExtra("nome", firebaseUser.getDisplayName());
                        intent.putExtra("email", firebaseUser.getEmail());
                        intent.putExtra("firebase_token", firebaseToken);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.auth_login_error, error),
                                Toast.LENGTH_LONG).show();

                        Log.e(TAG, "Erro no login com Firebase: " + error);
                    }
                });
            }
        });
    }
}

