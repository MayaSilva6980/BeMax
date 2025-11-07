package com.example.bemax.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bemax.model.dto.LoginRequest;
import com.example.bemax.model.dto.LoginResponse;
import com.example.bemax.network.RetrofitClient;
import com.example.bemax.network.api.CallApi;

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

    public void logout() {
        RetrofitClient.getInstance().clearAuthToken();
        Log.d(TAG, "Logout realizado");
    }

    // Interface de callback
    public interface AuthCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }
}