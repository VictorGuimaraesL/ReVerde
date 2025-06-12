package com.reverde.reverde.model.entities;

import java.time.LocalDate;

public class PerguntaTentativa {

    private int idTentativa;
    private int idUsuario;
    private int idPergunta;
    private String respostaUsuario;
    private Boolean correta;
    private LocalDate dataTentativa;

    public int getIdTentativa() {
        return idTentativa;
    }

    public void setIdTentativa(int idTentativa) {
        this.idTentativa = idTentativa;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdPergunta() {
        return idPergunta;
    }

    public void setIdPergunta(int idPergunta) {
        this.idPergunta = idPergunta;
    }

    public String getRespostaUsuario() {
        return respostaUsuario;
    }

    public void setRespostaUsuario(String respostaUsuario) {
        this.respostaUsuario = respostaUsuario;
    }

    public Boolean getCorreta() {
        return correta;
    }

    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }

    public LocalDate getDataTentativa() {
        return dataTentativa;
    }

    public void setDataTentativa(LocalDate dataTentativa) {
        this.dataTentativa = dataTentativa;
    }
}
