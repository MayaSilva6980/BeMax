package com.example.bemax.telas;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.bemax.R;
import com.example.bemax.util.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FrmPrincipal extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frm_principal);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);


        // tela inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FrmHome())
                    .commit();
        }

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new FrmHome();
                } else if (item.getItemId() == R.id.nav_sos) {
                    selectedFragment = new FrmAlerta();
                } else if (item.getItemId() == R.id.nav_settings) {
                    selectedFragment = new FrmConfig();
                }


                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();

                return true;
            }
        });

    }

}