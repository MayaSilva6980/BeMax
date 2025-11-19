package com.example.bemax.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.bemax.R;
import com.example.bemax.model.domain.User;
import com.example.bemax.repository.AuthRepository;
import com.example.bemax.ui.activity.ContactInfoActivity;
import com.example.bemax.ui.activity.HealthProfileActivity;
import com.example.bemax.ui.activity.PersonalInfoActivity;
import com.example.bemax.ui.activity.PrivacySecurityActivity;
import com.example.bemax.ui.activity.LoginActivity;
import com.example.bemax.ui.activity.MainActivity;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.util.manager.TokenManager;
import com.example.bemax.util.storage.SecureStorage;
import com.example.bemax.util.helper.StringHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
    private MaterialCardView btnHealthProfile;
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
    private TokenManager tokenManager;
    private MainActivity mainActivity;
    private User currentUser;

    public ConfigFragment(MainActivity principal) {
        mainActivity = principal;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_config, container, false);

        if (mainActivity == null) {
            mainActivity = (MainActivity) requireActivity();
        }
        authRepository = new AuthRepository();
        secureStorage = new SecureStorage(requireContext());
        secureStorage.setBiometricManager(mainActivity);
        tokenManager = TokenManager.getInstance(requireContext());
        tokenManager.setBiometricManager(mainActivity);

        iniciaControles(view);
        loadUserData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualizar status da biometria quando voltar para o fragment
        if (secureStorage != null && txtBiometricStatus != null) {
            updateBiometricStatus();
        }
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
        btnHealthProfile = view.findViewById(R.id.btnHealthProfile);
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
        btnHealthProfile.setOnClickListener(this);
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

                // Foto - tentar do SecureStorage primeiro (Google login)
                String photoUrl = secureStorage.getUserPhotoUrl();
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this)
                            .load(photoUrl)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imgUserPhoto);
                } else if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                    // Fallback para foto do User object
                    Glide.with(this)
                            .load(currentUser.getPhotoUrl())
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imgUserPhoto);
                } else {
                    // Sem foto, usar placeholder
                    imgUserPhoto.setImageResource(R.drawable.ic_profile);
                }

                // Progresso do Perfil (calcular baseado nos dados preenchidos)
                int completion = calculateProfileCompletion(currentUser);
                progressProfileCompletion.setProgress(completion);
                txtProfileCompletion.setText(completion + "%");
            }
        }

        // Status da biometria
        updateBiometricStatus();
    }

    /**
     * Atualiza o status da biometria no card de Privacidade e Segurança
     */
    private void updateBiometricStatus() {
        boolean biometricEnabled = secureStorage.isBiometricEnabled();
        if (biometricEnabled) {
            txtBiometricStatus.setText(R.string.biometric_enabled);
            txtBiometricStatus.setTextColor(requireContext().getColor(R.color.bemax_success));
            txtBiometricStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0);
            txtBiometricStatus.setCompoundDrawableTintList(requireContext().getColorStateList(R.color.bemax_success));
        } else {
            txtBiometricStatus.setText(R.string.biometric_disabled);
            txtBiometricStatus.setTextColor(requireContext().getColor(R.color.bemax_gray_dark));
            txtBiometricStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_block, 0, 0, 0);
            txtBiometricStatus.setCompoundDrawableTintList(requireContext().getColorStateList(R.color.bemax_gray_dark));
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
        else if (id == R.id.btnHealthProfile) {
            startActivity(new Intent(mainActivity, HealthProfileActivity.class));
        }
        else if (id == R.id.btnContatoFamilia) {
            startActivity(new Intent(mainActivity, ContactInfoActivity.class));
        }
        else if (id == R.id.btnNotifications) {
            // TODO: Implementar tela de notificações
            NotificationHelper.showInfo(mainActivity, "Em desenvolvimento");
        }
        else if (id == R.id.btnPrivacy) {
            startActivity(new Intent(mainActivity, PrivacySecurityActivity.class));
        }
        else if (id == R.id.btnTerms) {
            // TODO: Abrir termos e políticas (WebView ou Intent)
            NotificationHelper.showInfo(mainActivity, "Em desenvolvimento");
        }
        else if (id == R.id.btnHelp) {
            // TODO: Abrir ajuda/FAQ
            NotificationHelper.showInfo(mainActivity, "Em desenvolvimento");
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
        
        // Mostrar loading
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                NotificationHelper.showInfo(mainActivity, getString(R.string.logout_in_progress));
            });
        }

        // TokenManager tem tokens em memória, recupera SEM biometria!
        tokenManager.getAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                Log.d("ConfigFragment", "Access token recuperado da memória");
                
                // Agora buscar refresh token (também da memória!)
                tokenManager.getRefreshToken(new TokenManager.TokenCallback() {
                    @Override
                    public void onSuccess(String refreshToken) {
                        Log.d("ConfigFragment", "Refresh token recuperado da memória");
                        Log.d("ConfigFragment", "Tokens recuperados SEM biometria: Access=true, Refresh=true");
                        performLogoutWithRetry(accessToken, refreshToken, 0);
                    }

                    @Override
                    public void onError(String error) {
                        Log.w("ConfigFragment", "Erro ao recuperar refresh token: " + error);
                        handleLogoutTokenError("refresh token");
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.w("ConfigFragment", "Erro ao recuperar access token: " + error);
                handleLogoutTokenError("access token");
            }
        });
    }
    
    /**
     * Logout com retry automático
     */
    private void performLogoutWithRetry(String accessToken, String refreshToken, int attemptNumber) {
        final int MAX_RETRIES = 3;
        final int RETRY_DELAY_MS = 2000;
        
        Log.d("ConfigFragment", "Tentativa de logout " + (attemptNumber + 1) + "/" + MAX_RETRIES);
        Log.d("ConfigFragment", "Access Token: " + (accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null"));
        Log.d("ConfigFragment", "Refresh Token: " + (refreshToken != null ? refreshToken.substring(0, Math.min(20, refreshToken.length())) + "..." : "null"));
        
        authRepository.logout(accessToken, refreshToken, new AuthRepository.LogoutCallback() {
            @Override
            public void onSuccess() {
                // Logout no backend bem-sucedido
                Log.d("ConfigFragment", "Logout no backend realizado com sucesso");
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        NotificationHelper.showSuccess(mainActivity, getString(R.string.logout_success));
                        completeLogout();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ConfigFragment", "Erro no logout do backend (tentativa " + (attemptNumber + 1) + "): " + error);
                
                if (attemptNumber < MAX_RETRIES - 1) {
                    //Ainda tem tentativas, retry
                    Log.d("ConfigFragment", " Aguardando " + RETRY_DELAY_MS + "ms antes de retry...");
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            NotificationHelper.showWarning(mainActivity, 
                                "Tentando novamente... (" + (attemptNumber + 2) + "/" + MAX_RETRIES + ")");
                        });
                        
                        // Retry após delay
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            performLogoutWithRetry(accessToken, refreshToken, attemptNumber + 1);
                        }, RETRY_DELAY_MS);
                    }
                } else {
                    //Esgotou tentativas, perguntar ao usuário
                    Log.e("ConfigFragment", "Esgotadas " + MAX_RETRIES + " tentativas de logout");
                    handleLogoutBackendError(error);
                }
            }
        });
    }
    
    /**
     * Erro ao recuperar tokens - pergunta ao usuário
     */
    private void handleLogoutTokenError(String tokenType) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(mainActivity)
                    .setTitle(R.string.logout_error_title)
                    .setMessage(getString(R.string.logout_token_error, tokenType))
                    .setPositiveButton(R.string.logout_anyway, (dialog, which) -> {
                        Log.w("ConfigFragment", "Usuário optou por logout local sem notificar backend");
                        completeLogout();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                        btnLogout.setEnabled(true);
                    })
                    .setCancelable(false)
                    .show();
        });
    }
    
    /**
     * Erro no backend após retries - pergunta ao usuário
     */
    private void handleLogoutBackendError(String error) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(mainActivity)
                    .setTitle(R.string.logout_backend_error_title)
                    .setMessage(getString(R.string.logout_backend_error, error))
                    .setPositiveButton(R.string.logout_anyway, (dialog, which) -> {
                        Log.w("ConfigFragment", "Usuário optou por logout local após falhas no backend");
                        completeLogout();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                        btnLogout.setEnabled(true);
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void completeLogout() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            Log.d("ConfigFragment", "Realizando logout completo...");
            
            // Limpar cache de imagens do Glide
            try {
                Glide.get(requireContext()).clearMemory();
                Log.d("ConfigFragment", "Cache de imagens limpo (memória)");
            } catch (Exception e) {
                Log.e("ConfigFragment", "Erro ao limpar cache do Glide: " + e.getMessage());
            }
            
            // Limpar tokens do TokenManager (memória + storage + foto)
            tokenManager.clearTokens();
            Log.d("ConfigFragment", "Tokens limpos");
            
            // Logout do Firebase
            FirebaseAuth.getInstance().signOut();
            Log.d("ConfigFragment", "Firebase logout");

            Intent intent = new Intent(mainActivity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (mainActivity != null) {
                mainActivity.finish();
            }
            
            Log.d("ConfigFragment", "Redirecionando para tela de login");
        });
    }
}