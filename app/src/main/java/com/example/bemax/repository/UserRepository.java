package com.example.bemax.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bemax.model.dto.MeResponse;
import com.example.bemax.network.RetrofitClient;
import com.example.bemax.network.api.CallApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final CallApi callApi;

    public UserRepository() {
        this.callApi = RetrofitClient.getInstance().createService(CallApi.class);
    }

    public void getMe(String accessToken, MeCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Buscando dados do usuário...");

        callApi.getMe(authHeader).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(@NonNull Call<MeResponse> call, @NonNull Response<MeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeResponse meResponse = response.body();
                    Log.d(TAG, "Dados do usuário obtidos com sucesso!");
                    
                    // Log detalhado
                    if (meResponse.getUser() != null) {
                        Log.d(TAG, "User: " + meResponse.getUser().getFullName());
                    }
                    if (meResponse.getHealthProfile() != null) {
                        Log.d(TAG, "Health Profile: " + meResponse.getHealthProfile().getBloodType());
                    }
                    if (meResponse.getReminders() != null) {
                        Log.d(TAG, "Reminders: " + meResponse.getReminders().size());
                    }
                    if (meResponse.getStats() != null) {
                        Log.d(TAG, "Stats - Total: " + meResponse.getStats().getTotalReminders());
                    }
                    
                    callback.onSuccess(meResponse);
                } else {
                    String errorMsg = "Erro ao buscar dados do usuário: " + response.code();
                    Log.e(TAG, errorMsg);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorBody);
                            callback.onError(errorMsg + " - " + errorBody);
                        } else {
                            callback.onError(errorMsg);
                        }
                    } catch (IOException e) {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeResponse> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao buscar dados do usuário: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public interface MeCallback {
        void onSuccess(MeResponse response);
        void onError(String error);
    }
}
