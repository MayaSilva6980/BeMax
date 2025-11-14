package com.example.bemax.util.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class InputMaskHelper {
    public static void aplicarMascara(EditText editText, String mask) {
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

                StringBuilder maskedText = new StringBuilder();
                int i = 0;
                for (char m : mask.toCharArray()) {
                    if (m != '#' && str.length() > oldText.length()) {
                        maskedText.append(m);
                        continue;
                    }
                    try {
                        maskedText.append(str.charAt(i));
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }

                isUpdating = true;
                editText.setText(maskedText.toString());
                editText.setSelection(maskedText.length());
            }
        });
    }
}
