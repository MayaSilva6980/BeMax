package com.example.bemax.model.dto;

import com.google.gson.annotations.SerializedName;

public class FirebaseLoginRequest {
    @SerializedName("id_token")
    private String firebaseToken;
    @SerializedName("device_info")
    private String deviceInfo;

    public FirebaseLoginRequest(String firebaseToken, String deviceInfo) {
        this.firebaseToken = firebaseToken;
        this.deviceInfo = deviceInfo;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}

