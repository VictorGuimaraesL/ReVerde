package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.PerguntaTentativa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerguntaTentativaDAOJDBCTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private PerguntaTentativaDAOJDBC perguntaTentativaDAO;

    @BeforeEach
    void setUp() throws SQLException {
        perguntaTentativaDAO = new PerguntaTentativaDAOJDBC(mockConnection);
    }

    @Test
    @DisplayName("Deve inserir uma nova tentativa de pergunta e atribuir o ID gerado")
    void insert_ShouldInsertNewTentativaAndAssignGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(
                "INSERT INTO quiztentativa (id_usuario, id_pergunta, resposta_usuario, correta, data_tentativa) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )).thenReturn(mockPreparedStatement);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(301);

        PerguntaTentativa tentativa = new PerguntaTentativa();
        tentativa.setIdUsuario(1);
        tentativa.setIdPergunta(5);
        tentativa.setRespostaUsuario("Resposta Correta");
        tentativa.setCorreta(true);
        tentativa.setDataTentativa(LocalDate.now());

        perguntaTentativaDAO.insert(tentativa);

        verify(mockPreparedStatement).setInt(1, tentativa.getIdUsuario());
        verify(mockPreparedStatement).setInt(2, tentativa.getIdPergunta());
        verify(mockPreparedStatement).setString(3, tentativa.getRespostaUsuario());
        verify(mockPreparedStatement).setBoolean(4, tentativa.getCorreta());
        verify(mockPreparedStatement).setDate(5, Date.valueOf(tentativa.getDataTentativa()));
        verify(mockPreparedStatement).executeUpdate();

        assertEquals(301, tentativa.getIdTentativa());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista de tentativas de perguntas para um usuário específico")
    void findByUsuario_ShouldReturnListOfTentativasForUser() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_tentativa")).thenReturn(10, 11);
        when(mockResultSet.getInt("id_usuario")).thenReturn(1, 1);
        when(mockResultSet.getInt("id_pergunta")).thenReturn(5, 6);
        when(mockResultSet.getString("resposta_usuario")).thenReturn("Resp 1", "Resp 2");
        when(mockResultSet.getBoolean("correta")).thenReturn(true, false);
        when(mockResultSet.getDate("data_tentativa")).thenReturn(
                Date.valueOf(LocalDate.of(2024, 6, 1)),
                Date.valueOf(LocalDate.of(2024, 6, 2))
        );

        List<PerguntaTentativa> tentativas = perguntaTentativaDAO.findByUsuario(1);

        assertNotNull(tentativas);
        assertEquals(2, tentativas.size());
        assertEquals(10, tentativas.get(0).getIdTentativa());
        assertTrue(tentativas.get(0).getCorreta());
        assertEquals(11, tentativas.get(1).getIdTentativa());
        assertFalse(tentativas.get(1).getCorreta());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não houver tentativas para o usuário")
    void findByUsuario_ShouldReturnEmptyListWhenNoTentativasFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<PerguntaTentativa> tentativas = perguntaTentativaDAO.findByUsuario(999);

        assertNotNull(tentativas);
        assertTrue(tentativas.isEmpty());
        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista de tentativas de perguntas para uma pergunta específica")
    void findByPergunta_ShouldReturnListOfTentativasForPergunta() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getInt("id_tentativa")).thenReturn(10);
        when(mockResultSet.getInt("id_usuario")).thenReturn(1);
        when(mockResultSet.getInt("id_pergunta")).thenReturn(5);
        when(mockResultSet.getString("resposta_usuario")).thenReturn("Resp Unica");
        when(mockResultSet.getBoolean("correta")).thenReturn(true);
        when(mockResultSet.getDate("data_tentativa")).thenReturn(Date.valueOf(LocalDate.of(2024, 6, 5)));

        List<PerguntaTentativa> tentativas = perguntaTentativaDAO.findByPergunta(5);

        assertNotNull(tentativas);
        assertEquals(1, tentativas.size());
        assertEquals(10, tentativas.get(0).getIdTentativa());
        assertEquals(5, tentativas.get(0).getIdPergunta());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não houver tentativas para a pergunta")
    void findByPergunta_ShouldReturnEmptyListWhenNoTentativasForPerguntaFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<PerguntaTentativa> tentativas = perguntaTentativaDAO.findByPergunta(999);

        assertNotNull(tentativas);
        assertTrue(tentativas.isEmpty());
        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar todas as tentativas de perguntas")
    void findAll_ShouldReturnAllTentativas() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_tentativa")).thenReturn(10, 11);
        when(mockResultSet.getInt("id_usuario")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_pergunta")).thenReturn(5, 6);
        when(mockResultSet.getString("resposta_usuario")).thenReturn("Resp Total 1", "Resp Total 2");
        when(mockResultSet.getBoolean("correta")).thenReturn(true, false);
        when(mockResultSet.getDate("data_tentativa")).thenReturn(
                Date.valueOf(LocalDate.of(2024, 6, 1)),
                Date.valueOf(LocalDate.of(2024, 6, 2))
        );

        List<PerguntaTentativa> todasTentativas = perguntaTentativaDAO.findAll();

        assertNotNull(todasTentativas);
        assertEquals(2, todasTentativas.size());
        assertEquals("Resp Total 1", todasTentativas.get(0).getRespostaUsuario());
        assertEquals("Resp Total 2", todasTentativas.get(1).getRespostaUsuario());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver tentativas em findAll")
    void findAll_ShouldReturnEmptyListWhenNoTentativasExist() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<PerguntaTentativa> todasTentativas = perguntaTentativaDAO.findAll();

        assertNotNull(todasTentativas);
        assertTrue(todasTentativas.isEmpty());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }
}
