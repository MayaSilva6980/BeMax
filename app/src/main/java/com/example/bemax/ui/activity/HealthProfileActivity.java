package com.example.bemax.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.bemax.R;
import com.example.bemax.model.domain.HealthProfile;
import com.example.bemax.repository.HealthProfileRepository;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.helper.ErrorHelper;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.util.security.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HealthProfileActivity extends BaseActivity {
    private static final String TAG = "HealthProfileActivity";
    private static final int REQUEST_EDIT_PROFILE = 100;

    // Views
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private View contentContainer;
    private View emptyStateContainer;
    private FloatingActionButton fabEdit;
    private MaterialButton btnCreateProfile;

    // Data Views
    private TextView txtBloodType;
    private TextView txtHeight;
    private TextView txtWeight;
    private TextView txtAllergies;
    private TextView txtMedications;
    private TextView txtNotes;
    private MaterialCardView cardNotes;

    // Repository
    private HealthProfileRepository repository;
    private TokenManager tokenManager;
    private HealthProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_profile);

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
        // Sem parâmetros extras para esta Activity
    }

    @Override
    public void initializeControls() throws Exception {
        initViews();
        setupListeners();
    }

    @Override
    public void loadData() throws Exception {
        loadHealthProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        contentContainer = findViewById(R.id.contentContainer);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        fabEdit = findViewById(R.id.fabEdit);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);

        txtBloodType = findViewById(R.id.txtBloodType);
        txtHeight = findViewById(R.id.txtHeight);
        txtWeight = findViewById(R.id.txtWeight);
        txtAllergies = findViewById(R.id.txtAllergies);
        txtMedications = findViewById(R.id.txtMedications);
        txtNotes = findViewById(R.id.txtNotes);
        cardNotes = findViewById(R.id.cardNotes);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        fabEdit.setOnClickListener(v -> openEditScreen());
        btnCreateProfile.setOnClickListener(v -> openEditScreen());
    }

    private void loadHealthProfile() {
        showLoading(true);

        tokenManager.getAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                repository.getHealthProfile(token, new HealthProfileRepository.GetHealthProfileCallback() {
                    @Override
                    public void onSuccess(HealthProfile profile) {
                        runOnUiThread(() -> {
                            currentProfile = profile;
                            showLoading(false);
                            displayProfile(profile);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Log.w(TAG, "Erro ao carregar perfil: " + error);

                            // Se for 404, perfil não existe
                            if (error.contains("ainda não foi criado") || error.contains("404")) {
                                showEmptyState();
                            } else {
                                ErrorHelper.handleGenericError(
                                    findViewById(android.R.id.content),
                                    getString(R.string.health_profile_loading_error)
                                );
                            }
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

    private void displayProfile(HealthProfile profile) {
        contentContainer.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        fabEdit.setVisibility(View.VISIBLE);

        // Tipo Sanguíneo
        if (profile.getBloodType() != null && !profile.getBloodType().isEmpty()) {
            txtBloodType.setText(profile.getBloodType());
        } else {
            txtBloodType.setText(R.string.not_available);
        }

        // Altura
        if (profile.getHeight() != null && profile.getHeight() > 0) {
            txtHeight.setText(getString(R.string.height_cm, profile.getHeight()));
        } else {
            txtHeight.setText(R.string.not_available);
        }

        // Peso
        if (profile.getWeight() != null && profile.getWeight() > 0) {
            txtWeight.setText(getString(R.string.weight_kg, profile.getWeight()));
        } else {
            txtWeight.setText(R.string.not_available);
        }

        // Alergias
        List<String> allergies = profile.getAllergies();
        if (allergies != null && !allergies.isEmpty()) {
            StringBuilder allergiesText = new StringBuilder();
            for (int i = 0; i < allergies.size(); i++) {
                allergiesText.append("• ").append(allergies.get(i));
                if (i < allergies.size() - 1) {
                    allergiesText.append("\n");
                }
            }
            txtAllergies.setText(allergiesText.toString());
        } else {
            txtAllergies.setText(R.string.health_profile_no_allergies);
        }

        // Medicamentos
        List<String> medications = profile.getMedications();
        if (medications != null && !medications.isEmpty()) {
            StringBuilder medicationsText = new StringBuilder();
            for (int i = 0; i < medications.size(); i++) {
                medicationsText.append("• ").append(medications.get(i));
                if (i < medications.size() - 1) {
                    medicationsText.append("\n");
                }
            }
            txtMedications.setText(medicationsText.toString());
        } else {
            txtMedications.setText(R.string.health_profile_no_medications);
        }

        // Observações
        if (profile.getNotes() != null && !profile.getNotes().isEmpty()) {
            txtNotes.setText(profile.getNotes());
            cardNotes.setVisibility(View.VISIBLE);
        } else {
            cardNotes.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        contentContainer.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        fabEdit.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentContainer.setVisibility(show ? View.GONE : contentContainer.getVisibility());
        emptyStateContainer.setVisibility(show ? View.GONE : emptyStateContainer.getVisibility());
        fabEdit.setVisibility(show ? View.GONE : fabEdit.getVisibility());
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, HealthProfileFormActivity.class);
        if (currentProfile != null) {
            intent.putExtra("health_profile", currentProfile);
        }
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Recarregar perfil atualizado
            NotificationHelper.showSuccess(this, getString(R.string.health_profile_saved));
            loadHealthProfile();
        }
    }
}

