package com.example.bemax.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;

import com.example.bemax.R;
import com.example.bemax.model.domain.EmergencyContact;
import com.example.bemax.model.dto.EmergencyContactRequest;
import com.example.bemax.network.repository.EmergencyContactRepository;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.helper.InputMaskHelper;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.util.security.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class EmergencyContactFormActivity extends BaseActivity {
    private static final String TAG = "ContactForm";

    private MaterialToolbar toolbar;
    private TextInputLayout layoutContactName, layoutRelationship, layoutPhone, layoutEmail, layoutNotes;
    private TextInputEditText edtContactName, edtPhone, edtEmail, edtNotes;
    private AutoCompleteTextView edtRelationship;
    private MaterialButton btnSave;
    private FrameLayout progressOverlay;

    private EmergencyContactRepository repository;
    private TokenManager tokenManager;
    private EmergencyContact contactToEdit;
    private boolean isEditMode = false;
    
    // Map para converter display name -> backend value
    private final Map<String, String> relationshipMap = new HashMap<>();
    private final Map<String, String> relationshipReverseMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact_form);

        repository = new EmergencyContactRepository();
        tokenManager = TokenManager.getInstance(this);
        tokenManager.setBiometricManager(this);

        obtainParameters();
        initializeControls();
        loadData();
    }

    @Override
    public void obtainParameters() {
        if (getIntent().hasExtra("contact")) {
            contactToEdit = (EmergencyContact) getIntent().getSerializableExtra("contact");
            isEditMode = contactToEdit != null;
        }
    }

    @Override
    public void initializeControls() {
        toolbar = findViewById(R.id.toolbar);
        layoutContactName = findViewById(R.id.layoutContactName);
        layoutRelationship = findViewById(R.id.layoutRelationship);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutNotes = findViewById(R.id.layoutNotes);
        edtContactName = findViewById(R.id.edtContactName);
        edtRelationship = findViewById(R.id.edtRelationship);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtNotes = findViewById(R.id.edtNotes);
        btnSave = findViewById(R.id.btnSave);
        progressOverlay = findViewById(R.id.progressOverlay);

        toolbar.setTitle(isEditMode ? R.string.emergency_contact_edit : R.string.emergency_contact_add);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup relationship dropdown
        setupRelationshipDropdown();
        
        // Add phone mask (##) #####-####
        InputMaskHelper.aplicarMascara(edtPhone, "(##) #####-####");

        btnSave.setOnClickListener(v -> saveContact());
    }
    
    private void setupRelationshipDropdown() {
        // Populate maps
        relationshipMap.put(getString(R.string.relationship_spouse), "spouse");
        relationshipMap.put(getString(R.string.relationship_parent), "parent");
        relationshipMap.put(getString(R.string.relationship_child), "child");
        relationshipMap.put(getString(R.string.relationship_sibling), "sibling");
        relationshipMap.put(getString(R.string.relationship_friend), "friend");
        relationshipMap.put(getString(R.string.relationship_doctor), "doctor");
        relationshipMap.put(getString(R.string.relationship_caregiver), "caregiver");
        relationshipMap.put(getString(R.string.relationship_other), "other");
        
        // Reverse map
        for (Map.Entry<String, String> entry : relationshipMap.entrySet()) {
            relationshipReverseMap.put(entry.getValue(), entry.getKey());
        }
        
        // Create adapter
        String[] relationships = {
            getString(R.string.relationship_spouse),
            getString(R.string.relationship_parent),
            getString(R.string.relationship_child),
            getString(R.string.relationship_sibling),
            getString(R.string.relationship_friend),
            getString(R.string.relationship_doctor),
            getString(R.string.relationship_caregiver),
            getString(R.string.relationship_other)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            relationships
        );
        
        edtRelationship.setAdapter(adapter);
    }

    @Override
    public void loadData() {
        if (isEditMode && contactToEdit != null) {
            edtContactName.setText(contactToEdit.getName());
            
            // Convert backend value to display name
            String displayName = relationshipReverseMap.get(contactToEdit.getRelationship());
            if (displayName != null) {
                edtRelationship.setText(displayName, false);
            }
            
            edtPhone.setText(contactToEdit.getPhone());
            edtEmail.setText(contactToEdit.getEmail());
            edtNotes.setText(contactToEdit.getNotes());
        }
    }

    private void saveContact() {
        if (!validateForm()) return;

        String name = edtContactName.getText().toString().trim();
        String relationshipDisplay = edtRelationship.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();
        
        // Convert display name to backend value
        String relationshipValue = relationshipMap.get(relationshipDisplay);
        if (relationshipValue == null) {
            relationshipValue = "other"; // Default fallback
        }

        EmergencyContactRequest request = new EmergencyContactRequest(name, relationshipValue, phone, 
            email.isEmpty() ? null : email, notes.isEmpty() ? null : notes);

        showLoading();
        tokenManager.getAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                if (isEditMode) {
                    updateContact(token, request);
                } else {
                    createContact(token, request);
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    NotificationHelper.showError(EmergencyContactFormActivity.this, error);
                });
            }
        });
    }

    private void createContact(String token, EmergencyContactRequest request) {
        repository.createEmergencyContact(token, request, 
            new EmergencyContactRepository.OnContactSavedCallback() {
            @Override
            public void onSuccess(EmergencyContact contact) {
                runOnUiThread(() -> {
                    hideLoading();
                    NotificationHelper.showSuccess(EmergencyContactFormActivity.this, 
                        getString(R.string.contact_created_success));
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> {
                    hideLoading();
                    NotificationHelper.showError(EmergencyContactFormActivity.this, 
                        getString(R.string.contact_create_error));
                });
            }
        });
    }

    private void updateContact(String token, EmergencyContactRequest request) {
        repository.updateEmergencyContact(token, contactToEdit.getId(), request, 
            new EmergencyContactRepository.OnContactSavedCallback() {
            @Override
            public void onSuccess(EmergencyContact contact) {
                runOnUiThread(() -> {
                    hideLoading();
                    NotificationHelper.showSuccess(EmergencyContactFormActivity.this, 
                        getString(R.string.contact_updated_success));
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> {
                    hideLoading();
                    NotificationHelper.showError(EmergencyContactFormActivity.this, 
                        getString(R.string.contact_update_error));
                });
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        String name = edtContactName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            layoutContactName.setError(getString(R.string.error_contact_name_required));
            isValid = false;
        } else {
            layoutContactName.setError(null);
        }

        String relationship = edtRelationship.getText().toString().trim();
        if (TextUtils.isEmpty(relationship)) {
            layoutRelationship.setError(getString(R.string.error_contact_relationship_required));
            isValid = false;
        } else {
            layoutRelationship.setError(null);
        }

        String phone = edtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            layoutPhone.setError(getString(R.string.error_contact_phone_required));
            isValid = false;
        } else if (phone.replaceAll("[^\\d]", "").length() < 10) {
            layoutPhone.setError(getString(R.string.error_contact_phone_invalid));
            isValid = false;
        } else {
            layoutPhone.setError(null);
        }
        
        // Validate email (optional but if provided, must be valid)
        String email = edtEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_contact_email_invalid));
            isValid = false;
        } else {
            layoutEmail.setError(null);
        }

        return isValid;
    }

    private void showLoading() {
        progressOverlay.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
    }

    private void hideLoading() {
        progressOverlay.setVisibility(View.GONE);
        btnSave.setEnabled(true);
    }
}

