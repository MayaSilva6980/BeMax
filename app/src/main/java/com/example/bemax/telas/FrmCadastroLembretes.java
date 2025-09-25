package com.example.bemax.telas;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.bemax.R;
import com.example.bemax.adapters.HorarioAdapter;
import com.example.bemax.util.BaseActivity;

import java.util.Arrays;
import java.util.List;

public class FrmCadastroLembretes extends BaseActivity implements View.OnClickListener
{
    public RecyclerView hoursRecycler = null;
    public RecyclerView minutesRecycler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.frm_cadastro_lembrete);

        iniciaControles();
    }

    @Override
    public void obtemParametros() throws Exception {

    }

    @Override
    public void iniciaControles()
    {
        hoursRecycler = findViewById(R.id.hoursRecycler);
        minutesRecycler = findViewById(R.id.minutesRecycler);
    }

    @Override
    public void carregaDados() throws Exception {

    }

    @Override
    public void onClick(View v) {

    }

    private  void preencheListasHorarios()
    {

        List<String> hours = Arrays.asList("07","08","09","10","11","12");
        List<String> minutes = Arrays.asList("00","05","10","15","20","25","30","35","40","45","50","55");

        HorarioAdapter hoursAdapter = new HorarioAdapter(hours);
        HorarioAdapter minutesAdapter = new HorarioAdapter(minutes);

        hoursRecycler.setLayoutManager(new LinearLayoutManager(this));
        minutesRecycler.setLayoutManager(new LinearLayoutManager(this));

        hoursRecycler.setAdapter(hoursAdapter);
        minutesRecycler.setAdapter(minutesAdapter);

// snap helper = trava no item central
        SnapHelper snapHelperHours = new LinearSnapHelper();
        SnapHelper snapHelperMinutes = new LinearSnapHelper();
        snapHelperHours.attachToRecyclerView(hoursRecycler);
        snapHelperMinutes.attachToRecyclerView(minutesRecycler);

// listener pra detectar item central
        hoursRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelperHours.findSnapView(recyclerView.getLayoutManager());
                    int pos = recyclerView.getLayoutManager().getPosition(centerView);
                    hoursAdapter.setSelectedPosition(pos);
                }
            }
        });

        minutesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    View centerView = snapHelperMinutes.findSnapView(recyclerView.getLayoutManager());
                    int pos = recyclerView.getLayoutManager().getPosition(centerView);
                    minutesAdapter.setSelectedPosition(pos);
                }
            }
        });

    }

}
