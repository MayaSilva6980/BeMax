package com.example.bemax.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.bemax.R;
import com.example.bemax.model.domain.HealthProfile;
import com.example.bemax.model.dto.HealthProfileRequest;
import com.example.bemax.repository.HealthProfileRepository;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.helper.ErrorHelper;
import com.example.bemax.util.security.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthProfileFormActivity extends BaseActivity {
    private static final String TAG = "HealthProfileFormActivity";

    // Views
    private MaterialToolbar toolbar;
    private AutoCompleteTextView actvBloodType;
    private TextInputEditText edtHeight;
    private TextInputEditText edtWeight;
    private TextInputEditText edtAllergies;
    private TextInputEditText edtMedications;
    private TextInputEditText edtNotes;
    private MaterialButton btnSave;
    private View loadingOverlay;

    // Layouts for error handling
    private TextInputLayout tilBloodType;
    private TextInputLayout tilHeight;
    private TextInputLayout tilWeight;

    // Repository
    private HealthProfileRepository repository;
    private TokenManager tokenManager;
    private HealthProfile existingProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_profile_form);

        repository = new HealthProfileRepository();
        tokenManager = TokenManager.getInstance(this);
        tokenManager.setBiometricManager(this);

        try {
            obtainParameters();
            initializeControls();
            loadData();
        } catch (Exception e) {
            Log.e(TAG, "Erro no onCreate: " + e.getMessage(), e);
        }
    }

    @Override
    public void obtainParameters() {
        // Verificar se está editando perfil existente
        if (getIntent().hasExtra("health_profile")) {
            existingProfile = (HealthProfile) getIntent().getSerializableExtra("health_profile");
        }
    }

    @Override
    public void initializeControls() throws Exception {
        initViews();
        setupBloodTypeDropdown();
        setupListeners();
    }

    @Override
    public void loadData() throws Exception {
        if (existingProfile != null) {
            prefillFields();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        actvBloodType = findViewById(R.id.actvBloodType);
        edtHeight = findViewById(R.id.edtHeight);
        edtWeight = findViewById(R.id.edtWeight);
        edtAllergies = findViewById(R.id.edtAllergies);
        edtMedications = findViewById(R.id.edtMedications);
        edtNotes = findViewById(R.id.edtNotes);
        btnSave = findViewById(R.id.btnSave);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        tilBloodType = findViewById(R.id.tilBloodType);
        tilHeight = findViewById(R.id.tilHeight);
        tilWeight = findViewById(R.id.tilWeight);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupBloodTypeDropdown() {
        String[] bloodTypes = {
                getString(R.string.blood_type_a_positive),
                getString(R.string.blood_type_a_negative),
                getString(R.string.blood_type_b_positive),
                getString(R.string.blood_type_b_negative),
                getString(R.string.blood_type_ab_positive),
                getString(R.string.blood_type_ab_negative),
                getString(R.string.blood_type_o_positive),
                getString(R.string.blood_type_o_negative)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                bloodTypes
        );
        actvBloodType.setAdapter(adapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveHealthProfile());
    }

    private void prefillFields() {
        if (existingProfile == null) return;

        // Tipo Sanguíneo
        if (existingProfile.getBloodType() != null) {
            actvBloodType.setText(existingProfile.getBloodType(), false);
        }

        // Altura
        if (existingProfile.getHeight() != null && existingProfile.getHeight() > 0) {
            edtHeight.setText(String.valueOf(existingProfile.getHeight()));
        }

        // Peso
        if (existingProfile.getWeight() != null && existingProfile.getWeight() > 0) {
            edtWeight.setText(String.valueOf(existingProfile.getWeight()));
        }

        // Alergias
        if (existingProfile.getAllergies() != null && !existingProfile.getAllergies().isEmpty()) {
            StringBuilder allergiesText = new StringBuilder();
            for (int i = 0; i < existingProfile.getAllergies().size(); i++) {
                allergiesText.append(existingProfile.getAllergies().get(i));
                if (i < existingProfile.getAllergies().size() - 1) {
                    allergiesText.append("\n");
                }
            }
            edtAllergies.setText(allergiesText.toString());
        }

        // Medicamentos
        if (existingProfile.getMedications() != null && !existingProfile.getMedications().isEmpty()) {
            StringBuilder medicationsText = new StringBuilder();
            for (int i = 0; i < existingProfile.getMedications().size(); i++) {
                medicationsText.append(existingProfile.getMedications().get(i));
                if (i < existingProfile.getMedications().size() - 1) {
                    medicationsText.append("\n");
                }
            }
            edtMedications.setText(medicationsText.toString());
        }

        // Observações
        if (existingProfile.getNotes() != null && !existingProfile.getNotes().isEmpty()) {
            edtNotes.setText(existingProfile.getNotes());
        }
    }

    private void saveHealthProfile() {
        // Limpar erros
        tilBloodType.setError(null);
        tilHeight.setError(null);
        tilWeight.setError(null);

        // Validar campos
        String bloodType = actvBloodType.getText().toString().trim();
        String heightStr = edtHeight.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();
        String allergiesText = edtAllergies.getText().toString().trim();
        String medicationsText = edtMedications.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();

        boolean hasError = false;

        // Tipo sanguíneo é opcional mas se preenchido deve ser válido
        if (!bloodType.isEmpty()) {
            String[] validTypes = {
                    getString(R.string.blood_type_a_positive),
                    getString(R.string.blood_type_a_negative),
                    getString(R.string.blood_type_b_positive),
                    getString(R.string.blood_type_b_negative),
                    getString(R.string.blood_type_ab_positive),
                    getString(R.string.blood_type_ab_negative),
                    getString(R.string.blood_type_o_positive),
                    getString(R.string.blood_type_o_negative)
            };
            if (!Arrays.asList(validTypes).contains(bloodType)) {
                tilBloodType.setError("Selecione um tipo sanguíneo válido");
                hasError = true;
            }
        }

        // Converter altura e peso
        Integer height = null;
        if (!heightStr.isEmpty()) {
            try {
                height = Integer.parseInt(heightStr);
                if (height < 50 || height > 250) {
                    tilHeight.setError("Altura deve estar entre 50 e 250 cm");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilHeight.setError("Altura inválida");
                hasError = true;
            }
        }

        Integer weight = null;
        if (!weightStr.isEmpty()) {
            try {
                weight = Integer.parseInt(weightStr);
                if (weight < 20 || weight > 300) {
                    tilWeight.setError("Peso deve estar entre 20 e 300 kg");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError("Peso inválido");
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        // Converter alergias e medicamentos em listas
        List<String> allergies = parseTextToList(allergiesText);
        List<String> medications = parseTextToList(medicationsText);

        // Criar request
        HealthProfileRequest request = new HealthProfileRequest(
                bloodType.isEmpty() ? null : bloodType,
                height,
                weight,
                allergies.isEmpty() ? null : allergies,
                medications.isEmpty() ? null : medications,
                notes.isEmpty() ? null : notes
        );

        // Salvar
        showLoading(true);

        tokenManager.getAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                repository.updateHealthProfile(token, request, new HealthProfileRepository.UpdateHealthProfileCallback() {
                    @Override
                    public void onSuccess(HealthProfile profile) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Log.d(TAG, "Perfil de saúde salvo com sucesso");
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Log.e(TAG, "Erro ao salvar perfil: " + error);
                            ErrorHelper.handleGenericError(
                                findViewById(android.R.id.content),
                                getString(R.string.health_profile_error)
                            );
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    ErrorHelper.handleGenericError(
                        findViewById(android.R.id.content),
                        "Erro ao obter token: " + error
                    );
                });
            }
        });
    }

    /**
     * Converte texto com quebras de linha em lista de strings
     */
    private List<String> parseTextToList(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}

