package com.example.bemax.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bemax.R;

public abstract class BaseActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            // Se estiver usando Edge-to-Edge, configura aqui
            View root = findViewById(R.id.root_layout);
            if (root != null) {
                ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) ->
                {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, getStatusBarHeight(this), systemBars.right, systemBars.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });
            }

            obtemParametros();

            iniciaControles();

            carregaDados();
        }
        catch (Exception err)
        {
            //TODO: fazer um alert
        }
    }


    public abstract  void obtemParametros() throws Exception;

    public abstract  void iniciaControles() throws Exception;
    public abstract  void carregaDados() throws Exception;

    public int getStatusBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

}
