package com.example.bemax.model.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para criação de lembretes
 */
public class ReminderRequest {
    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("reminder_at")
    private String reminderAt;

    // Constructor
    public ReminderRequest(String categoryId, String title, String description, 
                          String frequency, String startDate, String endDate, String reminderAt) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reminderAt = reminderAt;
    }

    // Getters and Setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(String reminderAt) {
        this.reminderAt = reminderAt;
    }
}

