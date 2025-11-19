package com.example.bemax.util.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.bemax.R;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 *  SecureBiometricManager - Biometria 100% Segura
 *
 * Implementa autenticação biométrica usando:
 * 1. Android Keystore (chaves protegidas por hardware)
 * 2. BiometricPrompt com CryptoObject
 * 3. Criptografia AES-256-GCM
 *
 * Padrão recomendado pelo Google:
 * https://developer.android.com/training/sign-in/biometric-auth
 */
public class SecureBiometricManager {
    private static final String TAG = "SecureBiometricManager";

    // Configurações do Keystore
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "BeMaxBiometricKey";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    private final FragmentActivity activity;
    private final Context context;
    private BiometricPrompt biometricPrompt;

    public SecureBiometricManager(FragmentActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    /**
     * Verifica se o dispositivo suporta biometria STRONG
     */
    public BiometricStatus checkBiometricAvailability() {
        BiometricManager biometricManager = BiometricManager.from(context);

        // Requer BIOMETRIC_STRONG (impressão digital, face, íris)
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "Biometria STRONG disponível");
                return BiometricStatus.AVAILABLE;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "Sem hardware biométrico");
                return BiometricStatus.NO_HARDWARE;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Hardware biométrico indisponível");
                return BiometricStatus.UNAVAILABLE;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "Nenhuma biometria cadastrada");
                return BiometricStatus.NOT_ENROLLED;

            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Log.e(TAG, "Atualização de segurança necessária");
                return BiometricStatus.SECURITY_UPDATE_REQUIRED;

            default:
                return BiometricStatus.UNKNOWN;
        }
    }

    /**
     * Gera ou recupera a chave secreta no Keystore
     * Chave protegida por hardware + biometria obrigatória
     */
    private SecretKey getOrCreateSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        // Verificar se chave já existe
        if (keyStore.containsAlias(KEY_ALIAS)) {
            SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            Log.d(TAG, "Chave biométrica recuperada com sucesso");
            return key;
        }

        // Criar nova chave
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
        );

        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                // CRÍTICO: Requer autenticação biométrica para usar a chave
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true) // Invalida se biometria mudar
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256) // AES-256
                // Timeout de 0 = requer biometria a cada uso
                .setUserAuthenticationValidityDurationSeconds(-1) // Requer biometria SEMPRE
                .build();

        keyGenerator.init(keySpec);
        SecretKey key = keyGenerator.generateKey();

        Log.d(TAG, "Nova chave biométrica criada no Keystore com sucesso");
        return key;
    }

    /**
     * Criptografa dados usando biometria
     * Usuário PRECISA autenticar com biometria para criptografar
     */
    public void encryptWithBiometric(String data, BiometricCallback callback) {
        try {
            SecretKey secretKey = getOrCreateSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // CryptoObject vincula a criptografia à biometria
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

            authenticate(cryptoObject, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    try {
                        Cipher authenticatedCipher = result.getCryptoObject().getCipher();

                        // Criptografar dados
                        byte[] encryptedBytes = authenticatedCipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
                        byte[] iv = authenticatedCipher.getIV();

                        // Combinar IV + dados criptografados
                        String encryptedData = Base64.encodeToString(iv, Base64.NO_WRAP) +
                                ":" +
                                Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);

                        Log.d(TAG, "Dados criptografados com biometria");
                        callback.onEncryptSuccess(encryptedData);

                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao criptografar", e);
                        callback.onError("Erro ao criptografar dados: " + e.getMessage());
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    Log.e(TAG, "Erro na autenticação: " + errorCode);
                    callback.onError(errString.toString());
                }

                @Override
                public void onAuthenticationFailed() {
                    Log.w(TAG, "Biometria não reconhecida");
                    callback.onFailed();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar criptografia", e);
            callback.onError("Erro ao configurar criptografia: " + e.getMessage());
        }
    }

    /**
     * Descriptografa dados usando biometria
     * Usuário PRECISA autenticar com biometria para descriptografar
     */
    public void decryptWithBiometric(String encryptedData, BiometricCallback callback) {
        try {
            // Separar IV e dados criptografados
            String[] parts = encryptedData.split(":");
            if (parts.length != 2) {
                callback.onError("Formato de dados inválido");
                return;
            }

            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP);

            SecretKey secretKey = getOrCreateSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            // CryptoObject vincula a descriptografia à biometria
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

            authenticate(cryptoObject, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    try {
                        Cipher authenticatedCipher = result.getCryptoObject().getCipher();

                        // Descriptografar dados
                        byte[] decryptedBytes = authenticatedCipher.doFinal(encryptedBytes);
                        String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);

                        Log.d(TAG, "Dados descriptografados com biometria");
                        callback.onDecryptSuccess(decryptedData);

                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao descriptografar", e);
                        callback.onError("Erro ao descriptografar dados: " + e.getMessage());
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    Log.e(TAG, "Erro na autenticação: " + errorCode);

                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_CANCELED) {
                        callback.onCancel();
                    } else {
                        callback.onError(errString.toString());
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    Log.w(TAG, "Biometria não reconhecida");
                    callback.onFailed();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar descriptografia", e);
            callback.onError("Erro ao configurar descriptografia: " + e.getMessage());
        }
    }

    /**
     * Autentica usando biometria com CryptoObject
     */
    private void authenticate(BiometricPrompt.CryptoObject cryptoObject,
                              BiometricPrompt.AuthenticationCallback authCallback) {

        biometricPrompt = new BiometricPrompt(activity,
                ContextCompat.getMainExecutor(context),
                authCallback);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.biometric_title))
                .setSubtitle(context.getString(R.string.biometric_subtitle))
                .setDescription(context.getString(R.string.biometric_description))
                .setNegativeButtonText(context.getString(R.string.cancel))
                // Apenas BIOMETRIC_STRONG, sem fallback para PIN
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setConfirmationRequired(true) // Requer confirmação explícita
                .build();

        biometricPrompt.authenticate(promptInfo, cryptoObject);
    }

    /**
     * Remove a chave biométrica do Keystore
     */
    public void deleteBiometricKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry(KEY_ALIAS);
            Log.d(TAG, "Chave biométrica removida");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao remover chave", e);
        }
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
        UNKNOWN;

        public String getMessage(Context context) {
            switch (this) {
                case NO_HARDWARE:
                case UNAVAILABLE:
                    return context.getString(R.string.biometric_not_available);
                case NOT_ENROLLED:
                    return context.getString(R.string.biometric_not_enrolled);
                case SECURITY_UPDATE_REQUIRED:
                    return context.getString(R.string.biometric_security_update);
                default:
                    return context.getString(R.string.biometric_error);
            }
        }
    }

    /**
     * Interface de callback para resultados da autenticação
     */
    public interface BiometricCallback {
        default void onEncryptSuccess(String encryptedData) {}
        default void onDecryptSuccess(String decryptedData) {}
        void onError(String error);
        void onFailed();
        default void onCancel() {}
    }
}

