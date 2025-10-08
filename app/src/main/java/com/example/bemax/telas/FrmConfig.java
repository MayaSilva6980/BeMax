package com.example.bemax.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bemax.R;
import com.google.android.material.card.MaterialCardView;

public class FrmConfig extends Fragment implements View.OnClickListener
{
    private MaterialCardView btnPersonalInfo = null;
    private MaterialCardView btnMedicalInfo = null;
    private MaterialCardView btnEmergencySettings = null;
    private MaterialCardView btnFamilyContacts = null;

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


    public void iniciaControles(View view)
    {
        btnPersonalInfo = view.findViewById(R.id.btnPersonalInfo);
        btnMedicalInfo = view.findViewById(R.id.btnMedicalInfo);
        btnEmergencySettings = view.findViewById(R.id.btnEmergencySettings);
        btnFamilyContacts = view.findViewById(R.id.btnFamilyContacts);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnPersonalInfo)
        {
            startActivity(new Intent(frmPrincipal, FrmInfoPessoal.class));
        }
        else if (v.getId() == R.id.btnMedicalInfo)
        {

        }
        else if (v.getId() == R.id.btnEmergencySettings)
        {

        }
        else if (v.getId() == R.id.btnFamilyContacts)
        {

        }

    }
}
