package com.example.bemax.telas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bemax.R;
import com.example.bemax.util.BaseActivity;

public class FrmInfoMedica extends BaseActivity implements  View.OnClickListener
{
    // TextInputEditText
    private EditText txtTipoSanguineo;
    private EditText txtAlergias;
    private EditText txtDoencasCronicas;
    private EditText txtMedicamentos;
    private EditText txtHistoricoMedico;

    // Buttons e TextViews
    private Button btnSalvarMedico;
    private TextView btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_info_medica);
            iniciaControles();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void obtemParametros() {

    }

    @Override
    public void iniciaControles() throws Exception
    {
        txtTipoSanguineo = findViewById(R.id.txtTipoSanguineo);
        txtAlergias = findViewById(R.id.txtAlergias);
        txtDoencasCronicas = findViewById(R.id.txtDoencasCronicas);
        txtMedicamentos = findViewById(R.id.txtMedicamentos);
        txtHistoricoMedico = findViewById(R.id.txtHistoricoMedico);

        btnSalvarMedico = findViewById(R.id.btnSalvarMedico);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnCancelar.setOnClickListener(this);
    }

    @Override
    public void carregaDados() throws Exception {

    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btnCancelar)
        {
            onBackPressed();
        }
    }

}
