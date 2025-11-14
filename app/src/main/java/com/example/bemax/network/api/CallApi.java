package com.example.bemax.network.api;

import com.example.bemax.model.dto.ApiResponse;
import com.example.bemax.model.dto.FirebaseLoginRequest;
import com.example.bemax.model.dto.LoginRequest;
import com.example.bemax.model.dto.LoginResponse;
import com.example.bemax.model.dto.RefreshTokenRequest;
import com.example.bemax.model.dto.RegisterRequest;
import com.example.bemax.model.dto.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

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
}
