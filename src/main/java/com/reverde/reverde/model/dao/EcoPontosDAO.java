package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.EcoPontos;

import java.util.List;

public interface EcoPontosDAO {
    void insert(EcoPontos obj);
    void update(EcoPontos obj);
    EcoPontos findByUsuario(Integer idUsuario);
    List<EcoPontos> findAll();
    void adicionarPontos(int idUsuario, int pontos); // Novo método
    void removerPontos(int idUsuario, int pontos); // Novo método
}