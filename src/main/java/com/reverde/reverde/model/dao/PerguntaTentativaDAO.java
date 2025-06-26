package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.PerguntaTentativa;
import java.util.List;

public interface PerguntaTentativaDAO {

    void insert(PerguntaTentativa obj);

    List<PerguntaTentativa> findByUsuario(int idUsuario);

    List<PerguntaTentativa> findByPergunta(int idPergunta);

    List<PerguntaTentativa> findAll();
}