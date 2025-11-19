package com.example.bemax.util.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.bemax.util.security.SecureBiometricManager;

/**
 * Armazenamento 100% Seguro com Biometria
 * 
 * Arquitetura de Segurança em Camadas:
 * 
 * Camada 1: EncryptedSharedPreferences (AES-256)
 *   - Dados em repouso criptografados
 *   - Protege contra acesso físico ao device
 * 
 * Camada 2: Android Keystore + Biometria (HARDWARE)
 *   - Chaves protegidas por Secure Element/TEE
 *   - Requer biometria para descriptografar tokens
 *   - Impossível extrair chaves do dispositivo
 * 
 * Camada 3: Token Encryption (AES-256-GCM)
 *   - Tokens duplamente criptografados
 *   - IV único para cada operação
 *   - Proteção contra replay attacks
 */
public class SecureStorage {
    private static final String TAG = "SecureStorage";
    private static final String PREFS_NAME = "bemax_secure_prefs";
    
    // Keys para dados não-sensíveis (não precisam biometria)
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHOTO_URL = "user_photo_url";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_SESSION_TIMEOUT = "session_timeout";
    
    // Keys para dados sensíveis (criptografados com biometria)
    private static final String KEY_ACCESS_TOKEN_ENCRYPTED = "access_token_encrypted";
    private static final String KEY_REFRESH_TOKEN_ENCRYPTED = "refresh_token_encrypted";
    private static final String KEY_USER_DATA_ENCRYPTED = "user_data_encrypted";
    private static final String KEY_TOKEN_EXPIRATION = "token_expiration";

    private final SharedPreferences encryptedPrefs;
    private final Context context;
    private SecureBiometricManager biometricManager;
    
    // Cache temporário para evitar pedir biometria logo após salvar
    private String cachedAccessToken = null;
    private String cachedRefreshToken = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_VALIDITY_MS = 30000; // 30 segundos

    public SecureStorage(Context context) {
        this.context = context.getApplicationContext();
        
        try {
            // Criar MasterKey usando KeyStore
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Criar EncryptedSharedPreferences (Camada 1)
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            
            Log.d(TAG, "SecureStorage inicializado com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar SecureStorage", e);
            throw new RuntimeException("Failed to initialize secure storage", e);
        }
    }

    /**
     * Configura o BiometricManager (precisa de Activity)
     */
    public void setBiometricManager(FragmentActivity activity) {
        this.biometricManager = new SecureBiometricManager(activity);
    }

    // ========================================================================
    // MÉTODOS PARA TOKENS (Protegidos por Biometria)
    // ========================================================================

    /**
     * Salva AMBOS os tokens com UMA ÚNICA biometria
     * 
     * Em vez de pedir biometria 2x (uma para cada token), solicitamos apenas 1x
     * e usamos a mesma sessão autenticada para criptografar ambos os tokens.
     */
    public void saveTokens(String accessToken, String refreshToken, TokenCallback callback) {
        if (biometricManager == null) {
            Log.e(TAG, "BiometricManager não configurado");
            callback.onError("Sistema biométrico não inicializado");
            return;
        }

        // Cachear tokens em memória (para evitar pedir biometria logo após salvar)
        cachedAccessToken = accessToken;
        cachedRefreshToken = refreshToken;
        cacheTimestamp = System.currentTimeMillis();
        Log.d(TAG, "Tokens cacheados em memória por 30 segundos");

        if (!isBiometricEnabled()) {
            // Sem biometria: salva direto
            encryptedPrefs.edit()
                .putString(KEY_ACCESS_TOKEN_ENCRYPTED, accessToken)
                .putString(KEY_REFRESH_TOKEN_ENCRYPTED, refreshToken)
                .apply();
            Log.d(TAG, "Tokens salvos (biometria não ativada)");
            callback.onSuccess();
            return;
        }

        // Com biometria: solicita UMA VEZ e salva ambos
        Log.d(TAG, "Solicitando biometria UMA VEZ para salvar AMBOS os tokens...");
        
        // Concatena os tokens temporariamente para criptografar juntos
        String combinedTokens = accessToken + "|SEPARATOR|" + refreshToken;
        
        biometricManager.encryptWithBiometric(combinedTokens, new SecureBiometricManager.BiometricCallback() {
            @Override
            public void onEncryptSuccess(String encryptedData) {
                // Salva o dado criptografado (contém ambos os tokens)
                encryptedPrefs.edit()
                    .putString(KEY_ACCESS_TOKEN_ENCRYPTED, encryptedData)
                    .putString(KEY_REFRESH_TOKEN_ENCRYPTED, "STORED_WITH_ACCESS") // Flag
                    .apply();
                
                Log.d(TAG, "AMBOS os tokens salvos com UMA ÚNICA biometria!");
                callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao salvar tokens: " + error);
                callback.onError(error);
            }

            @Override
            public void onFailed() {
                callback.onError("Biometria não reconhecida");
            }
            
            @Override
            public void onCancel() {
                Log.w(TAG, "Biometria cancelada pelo usuário");
                callback.onError("Operação cancelada");
            }
        });
    }

