package com.example.bemax.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bemax.R;
import com.example.bemax.repository.AuthRepository;
import com.example.bemax.ui.activity.ContactInfoActivity;
import com.example.bemax.ui.activity.MedicalInfoActivity;
import com.example.bemax.ui.activity.PersonalInfoActivity;
import com.example.bemax.ui.activity.LoginActivity;
import com.example.bemax.ui.activity.MainActivity;
import com.example.bemax.util.storage.SecureStorage;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class ConfigFragment extends Fragment implements View.OnClickListener {
    private MaterialCardView btnInfoPessoal = null;
    private MaterialCardView btnInfoMedica = null;
    private MaterialCardView btnEmergencySettings = null;
    private MaterialCardView btnContatoFamilia = null;
    private FloatingActionButton btnLogout = null;
    private AuthRepository authRepository;
    private SecureStorage secureStorage;

    MainActivity mainActivity = null;

    public ConfigFragment(MainActivity principal) {
        mainActivity = principal;
        authRepository = new AuthRepository();
        secureStorage = new SecureStorage(principal);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout do fragmento
        View view = inflater.inflate(R.layout.frm_config, container, false);

        iniciaControles(view);
        return view;
    }


    public void iniciaControles(View view) {
        btnInfoPessoal = view.findViewById(R.id.btnInfoPessoal);
        btnInfoMedica = view.findViewById(R.id.btnInfoMedica);
        btnEmergencySettings = view.findViewById(R.id.btnEmergencySettings);
        btnContatoFamilia = view.findViewById(R.id.btnContatoFamilia);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnInfoPessoal.setOnClickListener(this);
        btnInfoMedica.setOnClickListener(this);
        btnContatoFamilia.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnInfoPessoal) {
            startActivity(new Intent(mainActivity, PersonalInfoActivity.class));
        }
        else if (v.getId() == R.id.btnInfoMedica) {
            startActivity(new Intent(mainActivity, MedicalInfoActivity.class));
        }
        else if (v.getId() == R.id.btnContatoFamilia) {
            startActivity(new Intent(mainActivity, ContactInfoActivity.class));
        }
        else if (v.getId() == R.id.btnLogout) {
            showLogoutConfirmation();
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(mainActivity)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout_confirm, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.logout_cancel, null)
                .show();
    }

    private void performLogout() {
        btnLogout.setEnabled(false);

        String accessToken = secureStorage.getAccessToken();
        String refreshToken = secureStorage.getRefreshToken();

        if (accessToken != null && !accessToken.isEmpty() &&
                refreshToken != null && !refreshToken.isEmpty()) {
            authRepository.logout(accessToken, refreshToken, new AuthRepository.LogoutCallback() {
                @Override
                public void onSuccess() {
                    completeLogout();
                }
            });
        } else {
            completeLogout();
        }
    }

    private void completeLogout() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            secureStorage.clearTokens();

            FirebaseAuth.getInstance().signOut();

            // Ir para tela de login
            Intent intent = new Intent(mainActivity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(mainActivity, R.string.logout_success, Toast.LENGTH_SHORT).show();

            if (mainActivity != null) {
                mainActivity.finish();
            }
        });
    }
}
