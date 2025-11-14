package com.example.bemax.repository;

public class SecurityRepository {

    // TODO: Implementar métodos quando backend estiver pronto

    public interface PasswordChangeCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface DeleteAccountCallback {
        void onSuccess();
        void onError(String message);
    }

    public void changePassword(String accessToken, String currentPassword, String newPassword, PasswordChangeCallback callback) {
        // TODO: Implementar chamada ao backend
    }

    public void deleteAccount(String accessToken, String password, DeleteAccountCallback callback) {
        // TODO: Implementar chamada ao backend
    }
}