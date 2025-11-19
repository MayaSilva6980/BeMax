package com.example.bemax.network;

import com.example.bemax.network.interceptor.AuthInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final AuthInterceptor authInterceptor;

   private static final String BASE_URL = "http://localhost:3000/";
   // private static final String BASE_URL = "http://10.0.2.2:3000/";

    private RetrofitClient() {
        // Configuração do Gson
        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        // Logging Interceptor (apenas para debug)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth Interceptor
        authInterceptor = new AuthInterceptor();

        // Configuração do OkHttpClient com correções para EOF
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                // ✅ Timeouts aumentados para evitar EOF prematuro
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(90, TimeUnit.SECONDS)  // Timeout total da chamada
                // ✅ Retry automático em caso de falha
                .retryOnConnectionFailure(true)
                // ✅ Forçar HTTP/1.1 (mais estável para localhost)
                .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
                // ✅ Desabilitar compressão automática (pode causar EOF)
                .addNetworkInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder()
                            .removeHeader("Accept-Encoding")  // Remove compressão gzip
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();

        // Configuração do Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public void setAuthToken(String token) {
        authInterceptor.setAuthToken(token);
    }

    public void clearAuthToken() {
        authInterceptor.setAuthToken("");
    }
}
