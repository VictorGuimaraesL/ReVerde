package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.Usuario;
import java.util.List;

public interface UsuarioDAO {

    void insert(Usuario obj);

    void update(Usuario obj);

    void deleteById(int id);

    Usuario findById(int id);

    Usuario findByEmail(String email);

    List<Usuario> findAll();
}