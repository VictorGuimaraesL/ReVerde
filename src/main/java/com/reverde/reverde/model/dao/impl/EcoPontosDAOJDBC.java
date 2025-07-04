package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.entities.EcoPontos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                    Statement.RETURN_GENERATED_KEYS);

            st.setInt(1, obj.getIdUsuario());
            st.setInt(2, obj.getTotalPontos());
            st.setTimestamp(3, Timestamp.valueOf(obj.getAtualizacao().atStartOfDay()));

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    obj.setIdPonto(rs.getInt(1));
                }
                DB.closeResultSet(rs);
            } else {
                throw new RuntimeException("Nenhuma linha afetada! EcoPontos não inseridos.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir EcoPontos: " + e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void update(EcoPontos obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE EcoPontos SET total_pontos = ?, atualizado_em = ? WHERE id_ponto = ?"
            );

            st.setInt(1, obj.getTotalPontos());
            st.setTimestamp(2, Timestamp.valueOf(obj.getAtualizacao().atStartOfDay()));
            st.setInt(3, obj.getIdPonto()); // Atualiza pelo ID do ponto, não do usuário

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar EcoPontos: " + e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public EcoPontos findByUsuario(Integer idUsuario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM EcoPontos WHERE id_usuario = ?");
            st.setInt(1, idUsuario);
            rs = st.executeQuery();
            if (rs.next()) {
                return instantiateEcoPontos(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar EcoPontos por ID de usuário: " + e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    @Override
    public List<EcoPontos> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM EcoPontos");
            rs = st.executeQuery();

            List<EcoPontos> list = new ArrayList<>();
            while (rs.next()) {
                list.add(instantiateEcoPontos(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os EcoPontos: " + e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    @Override
    public void adicionarPontos(int idUsuario, int pontos) {
        EcoPontos ecoPontos = findByUsuario(idUsuario);
        if (ecoPontos == null) {
            // Se não existir, cria um novo registro de EcoPontos para o usuário
            ecoPontos = new EcoPontos();
            ecoPontos.setIdUsuario(idUsuario);
            ecoPontos.setTotalPontos(pontos);
            ecoPontos.setAtualizacao(LocalDate.now());
            insert(ecoPontos);
        } else {
            // Se existir, atualiza o total de pontos
            ecoPontos.setTotalPontos(ecoPontos.getTotalPontos() + pontos);
            ecoPontos.setAtualizacao(LocalDate.now());
            update(ecoPontos);
        }
    }

    @Override
    public void removerPontos(int idUsuario, int pontos) {
        EcoPontos ecoPontos = findByUsuario(idUsuario);
        if (ecoPontos != null) {
            ecoPontos.setTotalPontos(Math.max(0, ecoPontos.getTotalPontos() - pontos)); // Garante que não fique negativo
            ecoPontos.setAtualizacao(LocalDate.now());
            update(ecoPontos);
        } else {
            System.out.println("Aviso: Tentativa de remover pontos de usuário sem registro de EcoPontos.");
        }
    }

    private EcoPontos instantiateEcoPontos(ResultSet rs) throws SQLException {
        EcoPontos obj = new EcoPontos();
        obj.setIdPonto(rs.getInt("id_ponto"));
        obj.setIdUsuario(rs.getInt("id_usuario"));
        obj.setTotalPontos(rs.getInt("total_pontos"));
        obj.setAtualizacao(rs.getTimestamp("atualizado_em").toLocalDateTime().toLocalDate());
        return obj;
    }
}