package com.example.bemax.telas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bemax.R;

public class FrmConfig extends Fragment
{

    FrmPrincipal frmPrincipal = null;

    public FrmConfig(FrmPrincipal principal)
    {
        frmPrincipal = principal;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Infla o layout do fragmento
        View view = inflater.inflate(R.layout.frm_config, container, false);

        iniciaControles(view);
        return view;
    }


    public void iniciaControles(View view) {
    }

}
