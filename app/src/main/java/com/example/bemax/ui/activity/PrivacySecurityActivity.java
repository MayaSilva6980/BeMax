package com.example.bemax.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.bemax.R;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.util.storage.SecureStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Activity para gerenciar configurações de Privacidade e Segurança
 */
public class PrivacySecurityActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PrivacySecurityActivity";

    // Views
    private MaterialToolbar toolbar;
    private SwitchMaterial switchBiometric;
    private TextView txtBiometricStatus;
    private LinearLayout btnClearCache;
    private LinearLayout btnManageData;

    // Storage
    private SecureStorage secureStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_security);

        try {
            obtainParameters();
            initializeControls();
            loadData();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar tela: " + e.getMessage(), e);
            NotificationHelper.showError(this, "Erro ao carregar tela");
            finish();
        }
    }

    @Override
    public void obtainParameters() {
        secureStorage = new SecureStorage(this);
    }

    @Override
    public void initializeControls() throws Exception {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        switchBiometric = findViewById(R.id.switchBiometric);
        txtBiometricStatus = findViewById(R.id.txtBiometricStatus);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnManageData = findViewById(R.id.btnManageData);

        // Listeners
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                handleBiometricToggle(isChecked);
            }
        });

        btnClearCache.setOnClickListener(this);
        btnManageData.setOnClickListener(this);
    }

    @Override
    public void loadData() throws Exception {
        loadBiometricStatus();
    }

    /**
     * Carrega o status atual da biometria
     */
    private void loadBiometricStatus() {
        boolean isEnabled = secureStorage.isBiometricEnabled();
        switchBiometric.setChecked(isEnabled);
        updateBiometricStatusText(isEnabled);
    }

    /**
     * Atualiza o texto de status da biometria
     */
    private void updateBiometricStatusText(boolean isEnabled) {
        if (isEnabled) {
            txtBiometricStatus.setText(R.string.biometric_enabled);
            txtBiometricStatus.setTextColor(getColor(R.color.status_success));
            txtBiometricStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            txtBiometricStatus.setCompoundDrawableTintList(getColorStateList(R.color.status_success));
            txtBiometricStatus.setVisibility(View.VISIBLE);
        } else {
            txtBiometricStatus.setText(R.string.biometric_disabled);
            txtBiometricStatus.setTextColor(getColor(R.color.on_surface_variant));
            txtBiometricStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_block, 0, 0, 0);
            txtBiometricStatus.setCompoundDrawableTintList(getColorStateList(R.color.on_surface_variant));
            txtBiometricStatus.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Manipula a mudança do toggle de biometria
     */
    private void handleBiometricToggle(boolean enable) {
        if (enable) {
            // Ativar biometria - será configurada no próximo login
            showBiometricEnableDialog();
        } else {
            // Desativar biometria
            showBiometricDisableDialog();
        }
    }

    /**
     * Mostra diálogo para ativar biometria
     */
    private void showBiometricEnableDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_enable_biometric)
                .setMessage(R.string.privacy_biometric_enable_message)
                .setPositiveButton(R.string.activate, (dialog, which) -> {
                    // Marca preferência para ativar no próximo login
                    secureStorage.setBiometricPreference(true);
                    updateBiometricStatusText(true);
                    NotificationHelper.showSuccess(this, getString(R.string.privacy_biometric_will_enable));
                })
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    switchBiometric.setChecked(false);
                    updateBiometricStatusText(false);
                })
                .setOnCancelListener(dialog -> {
                    switchBiometric.setChecked(false);
                    updateBiometricStatusText(false);
                })
                .show();
    }

    /**
     * Mostra diálogo para desativar biometria
     */
    private void showBiometricDisableDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_disable_biometric)
                .setMessage(R.string.privacy_biometric_disable_message)
                .setPositiveButton(R.string.deactivate, (dialog, which) -> {
                    // Remove dados biométricos e preferência
                    secureStorage.clearBiometricData();
                    secureStorage.setBiometricPreference(false);
                    updateBiometricStatusText(false);
                    NotificationHelper.showSuccess(this, getString(R.string.privacy_biometric_disabled_success));
                })
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    switchBiometric.setChecked(true);
                    updateBiometricStatusText(true);
                })
                .setOnCancelListener(dialog -> {
                    switchBiometric.setChecked(true);
                    updateBiometricStatusText(true);
                })
                .show();
    }

    /**
     * Limpa o cache da aplicação
     */
    private void clearCache() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_clear_cache)
                .setMessage(R.string.privacy_clear_cache_confirm)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    try {
                        // Limpa cache do Glide
                        Glide.get(this).clearMemory();
                        new Thread(() -> Glide.get(this).clearDiskCache()).start();

                        // Limpa cache da aplicação
                        deleteCache(getCacheDir());

                        runOnUiThread(() -> 
                            NotificationHelper.showSuccess(this, getString(R.string.privacy_cache_cleared))
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao limpar cache: " + e.getMessage(), e);
                        runOnUiThread(() -> 
                            NotificationHelper.showError(this, getString(R.string.privacy_cache_error))
                        );
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    /**
     * Deleta arquivos do diretório de cache
     */
    private void deleteCache(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    deleteCache(new java.io.File(dir, child));
                }
            }
        }
        if (dir != null) {
            dir.delete();
        }
    }

    /**
     * Gerencia dados salvos
     */
    private void manageData() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_manage_data)
                .setMessage(R.string.privacy_manage_data_message)
                .setItems(new CharSequence[]{
                        getString(R.string.privacy_view_data),
                        getString(R.string.privacy_export_data),
                        getString(R.string.privacy_delete_data)
                }, (dialog, which) -> {
                    switch (which) {
                        case 0: // Visualizar
                            showDataSummary();
                            break;
                        case 1: // Exportar
                            NotificationHelper.showInfo(this, "Funcionalidade em desenvolvimento");
                            break;
                        case 2: // Deletar
                            confirmDeleteData();
                            break;
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    /**
     * Mostra resumo dos dados salvos
     */
    private void showDataSummary() {
        String email = secureStorage.getUserEmail();
        boolean hasBiometric = secureStorage.isBiometricEnabled();
        
        String message = String.format(
                "Email: %s\n" +
                "Biometria: %s\n" +
                "Token: Salvo com segurança",
                email != null ? email : "Não disponível",
                hasBiometric ? "Ativada" : "Desativada"
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_view_data)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Confirma exclusão de dados
     */
    private void confirmDeleteData() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_delete_data)
                .setMessage(R.string.privacy_delete_data_warning)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    secureStorage.clearTokens();
                    NotificationHelper.showWarning(this, getString(R.string.privacy_data_deleted));
                    // Volta para login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    /**
     * Abre política de privacidade
     */
    private void openPrivacyPolicy() {
        // TODO: Substituir pela URL real da política de privacidade
        String url = "https://bemax.com.br/privacy-policy";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Abre termos de uso
     */
    private void openTermsOfUse() {
        // TODO: Substituir pela URL real dos termos de uso
        String url = "https://bemax.com.br/terms";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnClearCache) {
            clearCache();
        } else if (id == R.id.btnManageData) {
            manageData();
        }
    }
}
