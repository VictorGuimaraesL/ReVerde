package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.PerguntaValidacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerguntaValidacaoDAOJDBCTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private PerguntaValidacaoDAOJDBC perguntaValidacaoDAO;

    @BeforeEach
    void setUp() throws SQLException {
        perguntaValidacaoDAO = new PerguntaValidacaoDAOJDBC(mockConnection);
    }

    @Test
    @DisplayName("Deve inserir uma nova pergunta de validação e atribuir o ID gerado")
    void insert_ShouldInsertNewPerguntaAndAssignGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(401);

        PerguntaValidacao pergunta = new PerguntaValidacao();
        pergunta.setIdHabito(1);
        pergunta.setPergunta("Qual a cor do céu?");
        pergunta.setRespostaCorreta("Azul");

        perguntaValidacaoDAO.insert(pergunta);

        verify(mockConnection).prepareStatement(
                "INSERT INTO PerguntaValidacao (id_habito, pergunta, resposta_correta) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        verify(mockPreparedStatement).setInt(1, pergunta.getIdHabito());
        verify(mockPreparedStatement).setString(2, pergunta.getPergunta());
        verify(mockPreparedStatement).setString(3, pergunta.getRespostaCorreta());
        verify(mockPreparedStatement).executeUpdate();
        assertEquals(401, pergunta.getIdPergunta());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve atualizar uma pergunta de validação existente")
    void update_ShouldUpdateExistingPergunta() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        PerguntaValidacao pergunta = new PerguntaValidacao();
        pergunta.setIdPergunta(1);
        pergunta.setIdHabito(2);
        pergunta.setPergunta("Qual a temperatura da água?");
        pergunta.setRespostaCorreta("Fria");

        perguntaValidacaoDAO.update(pergunta);

        verify(mockConnection).prepareStatement(
                "UPDATE PerguntaValidacao SET id_habito = ?, pergunta = ?, resposta_correta = ? WHERE id_pergunta = ?"
        );
        verify(mockPreparedStatement).setInt(1, pergunta.getIdHabito());
        verify(mockPreparedStatement).setString(2, pergunta.getPergunta());
        verify(mockPreparedStatement).setString(3, pergunta.getRespostaCorreta());
        verify(mockPreparedStatement).setInt(4, pergunta.getIdPergunta());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve deletar uma pergunta de validação por ID")
    void deleteById_ShouldDeletePergunta() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        perguntaValidacaoDAO.deleteById(1);

        verify(mockConnection).prepareStatement("DELETE FROM PerguntaValidacao WHERE id_pergunta = ?");
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve encontrar uma pergunta de validação por ID")
    void findById_ShouldReturnPerguntaWhenFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id_pergunta")).thenReturn(1);
        when(mockResultSet.getInt("id_habito")).thenReturn(10);
        when(mockResultSet.getString("pergunta")).thenReturn("Questão Teste");
        when(mockResultSet.getString("resposta_correta")).thenReturn("Certa");

        PerguntaValidacao pergunta = perguntaValidacaoDAO.findById(1);

        assertNotNull(pergunta);
        assertEquals(1, pergunta.getIdPergunta());
        assertEquals(10, pergunta.getIdHabito());
        assertEquals("Questão Teste", pergunta.getPergunta());
        assertEquals("Certa", pergunta.getRespostaCorreta());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar null quando a pergunta não for encontrada por ID")
    void findById_ShouldReturnNullWhenNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        PerguntaValidacao pergunta = perguntaValidacaoDAO.findById(999);

        assertNull(pergunta);
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista de perguntas de validação por ID de hábito")
    void findByHabito_ShouldReturnListOfPerguntasForHabito() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_pergunta")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_habito")).thenReturn(10, 10);
        when(mockResultSet.getString("pergunta")).thenReturn("Pergunta A", "Pergunta B");
        when(mockResultSet.getString("resposta_correta")).thenReturn("Resp A", "Resp B");

        List<PerguntaValidacao> perguntas = perguntaValidacaoDAO.findByHabito(10);

        assertNotNull(perguntas);
        assertEquals(2, perguntas.size());
        assertEquals("Pergunta A", perguntas.get(0).getPergunta());
        assertEquals("Pergunta B", perguntas.get(1).getPergunta());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não houver perguntas para o hábito")
    void findByHabito_ShouldReturnEmptyListWhenNoPerguntasFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<PerguntaValidacao> perguntas = perguntaValidacaoDAO.findByHabito(999);

        assertNotNull(perguntas);
        assertTrue(perguntas.isEmpty());
        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar todas as perguntas de validação")
    void findAll_ShouldReturnAllPerguntas() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_pergunta")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_habito")).thenReturn(10, 11);
        when(mockResultSet.getString("pergunta")).thenReturn("Total Q1", "Total Q2");
        when(mockResultSet.getString("resposta_correta")).thenReturn("Total R1", "Total R2");

        List<PerguntaValidacao> todasPerguntas = perguntaValidacaoDAO.findAll();

        assertNotNull(todasPerguntas);
        assertEquals(2, todasPerguntas.size());
        assertEquals("Total Q1", todasPerguntas.get(0).getPergunta());
        assertEquals("Total Q2", todasPerguntas.get(1).getPergunta());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver perguntas em findAll")
    void findAll_ShouldReturnEmptyListWhenNoPerguntasExist() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<PerguntaValidacao> todasPerguntas = perguntaValidacaoDAO.findAll();

        assertNotNull(todasPerguntas);
        assertTrue(todasPerguntas.isEmpty());

        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }
}
