package com.example.bemax.model.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Role implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name; // "ADMIN", "USER", "MODERATOR", etc.

    @SerializedName("description")
    private String description;

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}