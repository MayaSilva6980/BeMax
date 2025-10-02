package com.example.bemax.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.bemax.adapters.HorarioAdapter;

public class CenterItemScrollListener extends RecyclerView.OnScrollListener {
    private final SnapHelper snapHelper;
    private final HorarioAdapter adapter;

    public CenterItemScrollListener(SnapHelper snapHelper, HorarioAdapter adapter) {
        this.snapHelper = snapHelper;
        this.adapter = adapter;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
    {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {

            View centerView = snapHelper.findSnapView(recyclerView.getLayoutManager());

            if (centerView != null)
            {
                int pos = recyclerView.getLayoutManager().getPosition(centerView);
                adapter.setPosicaoSelecionada(pos);
            }
        }
    }
}