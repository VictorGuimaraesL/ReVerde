package com.reverde.reverde.model.dao;

import com.reverde.reverde.model.entities.Habito;
import java.util.List;

public interface HabitoDAO {

    void insert(Habito obj);

    void update(Habito obj);

    void deleteById(int id);

    Habito findById(int id);

    List<Habito> findAll();
}