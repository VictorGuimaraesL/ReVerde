package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.entities.EcoPontos;

import java.sql.*;
import java.time.LocalDate;

public class EcoPontosDAOJDBC implements EcoPontosDAO {

    private Connection conn;

    public EcoPontosDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(EcoPontos obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO EcoPontos (id_usuario, total_pontos, atualizado_em) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            st.setInt(1, obj.getIdUsuario());
            st.setInt(2, obj.getTotalPontos());
            st.setTimestamp(3, Timestamp.valueOf(obj.getAtualizacao().atStartOfDay()));

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obj.setIdPonto(id);
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
    public void update(EcoPontos obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE EcoPontos SET total_pontos = ?, atualizado_em = ? WHERE id_usuario = ?"
            );
            st.setInt(1, obj.getTotalPontos());
            st.setTimestamp(2, Timestamp.valueOf(obj.getAtualizacao().atStartOfDay()));
            st.setInt(3, obj.getIdUsuario());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public EcoPontos findByUsuario(int idUsuario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM EcoPontos WHERE id_usuario = ?"
            );
            st.setInt(1, idUsuario);
            rs = st.executeQuery();

            if (rs.next()) {
                EcoPontos eco = instantiateEcoPontos(rs);
                return eco;
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
    public void adicionarPontos(int idUsuario, int pontos) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE EcoPontos SET total_pontos = total_pontos + ?, atualizado_em = ? WHERE id_usuario = ?"
            );
            st.setInt(1, pontos);
            st.setTimestamp(2, Timestamp.valueOf(LocalDate.now().atStartOfDay()));
            st.setInt(3, idUsuario);

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void removerPontos(int idUsuario, int pontos) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE EcoPontos SET total_pontos = GREATEST(total_pontos - ?, 0), atualizado_em = ? WHERE id_usuario = ?"
            );
            st.setInt(1, pontos);
            st.setTimestamp(2, Timestamp.valueOf(LocalDate.now().atStartOfDay()));
            st.setInt(3, idUsuario);

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    private EcoPontos instantiateEcoPontos(ResultSet rs) throws SQLException {
        EcoPontos eco = new EcoPontos();
        eco.setIdPonto(rs.getInt("id_ponto"));
        eco.setIdUsuario(rs.getInt("id_usuario"));
        eco.setTotalPontos(rs.getInt("total_pontos"));
        eco.setAtualizacao(rs.getTimestamp("atualizado_em").toLocalDateTime().toLocalDate());
        return eco;
    }
}