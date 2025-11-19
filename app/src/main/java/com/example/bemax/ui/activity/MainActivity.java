package com.example.bemax.ui.activity;

import static com.example.bemax.util.helper.StringHelper.getFirstName;
import static com.example.bemax.util.helper.StringHelper.getGreeting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

public class MainActivity extends BaseActivity  implements NavigationView.OnNavigationItemSelectedListener {
    private SecureStorage secureStorage;
    private User currentUser;
    private String accessToken;
    private UserRepository userRepository;
    private MeResponse meData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_principal2);

        secureStorage = new SecureStorage(this);
        userRepository = new UserRepository();

        obtemParametros();
        loadUserData();
        iniciaControles();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // tela inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(this, currentUser, meData))
                    .commit();
        }

        bottomNav.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    @Override
    public void obtemParametros() {
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("user");

        if (currentUser == null) {
            // Tentar recuperar do SecureStorage
            String userJson = secureStorage.getUserData();
            if (userJson != null) {
                Gson gson = new Gson();
                currentUser = gson.fromJson(userJson, User.class);
            }
        }

        if (intent.hasExtra("access_token")) {
            accessToken = intent.getStringExtra("access_token");
        }

        // Fallback: tenta obter do SecureStorage
        if (accessToken == null) {
            accessToken = secureStorage.getAccessToken();
        }
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

    private void loadUserData() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userRepository.getMe(accessToken, new UserRepository.MeCallback() {
                @Override
                public void onSuccess(MeResponse response) {
                    meData = response;

                    // Atualizar o currentUser com os dados mais recentes
                    if (response.getUser() != null) {
                        currentUser = response.getUser();

                        // Salvar dados atualizados no storage
                        Gson gson = new Gson();
                        secureStorage.saveUserData(gson.toJson(currentUser));
                    }

                    // Atualizar a UI se necessário
                    runOnUiThread(() -> {
                        iniciaControles();
                        updateActiveFragment();
                    });

                    Log.d("MainActivity", "Dados do usuário carregados com sucesso");
                }

                @Override
                public void onError(String error) {
                    Log.e("MainActivity", "Erro ao carregar dados: " + error);
                    Toast.makeText(MainActivity.this,
                            "Erro ao carregar dados do usuário",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateActiveFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof HomeFragment) {
            // Recriar o HomeFragment com dados atualizados
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(this, currentUser, meData))
                    .commit();
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