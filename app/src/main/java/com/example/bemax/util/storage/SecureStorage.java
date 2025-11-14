package com.example.bemax.util.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Classe para armazenar dados de forma segura usando Android Keystore
 * e EncryptedSharedPreferences
 */
public class SecureStorage {
    private static final String TAG = "SecureStorage";
    private static final String PREFS_NAME = "bemax_secure_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN_EXPIRATION = "token_expiration";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    // Session Timeout
    private static final String KEY_SESSION_TIMEOUT = "session_timeout";

    private final SharedPreferences encryptedPrefs;
    private final Context context;

    public SecureStorage(Context context) {
        this.context = context;
        
        try {
            // Criar MasterKey usando KeyStore
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Criar EncryptedSharedPreferences
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            
            Log.d(TAG, "SecureStorage initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SecureStorage", e);
            throw new RuntimeException("Failed to initialize secure storage", e);
        }
    }

    /**
     * Salva o token de acesso de forma criptografada
     */
    public void saveAccessToken(String token) {
        encryptedPrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
        Log.d(TAG, "Access token saved securely");
    }

    /**
     * Recupera o token de acesso
     */
    public String getAccessToken() {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Salva o refresh token
     */
    public void saveRefreshToken(String token) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    /**
     * Recupera o refresh token
     */
    public String getRefreshToken() {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Salva o email do usuário
     */
    public void saveUserEmail(String email) {
        encryptedPrefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    /**
     * Recupera o email do usuário
     */
    public String getUserEmail() {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Salva o tempo de expiração do token (em milissegundos)
     */
    public void saveTokenExpiration(long expirationTimeMs) {
        encryptedPrefs.edit().putLong(KEY_TOKEN_EXPIRATION, expirationTimeMs).apply();
    }

    /**
     * Verifica se o token expirou
     */
    public boolean isTokenExpired() {
        long expirationTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRATION, 0);
        return System.currentTimeMillis() >= expirationTime;
    }

    /**
     * Marca biometria como habilitada
     */
    public void setBiometricEnabled(boolean enabled) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
        Log.d(TAG, "Biometric enabled: " + enabled);
    }

    /**
     * Verifica se biometria está habilitada
     */
    public boolean isBiometricEnabled() {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    /**
     * Verifica se existe token salvo
     */
    public boolean hasValidToken() {
        String token = getAccessToken();
        return token != null && !token.isEmpty() && !isTokenExpired();
    }

    // Limpa todos os dados salvos (logout)
    public void clearAll() {
        encryptedPrefs.edit().clear().apply();
        Log.d(TAG, "All secure data cleared");
    }

    /**
     * Apenas limpa tokens, mantém preferência de biometria
     */
    public void clearTokens() {
        boolean biometricEnabled = isBiometricEnabled();
        encryptedPrefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRATION)
                .remove(KEY_USER_DATA)
                .remove(KEY_USER_EMAIL)
                .apply();
        setBiometricEnabled(biometricEnabled);
        Log.d(TAG, "Tokens and user data cleared");
    }

    // Salvar dados completos do usuário como JSON
    public void saveUserData(String userJson) {
        encryptedPrefs.edit().putString(KEY_USER_DATA, userJson).apply();
        Log.d(TAG, "User data saved");
    }

    public String getUserData() {
        return encryptedPrefs.getString(KEY_USER_DATA, null);
    }

     // Verifica se o token está próximo de expirar (menos de 5 minutos)
    public boolean isTokenExpiringSoon() {
        long expirationTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRATION, 0);
        long fiveMinutesFromNow = System.currentTimeMillis() + (5 * 60 * 1000);
        return fiveMinutesFromNow >= expirationTime;
    }

     // Verifica se tem refresh token salvo
    public boolean hasRefreshToken() {
        String refreshToken = getRefreshToken();
        return refreshToken != null && !refreshToken.isEmpty();
    }

    public void saveSessionTimeout(long timeoutMs) {
        encryptedPrefs.edit().putLong(KEY_SESSION_TIMEOUT, timeoutMs).apply();
        Log.d(TAG, "Session timeout saved: " + timeoutMs + "ms");
    }

    public long getSessionTimeout() {
        return encryptedPrefs.getLong(KEY_SESSION_TIMEOUT, 15 * 60 * 1000); // default 15 min
    }
}