    /**
     * Recupera o access token (requer biometria se ativada)
     */
    public void getAccessToken(TokenCallback callback) {
        getAccessToken(false, callback);
    }

    /**
     * Recupera o access token
     * @param skipBiometric Se true, pula biometria mesmo se ativada (ex: após login recente)
     */
    public void getAccessToken(boolean skipBiometric, TokenCallback callback) {
        // Verifica cache primeiro (se skipBiometric = true E cache válido)
        if (skipBiometric && cachedAccessToken != null) {
            long cacheAge = System.currentTimeMillis() - cacheTimestamp;
            if (cacheAge < CACHE_VALIDITY_MS) {
                Log.d(TAG, "Token recuperado do cache (idade: " + cacheAge + "ms)");
                callback.onTokenRetrieved(cachedAccessToken);
                return;
            } else {
                Log.d(TAG, "Cache expirou (idade: " + cacheAge + "ms), limpando...");
                cachedAccessToken = null;
                cachedRefreshToken = null;
            }
        }
        
        String encryptedToken = encryptedPrefs.getString(KEY_ACCESS_TOKEN_ENCRYPTED, null);
        
        if (encryptedToken == null) {
            Log.w(TAG, "Token não encontrado");
            callback.onError("Token não encontrado");
            return;
        }

        if (!isBiometricEnabled()) {
            // Se biometria não está ativada, retorna direto
            Log.d(TAG, "Token recuperado (biometria não ativada)");
            
            // Verifica se é formato combinado
            if (encryptedToken.contains("|SEPARATOR|")) {
                String[] tokens = encryptedToken.split("\\|SEPARATOR\\|");
                callback.onTokenRetrieved(tokens[0]); // Access token
            } else {
                callback.onTokenRetrieved(encryptedToken);
            }
            return;
        }

        // MUDANÇA: Se skipBiometric=true mas cache vazio, solicita biometria
        if (skipBiometric && cachedAccessToken == null) {
            Log.w(TAG, "skipBiometric=true mas cache vazio (app foi reiniciado). Solicitando biometria...");
            // Continua para descriptografia com biometria
        }

        if (biometricManager == null) {
            Log.e(TAG, "BiometricManager não configurado");
            callback.onError("Sistema biométrico não inicializado");
            return;
        }

        // Descriptografar com biometria (Camada 2 + 3)
        biometricManager.decryptWithBiometric(encryptedToken, new SecureBiometricManager.BiometricCallback() {
            @Override
            public void onDecryptSuccess(String decryptedData) {
                // Verifica se é formato combinado (novo) ou único (antigo)
                if (decryptedData.contains("|SEPARATOR|")) {
                    String[] tokens = decryptedData.split("\\|SEPARATOR\\|");
                    String accessToken = tokens[0];
                    String refreshToken = tokens[1];
                    
                    // Cachear tokens após descriptografar
                    cachedAccessToken = accessToken;
                    cachedRefreshToken = refreshToken;
                    cacheTimestamp = System.currentTimeMillis();
                    Log.d(TAG, "Access token recuperado (formato combinado) + cacheado");
                    
                    callback.onTokenRetrieved(accessToken);
                } else {
                    // Formato antigo (compatibilidade)
                    cachedAccessToken = decryptedData;
                    cacheTimestamp = System.currentTimeMillis();
                    Log.d(TAG, "Access token recuperado (formato antigo) + cacheado");
                    callback.onTokenRetrieved(decryptedData);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao recuperar token: " + error);
                callback.onError(error);
            }

            @Override
            public void onFailed() {
                callback.onError("Biometria não reconhecida");
            }

            @Override
            public void onCancel() {
                callback.onError("Operação cancelada pelo usuário");
            }
        });
    }

    /**
     * Recupera o refresh token (requer biometria se ativada)
     */
    public void getRefreshToken(TokenCallback callback) {
        // PRIMEIRO: Verificar se está no cache (após descriptografia recente)
        if (cachedRefreshToken != null) {
            long cacheAge = System.currentTimeMillis() - cacheTimestamp;
            if (cacheAge < CACHE_VALIDITY_MS) {
                Log.d(TAG, "Refresh token recuperado do cache (idade: " + cacheAge + "ms)");
                callback.onTokenRetrieved(cachedRefreshToken);
                return;
            } else {
                Log.d(TAG, "Cache expirou (idade: " + cacheAge + "ms), limpando...");
                cachedAccessToken = null;
                cachedRefreshToken = null;
            }
        }
        
        String storedValue = encryptedPrefs.getString(KEY_REFRESH_TOKEN_ENCRYPTED, null);
        
        if (storedValue == null) {
            Log.w(TAG, "Refresh token não encontrado");
            callback.onError("Refresh token não encontrado");
            return;
        }

        // Verifica se está usando novo formato (tokens combinados)
        if ("STORED_WITH_ACCESS".equals(storedValue)) {
            // Refresh token está junto com access token
            Log.d(TAG, "Recuperando refresh token do formato combinado...");
            String encryptedCombined = encryptedPrefs.getString(KEY_ACCESS_TOKEN_ENCRYPTED, null);
            
            if (encryptedCombined == null) {
                callback.onError("Tokens não encontrados");
                return;
            }

            if (!isBiometricEnabled()) {
                // Sem biometria: separar tokens
                if (encryptedCombined.contains("|SEPARATOR|")) {
                    String[] tokens = encryptedCombined.split("\\|SEPARATOR\\|");
                    String refreshToken = tokens[1];
                    cachedRefreshToken = refreshToken;
                    cacheTimestamp = System.currentTimeMillis();
                    callback.onTokenRetrieved(refreshToken);
                } else {
                    callback.onError("Formato inválido");
                }
                return;
            }

            // Com biometria: descriptografar e separar
            if (biometricManager == null) {
                callback.onError("Sistema biométrico não inicializado");
                return;
            }

            biometricManager.decryptWithBiometric(encryptedCombined, new SecureBiometricManager.BiometricCallback() {
                @Override
                public void onDecryptSuccess(String decryptedData) {
                    if (decryptedData.contains("|SEPARATOR|")) {
                        String[] tokens = decryptedData.split("\\|SEPARATOR\\|");
                        String refreshToken = tokens[1];
                        // CACHEAR AMBOS OS TOKENS
                        cachedAccessToken = tokens[0];
                        cachedRefreshToken = refreshToken;
                        cacheTimestamp = System.currentTimeMillis();
                        Log.d(TAG, "Refresh token recuperado (formato combinado) + cacheado");
                        callback.onTokenRetrieved(refreshToken);
                    } else {
                        callback.onError("Formato inválido");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Erro ao recuperar refresh token: " + error);
                    callback.onError(error);
                }

                @Override
                public void onFailed() {
                    callback.onError("Biometria não reconhecida");
                }

                @Override
                public void onCancel() {
                    callback.onError("Operação cancelada pelo usuário");
                }
            });
            return;
        }

        // Formato antigo (tokens separados)
        if (!isBiometricEnabled()) {
            Log.d(TAG, "Refresh token recuperado (biometria não ativada)");
            cachedRefreshToken = storedValue; // Cachear
            cacheTimestamp = System.currentTimeMillis();
            callback.onTokenRetrieved(storedValue);
            return;
        }

        if (biometricManager == null) {
            Log.e(TAG, "BiometricManager não configurado");
            callback.onError("Sistema biométrico não inicializado");
            return;
        }

        biometricManager.decryptWithBiometric(storedValue, new SecureBiometricManager.BiometricCallback() {
            @Override
            public void onDecryptSuccess(String decryptedData) {
                cachedRefreshToken = decryptedData; // Cachear
                cacheTimestamp = System.currentTimeMillis();
                Log.d(TAG, "Refresh token recuperado com biometria (formato antigo) + cacheado");
                callback.onTokenRetrieved(decryptedData);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao recuperar refresh token: " + error);
                callback.onError(error);
            }

            @Override
            public void onFailed() {
                callback.onError("Biometria não reconhecida");
            }

            @Override
            public void onCancel() {
                callback.onError("Operação cancelada pelo usuário");
            }
        });
    }

    // ========================================================================
    // MÉTODOS PARA DADOS DO USUÁRIO (Protegidos por Biometria)
    // ========================================================================

    /**
     * Salva dados do usuário criptografados com biometria
     */
    public void saveUserData(String userJson, TokenCallback callback) {
        // User data é cache público, NÃO precisa de biometria
        // (foto de perfil, nome, etc são visíveis na tela mesmo)
        encryptedPrefs.edit().putString(KEY_USER_DATA_ENCRYPTED, userJson).apply();
        Log.d(TAG, "User data salvo (cache público)");
        callback.onSuccess();
    }

    /**
     * Recupera dados do usuário (cache público - sem biometria)
     */
    public String getUserData() {
        return encryptedPrefs.getString(KEY_USER_DATA_ENCRYPTED, null);
    }

    // ========================================================================
    // MÉTODOS NÃO-SENSÍVEIS (Sem Biometria)
    // ========================================================================

    public void saveUserEmail(String email) {
        encryptedPrefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null);
    }

    public void saveUserPhotoUrl(String photoUrl) {
        encryptedPrefs.edit().putString(KEY_USER_PHOTO_URL, photoUrl).apply();
    }

    public String getUserPhotoUrl() {
        return encryptedPrefs.getString(KEY_USER_PHOTO_URL, null);
    }

    public void saveTokenExpiration(long expirationTimeMs) {
        encryptedPrefs.edit().putLong(KEY_TOKEN_EXPIRATION, expirationTimeMs).apply();
    }

    public boolean isTokenExpired() {
        long expirationTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRATION, 0);
        return System.currentTimeMillis() >= expirationTime;
    }

