package com.example.bemax.model.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Stats implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("total_reminders")
    private Integer totalReminders;

    @SerializedName("active_reminders")
    private Integer activeReminders;

    @SerializedName("upcoming_reminders")
    private Integer upcomingReminders;

    @SerializedName("today_reminders")
    private Integer todayReminders;

    // Getters e Setters
    public Integer getTotalReminders() {
        return totalReminders != null ? totalReminders : 0;
    }

    public void setTotalReminders(Integer totalReminders) {
        this.totalReminders = totalReminders;
    }

    public Integer getActiveReminders() {
        return activeReminders != null ? activeReminders : 0;
    }

    public void setActiveReminders(Integer activeReminders) {
        this.activeReminders = activeReminders;
    }

    public Integer getUpcomingReminders() {
        return upcomingReminders != null ? upcomingReminders : 0;
    }

    public void setUpcomingReminders(Integer upcomingReminders) {
        this.upcomingReminders = upcomingReminders;
    }

    public Integer getTodayReminders() {
        return todayReminders != null ? todayReminders : 0;
    }

    public void setTodayReminders(Integer todayReminders) {
        this.todayReminders = todayReminders;
    }
}
