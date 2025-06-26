package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.HabitoDAO;
import com.reverde.reverde.model.entities.Habito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitoDAOJDBC implements HabitoDAO {

    private Connection conn;

    public HabitoDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Habito obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO Habito (nome, descricao, pontuacao) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            st.setString(1, obj.getNome());
            st.setString(2, obj.getDescricao());
            st.setInt(3, obj.getPontuacao());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obj.setIdHabito(id);
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
    public void update(Habito obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE Habito SET nome = ?, descricao = ?, pontuacao = ? WHERE id_habito = ?"
            );
            st.setString(1, obj.getNome());
            st.setString(2, obj.getDescricao());
            st.setInt(3, obj.getPontuacao());
            st.setInt(4, obj.getIdHabito());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void deleteById(int id) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "DELETE FROM Habito WHERE id_habito = ?"
            );
            st.setInt(1, id);

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Habito findById(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM Habito WHERE id_habito = ?"
            );
            st.setInt(1, id);

            rs = st.executeQuery();

            if (rs.next()) {
                Habito habito = instantiateHabito(rs);
                return habito;
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
    public List<Habito> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM Habito ORDER BY nome"
            );

            rs = st.executeQuery();

            List<Habito> list = new ArrayList<>();

            while (rs.next()) {
                Habito habito = instantiateHabito(rs);
                list.add(habito);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private Habito instantiateHabito(ResultSet rs) throws SQLException {
        Habito habito = new Habito();
        habito.setIdHabito(rs.getInt("id_habito"));
        habito.setNome(rs.getString("nome"));
        habito.setDescricao(rs.getString("descricao"));
        habito.setPontuacao(rs.getInt("pontuacao"));
        return habito;
    }
}