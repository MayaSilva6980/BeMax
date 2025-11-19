package com.example.bemax.model.dto;

import com.example.bemax.model.domain.HealthProfile;
import com.example.bemax.model.domain.Reminder;
import com.example.bemax.model.domain.Stats;
import com.example.bemax.model.domain.User;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("user")
    private User user;

    @SerializedName("health_profile")
    private HealthProfile healthProfile;

    @SerializedName("reminders")
    private List<Reminder> reminders;

    @SerializedName("stats")
    private Stats stats;

    // Getters e Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HealthProfile getHealthProfile() {
        return healthProfile;
    }

    public void setHealthProfile(HealthProfile healthProfile) {
        this.healthProfile = healthProfile;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
