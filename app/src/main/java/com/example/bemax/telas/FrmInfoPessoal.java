package com.example.bemax.telas;

import android.os.Bundle;
import android.view.View;

import com.example.bemax.R;
import com.example.bemax.util.BaseActivity;

public class FrmInfoPessoal extends BaseActivity implements  View.OnClickListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.frm_info_pessoal);
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
    public void iniciaControles() throws Exception {

    }

    @Override
    public void carregaDados() throws Exception {

    }

    @Override
    public void onClick(View v) {

    }

}
