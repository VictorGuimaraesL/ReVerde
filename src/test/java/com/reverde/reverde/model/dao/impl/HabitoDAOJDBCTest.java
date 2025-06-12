package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.Habito;
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
public class HabitoDAOJDBCTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private Statement mockStatement;

    private HabitoDAOJDBC habitoDAO;

    @BeforeEach
    void setUp() throws SQLException {
        habitoDAO = new HabitoDAOJDBC(mockConnection);
    }

    @Test
    @DisplayName("Deve inserir um novo hábito e atribuir o ID gerado")
    void insert_ShouldInsertNewHabitoAndAssignGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(10);

        Habito habito = new Habito();
        habito.setNome("Economizar Água");
        habito.setDescricao("Fechar a torneira ao escovar os dentes");
        habito.setPontuacao(5);

        habitoDAO.insert(habito);

        verify(mockConnection).prepareStatement(
                "INSERT INTO Habito (nome, descricao, pontuacao) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        verify(mockPreparedStatement).setString(1, habito.getNome());
        verify(mockPreparedStatement).setString(2, habito.getDescricao());
        verify(mockPreparedStatement).setInt(3, habito.getPontuacao());
        verify(mockPreparedStatement).executeUpdate();
        assertEquals(10, habito.getIdHabito());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve atualizar um hábito existente")
    void update_ShouldUpdateExistingHabito() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Habito habito = new Habito();
        habito.setIdHabito(1);
        habito.setNome("Economizar Água Atualizado");
        habito.setDescricao("Tomar banhos mais curtos");
        habito.setPontuacao(10);

        habitoDAO.update(habito);

        verify(mockConnection).prepareStatement(
                "UPDATE Habito SET nome = ?, descricao = ?, pontuacao = ? WHERE id_habito = ?"
        );
        verify(mockPreparedStatement).setString(1, habito.getNome());
        verify(mockPreparedStatement).setString(2, habito.getDescricao());
        verify(mockPreparedStatement).setInt(3, habito.getPontuacao());
        verify(mockPreparedStatement).setInt(4, habito.getIdHabito());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve deletar um hábito por ID")
    void deleteById_ShouldDeleteHabito() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        habitoDAO.deleteById(1);

        verify(mockConnection).prepareStatement("DELETE FROM Habito WHERE id_habito = ?");
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve encontrar um hábito por ID")
    void findById_ShouldReturnHabitoWhenFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id_habito")).thenReturn(1);
        when(mockResultSet.getString("nome")).thenReturn("Reciclar");
        when(mockResultSet.getString("descricao")).thenReturn("Separar lixo reciclável");
        when(mockResultSet.getInt("pontuacao")).thenReturn(15);

        Habito habito = habitoDAO.findById(1);

        assertNotNull(habito);
        assertEquals(1, habito.getIdHabito());
        assertEquals("Reciclar", habito.getNome());
        assertEquals("Separar lixo reciclável", habito.getDescricao());
        assertEquals(15, habito.getPontuacao());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar null quando o hábito não for encontrado por ID")
    void findById_ShouldReturnNullWhenNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Habito habito = habitoDAO.findById(999);

        assertNull(habito);
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar todos os hábitos ordenados por nome")
    void findAll_ShouldReturnAllHabitosOrderedByName() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_habito")).thenReturn(1, 2);
        when(mockResultSet.getString("nome")).thenReturn("A", "B");
        when(mockResultSet.getString("descricao")).thenReturn("Desc A", "Desc B");
        when(mockResultSet.getInt("pontuacao")).thenReturn(5, 10);

        List<Habito> habitos = habitoDAO.findAll();

        assertNotNull(habitos);
        assertEquals(2, habitos.size());
        assertEquals("A", habitos.get(0).getNome());
        assertEquals("B", habitos.get(1).getNome());

        verify(mockResultSet, times(3)).next();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não houver hábitos")
    void findAll_ShouldReturnEmptyListWhenNoHabitosExist() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Habito> habitos = habitoDAO.findAll();

        assertNotNull(habitos);
        assertTrue(habitos.isEmpty());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }
}
