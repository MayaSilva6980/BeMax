package com.example.bemax.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.gson.Gson;

public class Apoio {

    private static final String PREF_NAME = "BeMaxPrefs";
    private static final Gson gson = new Gson();

    // Salvar String genérica
    public static void salvarString(Context context, String chave, String valor) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(chave, valor).apply();
    }

    // Ler String genérica
    public static String getString(Context context, String chave, String valorPadrao) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(chave, valorPadrao);
    }

    // Salvar Int
    public static void salvarInt(Context context, String chave, int valor) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(chave, valor).apply();
    }

    // Ler Int
    public static int getInt(Context context, String chave, int valorPadrao) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(chave, valorPadrao);
    }

    // Salvar Boolean
    public static void salvarBoolean(Context context, String chave, boolean valor) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(chave, valor).apply();
    }

    // Ler Boolean
    public static boolean getBoolean(Context context, String chave, boolean valorPadrao) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(chave, valorPadrao);
    }

    // Salvar qualquer objeto model
    public static void salvarModel(Context context, String chave, Object objeto) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(objeto);
        prefs.edit().putString(chave, json).apply();
    }

    // Ler model genérico
    public static <T> T getModel(Context context, String chave, Class<T> classe) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
    public static void remover(Context context, String chave) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(chave).apply();
    }

    // Limpar tudo
    public static void limparPreferencias(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    public static void aplicarMascara(EditText editText, String mascara) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isUpdating;
            String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString().replaceAll("[^\\d]", "");

                if (isUpdating) {
                    oldText = str;
                    isUpdating = false;
                    return;
                }

                StringBuilder mascaraAplicada = new StringBuilder();
                int i = 0;
                for (char m : mascara.toCharArray()) {
                    if (m != '#' && str.length() > oldText.length()) {
                        mascaraAplicada.append(m);
                        continue;
                    }
                    try {
                        mascaraAplicada.append(str.charAt(i));
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }

                isUpdating = true;
                editText.setText(mascaraAplicada.toString());
                editText.setSelection(mascaraAplicada.length());
            }

        });
    }
}
