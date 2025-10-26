package com.example.bemax.telas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bemax.R;
import com.example.bemax.model.dto.LoginResponse;
import com.example.bemax.repository.AuthRepository;
import com.example.bemax.util.BaseActivity;
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

public class FrmLogin extends BaseActivity implements View.OnClickListener {
    // Controles do Layout
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextSenha;
    private Button btnContinue;
    private LinearLayout btnGoogle;
    private TextView txtSignup;
    private ProgressBar progressBar;


    // variaveis da classe

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_login);

        authRepository = new AuthRepository();
        iniciaControles();
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
                .requestIdToken(getString(R.string.default_web_client_id)) // vem do google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public void carregaDados() throws Exception
    {

    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnContinue) {
            realizarLoginRetrofit();
        }
        else if (view.getId() == R.id.btnGoogle) {
            signIn();
        }
        else if (view.getId() == R.id.txtSignup){

            startActivity(new Intent(this, FrmCadastro.class));
        }

    }

    private void realizarLoginRetrofit() {
        String email = editTextEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email é obrigatório");
            editTextEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            editTextSenha.setError("Senha é obrigatória");
            editTextSenha.requestFocus();
            return;
        }

        if (senha.length() < 6) {
            editTextSenha.setError("Senha deve ter no mínimo 6 caracteres");
            editTextSenha.requestFocus();
            return;
        }

        btnContinue.setEnabled(false);
        btnContinue.setText("Entrando...");
        progressBar.setVisibility(View.VISIBLE);

        authRepository.login(email, senha, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                runOnUiThread(() -> {
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Continue");
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(FrmLogin.this,
                            "Login realizado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    // Log debug
                    Log.d("FrmLogin", "Login realizado com sucesso!");
                    Log.d("FrmLogin", "Access Token: " + response.getAccessToken().substring(0, Math.min(20, response.getAccessToken().length())) + "...");
                    Log.d("FrmLogin", "Refresh Token: " + response.getRefreshToken());
                    Log.d("FrmLogin", "Expires In: " + response.getExpiresIn());

                    Intent intent = new Intent(FrmLogin.this, FrmPrincipal.class);
                    intent.putExtra("user_email", email);
                    intent.putExtra("access_token", response.getAccessToken());
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Continue");
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(FrmLogin.this,
                            "Erro ao fazer login: " + error,
                            Toast.LENGTH_LONG).show();

                    // Log debug
                    Log.e("FrmLogin", "Erro no login: " + error);
                });
            }
        });
    }
    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                Toast.makeText(this, "Falha no Google SignIn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FrmLogin", "Erro no Google SignIn", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(this, "Logado: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                // redireciona para tela principal
                Intent intent = new Intent(FrmLogin.this, FrmCadastro.class);
                intent.putExtra("nome", user.getDisplayName());
                intent.putExtra("email", user.getEmail());
                intent.putExtra("telefone", user.getPhoneNumber());
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(this, "Erro ao autenticar no Firebase.", Toast.LENGTH_SHORT).show();
                Log.e("FrmLogin", "Erro no Firebase Auth", task.getException());
            }
        });
    }

}
