package com.example.bemax.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bemax.R;
import com.example.bemax.ui.activity.MainActivity;

public class AlertFragment extends Fragment implements View.OnClickListener
{
    // controles
    private TextView txtStatusEmergencia;
    private TextView txtResumo;
    private TextView txtNomeUsuario;
    private TextView txtTipoSanguineo;
    private TextView txtDoencas;
    private TextView txtAlergias;
    private TextView txtLocalizacao;
    private TextView txtTempoDecorrido;
    private TextView txtContatoEmergencia;
    private Button btnLigarEmergencia;
    private Button btnEnviarMensagem;
    private Button btnSomAlerta;

    MainActivity mainActivity = null;
    private
    String telefoneParente = "11954000626";
    public AlertFragment(MainActivity principal)
    {
        mainActivity = principal;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Infla o layout do fragmento
        View view = inflater.inflate(R.layout.frm_alerta, container, false);

        iniciaControles(view);
        return view;
    }

    public void iniciaControles(View view)
    {
        txtNomeUsuario = view.findViewById(R.id.txtNomeUsuario);
        txtTipoSanguineo = view.findViewById(R.id.txtTipoSanguineo);
        txtDoencas = view.findViewById(R.id.txtDoencas);
        txtAlergias = view.findViewById(R.id.txtAlergias);
        txtLocalizacao = view.findViewById(R.id.txtLocalizacao);
        txtTempoDecorrido = view.findViewById(R.id.txtTempoDecorrido);
        txtContatoEmergencia = view.findViewById(R.id.txtContatoEmergencia);

        btnLigarEmergencia = view.findViewById(R.id.btnLigarEmergencia);
        btnLigarEmergencia.setOnClickListener(this);

        loadData();
    }

    public void loadData()
    {
        //preenche campos da tela
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btnLigarEmergencia)
        {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + telefoneParente));
            this.startActivity(intent);
        }
    }

}