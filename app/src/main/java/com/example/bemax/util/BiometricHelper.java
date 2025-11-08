package com.example.bemax.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.bemax.R;

/**
 * Helper class para gerenciar autenticação biométrica
 */
public class BiometricHelper {
    private static final String TAG = "BiometricHelper";
    
    private final FragmentActivity activity;
    private final Context context;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public BiometricHelper(FragmentActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    /**
     * Verifica se o dispositivo suporta biometria
     */
    public BiometricStatus checkBiometricAvailability() {
        BiometricManager biometricManager = BiometricManager.from(context);
        
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "Biometric available");
                return BiometricStatus.AVAILABLE;
                
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric hardware");
                return BiometricStatus.NO_HARDWARE;
                
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric hardware unavailable");
                return BiometricStatus.UNAVAILABLE;
                
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "No biometric enrolled");
                return BiometricStatus.NOT_ENROLLED;
                
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Log.e(TAG, "Security update required");
                return BiometricStatus.SECURITY_UPDATE_REQUIRED;
                
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Log.e(TAG, "Biometric unsupported");
                return BiometricStatus.UNSUPPORTED;
                
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Log.e(TAG, "Biometric status unknown");
                return BiometricStatus.UNKNOWN;
                
            default:
                return BiometricStatus.UNKNOWN;
        }
    }

    /**
     * Autentica usando biometria
     */
    public void authenticate(BiometricCallback callback) {
        authenticate(callback, true);
    }

    /**
     * Autentica usando biometria com opção de permitir credenciais do device
     */
    public void authenticate(BiometricCallback callback, boolean allowDeviceCredential) {
        BiometricStatus status = checkBiometricAvailability();
        
        if (status != BiometricStatus.AVAILABLE) {
            callback.onError(status.getMessage(context));
            return;
        }

        // Configurar callback de autenticação
        biometricPrompt = new BiometricPrompt(activity,
                ContextCompat.getMainExecutor(context),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            // Usuário clicou em "Usar senha"
                            callback.onUsePassword();
                        } else if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                   errorCode == BiometricPrompt.ERROR_CANCELED) {
                            // Usuário cancelou
                            callback.onCancel();
                        } else {
                            // Erro real
                            Log.e(TAG, "Authentication error: " + errorCode + " - " + errString);
                            callback.onError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.d(TAG, "Authentication succeeded");
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.w(TAG, "Authentication failed - biometric not recognized");
                        callback.onFailed();
                    }
                });

        // Configurar informações do prompt
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.biometric_title))
                .setSubtitle(context.getString(R.string.biometric_subtitle))
                .setDescription(context.getString(R.string.biometric_description));

        if (allowDeviceCredential) {
            // Permite usar PIN/padrão/senha do device como fallback
            builder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG |
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            );
        } else {
            // Apenas biometria, com botão de cancelar
            builder.setNegativeButtonText(context.getString(R.string.biometric_negative_button));
        }

        promptInfo = builder.build();

        // Mostrar prompt
        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Enum com status da biometria
     */
    public enum BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        UNAVAILABLE,
        NOT_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED,
        UNKNOWN;

        public String getMessage(Context context) {
            switch (this) {
                case NO_HARDWARE:
                case UNAVAILABLE:
                case UNSUPPORTED:
                    return context.getString(R.string.biometric_not_available);
                case NOT_ENROLLED:
                    return context.getString(R.string.biometric_not_enrolled);
                case SECURITY_UPDATE_REQUIRED:
                    return "Security update required";
                default:
                    return context.getString(R.string.biometric_error);
            }
        }
    }

    /**
     * Interface de callback para resultados da autenticação
     */
    public interface BiometricCallback {
        void onSuccess();
        
        void onError(String error);
        
        void onFailed();
        
        default void onUsePassword() {
            // Usuário escolheu usar senha
        }
        
        default void onCancel() {
            // Usuário cancelou
        }
    }
}

