package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.PerguntaValidacaoDAO;
import com.reverde.reverde.model.entities.PerguntaValidacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerguntaValidacaoDAOJDBC implements PerguntaValidacaoDAO {

    private Connection conn;

    public PerguntaValidacaoDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(PerguntaValidacao obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO PerguntaValidacao (id_habito, pergunta, resposta_correta) " +
                            "VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            st.setInt(1, obj.getIdHabito());
            st.setString(2, obj.getPergunta());
            st.setString(3, obj.getRespostaCorreta());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    obj.setIdPergunta(rs.getInt(1));
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
    public void update(PerguntaValidacao obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE PerguntaValidacao SET id_habito = ?, pergunta = ?, resposta_correta = ? " +
                            "WHERE id_pergunta = ?"
            );
            st.setInt(1, obj.getIdHabito());
            st.setString(2, obj.getPergunta());
            st.setString(3, obj.getRespostaCorreta());
            st.setInt(4, obj.getIdPergunta());

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
                    "DELETE FROM PerguntaValidacao WHERE id_pergunta = ?"
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
    public PerguntaValidacao findById(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM PerguntaValidacao WHERE id_pergunta = ?"
            );
            st.setInt(1, id);

            rs = st.executeQuery();

            if (rs.next()) {
                PerguntaValidacao obj = instantiatePerguntaValidacao(rs);
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
    public List<PerguntaValidacao> findByHabito(int idHabito) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM PerguntaValidacao WHERE id_habito = ?"
            );
            st.setInt(1, idHabito);

            rs = st.executeQuery();

            List<PerguntaValidacao> list = new ArrayList<>();

            while (rs.next()) {
                PerguntaValidacao obj = instantiatePerguntaValidacao(rs);
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
    public List<PerguntaValidacao> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM PerguntaValidacao"
            );

            rs = st.executeQuery();

            List<PerguntaValidacao> list = new ArrayList<>();

            while (rs.next()) {
                PerguntaValidacao obj = instantiatePerguntaValidacao(rs);
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

    private PerguntaValidacao instantiatePerguntaValidacao(ResultSet rs) throws SQLException {
        PerguntaValidacao obj = new PerguntaValidacao();
        obj.setIdPergunta(rs.getInt("id_pergunta"));
        obj.setIdHabito(rs.getInt("id_habito"));
        obj.setPergunta(rs.getString("pergunta"));
        obj.setRespostaCorreta(rs.getString("resposta_correta"));
        return obj;
    }
}