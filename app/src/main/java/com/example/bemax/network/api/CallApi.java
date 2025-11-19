package com.example.bemax.network.api;

import com.example.bemax.model.domain.Category;
import com.example.bemax.model.domain.EmergencyContact;
import com.example.bemax.model.domain.HealthProfile;
import com.example.bemax.model.domain.Reminder;
import com.example.bemax.model.dto.ApiResponse;
import com.example.bemax.model.dto.EmergencyContactRequest;
import com.example.bemax.model.dto.FirebaseLoginRequest;
import com.example.bemax.model.dto.HealthProfileRequest;
import com.example.bemax.model.dto.LoginRequest;
import com.example.bemax.model.dto.LoginResponse;
import com.example.bemax.model.dto.MeResponse;
import com.example.bemax.model.dto.RefreshTokenRequest;
import com.example.bemax.model.dto.RegisterRequest;
import com.example.bemax.model.dto.RegisterResponse;
import com.example.bemax.model.dto.ReminderRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CallApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/registry")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("auth/firebase/login")
    Call<LoginResponse> loginWithFirebase(@Body FirebaseLoginRequest request);

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(
            @Header("Authorization") String token,
            @Body RefreshTokenRequest request
    );

    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(
            @Header("Authorization") String authHeader,
            @Body RefreshTokenRequest request
    );

    @GET("me")
    Call<MeResponse> getMe(@Header("Authorization") String authHeader);

    @POST("reminders")
    Call<Reminder> createReminder(
            @Header("Authorization") String authHeader,
            @Body ReminderRequest request
    );

    @PUT("reminders/{id}")
    Call<Reminder> updateReminder(
            @Header("Authorization") String authHeader,
            @Path("id") String reminderId,
            @Body ReminderRequest request
    );

    @DELETE("reminders/{id}")
    Call<ApiResponse<Void>> deleteReminder(
            @Header("Authorization") String authHeader,
            @Path("id") String reminderId
    );

    @GET("reminders")
    Call<List<Reminder>> getReminders(@Header("Authorization") String authHeader);

    @GET("reminder-categories")
    Call<List<Category>> getReminderCategories(@Header("Authorization") String authHeader);

    // Health Profile Endpoints
    @GET("health-profile")
    Call<HealthProfile> getHealthProfile(@Header("Authorization") String authHeader);

    @PUT("health-profile")
    Call<HealthProfile> updateHealthProfile(
            @Header("Authorization") String authHeader,
            @Body HealthProfileRequest request
    );

    // Emergency Contacts Endpoints
    @GET("emergency-contacts")
    Call<List<EmergencyContact>> getEmergencyContacts(@Header("Authorization") String authHeader);

    @POST("emergency-contacts")
    Call<EmergencyContact> createEmergencyContact(
            @Header("Authorization") String authHeader,
            @Body EmergencyContactRequest request
    );

    @PUT("emergency-contacts/{id}")
    Call<com.example.bemax.model.domain.EmergencyContact> updateEmergencyContact(
            @Header("Authorization") String authHeader,
            @Path("id") String contactId,
            @Body EmergencyContactRequest request
    );

    @DELETE("emergency-contacts/{id}")
    Call<ApiResponse<Void>> deleteEmergencyContact(
            @Header("Authorization") String authHeader,
            @Path("id") String contactId
    );
}
