package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.RegistroHabito;
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
public class RegistroHabitoDAOJDBCTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private RegistroHabitoDAOJDBC registroHabitoDAO;

    @BeforeEach
    void setUp() throws SQLException {
        registroHabitoDAO = new RegistroHabitoDAOJDBC(mockConnection);
    }

    @Test
    @DisplayName("Deve inserir um novo registro de hábito e atribuir o ID gerado")
    void insert_ShouldInsertNewRegistroHabitoAndAssignGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(501);

        RegistroHabito registro = new RegistroHabito();
        registro.setIdUsuario(1);
        registro.setIdHabito(10);
        registro.setDataRegistro(LocalDate.now());
        registro.setValidado(false);

        registroHabitoDAO.insert(registro);

        verify(mockConnection).prepareStatement(
                "INSERT INTO RegistroHabito (id_usuario, id_habito, data_registro, validado) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        verify(mockPreparedStatement).setInt(1, registro.getIdUsuario());
        verify(mockPreparedStatement).setInt(2, registro.getIdHabito());
        verify(mockPreparedStatement).setDate(3, Date.valueOf(registro.getDataRegistro()));
        verify(mockPreparedStatement).setBoolean(4, registro.isValidado());
        verify(mockPreparedStatement).executeUpdate();
        assertEquals(501, registro.getIdRegistro());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve atualizar o status 'validado' de um registro de hábito")
    void updateValidado_ShouldUpdateValidadoStatus() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        registroHabitoDAO.updateValidado(1, true);

        verify(mockConnection).prepareStatement(
                "UPDATE RegistroHabito SET validado = ? WHERE id_registro = ?"
        );
        verify(mockPreparedStatement).setBoolean(1, true);
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve encontrar um registro de hábito por ID")
    void findById_ShouldReturnRegistroHabitoWhenFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id_registro")).thenReturn(1);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10);
        when(mockResultSet.getInt("id_habito")).thenReturn(20);
        when(mockResultSet.getDate("data_registro")).thenReturn(Date.valueOf(LocalDate.of(2024, 6, 10)));
        when(mockResultSet.getBoolean("validado")).thenReturn(true);

        RegistroHabito registro = registroHabitoDAO.findById(1);

        assertNotNull(registro);
        assertEquals(1, registro.getIdRegistro());
        assertEquals(10, registro.getIdUsuario());
        assertEquals(20, registro.getIdHabito());
        assertEquals(LocalDate.of(2024, 6, 10), registro.getDataRegistro());
        assertTrue(registro.isValidado());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar null quando o registro de hábito não for encontrado por ID")
    void findById_ShouldReturnNullWhenNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        RegistroHabito registro = registroHabitoDAO.findById(999);

        assertNull(registro);
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista de registros de hábito para um usuário específico")
    void findByUsuario_ShouldReturnListOfRegistrosForUser() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_registro")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10, 10);
        when(mockResultSet.getInt("id_habito")).thenReturn(20, 21);
        when(mockResultSet.getDate("data_registro")).thenReturn(
                Date.valueOf(LocalDate.of(2024, 6, 1)),
                Date.valueOf(LocalDate.of(2024, 6, 2))
        );
        when(mockResultSet.getBoolean("validado")).thenReturn(true, false);

        List<RegistroHabito> registros = registroHabitoDAO.findByUsuario(10);

        assertNotNull(registros);
        assertEquals(2, registros.size());
        assertEquals(1, registros.get(0).getIdRegistro());
        assertEquals(LocalDate.of(2024, 6, 1), registros.get(0).getDataRegistro());
        assertTrue(registros.get(0).isValidado());
        assertEquals(2, registros.get(1).getIdRegistro());
        assertEquals(LocalDate.of(2024, 6, 2), registros.get(1).getDataRegistro());
        assertFalse(registros.get(1).isValidado());

        verify(mockResultSet, times(3)).next();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando nenhum registro for encontrado para o usuário")
    void findByUsuario_ShouldReturnEmptyListWhenNoRegistrosFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<RegistroHabito> registros = registroHabitoDAO.findByUsuario(999);

        assertNotNull(registros);
        assertTrue(registros.isEmpty());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar todos os registros de hábito")
    void findAll_ShouldReturnAllRegistros() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_registro")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10, 11);
        when(mockResultSet.getInt("id_habito")).thenReturn(20, 22);
        when(mockResultSet.getDate("data_registro")).thenReturn(
                Date.valueOf(LocalDate.of(2024, 6, 1)),
                Date.valueOf(LocalDate.of(2024, 6, 3))
        );
        when(mockResultSet.getBoolean("validado")).thenReturn(true, true);

        List<RegistroHabito> todosRegistros = registroHabitoDAO.findAll();

        assertNotNull(todosRegistros);
        assertEquals(2, todosRegistros.size());
        assertEquals(1, todosRegistros.get(0).getIdRegistro());
        assertEquals(2, todosRegistros.get(1).getIdRegistro());

        verify(mockResultSet, times(3)).next();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver registros em findAll")
    void findAll_ShouldReturnEmptyListWhenNoRegistrosExist() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<RegistroHabito> todosRegistros = registroHabitoDAO.findAll();

        assertNotNull(todosRegistros);
        assertTrue(todosRegistros.isEmpty());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve lançar RuntimeException em caso de SQLException durante a inserção")
    void insert_ShouldThrowRuntimeExceptionOnSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Erro de conexão simulado"));

        RegistroHabito registro = new RegistroHabito();
        registro.setIdUsuario(1);
        registro.setIdHabito(10);
        registro.setDataRegistro(LocalDate.now());
        registro.setValidado(false);

        assertThrows(RuntimeException.class, () -> registroHabitoDAO.insert(registro));
    }
}