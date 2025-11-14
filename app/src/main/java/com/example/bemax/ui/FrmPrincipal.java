package com.example.bemax.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.bemax.R;
import com.example.bemax.model.User;
import com.example.bemax.util.BaseActivity;
import com.example.bemax.util.SecureStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

public class FrmPrincipal extends BaseActivity  implements NavigationView.OnNavigationItemSelectedListener {
    private SecureStorage secureStorage;
    private User currentUser;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_principal2);

        secureStorage = new SecureStorage(this);

        obtemParametros();
        iniciaControles();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);


        // tela inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FrmHome(this, currentUser))
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
            toolbarTitle.setText(getGreeting() + ", " + firstName);

            if (currentUser.isProfileCompleted()) {
                toolbarSubtitle.setText("Perfil completo ✓");
            } else {
                toolbarSubtitle.setText("Complete seu perfil");
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
            toolbarTitle.setText(getGreeting());
            toolbarSubtitle.setText("BeMax");
        }

        toolbarProfileContainer.setOnClickListener(v -> {
            // Navegar para tela de perfil/configurações
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FrmConfig(this))
                    .commit();

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_settings);
        });
    }

    @Override
    public void carregaDados() throws Exception {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Fragment selectedFragment = null;
        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new FrmHome(this, currentUser);
        } else if (item.getItemId() == R.id.nav_sos) {
            selectedFragment = new FrmAlerta(this);
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new FrmConfig(this);
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        return true;
    }
    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 12) {
            return getString(R.string.good_morning);
        } else if (hour >= 12 && hour < 18) {
            return "Boa tarde";
        } else {
            return "Boa noite";
        }
    }

    private String getFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Usuário";
        }

        String[] parts = fullName.trim().split(" ");
        return parts[0];
    }
}