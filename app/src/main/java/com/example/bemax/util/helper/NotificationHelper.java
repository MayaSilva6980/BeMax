package com.example.bemax.util.helper;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.bemax.R;

public class NotificationHelper {

    // Tipos de notificação
    public enum NotificationType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    // Duração das notificações
    private static final int DURATION_SHORT = 5000;
    private static final int DURATION_LONG = 7000;

    /**
     * Mostra notificação de SUCESSO
     */
    public static void showSuccess(Activity activity, String message) {
        showNotification(activity, message, NotificationType.SUCCESS, DURATION_SHORT, null);
    }

    public static void showSuccess(Activity activity, String message, View.OnClickListener action) {
        showNotification(activity, message, NotificationType.SUCCESS, DURATION_SHORT, action);
    }

    /**
     * Mostra notificação de ERRO
     */
    public static void showError(Activity activity, String message) {
        showNotification(activity, message, NotificationType.ERROR, DURATION_LONG, null);
    }

    public static void showError(Activity activity, String message, View.OnClickListener action) {
        showNotification(activity, message, NotificationType.ERROR, DURATION_LONG, action);
    }

    /**
     * Mostra notificação de AVISO
     */
    public static void showWarning(Activity activity, String message) {
        showNotification(activity, message, NotificationType.WARNING, DURATION_SHORT, null);
    }

    public static void showWarning(Activity activity, String message, View.OnClickListener action) {
        showNotification(activity, message, NotificationType.WARNING, DURATION_SHORT, action);
    }

    /**
     * Mostra notificação de INFO
     */
    public static void showInfo(Activity activity, String message) {
        showNotification(activity, message, NotificationType.INFO, DURATION_SHORT, null);
    }

    public static void showInfo(Activity activity, String message, View.OnClickListener action) {
        showNotification(activity, message, NotificationType.INFO, DURATION_SHORT, action);
    }

    /**
     * Método principal para exibir notificação customizada
     */
    private static void showNotification(
            Activity activity,
            String message,
            NotificationType type,
            int duration,
            View.OnClickListener action
    ) {
        if (activity == null || activity.isFinishing()) return;

        activity.runOnUiThread(() -> {
            // Inflar o layout customizado
            LayoutInflater inflater = LayoutInflater.from(activity);
            View notificationView = inflater.inflate(R.layout.custom_notification, null);

            // Configurar elementos visuais
            CardView cardView = notificationView.findViewById(R.id.notification_card);
            View accentBar = notificationView.findViewById(R.id.notification_accent_bar);
            ImageView iconView = notificationView.findViewById(R.id.notification_icon);
            TextView messageView = notificationView.findViewById(R.id.notification_message);
            ImageView closeButton = notificationView.findViewById(R.id.notification_close);

            // Configurar mensagem
            messageView.setText(message);

            // Configurar cores e ícone baseado no tipo
            configureNotificationStyle(activity, cardView, accentBar, iconView, type);

            // Adicionar à activity
            ViewGroup rootView = activity.findViewById(android.R.id.content);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(32, 32, 32, 32);
            params.gravity = Gravity.TOP;

            rootView.addView(notificationView, params);

            // Animar entrada
            animateIn(notificationView);

            // Configurar ação (se houver)
            if (action != null) {
                notificationView.setOnClickListener(v -> {
                    action.onClick(v);
                    dismissNotification(notificationView, rootView);
                });
            }

            // Botão de fechar
            closeButton.setOnClickListener(v -> dismissNotification(notificationView, rootView));

            // Auto-dismissal
            notificationView.postDelayed(() -> {
                dismissNotification(notificationView, rootView);
            }, duration);
        });
    }

    /**
     * Configura estilo visual da notificação
     */
    private static void configureNotificationStyle(
            Context context,
            CardView cardView,
            View accentBar,
            ImageView iconView,
            NotificationType type
    ) {
        int backgroundColor;
        int accentColor;
        int iconColor;
        int iconResource;

        switch (type) {
            case SUCCESS:
                backgroundColor = ContextCompat.getColor(context, R.color.success_container);
                accentColor = ContextCompat.getColor(context, R.color.success);
                iconColor = ContextCompat.getColor(context, R.color.success);
                iconResource = R.drawable.ic_check_circle;
                break;

            case ERROR:
                backgroundColor = ContextCompat.getColor(context, R.color.error_container);
                accentColor = ContextCompat.getColor(context, R.color.error);
                iconColor = ContextCompat.getColor(context, R.color.error);
                iconResource = R.drawable.ic_error;
                break;

            case WARNING:
                backgroundColor = ContextCompat.getColor(context, R.color.warning_container);
                accentColor = ContextCompat.getColor(context, R.color.warning);
                iconColor = ContextCompat.getColor(context, R.color.warning);
                iconResource = R.drawable.ic_warning;
                break;

            case INFO:
            default:
                backgroundColor = ContextCompat.getColor(context, R.color.info_container);
                accentColor = ContextCompat.getColor(context, R.color.info);
                iconColor = ContextCompat.getColor(context, R.color.info);
                iconResource = R.drawable.ic_info;
                break;
        }

        cardView.setCardBackgroundColor(backgroundColor);
        accentBar.setBackgroundColor(accentColor);
        iconView.setImageResource(iconResource);
        iconView.setColorFilter(iconColor);
    }

    /**
     * Anima entrada da notificação (desliza de cima)
     */
    private static void animateIn(View view) {
        view.setTranslationY(-300f);
        view.setAlpha(0f);

        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", -300f, 0f);
        translateY.setDuration(400);
        translateY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alpha.setDuration(300);

        translateY.start();
        alpha.start();
    }

    /**
     * Anima saída da notificação (desliza para cima)
     */
    private static void animateOut(View view, Runnable onComplete) {
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 0f, -300f);
        translateY.setDuration(300);
        translateY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        alpha.setDuration(200);

        translateY.start();
        alpha.start();

        view.postDelayed(onComplete, 300);
    }

    /**
     * Remove notificação com animação
     */
    private static void dismissNotification(View notificationView, ViewGroup rootView) {
        if (notificationView.getParent() == null) return;

        animateOut(notificationView, () -> {
            try {
                rootView.removeView(notificationView);
            } catch (Exception e) {
                // Ignorar se já foi removido
            }
        });
    }
}

