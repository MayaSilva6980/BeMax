package com.example.bemax.util;

import android.content.Context;

import com.example.bemax.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class for date and time formatting
 * Handles UTC to local timezone conversion
 */
public class DateTimeHelper {

    /**
     * Formats a UTC ISO datetime string to local time with smart date formatting
     */
    public static String formatReminderDateTime(Context context, String utcDateTimeString) {
        if (utcDateTimeString == null || utcDateTimeString.isEmpty()) {
            return context.getString(R.string.reminder_select_time);
        }

        try {
            // Parse UTC datetime
            Date utcDate = parseUtcDateTime(utcDateTimeString);
            if (utcDate == null) {
                return utcDateTimeString;
            }

            // Get current time in local timezone
            Calendar now = Calendar.getInstance();
            Calendar reminderDate = Calendar.getInstance();
            reminderDate.setTime(utcDate);

            // Format time in local timezone
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeFormat.setTimeZone(TimeZone.getDefault());
            String timeStr = timeFormat.format(utcDate);

            // Check if it's today
            if (isSameDay(now, reminderDate)) {
                return context.getString(R.string.date_today_at, timeStr);
            }

            // Check if it's tomorrow
            Calendar tomorrow = (Calendar) now.clone();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            if (isSameDay(tomorrow, reminderDate)) {
                return context.getString(R.string.date_tomorrow_at, timeStr);
            }

            // Format full date in local timezone
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault());
            String dateStr = dateFormat.format(utcDate);

            return context.getString(R.string.date_full_at, dateStr, timeStr);

        } catch (Exception e) {
            e.printStackTrace();
            return utcDateTimeString;
        }
    }

    private static Date parseUtcDateTime(String utcDateTimeString) {
        if (utcDateTimeString == null || utcDateTimeString.isEmpty()) {
            return null;
        }

        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(utcDateTimeString);
            } catch (ParseException e) {
                // Try next format
            }
        }

        return null;
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}


