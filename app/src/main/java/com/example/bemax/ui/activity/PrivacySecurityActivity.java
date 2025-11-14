package com.example.bemax.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import com.example.bemax.R;
import com.example.bemax.repository.SecurityRepository;
import com.example.bemax.util.helper.BiometricHelper;
import com.example.bemax.util.storage.SecureStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class PrivacySecurityActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialToolbar toolbar;
    private SwitchMaterial switchBiometric;
    private TextView txtBiometricStatus;
    private TextView txtSessionTimeout;
    private TextView txtDeviceCount;
    private MaterialCardView btnChangePassword;
    private MaterialCardView btnSessionTimeout;
    private MaterialCardView btnConnectedDevices;
    private MaterialCardView btnLoginHistory;
    private MaterialCardView btnDeleteAccount;

    private SecureStorage secureStorage;
    private BiometricHelper biometricHelper;
    private SecurityRepository securityRepository;

    // Session timeout values in milliseconds
    private static final long TIMEOUT_5_MIN = 5 * 60 * 1000;
    private static final long TIMEOUT_15_MIN = 15 * 60 * 1000;
    private static final long TIMEOUT_30_MIN = 30 * 60 * 1000;
    private static final long TIMEOUT_1_HOUR = 60 * 60 * 1000;
    private static final long TIMEOUT_NEVER = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_security);

        secureStorage = new SecureStorage(this);
        biometricHelper = new BiometricHelper(this);
        securityRepository = new SecurityRepository();

        initViews();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        switchBiometric = findViewById(R.id.switchBiometric);
        txtBiometricStatus = findViewById(R.id.txtBiometricStatus);
        txtSessionTimeout = findViewById(R.id.txtSessionTimeout);
        txtDeviceCount = findViewById(R.id.txtDeviceCount);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSessionTimeout = findViewById(R.id.btnSessionTimeout);
        btnConnectedDevices = findViewById(R.id.btnConnectedDevices);
        btnLoginHistory = findViewById(R.id.btnLoginHistory);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Listeners
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Evita loop ao carregar
                toggleBiometric(isChecked);
            }
        });
        btnChangePassword.setOnClickListener(this);
        btnSessionTimeout.setOnClickListener(this);
        btnConnectedDevices.setOnClickListener(this);
        btnLoginHistory.setOnClickListener(this);
        btnDeleteAccount.setOnClickListener(this);
    }

    private void loadData() {
        // Biometric status
        boolean biometricEnabled = secureStorage.isBiometricEnabled();
        switchBiometric.setChecked(biometricEnabled);
        updateBiometricStatus(biometricEnabled);

        // Session timeout
        long timeout = secureStorage.getSessionTimeout();
        updateSessionTimeoutText(timeout);

        // Device count (placeholder)
        txtDeviceCount.setText(getString(R.string.devices_count_1));
    }

    private void updateBiometricStatus(boolean enabled) {
        if (enabled) {
            txtBiometricStatus.setText(R.string.biometric_status_on);
            txtBiometricStatus.setTextColor(getResources().getColor(R.color.bemax_success));
        } else {
            txtBiometricStatus.setText(R.string.biometric_status_off);
            txtBiometricStatus.setTextColor(getResources().getColor(R.color.bemax_gray_dark));
        }
    }

    private void toggleBiometric(boolean enable) {
        if (enable) {
            // Check if biometric is available
            BiometricHelper.BiometricStatus status = biometricHelper.checkBiometricAvailability();
            if (status != BiometricHelper.BiometricStatus.AVAILABLE) {
                switchBiometric.setChecked(false);
                Toast.makeText(this, status.getMessage(this), Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate to enable
            biometricHelper.authenticate(new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess() {
                    secureStorage.setBiometricEnabled(true);
                    updateBiometricStatus(true);
                    Toast.makeText(PrivacySecurityActivity.this,
                            R.string.biometric_enabled_success,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    switchBiometric.setChecked(false);
                    Toast.makeText(PrivacySecurityActivity.this,
                            error,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed() {
                    switchBiometric.setChecked(false);
                    Toast.makeText(PrivacySecurityActivity.this,
                            R.string.biometric_failed,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    // Usuário cancelou - voltar switch para off
                    switchBiometric.setChecked(false);
                }
            }, false); // false = não permite fallback para PIN/senha do device
        } else {
            // Desativar biometria
            secureStorage.setBiometricEnabled(false);
            updateBiometricStatus(false);
            Toast.makeText(this, R.string.biometric_disabled_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnChangePassword) {
            showChangePasswordDialog();
        } else if (id == R.id.btnSessionTimeout) {
            showSessionTimeoutDialog();
        } else if (id == R.id.btnConnectedDevices) {
            showConnectedDevices();
        } else if (id == R.id.btnLoginHistory) {
            showLoginHistory();
        } else if (id == R.id.btnDeleteAccount) {
            showDeleteAccountDialog();
        }
    }

    // ========== CHANGE PASSWORD ==========

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_password);

        // Create layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Current password
        final EditText inputCurrentPassword = new EditText(this);
        inputCurrentPassword.setHint(R.string.current_password);
        inputCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputCurrentPassword);

        // New password
        final EditText inputNewPassword = new EditText(this);
        inputNewPassword.setHint(R.string.new_password);
        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNewPassword);

        // Confirm new password
        final EditText inputConfirmPassword = new EditText(this);
        inputConfirmPassword.setHint(R.string.confirm_new_password);
        inputConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputConfirmPassword);

        builder.setView(layout);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String currentPassword = inputCurrentPassword.getText().toString().trim();
            String newPassword = inputNewPassword.getText().toString().trim();
            String confirmPassword = inputConfirmPassword.getText().toString().trim();

            // Validate
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
                return;
            }

            // Call backend to change password
            changePassword(currentPassword, newPassword);
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        // TODO: Implementar chamada ao backend quando estiver pronto
        // String accessToken = secureStorage.getAccessToken();
        // securityRepository.changePassword(accessToken, currentPassword, newPassword, callback);

        // Placeholder
        Toast.makeText(this, R.string.password_changed_success, Toast.LENGTH_SHORT).show();
    }

    // ========== SESSION TIMEOUT ==========

    private void showSessionTimeoutDialog() {
        String[] options = {
                getString(R.string.session_timeout_never),
                getString(R.string.session_timeout_5min),
                getString(R.string.session_timeout_15min),
                getString(R.string.session_timeout_30min),
                getString(R.string.session_timeout_1hour)
        };

        long currentTimeout = secureStorage.getSessionTimeout();
        int selectedIndex = getTimeoutIndex(currentTimeout);

        new AlertDialog.Builder(this)
                .setTitle(R.string.session_timeout_title)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    long newTimeout = getTimeoutValue(which);
                    secureStorage.saveSessionTimeout(newTimeout);
                    updateSessionTimeoutText(newTimeout);
                    Toast.makeText(this, R.string.session_timeout_updated, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private long getTimeoutValue(int index) {
        switch (index) {
            case 0: return TIMEOUT_NEVER;
            case 1: return TIMEOUT_5_MIN;
            case 2: return TIMEOUT_15_MIN;
            case 3: return TIMEOUT_30_MIN;
            case 4: return TIMEOUT_1_HOUR;
            default: return TIMEOUT_15_MIN;
        }
    }

    private int getTimeoutIndex(long timeout) {
        if (timeout == TIMEOUT_NEVER) return 0;
        if (timeout == TIMEOUT_5_MIN) return 1;
        if (timeout == TIMEOUT_15_MIN) return 2;
        if (timeout == TIMEOUT_30_MIN) return 3;
        if (timeout == TIMEOUT_1_HOUR) return 4;
        return 2; // default to 15 min
    }

    private void updateSessionTimeoutText(long timeout) {
        int stringResId;
        if (timeout == TIMEOUT_NEVER) stringResId = R.string.session_timeout_never;
        else if (timeout == TIMEOUT_5_MIN) stringResId = R.string.session_timeout_5min;
        else if (timeout == TIMEOUT_15_MIN) stringResId = R.string.session_timeout_15min;
        else if (timeout == TIMEOUT_30_MIN) stringResId = R.string.session_timeout_30min;
        else if (timeout == TIMEOUT_1_HOUR) stringResId = R.string.session_timeout_1hour;
        else stringResId = R.string.session_timeout_15min;

        txtSessionTimeout.setText(stringResId);
    }

    // ========== CONNECTED DEVICES ==========

    private void showConnectedDevices() {
        // TODO: Implementar tela completa quando backend estiver pronto
        Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    // ========== LOGIN HISTORY ==========

    private void showLoginHistory() {
        // TODO: Implementar tela completa quando backend estiver pronto
        Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    // ========== DELETE ACCOUNT ==========

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_account_title);
        builder.setMessage(R.string.delete_account_message);

        // Password input
        final EditText inputPassword = new EditText(this);
        inputPassword.setHint(R.string.current_password);
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputPassword);

        builder.setView(layout);

        builder.setPositiveButton(R.string.delete_account_confirm, (dialog, which) -> {
            String password = inputPassword.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, R.string.password_incorrect, Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirm again
            showFinalDeleteConfirmation(password);
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showFinalDeleteConfirmation(String password) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account_title)
                .setMessage("⚠️ ÚLTIMA CONFIRMAÇÃO\n\nSua conta e todos os dados serão permanentemente excluídos.")
                .setPositiveButton(R.string.delete_account_confirm, (dialog, which) -> {
                    deleteAccount(password);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAccount(String password) {
        // TODO: Implementar chamada ao backend quando estiver pronto
        // String accessToken = secureStorage.getAccessToken();
        // securityRepository.deleteAccount(accessToken, password, callback);

        // Placeholder
        Toast.makeText(this, "Em desenvolvimento - integração com backend necessária", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}