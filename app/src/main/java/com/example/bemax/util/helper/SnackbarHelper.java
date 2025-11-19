package com.example.bemax.util.helper;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.example.bemax.R;

/**
 * Helper para exibir mensagens elegantes usando Snackbar Material Design 3
 * Substitui Toast tradicional por feedback visual mais rico
 */
public class SnackbarHelper {

    public enum MessageType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Mostra Snackbar de sucesso (verde)
     */
    public static void showSuccess(View view, String message) {
        show(view, message, MessageType.SUCCESS);
    }

    /**
     * Mostra Snackbar de sucesso com ação
     */
    public static void showSuccess(View view, String message, String actionText, View.OnClickListener actionListener) {
        show(view, message, MessageType.SUCCESS, actionText, actionListener);
    }

    /**
     * Mostra Snackbar de erro (vermelho)
     */
    public static void showError(View view, String message) {
        show(view, message, MessageType.ERROR);
    }

    /**
     * Mostra Snackbar de erro com ação
     */
    public static void showError(View view, String message, String actionText, View.OnClickListener actionListener) {
        show(view, message, MessageType.ERROR, actionText, actionListener);
    }

    /**
     * Mostra Snackbar de aviso (amarelo)
     */
    public static void showWarning(View view, String message) {
        show(view, message, MessageType.WARNING);
    }

    /**
     * Mostra Snackbar de informação (azul)
     */
    public static void showInfo(View view, String message) {
        show(view, message, MessageType.INFO);
    }

    /**
     * Mostra Snackbar customizado
     */
    private static void show(View view, String message, MessageType type) {
        show(view, message, type, null, null);
    }

    /**
     * Mostra Snackbar customizado com ação
     */
    private static void show(View view, String message, MessageType type, String actionText, View.OnClickListener actionListener) {
        if (view == null) return;

        int duration = (type == MessageType.ERROR) ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT;
        Snackbar snackbar = Snackbar.make(view, message, duration);

        // Configurar cores baseado no tipo
        int backgroundColor;
        int textColor = view.getContext().getColor(R.color.white);

        switch (type) {
            case SUCCESS:
                backgroundColor = view.getContext().getColor(R.color.status_success);
                message = "✓ " + message;
                break;
            case ERROR:
                backgroundColor = view.getContext().getColor(R.color.status_error);
                message = "✗ " + message;
                break;
            case WARNING:
                backgroundColor = view.getContext().getColor(R.color.status_warning);
                message = "⚠ " + message;
                textColor = view.getContext().getColor(R.color.on_surface);
                break;
            case INFO:
            default:
                backgroundColor = view.getContext().getColor(R.color.status_info);
                message = "ℹ " + message;
                break;
        }

        // Aplicar estilo
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.setTextColor(textColor);
        snackbar.setText(message);

        // Adicionar ação se fornecida
        if (actionText != null && actionListener != null) {
            snackbar.setAction(actionText, actionListener);
            snackbar.setActionTextColor(textColor);
        }

        snackbar.show();
    }

    /**
     * Mostra Snackbar de sucesso ao criar/atualizar
     */
    public static void showCreatedSuccess(View view, String itemName) {
        showSuccess(view, itemName + " criado com sucesso!");
    }

    /**
     * Mostra Snackbar de sucesso ao atualizar
     */
    public static void showUpdatedSuccess(View view, String itemName) {
        showSuccess(view, itemName + " atualizado com sucesso!");
    }

    /**
     * Mostra Snackbar de sucesso ao deletar
     */
    public static void showDeletedSuccess(View view, String itemName) {
        showSuccess(view, itemName + " excluído com sucesso!");
    }

    /**
     * Mostra Snackbar de erro genérico
     */
    public static void showGenericError(View view) {
        showError(view, "Algo deu errado. Tente novamente.");
    }

    /**
     * Mostra Snackbar de erro de conexão
     */
    public static void showConnectionError(View view) {
        showError(view, "Erro de conexão. Verifique sua internet.");
    }

    /**
     * Mostra Snackbar de erro com detalhes
     */
    public static void showErrorWithDetails(View view, String operation, String error) {
        String message = "Erro ao " + operation + ": " + error;
        showError(view, message);
    }
}

