package com.example.bemax.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bemax.model.domain.Category;
import com.example.bemax.model.domain.Reminder;
import com.example.bemax.model.dto.ApiResponse;
import com.example.bemax.model.dto.ReminderRequest;
import com.example.bemax.network.RetrofitClient;
import com.example.bemax.network.api.CallApi;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReminderRepository {
    private static final String TAG = "ReminderRepository";
    private final CallApi callApi;

    public ReminderRepository() {
        this.callApi = RetrofitClient.getInstance().createService(CallApi.class);
    }

    /**
     * Cria um novo lembrete
     */
    public void createReminder(String accessToken, ReminderRequest request, CreateReminderCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Criando lembrete: " + request.getTitle());
        Log.d(TAG, "Categoria: " + request.getCategoryId());
        Log.d(TAG, "Frequência: " + request.getFrequency());

        callApi.createReminder(authHeader, request).enqueue(new Callback<Reminder>() {
            @Override
            public void onResponse(@NonNull Call<Reminder> call, @NonNull Response<Reminder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Lembrete criado com sucesso: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao criar lembrete (code: " + response.code() + "): " + errorBody);
                        callback.onError("Erro ao criar lembrete (código: " + response.code() + ")");
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao criar lembrete");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Reminder> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao criar lembrete: " + t.getMessage();
                Log.e(TAG, "Error: " + errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Busca todas as categorias disponíveis
     */
    public void getReminderCategories(String accessToken, CategoriesCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Buscando categorias de lembretes...");
        Log.d(TAG, "Token: " + (accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null"));

        callApi.getReminderCategories(authHeader).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                Log.d(TAG, "Resposta recebida: HTTP " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    Log.d(TAG, "Categorias obtidas com sucesso: " + categories.size());
                    callback.onSuccess(categories);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao buscar categorias (code: " + response.code() + "): " + errorBody);
                        callback.onError("Erro ao buscar categorias (código: " + response.code() + ")");
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao buscar categorias");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao buscar categorias: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                Log.e(TAG, "Tipo do erro: " + t.getClass().getName());
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Atualiza um lembrete existente
     */
    public void updateReminder(String accessToken, String reminderId, ReminderRequest request, UpdateReminderCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Atualizando lembrete: " + reminderId);
        Log.d(TAG, "Título: " + request.getTitle());

        callApi.updateReminder(authHeader, reminderId, request).enqueue(new Callback<Reminder>() {
            @Override
            public void onResponse(@NonNull Call<Reminder> call, @NonNull Response<Reminder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Lembrete atualizado com sucesso: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao atualizar lembrete (code: " + response.code() + "): " + errorBody);
                        callback.onError("Erro ao atualizar lembrete (código: " + response.code() + ")");
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao atualizar lembrete");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Reminder> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao atualizar lembrete: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Deleta um lembrete
     */
    public void deleteReminder(String accessToken, String reminderId, DeleteReminderCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Deletando lembrete: " + reminderId);

        callApi.deleteReminder(authHeader, reminderId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Lembrete deletado com sucesso");
                    callback.onSuccess();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao deletar lembrete (code: " + response.code() + "): " + errorBody);
                        callback.onError("Erro ao deletar lembrete (código: " + response.code() + ")");
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao deletar lembrete");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao deletar lembrete: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Busca todos os lembretes do usuário
     */
    public void getReminders(String accessToken, GetRemindersCallback callback) {
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Buscando lembretes do usuário...");

        callApi.getReminders(authHeader).enqueue(new Callback<List<Reminder>>() {
            @Override
            public void onResponse(@NonNull Call<List<Reminder>> call, @NonNull Response<List<Reminder>> response) {
                Log.d(TAG, "Resposta recebida: HTTP " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Reminder> reminders = response.body();
                    Log.d(TAG, "Lembretes obtidos com sucesso: " + reminders.size());
                    callback.onSuccess(reminders);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao buscar lembretes (code: " + response.code() + "): " + errorBody);
                        callback.onError("Erro ao buscar lembretes (código: " + response.code() + ")");
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                        callback.onError("Erro ao buscar lembretes");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Reminder>> call, @NonNull Throwable t) {
                String errorMsg = "Erro de conexão ao buscar lembretes: " + t.getMessage();
                Log.e(TAG, "" + errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    // Callback interfaces
    public interface GetRemindersCallback {
        void onSuccess(List<Reminder> reminders);
        void onError(String error);
    }

    public interface CreateReminderCallback {
        void onSuccess(Reminder reminder);
        void onError(String error);
    }

    public interface UpdateReminderCallback {
        void onSuccess(Reminder reminder);
        void onError(String error);
    }

    public interface DeleteReminderCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface CategoriesCallback {
        void onSuccess(List<Category> categories);
        void onError(String error);
    }
}