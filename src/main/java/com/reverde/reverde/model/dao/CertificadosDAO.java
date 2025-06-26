package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.Certificados;
import java.util.List;

public interface CertificadosDAO {

    void insert(Certificados obj);

    void deleteById(int id);

    Certificados findById(int id);

    List<Certificados> findByUsuario(int idUsuario);

    List<Certificados> findAll();

    boolean podeTrocarPorCertificado(int idUsuario, int pontosNecessarios);
}

