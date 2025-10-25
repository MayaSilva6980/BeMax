package com.example.bemax.model;

import java.io.Serializable;

public class Contato implements Serializable {

    private String nome;
    private String parentesco;
    private String telefone;
    private String email;
    private String observacoes;

    // Construtor padrão (necessário se for usar Firebase ou serialização)
    public Contato() {
    }

    // Construtor completo
    public Contato(String nome, String parentesco, String telefone, String email, String observacoes) {
        this.nome = nome;
        this.parentesco = parentesco;
        this.telefone = telefone;
        this.email = email;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}