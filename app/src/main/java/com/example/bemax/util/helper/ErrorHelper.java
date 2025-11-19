package com.example.bemax.util.helper;

import android.app.Activity;
import android.util.Log;
import android.view.View;

/**
 * Helper para tratamento inteligente de erros do backend
 * Traduz erros técnicos para mensagens amigáveis ao usuário
 * 
 */
public class ErrorHelper {
    private static final String TAG = "ErrorHelper";

    // Flag para alternar entre Snackbar e Notification
    private static boolean useCustomNotifications = true;

    /**
     * Define se deve usar notificações customizadas (padrão: true)
     */
    public static void setUseCustomNotifications(boolean use) {
        useCustomNotifications = use;
    }

    // ==================== MÉTODOS PRINCIPAIS COM SUPORTE A NOTIFICATION ====================

    /**
     * Trata erro de login
     */
    public static void handleLoginError(View view, String error) {
        Log.e(TAG, "Erro de login: " + error);
        
        String userMessage = translateLoginError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), userMessage);
        } else {
            SnackbarHelper.showError(view, userMessage);
        }
    }

    /**
     * Trata erro de registro
     */
    public static void handleRegisterError(View view, String error) {
        Log.e(TAG, "Erro de registro: " + error);
        
        String userMessage = translateRegisterError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), userMessage);
        } else {
            SnackbarHelper.showError(view, userMessage);
        }
    }

    /**
     * Trata erro de API genérico
     */
    public static void handleApiError(View view, String operation, String error) {
        Log.e(TAG, "Erro em " + operation + ": " + error);
        
        String userMessage = translateApiError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), 
                "Erro ao " + operation + ": " + userMessage);
        } else {
            SnackbarHelper.showErrorWithDetails(view, operation, userMessage);
        }
    }

    /**
     * Trata erro de conexão
     */
    public static void handleConnectionError(View view) {
        Log.e(TAG, "Erro de conexão");
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), 
                "Sem conexão com a internet");
        } else {
            SnackbarHelper.showConnectionError(view);
        }
    }

    /**
     * Trata erro de token/autenticação
     */
    public static void handleAuthError(View view) {
        Log.e(TAG, "Erro de autenticação");
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), 
                "Sessão expirada. Faça login novamente.");
        } else {
            SnackbarHelper.showError(view, "Sessão expirada. Faça login novamente.");
        }
    }

    /**
     * Trata erro genérico quando não sabemos o contexto
     */
    public static void handleGenericError(View view, String error) {
        Log.e(TAG, "Erro genérico: " + error);
        
        String userMessage = translateGenericError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), userMessage);
        } else {
            SnackbarHelper.showError(view, userMessage);
        }
    }

    /**
     * Mostra mensagem de sucesso
     */
    public static void showSuccess(View view, String message) {
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showSuccess((Activity) view.getContext(), message);
        } else {
            SnackbarHelper.showSuccess(view, message);
        }
    }

    /**
     * Mostra mensagem de aviso
     */
    public static void showWarning(View view, String message) {
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showWarning((Activity) view.getContext(), message);
        } else {
            SnackbarHelper.showWarning(view, message);
        }
    }

    /**
     * Mostra mensagem informativa
     */
    public static void showInfo(View view, String message) {
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showInfo((Activity) view.getContext(), message);
        } else {
            SnackbarHelper.showInfo(view, message);
        }
    }

    // ==================== TRADUTORES DE ERRO ====================

    /**
     * Traduz erros de login para mensagens amigáveis
     */
    private static String translateLoginError(String error) {
        if (error == null) return "Erro ao fazer login";
        
        String errorLower = error.toLowerCase();
        
        // Erros de credenciais
        if (errorLower.contains("invalid") && errorLower.contains("credentials")) {
            return "E-mail ou senha incorretos";
        }
        if (errorLower.contains("invalid") && errorLower.contains("password")) {
            return "Senha incorreta";
        }
        if (errorLower.contains("invalid") && errorLower.contains("email")) {
            return "E-mail inválido";
        }
        if (errorLower.contains("user") && errorLower.contains("not found")) {
            return "Usuário não encontrado";
        }
        
        // Erros de conta
        if (errorLower.contains("account") && errorLower.contains("locked")) {
            return "Conta bloqueada. Entre em contato com o suporte";
        }
        if (errorLower.contains("account") && errorLower.contains("disabled")) {
            return "Conta desativada";
        }
        if (errorLower.contains("email") && errorLower.contains("not") && errorLower.contains("verified")) {
            return "Verifique seu e-mail antes de fazer login";
        }
        
        // Erros de rede
        if (isNetworkError(errorLower)) {
            return "Erro de conexão. Verifique sua internet";
        }
        
        // Erros de timeout
        if (isTimeoutError(errorLower)) {
            return "Tempo esgotado. Tente novamente";
        }
        
        // Erros de servidor
        if (isServerError(errorLower)) {
            return "Servidor indisponível no momento";
        }
        
        // Erro genérico de login
        return "Erro ao fazer login. Tente novamente";
    }

    /**
     * Traduz erros de registro
     */
    private static String translateRegisterError(String error) {
        if (error == null) return "Erro ao criar conta";
        
        String errorLower = error.toLowerCase();
        
        // Erros de duplicação
        if (errorLower.contains("email") && errorLower.contains("already") || errorLower.contains("exists")) {
            return "E-mail já cadastrado";
        }
        if (errorLower.contains("cpf") && errorLower.contains("already") || errorLower.contains("exists")) {
            return "CPF já cadastrado";
        }
        if (errorLower.contains("phone") && errorLower.contains("already") || errorLower.contains("exists")) {
            return "Telefone já cadastrado";
        }
        
        // Erros de validação
        if (errorLower.contains("invalid") && errorLower.contains("email")) {
            return "E-mail inválido";
        }
        if (errorLower.contains("invalid") && errorLower.contains("cpf")) {
            return "CPF inválido";
        }
        if (errorLower.contains("invalid") && errorLower.contains("phone")) {
            return "Telefone inválido";
        }
        if (errorLower.contains("password") && errorLower.contains("weak")) {
            return "Senha muito fraca. Use letras, números e símbolos";
        }
        if (errorLower.contains("password") && errorLower.contains("short")) {
            return "Senha muito curta. Mínimo 6 caracteres";
        }
        
        // Erros de rede
        if (isNetworkError(errorLower)) {
            return "Erro de conexão. Verifique sua internet";
        }
        
        // Erro genérico de registro
        return "Erro ao criar conta. Tente novamente";
    }

    /**
     * Traduz erros de API genéricos
     */
    private static String translateApiError(String error) {
        if (error == null) return "Erro desconhecido";
        
        String errorLower = error.toLowerCase();
        
        // Erros de autenticação
        if (errorLower.contains("unauthorized") || errorLower.contains("401")) {
            return "Sessão expirada. Faça login novamente";
        }
        if (errorLower.contains("forbidden") || errorLower.contains("403")) {
            return "Você não tem permissão para esta ação";
        }
        if (errorLower.contains("token") && (errorLower.contains("invalid") || errorLower.contains("expired"))) {
            return "Sessão expirada";
        }
        
        // Erros de validação
        if (errorLower.contains("validation") || errorLower.contains("400")) {
            return "Dados inválidos. Verifique os campos";
        }
        if (errorLower.contains("required")) {
            return "Preencha todos os campos obrigatórios";
        }
        
        // Erros de não encontrado
        if (errorLower.contains("not found") || errorLower.contains("404")) {
            return "Item não encontrado";
        }
        
        // Erros de conflito
        if (errorLower.contains("conflict") || errorLower.contains("409")) {
            return "Este item já existe";
        }
        
        // Erros de servidor
        if (isServerError(errorLower)) {
            return "Servidor indisponível. Tente mais tarde";
        }
        
        // Erros de rede
        if (isNetworkError(errorLower)) {
            return "Erro de conexão";
        }
        
        // Erros de timeout
        if (isTimeoutError(errorLower)) {
            return "Tempo esgotado";
        }
        
        // Se for uma mensagem curta e clara, retornar como está
        if (error.length() < 50 && !error.contains("Exception") && !error.contains("Error:")) {
            return error;
        }
        
        // Erro genérico
        return "Algo deu errado. Tente novamente";
    }

    /**
     * Traduz erros genéricos
     */
    private static String translateGenericError(String error) {
        if (error == null) return "Erro desconhecido";
        
        String errorLower = error.toLowerCase();
        
        // Usar os tradutores específicos
        if (isNetworkError(errorLower)) {
            return "Erro de conexão. Verifique sua internet";
        }
        if (isTimeoutError(errorLower)) {
            return "Tempo esgotado. Tente novamente";
        }
        if (isServerError(errorLower)) {
            return "Servidor indisponível no momento";
        }
        
        // Se tiver palavras técnicas, simplificar
        if (error.contains("Exception") || error.contains("Error:") || error.contains("java.")) {
            return "Algo deu errado. Tente novamente";
        }
        
        // Se for curta e clara, manter
        if (error.length() < 60) {
            return error;
        }
        
        // Erro genérico
        return "Erro inesperado. Tente novamente";
    }

    // ==================== HELPERS DE DETECÇÃO ====================

    /**
     * Verifica se é erro de rede
     */
    private static boolean isNetworkError(String errorLower) {
        return errorLower.contains("network") ||
               errorLower.contains("connection") ||
               errorLower.contains("unable to resolve host") ||
               errorLower.contains("failed to connect") ||
               errorLower.contains("no internet");
    }

    /**
     * Verifica se é erro de timeout
     */
    private static boolean isTimeoutError(String errorLower) {
        return errorLower.contains("timeout") ||
               errorLower.contains("time out") ||
               errorLower.contains("timed out");
    }

    /**
     * Verifica se é erro de servidor (5xx)
     */
    private static boolean isServerError(String errorLower) {
        return errorLower.contains("500") ||
               errorLower.contains("502") ||
               errorLower.contains("503") ||
               errorLower.contains("504") ||
               errorLower.contains("internal server error") ||
               errorLower.contains("service unavailable");
    }

    // ==================== MÉTODOS ESPECÍFICOS ADICIONAIS ====================

    /**
     * Trata erro de registro/cadastro
     */
    public static void handleRegistrationError(View view, String error) {
        String friendlyMessage = translateRegistrationError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), friendlyMessage);
        } else {
            SnackbarHelper.showError(view, friendlyMessage);
        }
    }

    /**
     * Trata erro de biometria
     */
    public static void handleBiometricError(View view, String error) {
        String friendlyMessage = translateBiometricError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), friendlyMessage);
        } else {
            SnackbarHelper.showError(view, friendlyMessage);
        }
    }

    /**
     * Trata erro ao salvar token
     */
    public static void handleSaveTokenError(View view, String error) {
        String friendlyMessage = "Erro ao salvar sessão: " + translateGenericError(error);
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), friendlyMessage);
        } else {
            SnackbarHelper.showError(view, friendlyMessage);
        }
    }

    /**
     * Trata erro do Google Sign-In (por código de status)
     */
    public static void handleGoogleSignInError(View view, int statusCode) {
        String friendlyMessage;
        switch (statusCode) {
            case 7:
                friendlyMessage = "Erro de configuração do Google Sign-In. Verifique o app";
                break;
            case 10:
                friendlyMessage = "Erro de desenvolvedor. Configure corretamente o Google Sign-In";
                break;
            case 12501:
                friendlyMessage = "Login cancelado";
                return; // Não mostrar erro se usuário cancelou
            default:
                friendlyMessage = "Erro no login com Google (código: " + statusCode + ")";
        }
        
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), friendlyMessage);
        } else {
            SnackbarHelper.showError(view, friendlyMessage);
        }
    }

    /**
     * Trata erro ao obter Firebase token
     */
    public static void handleFirebaseTokenError(View view) {
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), 
                "Erro ao obter token do Firebase");
        } else {
            SnackbarHelper.showError(view, "Erro ao obter token do Firebase");
        }
    }

    /**
     * Trata erro de autenticação do Firebase
     */
    public static void handleFirebaseAuthError(View view) {
        if (useCustomNotifications && view.getContext() instanceof Activity) {
            NotificationHelper.showError((Activity) view.getContext(), 
                "Erro na autenticação com Firebase");
        } else {
            SnackbarHelper.showError(view, "Erro na autenticação com Firebase");
        }
    }

    /**
     * Traduz erros de registro
     */
    private static String translateRegistrationError(String error) {
        if (error == null) return "Erro ao realizar cadastro";

        String errorLower = error.toLowerCase();

        // Email já existe
        if (errorLower.contains("email") && (errorLower.contains("exists") || errorLower.contains("already") || errorLower.contains("409"))) {
            return "Este e-mail já está cadastrado";
        }

        // CPF já existe
        if (errorLower.contains("cpf") && (errorLower.contains("exists") || errorLower.contains("already"))) {
            return "Este CPF já está cadastrado";
        }

        // Validação
        if (errorLower.contains("validation") || errorLower.contains("invalid")) {
            return "Dados inválidos. Verifique os campos";
        }

        // Servidor
        if (isServerError(errorLower)) {
            return "Servidor indisponível. Tente mais tarde";
        }

        // Rede
        if (isNetworkError(errorLower)) {
            return "Erro de conexão. Verifique sua internet";
        }

        // Genérico
        return "Erro ao realizar cadastro: " + translateGenericError(error);
    }

    /**
     * Traduz erros de biometria
     */
    private static String translateBiometricError(String error) {
        if (error == null) return "Erro de autenticação biométrica";

        String errorLower = error.toLowerCase();

        if (errorLower.contains("cancelad")) {
            return "Autenticação cancelada";
        }

        if (errorLower.contains("lock")) {
            return "Biometria bloqueada. Tente novamente mais tarde";
        }

        if (errorLower.contains("not available") || errorLower.contains("no hardware")) {
            return "Biometria não disponível neste dispositivo";
        }

        if (errorLower.contains("not enrolled") || errorLower.contains("no biometric")) {
            return "Nenhuma biometria cadastrada no dispositivo";
        }

        if (errorLower.contains("failed")) {
            return "Falha na autenticação biométrica";
        }

        return "Erro de biometria: " + error;
    }
}

