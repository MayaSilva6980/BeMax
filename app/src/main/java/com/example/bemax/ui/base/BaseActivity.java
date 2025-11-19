package com.example.bemax.ui.base;

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

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Initialize activity parameters and dependencies
     */
    public abstract void obtainParameters();

    /**
     * Initialize UI controls and set up listeners
     */
    public abstract void initializeControls() throws Exception;

    /**
     * Load initial data required by the activity
     */
    public abstract void loadData() throws Exception;

    public int getStatusBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

}
