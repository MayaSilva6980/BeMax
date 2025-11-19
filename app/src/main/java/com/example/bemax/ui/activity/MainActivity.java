package com.example.bemax.ui.activity;

import static com.example.bemax.util.helper.StringHelper.getFirstName;
import static com.example.bemax.util.helper.StringHelper.getGreeting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.bemax.R;
import com.example.bemax.model.dto.MeResponse;
import com.example.bemax.repository.UserRepository;
import com.example.bemax.ui.fragments.AlertFragment;
import com.example.bemax.ui.fragments.ConfigFragment;
import com.example.bemax.ui.fragments.HomeFragment;
import com.example.bemax.model.domain.User;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.storage.SecureStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    private SecureStorage secureStorage;
    private User currentUser;
    private String accessToken;
    private UserRepository userRepository;
    private MeResponse meData;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_principal2);

        loadingIndicator = findViewById(R.id.loading_indicator);
        
        secureStorage = new SecureStorage(this);
        secureStorage.setBiometricManager(this); // Configurar biometria
        userRepository = new UserRepository();

        // SEMPRE inicializar BottomNavigation primeiro
        setupBottomNavigation();

        // Verificar se acabou de fazer login (não precisa biometria)
        boolean skipBiometric = getIntent().getBooleanExtra("SKIP_BIOMETRIC", false);
        
        if (skipBiometric) {
            Log.d(TAG, "✅ Login recente - pulando biometria");
            
            // Tentar carregar do cache primeiro (instantâneo)
            loadUserDataFromCache();
            
            // Obter token SEM pedir biometria (acabou de salvar)
            obtainTokenWithoutBiometric();
        } else {
            Log.d(TAG, "🔐 Reabertura do app - pode requerer biometria");
            
            // Tentar carregar do cache primeiro (instantâneo)
            loadUserDataFromCache();

            // Obter token (pode requerer biometria)
            obtainTokenAndLoadData();
        }
    }

    /**
     * Carrega dados do cache local primeiro (instantâneo)
     */
    private void loadUserDataFromCache() {
        String userJson = secureStorage.getUserData();
        if (userJson != null && !userJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                currentUser = gson.fromJson(userJson, User.class);
                
                // Aplicar foto do Google se existir no storage
                String savedPhotoUrl = secureStorage.getUserPhotoUrl();
                if (savedPhotoUrl != null && !savedPhotoUrl.isEmpty()) {
                    currentUser.setPhotoUrl(savedPhotoUrl);
                }
                
                Log.d(TAG, "Dados do cache carregados: " + currentUser.getFullName());
                
                // Inicializar UI com dados do cache
                iniciaControles();
                
                // Carregar HomeFragment com dados do cache
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment(this, currentUser, meData))
                        .commit();
                        
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar cache: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Nenhum cache disponível");
        }
    }

    /**
     * Obtém token com biometria (se ativada) e carrega dados
     */
    private void obtainTokenAndLoadData() {
        showLoading(true);
        
        if (secureStorage.isBiometricEnabled()) {
            // Requer biometria para acessar token
            Log.d(TAG, "Biometria ativada, solicitando autenticação...");
            
            secureStorage.getAccessToken(new SecureStorage.TokenCallback() {
                @Override
                public void onTokenRetrieved(String token) {
                    runOnUiThread(() -> {
                        accessToken = token;
                        Log.d(TAG, "Token recuperado com biometria com sucesso");
                        loadUserDataFromBackend();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Erro ao recuperar token: " + error);
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    });
                }
            });
        } else {
            // Sem biometria, busca token diretamente
            secureStorage.getAccessToken(new SecureStorage.TokenCallback() {
                @Override
                public void onTokenRetrieved(String token) {
                    accessToken = token;
                    
                    if (accessToken == null || accessToken.isEmpty()) {
                        Log.w(TAG, "Token não disponível");
                        showLoading(false);
                        redirectToLogin();
                        return;
                    }
                    
                    loadUserDataFromBackend();
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    Log.e(TAG, "Erro ao obter token: " + error);
                    redirectToLogin();
                }
            });
        }
    }

    /**
     * Obtém token SEM pedir biometria (usado após login recente)
     */
    private void obtainTokenWithoutBiometric() {
        showLoading(true);
        
        // Busca token diretamente (SEM biometria, mesmo se estiver ativada)
        secureStorage.getAccessToken(true, new SecureStorage.TokenCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                accessToken = token;
                
                if (accessToken == null || accessToken.isEmpty()) {
                    Log.w(TAG, "Token não disponível após login");
                    showLoading(false);
                    redirectToLogin();
                    return;
                }
                
                Log.d(TAG, "✅ Token obtido sem biometria (login recente)");
                loadUserDataFromBackend();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e(TAG, "Erro ao obter token após login: " + error);
                redirectToLogin();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void obtemParametros() {
        // Não usado - token obtido via biometria em obtainTokenAndLoadData()
    }

    @Override
    public void iniciaControles() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        TextView toolbarSubtitle = findViewById(R.id.toolbar_subtitle);
        ImageView toolbarProfile = findViewById(R.id.toolbar_profile);
        View toolbarProfileContainer = findViewById(R.id.toolbar_profile_container);

        setSupportActionBar(toolbar);

        // Configura o cabeçalho com dados do usuário
        if (currentUser != null) {
            String firstName = getFirstName(currentUser.getFullName());
            toolbarTitle.setText(getGreeting(this) + ", " + firstName);

            if (currentUser.isProfileCompleted()) {
                toolbarSubtitle.setText(R.string.profile_complete_check);
            } else {
                toolbarSubtitle.setText(R.string.profile_incomplete);
            }
            
            // Carregar foto do usuário se existir
            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(toolbarProfile);
            }
        } else {
            toolbarTitle.setText(getGreeting(this));
            toolbarSubtitle.setText(R.string.profile_toolbar_title);
        }

        toolbarProfileContainer.setOnClickListener(v -> {
            // Navegar para tela de perfil/configurações
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ConfigFragment(this))
                    .commit();

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_settings);
        });
    }

    @Override
    public void carregaDados() throws Exception {
    }

    /**
     * Carrega dados do backend via /me
     */
    private void loadUserDataFromBackend() {
        if (accessToken != null && !accessToken.isEmpty()) {
            Log.d(TAG, "Carregando dados do backend via /me...");
            
            userRepository.getMe(accessToken, new UserRepository.MeCallback() {
                @Override
                public void onSuccess(MeResponse response) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        meData = response;

                        // Atualizar o currentUser com os dados mais recentes
                        if (response.getUser() != null) {
                            currentUser = response.getUser();
                            
                            // Aplicar foto do Google se existir no storage
                            String savedPhotoUrl = secureStorage.getUserPhotoUrl();
                            if (savedPhotoUrl != null && !savedPhotoUrl.isEmpty()) {
                                currentUser.setPhotoUrl(savedPhotoUrl);
                            }

                            // Salvar dados atualizados no storage
                            Gson gson = new Gson();
                            secureStorage.saveUserData(gson.toJson(currentUser), new SecureStorage.TokenCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Cache atualizado com sucesso");
                                }

                                @Override
                                public void onError(String error) {
                                    Log.w(TAG, "Erro ao salvar cache: " + error);
                                }
                            });
                            
                            Log.d(TAG, "Dados do backend carregados com sucesso");
                            Log.d(TAG, "  Nome: " + currentUser.getFullName());
                            Log.d(TAG, "  Email: " + currentUser.getEmail());
                            if (meData.getReminders() != null) {
                                Log.d(TAG, "  Lembretes: " + meData.getReminders().size());
                            }
                        }

                        // Atualizar a UI
                        iniciaControles();
                        updateActiveFragment();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Erro ao carregar dados do backend: " + error);
                        
                        // Se já tem dados do cache, continuar usando
                        if (currentUser != null) {
                            Log.d(TAG, "Continuando com dados do cache");
                            Toast.makeText(MainActivity.this,
                                getString(R.string.error_loading_user_data) + " (usando cache)", 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            // Sem cache, mostrar erro
                            Toast.makeText(MainActivity.this,
                                getString(R.string.error_loading_user_data), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            showLoading(false);
            Log.w(TAG, "AccessToken não disponível, redirecionando para login");
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateActiveFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof HomeFragment) {
            // Atualizar dados do fragment existente (sem recriar)
            ((HomeFragment) currentFragment).updateData(currentUser, meData);
            Log.d(TAG, "HomeFragment atualizado dinamicamente");
        } else if (currentFragment == null) {
            // Criar novo HomeFragment se não existir
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(this, currentUser, meData))
                    .commit();
            Log.d(TAG, "HomeFragment criado com novos dados");
        }
    }

    public MeResponse getMeData() {
        return meData;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new HomeFragment(this, currentUser, meData);
        } else if (item.getItemId() == R.id.nav_sos) {
            selectedFragment = new AlertFragment(this);
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new ConfigFragment(this);
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        return true;
    }
}
