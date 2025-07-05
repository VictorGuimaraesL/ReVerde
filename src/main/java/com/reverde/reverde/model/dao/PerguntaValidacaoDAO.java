package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.PerguntaValidacao;
import java.util.List;

public interface PerguntaValidacaoDAO {

    void insert(PerguntaValidacao obj);

    void update(PerguntaValidacao obj);

    void deleteById(int id);

    PerguntaValidacao findById(int id);

    List<PerguntaValidacao> findByHabito(int idHabito);

    List<PerguntaValidacao> findAll();
}
