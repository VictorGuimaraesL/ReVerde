package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.CertificadosDAO;
import com.reverde.reverde.model.entities.Certificados;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CertificadosDAOJDBC implements CertificadosDAO {

    private Connection conn;

    public CertificadosDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Certificados obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO Certificados (id_usuario, id_ponto, descricao, certificado, data_geracao) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            st.setInt(1, obj.getIdUsuario());
            st.setInt(2, obj.getIdPonto());
            st.setString(3, obj.getDescricao());
            st.setByte(4, obj.getCertificado());
            st.setTimestamp(5, Timestamp.valueOf(obj.getDataGeracao().atStartOfDay()));

            st.executeUpdate();

            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                obj.setIdCertificado(rs.getInt(1));
            }
            DB.closeResultSet(rs);

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
            st = conn.prepareStatement("DELETE FROM Certificados WHERE id_certificado = ?");
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Certificados findById(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM Certificados WHERE id_certificado = ?");
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                return instantiateCertificado(rs);
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
    public List<Certificados> findByUsuario(int idUsuario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM Certificados WHERE id_usuario = ?");
            st.setInt(1, idUsuario);
            rs = st.executeQuery();

            List<Certificados> list = new ArrayList<>();
            while (rs.next()) {
                list.add(instantiateCertificado(rs));
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
    public List<Certificados> findAll() {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM Certificados");

            List<Certificados> list = new ArrayList<>();
            while (rs.next()) {
                list.add(instantiateCertificado(rs));
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
    public boolean podeTrocarPorCertificado(int idUsuario, int pontosNecessarios) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT total_pontos FROM EcoPontos WHERE id_usuario = ?");
            st.setInt(1, idUsuario);
            rs = st.executeQuery();

            if (rs.next()) {
                int totalPontos = rs.getInt("total_pontos");
                return totalPontos >= pontosNecessarios;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private Certificados instantiateCertificado(ResultSet rs) throws SQLException {
        Certificados obj = new Certificados();
        obj.setIdCertificado(rs.getInt("id_certificado"));
        obj.setIdUsuario(rs.getInt("id_usuario"));
        obj.setIdPonto(rs.getInt("id_ponto"));
        obj.setDescricao(rs.getString("descricao"));
        obj.setCertificado(rs.getByte("certificado"));
        obj.setDataGeracao(rs.getTimestamp("data_geracao").toLocalDateTime().toLocalDate());
        return obj;
    }
}
