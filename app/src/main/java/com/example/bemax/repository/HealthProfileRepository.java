package com.example.bemax.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bemax.model.domain.HealthProfile;
import com.example.bemax.model.dto.HealthProfileRequest;
import com.example.bemax.network.RetrofitClient;
import com.example.bemax.network.api.CallApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthProfileRepository {
    private static final String TAG = "HealthProfileRepository";
    private final CallApi callApi;

    public HealthProfileRepository() {
        this.callApi = RetrofitClient.getInstance().createService(CallApi.class);
    }

    /**
     * Busca o perfil de saúde do usuário
     */
    public void getHealthProfile(String accessToken, GetHealthProfileCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Buscando perfil de saúde...");

        callApi.getHealthProfile(authHeader).enqueue(new Callback<HealthProfile>() {
            @Override
            public void onResponse(@NonNull Call<HealthProfile> call, @NonNull Response<HealthProfile> response) {
                Log.d(TAG, "Resposta recebida: HTTP " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    HealthProfile profile = response.body();
                    Log.d(TAG, "Perfil de saúde obtido com sucesso");
                    Log.d(TAG, " Tipo sanguíneo: " + profile.getBloodType());
                    Log.d(TAG, " Altura: " + profile.getHeight());
                    Log.d(TAG, " Peso: " + profile.getWeight());
                    callback.onSuccess(profile);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao buscar perfil (code: " + response.code() + "): " + errorBody);
                        
                        // Se for 404, perfil ainda não foi criado
                        if (response.code() == 404) {
                            callback.onError("Perfil de saúde ainda não foi criado");
                        } else {
                            callback.onError("Erro ao buscar perfil de saúde (código: " + response.code() + ")");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao buscar perfil de saúde");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<HealthProfile> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao buscar perfil: " + t.getMessage();
                Log.e(TAG, "" + errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Atualiza o perfil de saúde do usuário
     */
    public void updateHealthProfile(String accessToken, HealthProfileRequest request, UpdateHealthProfileCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Atualizando perfil de saúde...");
        Log.d(TAG, " Tipo sanguíneo: " + request.getBloodType());
        Log.d(TAG, " Altura: " + request.getHeight());
        Log.d(TAG, " Peso: " + request.getWeight());

        callApi.updateHealthProfile(authHeader, request).enqueue(new Callback<HealthProfile>() {
            @Override
            public void onResponse(@NonNull Call<HealthProfile> call, @NonNull Response<HealthProfile> response) {
                Log.d(TAG, "Resposta recebida: HTTP " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    HealthProfile profile = response.body();
                    Log.d(TAG, "Perfil de saúde atualizado com sucesso");
                    callback.onSuccess(profile);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao atualizar perfil (code: " + response.code() + "): " + errorBody);
                        callback.onError("Erro ao atualizar perfil de saúde (código: " + response.code() + ")");
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao atualizar perfil de saúde");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<HealthProfile> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao atualizar perfil: " + t.getMessage();
                Log.e(TAG, "" + errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    // Callback interfaces
    public interface GetHealthProfileCallback {
        void onSuccess(HealthProfile profile);
        void onError(String error);
    }

    public interface UpdateHealthProfileCallback {
        void onSuccess(HealthProfile profile);
        void onError(String error);
    }
}

