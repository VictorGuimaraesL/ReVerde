package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.entities.RegistroHabito;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroHabitoDAOJDBC implements RegistroHabitoDAO {

    private Connection conn;

    public RegistroHabitoDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(RegistroHabito obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO RegistroHabito (id_usuario, id_habito, data_registro, validado) " +
                            "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            st.setInt(1, obj.getIdUsuario());
            st.setInt(2, obj.getIdHabito());
            st.setDate(3, Date.valueOf(obj.getDataRegistro()));
            st.setBoolean(4, obj.isValidado());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    obj.setIdRegistro(rs.getInt(1));
                }
                DB.closeResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void updateValidado(int idRegistro, boolean validado) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE RegistroHabito SET validado = ? WHERE id_registro = ?"
            );
            st.setBoolean(1, validado);
            st.setInt(2, idRegistro);

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public RegistroHabito findById(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM RegistroHabito WHERE id_registro = ?"
            );
            st.setInt(1, id);

            rs = st.executeQuery();

            if (rs.next()) {
                RegistroHabito obj = instantiateRegistroHabito(rs);
                return obj;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    @Override
    public List<RegistroHabito> findByUsuario(int idUsuario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM RegistroHabito WHERE id_usuario = ?"
            );
            st.setInt(1, idUsuario);

            rs = st.executeQuery();

            List<RegistroHabito> list = new ArrayList<>();

            while (rs.next()) {
                RegistroHabito obj = instantiateRegistroHabito(rs);
                list.add(obj);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    @Override
    public List<RegistroHabito> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM RegistroHabito"
            );

            rs = st.executeQuery();

            List<RegistroHabito> list = new ArrayList<>();

            while (rs.next()) {
                RegistroHabito obj = instantiateRegistroHabito(rs);
                list.add(obj);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private RegistroHabito instantiateRegistroHabito(ResultSet rs) throws SQLException {
        RegistroHabito obj = new RegistroHabito();
        obj.setIdRegistro(rs.getInt("id_registro"));
        obj.setIdUsuario(rs.getInt("id_usuario"));
        obj.setIdHabito(rs.getInt("id_habito"));
        obj.setDataRegistro(rs.getDate("data_registro").toLocalDate());
        obj.setValidado(rs.getBoolean("validado"));
        return obj;
    }
}