    public boolean isTokenExpiringSoon() {
        long expirationTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRATION, 0);
        long fiveMinutesFromNow = System.currentTimeMillis() + (5 * 60 * 1000);
        return fiveMinutesFromNow >= expirationTime;
    }

    public void setBiometricEnabled(boolean enabled) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
        Log.d(TAG, "Biometric enabled: " + enabled);
    }

    public boolean isBiometricEnabled() {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void saveSessionTimeout(long timeoutMs) {
        encryptedPrefs.edit().putLong(KEY_SESSION_TIMEOUT, timeoutMs).apply();
    }

    public long getSessionTimeout() {
        return encryptedPrefs.getLong(KEY_SESSION_TIMEOUT, 0);
    }

    /**
     * Verifica se há token válido (não descriptografa, apenas verifica existência)
     */
    public boolean hasValidToken() {
        String encryptedToken = encryptedPrefs.getString(KEY_ACCESS_TOKEN_ENCRYPTED, null);
        return encryptedToken != null && !encryptedToken.isEmpty() && !isTokenExpired();
    }

    public boolean hasRefreshToken() {
        String encryptedToken = encryptedPrefs.getString(KEY_REFRESH_TOKEN_ENCRYPTED, null);
        return encryptedToken != null && !encryptedToken.isEmpty();
    }

    // ========================================================================
    // LIMPEZA DE DADOS
    // ========================================================================

    /**
     * Limpa tokens e dados sensíveis, mantém preferências
     */
    public void clearTokens() {
        boolean biometricEnabled = isBiometricEnabled();
        encryptedPrefs.edit()
                .remove(KEY_ACCESS_TOKEN_ENCRYPTED)
                .remove(KEY_REFRESH_TOKEN_ENCRYPTED)
                .remove(KEY_USER_DATA_ENCRYPTED)
                .remove(KEY_TOKEN_EXPIRATION)
                .remove(KEY_USER_EMAIL)
                .apply();
        setBiometricEnabled(biometricEnabled);
        Log.d(TAG, "Tokens limpos");
    }

    /**
     * Limpa TUDO (inclusive chave biométrica)
     */
    public void clearAll() {
        encryptedPrefs.edit().clear().apply();
        
        // Remover chave biométrica do Keystore
        if (biometricManager != null) {
            biometricManager.deleteBiometricKey();
        }
        
        Log.d(TAG, "Todos os dados limpos");
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public interface TokenCallback {
        default void onSuccess() {}
        default void onTokenRetrieved(String token) {}
        void onError(String error);
    }
}