package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.EcoPontos;

public interface EcoPontosDAO {

    void insert(EcoPontos obj);

    void update(EcoPontos obj);

    EcoPontos findByUsuario(int idUsuario);

    void adicionarPontos(int idUsuario, int pontos);

    void removerPontos(int idUsuario, int pontos);
}