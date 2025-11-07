package com.example.bemax.model.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("cpf")
    private String cpf;

    @SerializedName("phone")
    private String phone;

    @SerializedName("date_birth")
    private String dateBirth;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    // Getters
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getCpf() { return cpf; }
    public String getPhone() { return phone; }
    public String getDateBirth() { return dateBirth; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDateBirth(String dateBirth) { this.dateBirth = dateBirth; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
