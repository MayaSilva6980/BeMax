package com.example.bemax.util.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.bemax.util.AppConstants;
import com.google.gson.Gson;

public class PreferencesHelper {
    private static final Gson gson = new Gson();

    // Salvar String genérica
    public static void saveString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    // Ler String genérica
    public static String getString(Context context, String chave, String valorPadrao) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(chave, valorPadrao);
    }

    // Salvar Int
    public static void salvarInt(Context context, String chave, int valor) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(chave, valor).apply();
    }

    // Ler Int
    public static int getInt(Context context, String chave, int valorPadrao) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(chave, valorPadrao);
    }

    // Salvar Boolean
    public static void salvarBoolean(Context context, String chave, boolean valor) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(chave, valor).apply();
    }

    // Ler Boolean
    public static boolean getBoolean(Context context, String chave, boolean valorPadrao) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(chave, valorPadrao);
    }

    // Salvar qualquer objeto model
    public static void saveModel(Context context, String chave, Object objeto) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(objeto);
        prefs.edit().putString(chave, json).apply();
    }

    // Ler model genérico
    public static <T> T getModel(Context context, String chave, Class<T> classe) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(chave, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, classe);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Remover chave
    public static void remover(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    // Limpar tudo
    public static void limparPreferencias(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
