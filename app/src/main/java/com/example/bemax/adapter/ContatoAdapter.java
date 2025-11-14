package com.example.bemax.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.model.domain.Contato;

import java.util.ArrayList;

public class ContatoAdapter extends RecyclerView.Adapter<ContatoAdapter.ContatoViewHolder> {

    private ArrayList<Contato> contatos;
    private OnContatoClickListener listener;

    public interface OnContatoClickListener {
        void onEditarClick(Contato contato);
    }

    public ContatoAdapter(ArrayList<Contato> contatos, OnContatoClickListener listener) {
        this.contatos = contatos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adp_contato, parent, false);
        return new ContatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContatoViewHolder holder, int position) {
        Contato contato = contatos.get(position);
        holder.txtNomeContato.setText(contato.getNome());
        holder.txtParentesco.setText(contato.getParentesco());
        holder.txtTelefone.setText(contato.getTelefone());

        // Ação do botão de editar
        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(contato));
    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }

    static class ContatoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeContato, txtParentesco, txtTelefone;
        LinearLayout btnEditar;

        public ContatoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeContato = itemView.findViewById(R.id.txtNomeContatoItem);
            txtParentesco = itemView.findViewById(R.id.txtParentescoItem);
            txtTelefone = itemView.findViewById(R.id.txtTelefoneContatoItem);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }
    }
}
