package com.example.bemax.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.adapters.LembreteAdapter;
import com.example.bemax.model.Lembrete;

import java.util.ArrayList;

public class FrmAlerta  extends Fragment implements View.OnClickListener
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

    FrmPrincipal frmPrincipal = null;

    public FrmAlerta(FrmPrincipal principal)
    {
        frmPrincipal = principal;
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

        carregaDados();
    }

    public void carregaDados()
    {
        //preenche campos da tela

    }

    @Override
    public void onClick(View view)
    {

    }

}