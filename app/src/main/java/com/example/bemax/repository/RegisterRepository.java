package com.example.bemax.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bemax.model.dto.RegisterRequest;
import com.example.bemax.model.dto.RegisterResponse;
import com.example.bemax.network.RetrofitClient;
import com.example.bemax.network.api.CallApi;
import com.example.bemax.util.storage.PreferencesHelper;
import com.example.bemax.util.AppConstants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterRepository {

    private static final String TAG = "RegisterRepository";
    private final CallApi callApi;
    private final Context context;

    public RegisterRepository(Context contextParam) {
        context = contextParam;
        this.callApi = RetrofitClient.getInstance().createService(CallApi.class);
    }

        public void register (RegisterRequest request, RegisterCallback callback){
        // Log request details
        Log.d(TAG, "=== REGISTER REQUEST ===");
        Log.d(TAG, "Email: " + request.getEmail());
        Log.d(TAG, "FullName: " + request.getFullName());
        Log.d(TAG, "CPF: " + request.getCpf());
        Log.d(TAG, "Phone: " + request.getPhone());
        Log.d(TAG, "DateBirth: " + request.getDateBirth());
        
        callApi.register(request).enqueue(new Callback<RegisterResponse>() {

            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    Log.d(TAG, "Cadastro realizado com sucesso!");
                    Log.d(TAG, "Response: " + registerResponse);

                    PreferencesHelper.saveModel(context, AppConstants.Prefs_Usuario, registerResponse);
                    callback.onSuccess(registerResponse);
                }
                else {
                    String errorMsg = "Erro ao fazer cadastro: " + response.code();
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
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    // Interface de callback
    public interface RegisterCallback {
        void onSuccess(RegisterResponse response);
        void onError(String error);
    }

}
