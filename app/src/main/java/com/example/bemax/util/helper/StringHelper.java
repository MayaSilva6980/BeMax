package com.example.bemax.util.helper;

import android.content.Context;
import com.example.bemax.R;
import java.util.Calendar;

/**
 * Utilitários para manipulação de strings e formatação
 */
public class StringHelper {
    public static String getGreeting(Context context) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 6) {
            // 00:00 - 05:59 = Dawn
            return context.getString(R.string.greeting_good_dawn);
        } else if (hour >= 6 && hour < 12) {
            // 06:00 - 11:59 = Morning
            return context.getString(R.string.greeting_good_morning);
        } else if (hour >= 12 && hour < 18) {
            // 12:00 - 17:59 = Afternoon
            return context.getString(R.string.greeting_good_afternoon);
        } else {
            // 18:00 - 23:59 = Evening
            return context.getString(R.string.greeting_good_evening);
        }
    }

    public static String getFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Usuário";
        }

        String[] parts = fullName.trim().split(" ");
        return parts[0];
    }

    public static String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }

        String[] parts = fullName.trim().split(" ");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }
}