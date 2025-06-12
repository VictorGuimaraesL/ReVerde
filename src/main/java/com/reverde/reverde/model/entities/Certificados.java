package com.reverde.reverde.model.entities;

import java.time.LocalDate;

public class Certificados {

    private int idCertificado;
    private int idUsuario;
    private int idPonto;
    private String descricao;
    private Byte certificado;
    private LocalDate dataGeracao;

    public int getIdCertificado() {
        return idCertificado;
    }

    public void setIdCertificado(int idCertificado) {
        this.idCertificado = idCertificado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdPonto() {
        return idPonto;
    }

    public void setIdPonto(int idPonto) {
        this.idPonto = idPonto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Byte getCertificado() {
        return certificado;
    }

    public void setCertificado(Byte certificado) {
        this.certificado = certificado;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDate dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
}