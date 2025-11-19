package com.example.bemax.util.security;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.example.bemax.util.storage.SecureStorage;

/**
 * Gerenciador centralizado de tokens de autenticação.
 * Mantém tokens em memória durante a sessão ativa do app.
 * Só solicita biometria quando realmente necessário (app reiniciado, token expirado, etc).
 */
public class TokenManager {
    private static final String TAG = "TokenManager";
    private static TokenManager instance;

    private String accessToken;
    private String refreshToken;
    private long tokenTimestamp;
    private final SecureStorage secureStorage;

    // Tokens em memória são válidos por 5 minutos
    private static final long TOKEN_MEMORY_VALIDITY_MS = 5 * 60 * 1000; // 5 minutos

    private TokenManager(Context context) {
        this.secureStorage = new SecureStorage(context);
    }

    /**
     * Obtém instância singleton
     */
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Configura o BiometricManager (deve ser chamado com FragmentActivity)
     */
    public void setBiometricManager(FragmentActivity activity) {
        secureStorage.setBiometricManager(activity);
    }

    /**
     * Obtém o access token.
     * Se já está em memória e válido (< 5 minutos), retorna imediatamente.
     * Se não, busca do SecureStorage (pode solicitar biometria).
     */
    public void getAccessToken(TokenCallback callback) {
        // Se já tem em memória E é válido (< 5 minutos), retorna imediatamente
        if (accessToken != null && !accessToken.isEmpty()) {
            long cacheAge = System.currentTimeMillis() - tokenTimestamp;
            if (cacheAge < TOKEN_MEMORY_VALIDITY_MS) {
                Log.d(TAG, "Token recuperado da memória (sessão ativa, idade: " + (cacheAge / 1000) + "s)");
                callback.onSuccess(accessToken);
                return;
            } else {
                Log.d(TAG, "Token em memória expirou (idade: " + (cacheAge / 1000) + "s), recarregando...");
                accessToken = null; // Limpa o cache expirado
            }
        }

        // Não tem em memória OU expirou, buscar do SecureStorage
        Log.d(TAG, "Token não está em memória, buscando do SecureStorage...");
        secureStorage.getAccessToken(new SecureStorage.TokenCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                accessToken = token;
                tokenTimestamp = System.currentTimeMillis();
                Log.d(TAG, "Token carregado do SecureStorage e armazenado em memória");
                callback.onSuccess(token);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao recuperar token: " + error);
                callback.onError(error);
            }
        });
    }

    /**
     * Obtém o access token SEM solicitar biometria (usa cache de 30s do SecureStorage)
     * Use este método logo após login/registro para evitar biometria duplicada
     */
    public void getAccessTokenWithoutBiometric(TokenCallback callback) {
        // Se já tem em memória E é válido, retorna imediatamente
        if (accessToken != null && !accessToken.isEmpty()) {
            long cacheAge = System.currentTimeMillis() - tokenTimestamp;
            if (cacheAge < TOKEN_MEMORY_VALIDITY_MS) {
                Log.d(TAG, "Token recuperado da memória (sessão ativa, idade: " + (cacheAge / 1000) + "s)");
                callback.onSuccess(accessToken);
                return;
            } else {
                Log.d(TAG, "Token em memória expirou (idade: " + (cacheAge / 1000) + "s)");
                accessToken = null;
            }
        }

        Log.d(TAG, "Token não está em memória, buscando do SecureStorage (sem biometria)...");
        secureStorage.getAccessToken(true, new SecureStorage.TokenCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                accessToken = token;
                tokenTimestamp = System.currentTimeMillis();
                Log.d(TAG, "Token carregado do SecureStorage e armazenado em memória");
                callback.onSuccess(token);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao recuperar token: " + error);
                callback.onError(error);
            }
        });
    }

    /**
     * Obtém o refresh token.
     * Se já está em memória e válido, retorna imediatamente SEM biometria.
     */
    public void getRefreshToken(TokenCallback callback) {
        // Se já tem em memória E é válido, retorna imediatamente
        if (refreshToken != null && !refreshToken.isEmpty()) {
            long cacheAge = System.currentTimeMillis() - tokenTimestamp;
            if (cacheAge < TOKEN_MEMORY_VALIDITY_MS) {
                Log.d(TAG, "Refresh token recuperado da memória (sessão ativa, idade: " + (cacheAge / 1000) + "s)");
                callback.onSuccess(refreshToken);
                return;
            } else {
                Log.d(TAG, "Refresh token em memória expirou (idade: " + (cacheAge / 1000) + "s)");
                refreshToken = null;
            }
        }

        // Não tem em memória OU expirou, buscar do SecureStorage
        Log.d(TAG, "Refresh token não está em memória, buscando do SecureStorage...");
        secureStorage.getRefreshToken(new SecureStorage.TokenCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                refreshToken = token;
                tokenTimestamp = System.currentTimeMillis(); // Atualiza timestamp
                Log.d(TAG, "Refresh token carregado do SecureStorage e armazenado em memória");
                callback.onSuccess(token);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao recuperar refresh token: " + error);
                callback.onError(error);
            }
        });
    }

    /**
     * Salva os tokens (usado após login/registro)
     */
    public void saveTokens(String accessToken, String refreshToken, TokenCallback callback) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenTimestamp = System.currentTimeMillis();
        
        Log.d(TAG, "Salvando tokens no SecureStorage...");
        secureStorage.saveTokens(accessToken, refreshToken, new SecureStorage.TokenCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Tokens salvos com sucesso");
                callback.onSuccess(accessToken);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao salvar tokens: " + error);
                callback.onError(error);
            }
        });
    }

    /**
     * Limpa os tokens da memória e do armazenamento
     */
    public void clearTokens() {
        Log.d(TAG, "Limpando tokens da memória e armazenamento");
        accessToken = null;
        refreshToken = null;
        tokenTimestamp = 0;
        secureStorage.clearTokens();
    }

    /**
     * Verifica se há um token válido em memória
     */
    public boolean hasValidToken() {
        return accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Obtém o timestamp de quando o token foi carregado
     */
    public long getTokenTimestamp() {
        return tokenTimestamp;
    }

    /**
     * Callback interface
     */
    public interface TokenCallback {
        void onSuccess(String token);
        void onError(String error);
    }
}

