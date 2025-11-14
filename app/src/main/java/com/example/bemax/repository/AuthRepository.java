package com.example.bemax.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bemax.model.dto.ApiResponse;
import com.example.bemax.model.dto.FirebaseLoginRequest;
import com.example.bemax.model.dto.LoginRequest;
import com.example.bemax.model.dto.LoginResponse;
import com.example.bemax.model.dto.RefreshTokenRequest;
import com.example.bemax.network.RetrofitClient;
import com.example.bemax.network.api.CallApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final CallApi callApi;

    public AuthRepository() {
        this.callApi = RetrofitClient.getInstance().createService(CallApi.class);
    }

    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);

        callApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Verifica se tem token
                    if (loginResponse.hasToken()) {
                        // Salvar token no RetrofitClient
                        RetrofitClient.getInstance().setAuthToken(loginResponse.getAccessToken());

                        Log.d(TAG, "Login realizado com sucesso!");
                        Log.d(TAG, "Access Token: " + loginResponse.getAccessToken().substring(0, 20) + "...");
                        Log.d(TAG, "Token Type: " + loginResponse.getTokenType());

                        callback.onSuccess(loginResponse);
                    } else {
                        Log.e(TAG, "Resposta sem token");
                        callback.onError("Resposta inválida do servidor");
                    }
                } else {
                    String errorMsg = "Erro ao fazer login: " + response.code();
                    Log.e(TAG, errorMsg);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorBody);
                            callback.onError(errorMsg + " - " + errorBody);
                        } else {
                            callback.onError(errorMsg);
                        }
                    } catch (Exception e) {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    // Login com Firebase Token
    public void loginWithFirebase(String firebaseToken, AuthCallback callback) {
        FirebaseLoginRequest request = new FirebaseLoginRequest(firebaseToken, "mobile");

        Log.d(TAG, "Iniciando login com Firebase Token");
        Log.d(TAG, "Firebase Token (primeiros 30 chars): " + firebaseToken.substring(0, Math.min(30, firebaseToken.length())) + "...");

        callApi.loginWithFirebase(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Verifica se tem token
                    if (loginResponse.hasToken()) {
                        // Salvar token no RetrofitClient
                        RetrofitClient.getInstance().setAuthToken(loginResponse.getAccessToken());

                        Log.d(TAG, "Login com Firebase realizado com sucesso!");
                        Log.d(TAG, "Access Token: " + loginResponse.getAccessToken().substring(0, Math.min(20, loginResponse.getAccessToken().length())) + "...");
                        Log.d(TAG, "Token Type: " + loginResponse.getTokenType());

                        callback.onSuccess(loginResponse);
                    } else {
                        Log.e(TAG, "Resposta sem token");
                        callback.onError("Resposta inválida do servidor");
                    }
                } else {
                    String errorMsg = "Erro ao fazer login com Firebase: " + response.code();
                    Log.e(TAG, errorMsg);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorBody);
                            callback.onError(errorMsg + " - " + errorBody);
                        } else {
                            callback.onError(errorMsg);
                        }
                    } catch (Exception e) {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao fazer login com Firebase: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void refreshToken(String accessToken, String refreshToken, AuthCallback callback) {
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        CallApi api = retrofitClient.createService(CallApi.class);

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        String authHeader = "Bearer " + accessToken;

        Call<LoginResponse> call = api.refreshToken(authHeader, request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d("AuthRepository", "Token refreshed successfully");
                    callback.onSuccess(loginResponse);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("AuthRepository", "Refresh token failed: " + errorBody);
                        callback.onError("Sessão expirada. Faça login novamente.");
                    } catch (IOException e) {
                        callback.onError("Erro ao renovar sessão");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e("AuthRepository", "Refresh token network error", t);
                callback.onError("Erro de rede ao renovar sessão");
            }
        });
    }

    public void logout(String accessToken, String refreshToken, LogoutCallback callback) {
        String authHeader = "Bearer " + accessToken;
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        Log.d(TAG, "Fazendo logout no backend...");

        callApi.logout(authHeader, request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    RetrofitClient.getInstance().clearAuthToken();
                    Log.d(TAG, "Logout realizado com sucesso no backend");
                    callback.onSuccess();
                } else {
                    // Mesmo com erro no backend, limpar dados locais
                    RetrofitClient.getInstance().clearAuthToken();
                    Log.w(TAG, "Erro no logout do backend (code: " + response.code() + "), mas limpando dados locais");
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                // Mesmo com erro de rede, limpar dados locais
                RetrofitClient.getInstance().clearAuthToken();
                Log.e(TAG, "Falha na rede ao fazer logout, mas limpando dados locais", t);
                callback.onSuccess();
            }
        });
    }

    // Interfaces de callback
    public interface LogoutCallback {
        void onSuccess();
    }

    public interface AuthCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }
}