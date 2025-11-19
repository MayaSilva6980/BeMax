package com.example.bemax.network.repository;

import android.util.Log;

import com.example.bemax.model.domain.EmergencyContact;
import com.example.bemax.model.dto.ApiResponse;
import com.example.bemax.model.dto.EmergencyContactRequest;
import com.example.bemax.network.api.CallApi;
import com.example.bemax.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyContactRepository {
    private static final String TAG = "EmergencyContactRepo";
    private final CallApi api;

    public EmergencyContactRepository() {
        this.api = RetrofitClient.getInstance().createService(CallApi.class);
    }

    // Callback interfaces
    public interface OnContactsLoadedCallback {
        void onSuccess(List<EmergencyContact> contacts);
        void onError(String error);
    }

    public interface OnContactSavedCallback {
        void onSuccess(EmergencyContact contact);
        void onError(String error);
    }

    public interface OnContactDeletedCallback {
        void onSuccess();
        void onError(String error);
    }

    // Get all emergency contacts
    public void getEmergencyContacts(String token, OnContactsLoadedCallback callback) {
        String authHeader = "Bearer " + token;
        
        api.getEmergencyContacts(authHeader).enqueue(new Callback<List<EmergencyContact>>() {
            @Override
            public void onResponse(Call<List<EmergencyContact>> call, Response<List<EmergencyContact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Contacts loaded successfully: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Error: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<EmergencyContact>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Create new emergency contact
    public void createEmergencyContact(String token, EmergencyContactRequest request, OnContactSavedCallback callback) {
        String authHeader = "Bearer " + token;
        
        api.createEmergencyContact(authHeader, request).enqueue(new Callback<EmergencyContact>() {
            @Override
            public void onResponse(Call<EmergencyContact> call, Response<EmergencyContact> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Contact created successfully");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Error: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<EmergencyContact> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Update existing emergency contact
    public void updateEmergencyContact(String token, String contactId, EmergencyContactRequest request, OnContactSavedCallback callback) {
        String authHeader = "Bearer " + token;
        
        api.updateEmergencyContact(authHeader, contactId, request).enqueue(new Callback<EmergencyContact>() {
            @Override
            public void onResponse(Call<EmergencyContact> call, Response<EmergencyContact> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Contact updated successfully");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Error: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<EmergencyContact> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Delete emergency contact
    public void deleteEmergencyContact(String token, String contactId, OnContactDeletedCallback callback) {
        String authHeader = "Bearer " + token;
        
        api.deleteEmergencyContact(authHeader, contactId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Contact deleted successfully");
                    callback.onSuccess();
                } else {
                    String error = "Error: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
}

