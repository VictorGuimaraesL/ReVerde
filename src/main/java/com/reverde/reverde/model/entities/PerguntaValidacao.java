package com.reverde.reverde.model.entities;

public class PerguntaValidacao {

    private int idPergunta;
    private int idHabito;
    private String pergunta;
    private String respostaCorreta;
    private String respostaIncorreta;

    public int getIdPergunta() {
        return idPergunta;
    }

    public void setIdPergunta(int idPergunta) {
        this.idPergunta = idPergunta;
    }

    public int getIdHabito() {
        return idHabito;
    }

    public void setIdHabito(int idHabito) {
        this.idHabito = idHabito;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getRespostaCorreta() { return respostaCorreta; }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public String getRespostaIncorreta() {
        return respostaIncorreta;
    }

    public void setRespostaIncorreta(String respostaIncorreta) {
        this.respostaIncorreta = respostaIncorreta;
    }
}
