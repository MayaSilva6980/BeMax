package com.example.bemax.model.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Reminder implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private Category category;

    @SerializedName("status")
    private String status;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("reminder_at")
    private String reminderAt;

    @SerializedName("next_occurrence")
    private String nextOccurrence;

    @SerializedName("is_active")
    private Boolean isActive;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getNextOccurrence() {
        return nextOccurrence;
    }

    public void setNextOccurrence(String nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : false;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
