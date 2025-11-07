package com.example.bemax.model.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("password")
    private String password;

    @SerializedName("cpf")
    private String cpf;

    @SerializedName("phone")
    private String phone;

    @SerializedName("date_birth")
    private String dateBirth;

    // Construtor
    public RegisterRequest(String email, String fullName, String password, String cpf, String phone, String dateBirth) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.cpf = cpf;
        this.phone = phone;
        this.dateBirth = dateBirth;
    }

    // Getters
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPassword() { return password; }
    public String getCpf() { return cpf; }
    public String getPhone() { return phone; }
    public String getDateBirth() { return dateBirth; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password) { this.password = password; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDateBirth(String dateBirth) { this.dateBirth = dateBirth; }
}