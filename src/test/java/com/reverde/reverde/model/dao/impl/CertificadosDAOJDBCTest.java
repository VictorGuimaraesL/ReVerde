package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.model.entities.Certificados;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class CertificadosDAOJDBCTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private Statement mockStatement;
    @Mock
    private CertificadosDAOJDBC certificadosDAO;

    @BeforeEach
    void setUp() throws SQLException {
        certificadosDAO = new CertificadosDAOJDBC(mockConnection);
    }

    @Test
    @DisplayName("Deve inserir um novo certificado e atribuir o ID gerado")
    void insert_ShouldInsertNewCertificadoAndAssignGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(
                "INSERT INTO Certificados (id_usuario, id_ponto, descricao, certificado, data_geracao) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )).thenReturn(mockPreparedStatement);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(101);

        Certificados certificado = new Certificados();
        certificado.setIdUsuario(1);
        certificado.setIdPonto(50);
        certificado.setDescricao("Certificado de Teste");
        certificado.setCertificado(new byte[]{1, 2, 3});
        certificado.setDataGeracao(LocalDate.now());

        certificadosDAO.insert(certificado);

        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 50);
        verify(mockPreparedStatement).setString(3, "Certificado de Teste");
        verify(mockPreparedStatement).setBytes(4, new byte[]{1, 2, 3});
        verify(mockPreparedStatement).setTimestamp(5, Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        verify(mockPreparedStatement).executeUpdate();

        assertEquals(101, certificado.getIdCertificado());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar um certificado quando encontrado por ID")
    void findById_ShouldReturnCertificadoWhenFound() throws SQLException, IOException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getInt("id_certificado")).thenReturn(1);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10);
        when(mockResultSet.getInt("id_ponto")).thenReturn(20);
        when(mockResultSet.getString("descricao")).thenReturn("Certificado de Exemplo");

        Blob mockBlob = mock(Blob.class);
        byte[] mockCertificadoData = new byte[]{1, 2, 3};
        when(mockResultSet.getBlob("certificado")).thenReturn(mockBlob);
        when(mockBlob.getBinaryStream()).thenReturn(new ByteArrayInputStream(mockCertificadoData));

        when(mockResultSet.getTimestamp("data_geracao")).thenReturn(
                Timestamp.valueOf(LocalDate.of(2023, 1, 1).atStartOfDay())
        );

        Certificados certificado = certificadosDAO.findById(1);

        verify(mockConnection).prepareStatement("SELECT * FROM Certificados WHERE id_certificado = ?");
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(certificado);
        assertEquals(1, certificado.getIdCertificado());
        assertEquals(10, certificado.getIdUsuario());
        assertEquals(20, certificado.getIdPonto());
        assertEquals("Certificado de Exemplo", certificado.getDescricao());
        assertArrayEquals(new byte[]{1, 2, 3}, certificado.getCertificado());
        assertEquals(LocalDate.of(2023, 1, 1), certificado.getDataGeracao());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar null quando certificado não encontrado por ID")
    void findById_ShouldReturnNullWhenNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Certificados certificado = certificadosDAO.findById(999);

        assertNull(certificado);

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar true se o usuário tiver pontos suficientes para trocar por certificado")
    void podeTrocarPorCertificado_ShouldReturnTrueWhenPointsAreSufficient() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("total_pontos")).thenReturn(150);

        boolean podeTrocar = certificadosDAO.podeTrocarPorCertificado(1, 100);

        verify(mockConnection).prepareStatement("SELECT total_pontos FROM EcoPontos WHERE id_usuario = ?");
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeQuery();
        assertTrue(podeTrocar);

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar false se o usuário não tiver pontos suficientes para trocar por certificado")
    void podeTrocarPorCertificado_ShouldReturnFalseWhenPointsAreInsufficient() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("total_pontos")).thenReturn(50);

        boolean podeTrocar = certificadosDAO.podeTrocarPorCertificado(1, 100);

        assertFalse(podeTrocar);

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar false se o usuário não for encontrado para verificar pontos")
    void podeTrocarPorCertificado_ShouldReturnFalseWhenUserNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean podeTrocar = certificadosDAO.podeTrocarPorCertificado(999, 100);

        assertFalse(podeTrocar);
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve deletar um certificado por ID")
    void deleteById_ShouldDeleteCertificado() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        certificadosDAO.deleteById(1);

        verify(mockConnection).prepareStatement("DELETE FROM Certificados WHERE id_certificado = ?");
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve lançar RuntimeException em caso de SQLException durante a inserção")
    void insert_ShouldThrowRuntimeExceptionOnSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Erro de conexão simulado"));

        Certificados certificado = new Certificados();
        certificado.setIdUsuario(1);
        certificado.setIdPonto(50);
        certificado.setDescricao("Certificado de Erro");
        certificado.setCertificado(new byte[]{1});
        certificado.setDataGeracao(LocalDate.now());

        assertThrows(RuntimeException.class, () -> certificadosDAO.insert(certificado));
    }

    @Test
    @DisplayName("Deve retornar uma lista de certificados para um usuário específico")
    void findByUsuario_ShouldReturnListOfCertificatesForUser() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_certificado")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10, 10);
        when(mockResultSet.getInt("id_ponto")).thenReturn(20, 21);
        when(mockResultSet.getString("descricao")).thenReturn("Certificado 1", "Certificado 2");
        when(mockResultSet.getByte("certificado")).thenReturn((byte) 1, (byte) 1);
        when(mockResultSet.getTimestamp("data_geracao")).thenReturn(
                Timestamp.valueOf(LocalDate.of(2023, 1, 1).atStartOfDay()),
                Timestamp.valueOf(LocalDate.of(2023, 1, 2).atStartOfDay())
        );

        List<Certificados> certificadosList = certificadosDAO.findByUsuario(10);

        assertNotNull(certificadosList);
        assertEquals(2, certificadosList.size());
        assertEquals(1, certificadosList.get(0).getIdCertificado());
        assertEquals("Certificado 1", certificadosList.get(0).getDescricao());
        assertEquals(2, certificadosList.get(1).getIdCertificado());
        assertEquals("Certificado 2", certificadosList.get(1).getDescricao());

        verify(mockResultSet, times(3)).next();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando nenhum certificado for encontrado para o usuário")
    void findByUsuario_ShouldReturnEmptyListWhenNoCertificatesFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Certificados> certificadosList = certificadosDAO.findByUsuario(999);

        assertNotNull(certificadosList);
        assertTrue(certificadosList.isEmpty());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    @DisplayName("Deve retornar todos os certificados na base de dados")
    void findAll_ShouldReturnAllCertificados() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("id_certificado")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_usuario")).thenReturn(10, 11);
        when(mockResultSet.getInt("id_ponto")).thenReturn(20, 22);
        when(mockResultSet.getString("descricao")).thenReturn("All Cert 1", "All Cert 2");
        when(mockResultSet.getByte("certificado")).thenReturn((byte) 1, (byte) 1);
        when(mockResultSet.getTimestamp("data_geracao")).thenReturn(
                Timestamp.valueOf(LocalDate.of(2023, 1, 1).atStartOfDay()),
                Timestamp.valueOf(LocalDate.of(2023, 1, 3).atStartOfDay())
        );

        List<Certificados> allCertificados = certificadosDAO.findAll();

        assertNotNull(allCertificados);
        assertEquals(2, allCertificados.size());
        assertEquals("All Cert 1", allCertificados.get(0).getDescricao());
        assertEquals("All Cert 2", allCertificados.get(1).getDescricao());

        verify(mockResultSet, times(3)).next();
        verify(mockResultSet).close();
        verify(mockStatement).close();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver certificados em findAll")
    void findAll_ShouldReturnEmptyListWhenNoCertificatesExist() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Certificados> allCertificados = certificadosDAO.findAll();

        assertNotNull(allCertificados);
        assertTrue(allCertificados.isEmpty());

        verify(mockResultSet).close();
        verify(mockStatement).close();
    }
}
