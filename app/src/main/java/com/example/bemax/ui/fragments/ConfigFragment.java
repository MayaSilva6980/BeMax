package com.example.bemax.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.bemax.R;
import com.example.bemax.model.domain.User;
import com.example.bemax.repository.AuthRepository;
import com.example.bemax.ui.activity.ContactInfoActivity;
import com.example.bemax.ui.activity.MedicalInfoActivity;
import com.example.bemax.ui.activity.PersonalInfoActivity;
import com.example.bemax.ui.activity.LoginActivity;
import com.example.bemax.ui.activity.MainActivity;
import com.example.bemax.ui.activity.PrivacySecurityActivity;
import com.example.bemax.util.storage.SecureStorage;
import com.example.bemax.util.helper.StringHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class ConfigFragment extends Fragment implements View.OnClickListener {

    // Views do Perfil
    private ImageView imgUserPhoto;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private ProgressBar progressProfileCompletion;
    private TextView txtProfileCompletion;
    private TextView txtBiometricStatus;
    private TextView txtAppVersion;

    // Cards de Saúde
    private MaterialCardView btnInfoPessoal;
    private MaterialCardView btnInfoMedica;
    private MaterialCardView btnContatoFamilia;

    // Cards de Preferências
    private MaterialCardView btnNotifications;
    private MaterialCardView btnPrivacy;

    // Cards de Sobre
    private MaterialCardView btnTerms;
    private MaterialCardView btnHelp;

    // Botão de Logout
    private MaterialButton btnLogout;

    private AuthRepository authRepository;
    private SecureStorage secureStorage;
    private MainActivity mainActivity;
    private User currentUser;

    public ConfigFragment(MainActivity principal) {
        mainActivity = principal;
        authRepository = new AuthRepository();
        secureStorage = new SecureStorage(principal);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_config2, container, false);

        iniciaControles(view);
        loadUserData();

        return view;
    }

    public void iniciaControles(View view) {
        // Perfil
        imgUserPhoto = view.findViewById(R.id.imgUserPhoto);
        txtUserName = view.findViewById(R.id.txtUserName);
        txtUserEmail = view.findViewById(R.id.txtUserEmail);
        progressProfileCompletion = view.findViewById(R.id.progressProfileCompletion);
        txtProfileCompletion = view.findViewById(R.id.txtProfileCompletion);
        txtBiometricStatus = view.findViewById(R.id.txtBiometricStatus);
        txtAppVersion = view.findViewById(R.id.txtAppVersion);

        // Cards de Saúde
        btnInfoPessoal = view.findViewById(R.id.btnInfoPessoal);
        btnInfoMedica = view.findViewById(R.id.btnInfoMedica);
        btnContatoFamilia = view.findViewById(R.id.btnContatoFamilia);

        // Cards de Preferências
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnPrivacy = view.findViewById(R.id.btnPrivacy);

        // Cards de Sobre
        btnTerms = view.findViewById(R.id.btnTerms);
        btnHelp = view.findViewById(R.id.btnHelp);

        // Botão
        btnLogout = view.findViewById(R.id.btnLogout);

        // Listeners
        btnInfoPessoal.setOnClickListener(this);
        btnInfoMedica.setOnClickListener(this);
        btnContatoFamilia.setOnClickListener(this);
        btnNotifications.setOnClickListener(this);
        btnPrivacy.setOnClickListener(this);
        btnTerms.setOnClickListener(this);
        btnHelp.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        // Versão do App
        try {
            PackageInfo pInfo = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0);
            txtAppVersion.setText(getString(R.string.app_version, pInfo.versionName));
        } catch (Exception e) {
            txtAppVersion.setText(getString(R.string.app_version, "1.0.0"));
        }
    }

    private void loadUserData() {
        // Carregar dados do usuário do SecureStorage
        String userJson = secureStorage.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);

            if (currentUser != null) {
                // Nome
                txtUserName.setText(StringHelper.getFirstName(currentUser.getFullName()));

                // Email
                txtUserEmail.setText(currentUser.getEmail());

                // Foto
                if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                    Glide.with(this)
                            .load(currentUser.getPhotoUrl())
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imgUserPhoto);
                }

                // Progresso do Perfil (calcular baseado nos dados preenchidos)
                int completion = calculateProfileCompletion(currentUser);
                progressProfileCompletion.setProgress(completion);
                txtProfileCompletion.setText(completion + "%");
            }
        }

        // Status da biometria
        boolean biometricEnabled = secureStorage.isBiometricEnabled();
        if (biometricEnabled) {
            txtBiometricStatus.setText(R.string.biometric_enabled);
            txtBiometricStatus.setTextColor(getResources().getColor(R.color.bemax_success));
        } else {
            txtBiometricStatus.setText(R.string.biometric_disabled);
            txtBiometricStatus.setTextColor(getResources().getColor(R.color.bemax_gray_dark));
        }
    }

    private int calculateProfileCompletion(User user) {
        int total = 0;
        int filled = 0;

        // Campos obrigatórios
        total += 5; // Nome, Email, CPF, Telefone, Data Nascimento

        if (user.getFullName() != null && !user.getFullName().isEmpty()) filled++;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) filled++;
        if (user.getCpf() != null && !user.getCpf().isEmpty()) filled++;
        if (user.getPhone() != null && !user.getPhone().isEmpty()) filled++;
        if (user.getBirthDate() != null) filled++;

        return (int) ((filled * 100.0) / total);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnInfoPessoal) {
            startActivity(new Intent(mainActivity, PersonalInfoActivity.class));
        }
        else if (id == R.id.btnInfoMedica) {
            startActivity(new Intent(mainActivity, MedicalInfoActivity.class));
        }
        else if (id == R.id.btnContatoFamilia) {
            startActivity(new Intent(mainActivity, ContactInfoActivity.class));
        }
        else if (id == R.id.btnNotifications) {
            // TODO: Implementar tela de notificações
            Toast.makeText(mainActivity, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.btnPrivacy) {
            startActivity(new Intent(mainActivity, PrivacySecurityActivity.class));
        }
        else if (id == R.id.btnTerms) {
            // TODO: Abrir termos e políticas (WebView ou Intent)
            Toast.makeText(mainActivity, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.btnHelp) {
            // TODO: Abrir ajuda/FAQ
            Toast.makeText(mainActivity, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.btnLogout) {
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