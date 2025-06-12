package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.EcoPontos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EcoPontosDAOJDBCTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private EcoPontosDAOJDBC ecoPontosDAO;

    @BeforeEach
    void setUp() throws SQLException {
        ecoPontosDAO = new EcoPontosDAOJDBC(mockConnection);
    }

    @Test
    @DisplayName("Deve inserir novos EcoPontos e atribuir o ID gerado")
    void insert_ShouldInsertNewEcoPontosAndAssignGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(201);

        EcoPontos ecoPontos = new EcoPontos();
        ecoPontos.setIdUsuario(10);
        ecoPontos.setTotalPontos(100);
        ecoPontos.setAtualizacao(LocalDate.now());

        ecoPontosDAO.insert(ecoPontos);

        verify(mockConnection).prepareStatement(
                "INSERT INTO EcoPontos (id_usuario, total_pontos, atualizado_em) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        verify(mockPreparedStatement).setInt(1, ecoPontos.getIdUsuario());
        verify(mockPreparedStatement).setInt(2, ecoPontos.getTotalPontos());
        verify(mockPreparedStatement).setTimestamp(3, Timestamp.valueOf(ecoPontos.getAtualizacao().atStartOfDay()));
        verify(mockPreparedStatement).executeUpdate();
        assertEquals(201, ecoPontos.getIdPonto());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve atualizar EcoPontos existentes")
    void update_ShouldUpdateExistingEcoPontos() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        EcoPontos ecoPontos = new EcoPontos();
        ecoPontos.setIdUsuario(10);
        ecoPontos.setTotalPontos(150);
        ecoPontos.setAtualizacao(LocalDate.now());

        ecoPontosDAO.update(ecoPontos);

        verify(mockConnection).prepareStatement(
                "UPDATE EcoPontos SET total_pontos = ?, atualizado_em = ? WHERE id_usuario = ?"
        );
        verify(mockPreparedStatement).setInt(1, ecoPontos.getTotalPontos());
        verify(mockPreparedStatement).setTimestamp(2, Timestamp.valueOf(ecoPontos.getAtualizacao().atStartOfDay()));
        verify(mockPreparedStatement).setInt(3, ecoPontos.getIdUsuario());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve encontrar EcoPontos por ID de usuário")
    void findByUsuario_ShouldReturnEcoPontosWhenFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id_ponto")).thenReturn(1);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10);
        when(mockResultSet.getInt("total_pontos")).thenReturn(200);
        when(mockResultSet.getTimestamp("atualizado_em")).thenReturn(Timestamp.valueOf(LocalDate.of(2024, 5, 10).atStartOfDay()));

        EcoPontos ecoPontos = ecoPontosDAO.findByUsuario(10);

        assertNotNull(ecoPontos);
        assertEquals(1, ecoPontos.getIdPonto());
        assertEquals(10, ecoPontos.getIdUsuario());
        assertEquals(200, ecoPontos.getTotalPontos());
        assertEquals(LocalDate.of(2024, 5, 10), ecoPontos.getAtualizacao());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar null quando EcoPontos não for encontrado por ID de usuário")
    void findByUsuario_ShouldReturnNullWhenNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        EcoPontos ecoPontos = ecoPontosDAO.findByUsuario(999);

        assertNull(ecoPontos);
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve adicionar pontos ao total de EcoPontos de um usuário")
    void adicionarPontos_ShouldIncreaseTotalPoints() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        ecoPontosDAO.adicionarPontos(10, 50);

        verify(mockConnection).prepareStatement(
                "UPDATE EcoPontos SET total_pontos = total_pontos + ?, atualizado_em = ? WHERE id_usuario = ?"
        );
        verify(mockPreparedStatement).setInt(1, 50);
        verify(mockPreparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(mockPreparedStatement).setInt(3, 10);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve remover pontos do total de EcoPontos de um usuário, garantindo que não seja negativo")
    void removerPontos_ShouldDecreaseTotalPointsAndNotGoBelowZero() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        ecoPontosDAO.removerPontos(10, 30);

        verify(mockConnection).prepareStatement(
                "UPDATE EcoPontos SET total_pontos = GREATEST(total_pontos - ?, 0), atualizado_em = ? WHERE id_usuario = ?"
        );
        verify(mockPreparedStatement).setInt(1, 30);
        verify(mockPreparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(mockPreparedStatement).setInt(3, 10);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve lançar RuntimeException em caso de SQLException durante adicionarPontos")
    void adicionarPontos_ShouldThrowRuntimeExceptionOnSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Erro de DB simulado"));

        assertThrows(RuntimeException.class, () -> ecoPontosDAO.adicionarPontos(1, 10));
    }
}
