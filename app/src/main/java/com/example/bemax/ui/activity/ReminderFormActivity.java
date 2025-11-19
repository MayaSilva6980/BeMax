package com.example.bemax.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.bemax.R;
import com.example.bemax.model.domain.Category;
import com.example.bemax.model.domain.Reminder;
import com.example.bemax.model.dto.ReminderRequest;
import com.example.bemax.repository.ReminderRepository;
import com.example.bemax.ui.base.BaseActivity;
import com.example.bemax.util.helper.ErrorHelper;
import com.example.bemax.util.helper.NotificationHelper;
import com.example.bemax.util.security.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderFormActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ReminderFormActivity";

    // Mode
    private boolean isEditMode = false;
    private Reminder existingReminder = null;

    // UI Components
    private Toolbar toolbar;
    private TextInputEditText txtTitle, txtDescription;
    private MaterialCardView cardCategory, cardFrequency, cardStartDate, cardEndDate, cardReminderTime;
    private TextView txtSelectedCategory, txtSelectedFrequency, txtStartDate, txtEndDate, txtReminderTime;
    private ImageView imgCategoryIcon;
    private MaterialButton btnCreateReminder;
    private FrameLayout loadingOverlay;

    // Data
    private TokenManager tokenManager;
    private ReminderRepository reminderRepository;
    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId = null;
    private String selectedCategoryName = null;
    private String selectedFrequency = null;
    private Calendar startDateCalendar = null;
    private Calendar endDateCalendar = null;
    private Calendar reminderTimeCalendar = null;

    // Formatos de data
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_cadastro_lembrete);
            
            tokenManager = TokenManager.getInstance(this);
            tokenManager.setBiometricManager(this);
            reminderRepository = new ReminderRepository();
            
            // Verificar modo de edição
            checkEditMode();
            
            initializeControls();
            loadCategories(); 
            
            // Se é modo de edição, pré-preencher campos
            if (isEditMode && existingReminder != null) {
                prefillFields();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar activity", e);
            e.printStackTrace();
        }
    }

    @Override
    public void obtainParameters() {
        // Verificar modo de edição será feito em checkEditMode()
    }

    /**
     * Verifica se a activity foi aberta em modo de edição
     */
    private void checkEditMode() {
        String mode = getIntent().getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            isEditMode = true;
            existingReminder = (Reminder) getIntent().getSerializableExtra("REMINDER");
            Log.d(TAG, "Modo de EDIÇÃO ativado para: " + (existingReminder != null ? existingReminder.getTitle() : "null"));
        } else {
            isEditMode = false;
            Log.d(TAG, "Modo de CRIAÇÃO ativado");
        }
    }

    @Override
    public void initializeControls() throws Exception {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Ajustar título conforme o modo
            getSupportActionBar().setTitle(isEditMode ? R.string.reminder_edit : R.string.reminder_new);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Input fields
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);

        // Cards
        cardCategory = findViewById(R.id.cardCategory);
        cardFrequency = findViewById(R.id.cardFrequency);
        cardStartDate = findViewById(R.id.cardStartDate);
        cardEndDate = findViewById(R.id.cardEndDate);
        cardReminderTime = findViewById(R.id.cardReminderTime);

        // TextViews
        txtSelectedCategory = findViewById(R.id.txtSelectedCategory);
        txtSelectedFrequency = findViewById(R.id.txtSelectedFrequency);
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        txtReminderTime = findViewById(R.id.txtReminderTime);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);

        // Button
        btnCreateReminder = findViewById(R.id.btnCreateReminder);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        
        // Ajustar texto do botão conforme o modo
        btnCreateReminder.setText(isEditMode ? R.string.reminder_update : R.string.reminder_create);

        // Click listeners
        cardCategory.setOnClickListener(this);
        cardFrequency.setOnClickListener(this);
        cardStartDate.setOnClickListener(this);
        cardEndDate.setOnClickListener(this);
        cardReminderTime.setOnClickListener(this);
        btnCreateReminder.setOnClickListener(this);
    }

    @Override
    public void loadData() throws Exception {
        // Não há dados para carregar
    }

    /**
     * Carrega as categorias do backend
     */
    private void loadCategories() {
        Log.d(TAG, "Iniciando carregamento de categorias...");
        showLoading(true);
        
        tokenManager.getAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                Log.d(TAG, "Token obtido, buscando categorias do backend...");
                
                reminderRepository.getReminderCategories(token, new ReminderRepository.CategoriesCallback() {
                    @Override
                    public void onSuccess(List<Category> loadedCategories) {
                        runOnUiThread(() -> {
                            categories = loadedCategories;
                            showLoading(false);
                            Log.d(TAG, "Categorias carregadas com sucesso: " + categories.size());
                            
                            // Log das categorias
                            for (Category cat : categories) {
                                Log.d(TAG, " - " + cat.getName() + " (" + cat.getId() + ")");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Log.e(TAG, "Erro ao carregar categorias: " + error);
                            ErrorHelper.handleApiError(
                                findViewById(android.R.id.content),
                                "carregar categorias",
                                error
                            );
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "Erro ao obter token: " + error);
                    ErrorHelper.handleAuthError(findViewById(android.R.id.content));
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.cardCategory) {
            showCategoryDialog();
        } else if (id == R.id.cardFrequency) {
            showFrequencyDialog();
        } else if (id == R.id.cardStartDate) {
            showDatePicker(true);
        } else if (id == R.id.cardEndDate) {
            showDatePicker(false);
        } else if (id == R.id.cardReminderTime) {
            showTimePicker();
        } else if (id == R.id.btnCreateReminder) {
            saveReminder();
        }
    }

    /**
     * Mostra dialog de seleção de categoria
     */
    private void showCategoryDialog() {
        if (categories.isEmpty()) {
            NotificationHelper.showInfo(this, "Carregando categorias...");
            return;
        }

        // Converter lista de categorias para array de nomes
        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reminder_category)
                .setItems(categoryNames, (dialog, which) -> {
                    Category selected = categories.get(which);
                    selectedCategoryId = selected.getId();
                    selectedCategoryName = selected.getName();
                    txtSelectedCategory.setText(selectedCategoryName);
                    
                    Log.d(TAG, "Categoria selecionada: " + selectedCategoryName + " (" + selectedCategoryId + ")");
                })
                .show();
    }

    /**
     * Mostra dialog de seleção de frequência
     */
    private void showFrequencyDialog() {
        String[] frequencies = {
                getString(R.string.frequency_once),
                getString(R.string.frequency_daily),
                getString(R.string.frequency_weekly),
                getString(R.string.frequency_monthly),
                getString(R.string.frequency_yearly)
        };

        String[] frequencyValues = {"once", "daily", "weekly", "monthly", "yearly"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reminder_frequency)
                .setItems(frequencies, (dialog, which) -> {
                    selectedFrequency = frequencyValues[which];
                    txtSelectedFrequency.setText(frequencies[which]);
                    Log.d(TAG, "Frequência selecionada: " + selectedFrequency);
                })
                .show();
    }

    /**
     * Mostra dialog de seleção de data
     */
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        startDateCalendar = selectedDate;
                        txtStartDate.setText(displayDateFormat.format(selectedDate.getTime()));
                        Log.d(TAG, "Data de início: " + dateFormat.format(selectedDate.getTime()));
                    } else {
                        endDateCalendar = selectedDate;
                        txtEndDate.setText(displayDateFormat.format(selectedDate.getTime()));
                        Log.d(TAG, "Data de término: " + dateFormat.format(selectedDate.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Mostra dialog de seleção de horário
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderTimeCalendar = Calendar.getInstance();
                    reminderTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderTimeCalendar.set(Calendar.MINUTE, minute);
                    reminderTimeCalendar.set(Calendar.SECOND, 0);

                    txtReminderTime.setText(displayTimeFormat.format(reminderTimeCalendar.getTime()));
                    Log.d(TAG, "Horário selecionado: " + displayTimeFormat.format(reminderTimeCalendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // Formato 24h
        );

        timePickerDialog.show();
    }

    /**
     * Valida e salva o lembrete (criar ou atualizar)
     */
    private void saveReminder() {
        // Validações
        String title = txtTitle.getText() != null ? txtTitle.getText().toString().trim() : "";
        String description = txtDescription.getText() != null ? txtDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            txtTitle.setError(getString(R.string.error_title_required));
            txtTitle.requestFocus();
            return;
        }

        if (selectedCategoryId == null) {
            NotificationHelper.showWarning(this, getString(R.string.error_category_required));
            return;
        }

        if (selectedFrequency == null) {
            NotificationHelper.showWarning(this, getString(R.string.error_frequency_required));
            return;
        }

        if (startDateCalendar == null) {
            NotificationHelper.showWarning(this, getString(R.string.error_start_date_required));
            return;
        }

        if (reminderTimeCalendar == null) {
            NotificationHelper.showWarning(this, getString(R.string.error_reminder_time_required));
            return;
        }

        // Construir reminder_at combinando data de início + horário selecionado
        Calendar reminderAtCalendar = (Calendar) startDateCalendar.clone();
        reminderAtCalendar.set(Calendar.HOUR_OF_DAY, reminderTimeCalendar.get(Calendar.HOUR_OF_DAY));
        reminderAtCalendar.set(Calendar.MINUTE, reminderTimeCalendar.get(Calendar.MINUTE));
        reminderAtCalendar.set(Calendar.SECOND, 0);

        // Criar request
        String startDate = dateFormat.format(startDateCalendar.getTime());
        String endDate = endDateCalendar != null ? dateFormat.format(endDateCalendar.getTime()) : null;
        String reminderAt = isoDateTimeFormat.format(reminderAtCalendar.getTime());

        ReminderRequest request = new ReminderRequest(
                selectedCategoryId,
                title,
                description.isEmpty() ? null : description,
                selectedFrequency,
                startDate,
                endDate,
                reminderAt
        );

        String action = isEditMode ? "Atualizando" : "Criando";
        Log.d(TAG, action + " lembrete:");
        Log.d(TAG, " Título: " + title);
        Log.d(TAG, " Categoria: " + selectedCategoryId);
        Log.d(TAG, " Frequência: " + selectedFrequency);
        Log.d(TAG, " Data início: " + startDate);
        Log.d(TAG, " Data fim: " + endDate);
        Log.d(TAG, " Horário: " + reminderAt);

        // Obter token e salvar lembrete
        showLoading(true);
        btnCreateReminder.setEnabled(false);

        tokenManager.getAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                if (isEditMode) {
                    // MODO DE EDIÇÃO
                    reminderRepository.updateReminder(token, existingReminder.getId(), request, 
                        new ReminderRepository.UpdateReminderCallback() {
                                   @Override
                                   public void onSuccess(Reminder reminder) {
                                       runOnUiThread(() -> {
                                           showLoading(false);
                                           NotificationHelper.showSuccess(
                                               ReminderFormActivity.this,
                                               "Lembrete atualizado com sucesso"
                                           );

                                           Log.d(TAG, "Lembrete atualizado: " + reminder.getId());
                                           finish();
                                       });
                                   }

                                   @Override
                                   public void onError(String error) {
                                       runOnUiThread(() -> {
                                           showLoading(false);
                                           btnCreateReminder.setEnabled(true);
                                           NotificationHelper.showError(
                                               ReminderFormActivity.this,
                                               "Erro ao atualizar lembrete: " + error
                                           );

                                           Log.e(TAG, "Erro ao atualizar lembrete: " + error);
                                       });
                                   }
                        });
                } else {
                    // MODO DE CRIAÇÃO
                    reminderRepository.createReminder(token, request, 
                        new ReminderRepository.CreateReminderCallback() {
                                   @Override
                                   public void onSuccess(Reminder reminder) {
                                       runOnUiThread(() -> {
                                           showLoading(false);
                                           NotificationHelper.showSuccess(
                                               ReminderFormActivity.this,
                                               "Lembrete criado com sucesso"
                                           );

                                           Log.d(TAG, "Lembrete criado: " + reminder.getId());
                                           finish();
                                       });
                                   }

                                   @Override
                                   public void onError(String error) {
                                       runOnUiThread(() -> {
                                           showLoading(false);
                                           btnCreateReminder.setEnabled(true);
                                           NotificationHelper.showError(
                                               ReminderFormActivity.this,
                                               "Erro ao criar lembrete: " + error
                                           );

                                           Log.e(TAG, "Erro ao criar lembrete: " + error);
                                       });
                                   }
                        });
                }
            }

            @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        btnCreateReminder.setEnabled(true);
                        ErrorHelper.handleAuthError(findViewById(android.R.id.content));

                        Log.e(TAG, "Erro ao obter token: " + error);
                    });
                }
        });
    }

    /**
     * Pré-preenche os campos quando em modo de edição
     */
    private void prefillFields() {
        if (existingReminder == null) return;
        
        Log.d(TAG, "Preenchendo campos com dados do lembrete existente");
        
        // Título e descrição
        if (txtTitle != null && existingReminder.getTitle() != null) {
            txtTitle.setText(existingReminder.getTitle());
        }
        
        if (txtDescription != null && existingReminder.getDescription() != null) {
            txtDescription.setText(existingReminder.getDescription());
        }
        
        // Categoria
        if (existingReminder.getCategory() != null) {
            selectedCategoryId = existingReminder.getCategory().getId();
            selectedCategoryName = existingReminder.getCategory().getName();
            txtSelectedCategory.setText(selectedCategoryName);
        }
        
        // Frequência
        selectedFrequency = existingReminder.getFrequency();
        if (selectedFrequency != null) {
            txtSelectedFrequency.setText(getFrequencyDisplayText(selectedFrequency));
        }
        
        // Datas
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            
            // Start date
            if (existingReminder.getStartDate() != null) {
                startDateCalendar = Calendar.getInstance();
                startDateCalendar.setTime(isoFormat.parse(existingReminder.getStartDate()));
                txtStartDate.setText(displayDateFormat.format(startDateCalendar.getTime()));
            }
            
            // End date
            if (existingReminder.getEndDate() != null && !existingReminder.getEndDate().isEmpty()) {
                endDateCalendar = Calendar.getInstance();
                endDateCalendar.setTime(isoFormat.parse(existingReminder.getEndDate()));
                txtEndDate.setText(displayDateFormat.format(endDateCalendar.getTime()));
            }
            
            // Reminder time
            if (existingReminder.getReminderAt() != null) {
                reminderTimeCalendar = Calendar.getInstance();
                reminderTimeCalendar.setTime(isoFormat.parse(existingReminder.getReminderAt()));
                txtReminderTime.setText(displayTimeFormat.format(reminderTimeCalendar.getTime()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parsear datas", e);
        }
    }

    /**
     * Retorna texto amigável para exibir frequência
     */
    private String getFrequencyDisplayText(String frequency) {
        if (frequency == null) return "";
        
        switch (frequency.toLowerCase()) {
            case "once":
                return getString(R.string.frequency_once);
            case "daily":
                return getString(R.string.frequency_daily);
            case "weekly":
                return getString(R.string.frequency_weekly);
            case "monthly":
                return getString(R.string.frequency_monthly);
            case "yearly":
                return getString(R.string.frequency_yearly);
            default:
                return frequency;
        }
    }

    /**
     * Mostra/esconde loading overlay
     */
    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
