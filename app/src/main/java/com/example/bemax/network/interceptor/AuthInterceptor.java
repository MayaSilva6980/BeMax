package com.example.bemax.network.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private String authToken;

    public AuthInterceptor() {
        this.authToken = "";
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Se não tem token, prossegue sem adicionar header
        if (authToken == null || authToken.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        // Adiciona o token no header
        Request.Builder builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json");

        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }
}
