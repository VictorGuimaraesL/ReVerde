package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.EcoPontos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Mock
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
        ecoPontos.setIdPonto(1);
        ecoPontos.setIdUsuario(10);
        ecoPontos.setTotalPontos(150);
        ecoPontos.setAtualizacao(LocalDate.now());

        ecoPontosDAO.update(ecoPontos);

        verify(mockConnection).prepareStatement(
                "UPDATE EcoPontos SET total_pontos = ?, atualizado_em = ? WHERE id_ponto = ?"
        );
        verify(mockPreparedStatement).setInt(1, ecoPontos.getTotalPontos());
        verify(mockPreparedStatement).setTimestamp(2, Timestamp.valueOf(ecoPontos.getAtualizacao().atStartOfDay()));
        verify(mockPreparedStatement).setInt(3, ecoPontos.getIdPonto());
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
    @DisplayName("Deve adicionar pontos ao total de EcoPontos de um usuário existente")
    void adicionarPontos_ShouldIncreaseTotalPoints() throws SQLException {
        PreparedStatement mockSelectPs = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT * FROM EcoPontos WHERE id_usuario = ?"))
                .thenReturn(mockSelectPs);

        when(mockSelectPs.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id_ponto")).thenReturn(1);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10);
        when(mockResultSet.getInt("total_pontos")).thenReturn(100);
        when(mockResultSet.getTimestamp("atualizado_em")).thenReturn(Timestamp.valueOf(LocalDate.now().atStartOfDay()));

        PreparedStatement mockUpdatePs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(
                "UPDATE EcoPontos SET total_pontos = ?, atualizado_em = ? WHERE id_ponto = ?"
        )).thenReturn(mockUpdatePs);

        when(mockUpdatePs.executeUpdate()).thenReturn(1);

        ecoPontosDAO.adicionarPontos(10, 50);

        verify(mockSelectPs).setInt(1, 10);
        verify(mockSelectPs).executeQuery();

        verify(mockUpdatePs).setInt(1, 150);
        verify(mockUpdatePs).setTimestamp(eq(2), any(Timestamp.class));
        verify(mockUpdatePs).setInt(3, 1);
        verify(mockUpdatePs).executeUpdate();

        verify(mockResultSet).close();
        verify(mockSelectPs).close();
        verify(mockUpdatePs).close();
    }

    @Test
    @DisplayName("Deve remover pontos do total de EcoPontos de um usuário, garantindo que não seja negativo")
    void removerPontos_ShouldDecreaseTotalPointsAndNotGoBelowZero() throws SQLException {
        PreparedStatement mockSelectPs = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT * FROM EcoPontos WHERE id_usuario = ?"))
                .thenReturn(mockSelectPs);

        when(mockSelectPs.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id_ponto")).thenReturn(1);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10);
        when(mockResultSet.getInt("total_pontos")).thenReturn(100);
        when(mockResultSet.getTimestamp("atualizado_em")).thenReturn(Timestamp.valueOf(LocalDate.now().atStartOfDay()));

        PreparedStatement mockUpdatePs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(
                "UPDATE EcoPontos SET total_pontos = ?, atualizado_em = ? WHERE id_ponto = ?"
        )).thenReturn(mockUpdatePs);

        when(mockUpdatePs.executeUpdate()).thenReturn(1);

        ecoPontosDAO.removerPontos(10, 30);

        verify(mockSelectPs).setInt(1, 10);
        verify(mockSelectPs).executeQuery();

        verify(mockUpdatePs).setInt(1, 70);
        verify(mockUpdatePs).setTimestamp(eq(2), any(Timestamp.class));
        verify(mockUpdatePs).setInt(3, 1);
        verify(mockUpdatePs).executeUpdate();

        verify(mockResultSet).close();
        verify(mockSelectPs).close();
        verify(mockUpdatePs).close();
    }

    @Test
    @DisplayName("Deve lançar RuntimeException em caso de SQLException durante adicionarPontos")
    void adicionarPontos_ShouldThrowRuntimeExceptionOnSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Erro de DB simulado"));

        assertThrows(RuntimeException.class, () -> ecoPontosDAO.adicionarPontos(1, 10));
    }
}
