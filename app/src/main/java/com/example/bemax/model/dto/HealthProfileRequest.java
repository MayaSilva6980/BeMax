package com.example.bemax.model.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HealthProfileRequest {
    @SerializedName("blood_type")
    private String bloodType;

    @SerializedName("height")
    private Integer height;

    @SerializedName("weight")
    private Integer weight;

    @SerializedName("allergies")
    private List<String> allergies;

    @SerializedName("medications")
    private List<String> medications;

    @SerializedName("notes")
    private String notes;

    public HealthProfileRequest(String bloodType, Integer height, Integer weight, List<String> allergies, List<String> medications, String notes) {
        this.bloodType = bloodType;
        this.height = height;
        this.weight = weight;
        this.allergies = allergies;
        this.medications = medications;
        this.notes = notes;
    }

    // Getters and Setters
    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getMedications() {
        return medications;
    }

    public void setMedications(List<String> medications) {
        this.medications = medications;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

