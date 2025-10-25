package com.example.bemax.network.api;

import com.example.bemax.model.dto.ApiResponse;
import com.example.bemax.model.dto.LoginRequest;
import com.example.bemax.model.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<LoginResponse> register(@Body LoginRequest request);

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);
}