package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.RegistroHabito;
import java.util.List;

public interface RegistroHabitoDAO {

    void insert(RegistroHabito obj);

    void updateValidado(int idRegistro, boolean validado);

    RegistroHabito findById(int id);

    List<RegistroHabito> findByUsuario(int idUsuario);

    List<RegistroHabito> findAll();
}