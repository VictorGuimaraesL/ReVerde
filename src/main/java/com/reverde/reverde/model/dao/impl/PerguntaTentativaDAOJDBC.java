package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.PerguntaTentativaDAO;
import com.reverde.reverde.model.entities.PerguntaTentativa;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PerguntaTentativaDAOJDBC implements PerguntaTentativaDAO {

    private Connection conn;

    public PerguntaTentativaDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(PerguntaTentativa obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO PerguntaTentativa (id_usuario, id_pergunta, resposta_usuario, correta, data_tentativa) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            st.setInt(1, obj.getIdUsuario());
            st.setInt(2, obj.getIdPergunta());
            st.setString(3, obj.getRespostaUsuario());
            st.setBoolean(4, obj.getCorreta());
            st.setDate(5, Date.valueOf(obj.getDataTentativa()));

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    obj.setIdTentativa(rs.getInt(1));
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
    public List<PerguntaTentativa> findByUsuario(int idUsuario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM PerguntaTentativa WHERE id_usuario = ? ORDER BY data_tentativa DESC"
            );
            st.setInt(1, idUsuario);

            rs = st.executeQuery();

            List<PerguntaTentativa> list = new ArrayList<>();

            while (rs.next()) {
                PerguntaTentativa tentativa = instantiatePerguntaTentativa(rs);
                list.add(tentativa);
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
    public List<PerguntaTentativa> findByPergunta(int idPergunta) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM PerguntaTentativa WHERE id_pergunta = ? ORDER BY data_tentativa DESC"
            );
            st.setInt(1, idPergunta);

            rs = st.executeQuery();

            List<PerguntaTentativa> list = new ArrayList<>();

            while (rs.next()) {
                PerguntaTentativa tentativa = instantiatePerguntaTentativa(rs);
                list.add(tentativa);
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
    public List<PerguntaTentativa> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT * FROM PerguntaTentativa ORDER BY data_tentativa DESC"
            );

            rs = st.executeQuery();

            List<PerguntaTentativa> list = new ArrayList<>();

            while (rs.next()) {
                PerguntaTentativa tentativa = instantiatePerguntaTentativa(rs);
                list.add(tentativa);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private PerguntaTentativa instantiatePerguntaTentativa(ResultSet rs) throws SQLException {
        PerguntaTentativa tentativa = new PerguntaTentativa();
        tentativa.setIdTentativa(rs.getInt("id_tentativa"));
        tentativa.setIdUsuario(rs.getInt("id_usuario"));
        tentativa.setIdPergunta(rs.getInt("id_pergunta"));
        tentativa.setRespostaUsuario(rs.getString("resposta_usuario"));
        tentativa.setCorreta(rs.getBoolean("correta"));
        tentativa.setDataTentativa(rs.getDate("data_tentativa").toLocalDate());
        return tentativa;
    }
}
