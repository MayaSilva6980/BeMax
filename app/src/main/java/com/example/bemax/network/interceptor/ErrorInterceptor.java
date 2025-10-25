package com.example.bemax.network.interceptor;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ErrorInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (!response.isSuccessful()) {
            switch (response.code()) {
                case 401:
                    // Token expirado - fazer logout
                    break;
                case 403:
                    // Sem permissão
                    break;
                case 404:
                    // Não encontrado
                    break;
                case 500:
                    // Erro no servidor
                    break;
            }
        }

        return response;
    }
}