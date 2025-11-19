package com.example.bemax.model.dto;

public class EmergencyContactRequest {
    private String name;
    private String relationship;
    private String phone;
    private String email;
    private String notes;

    public EmergencyContactRequest(String name, String relationship, String phone, String email, String notes) {
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

