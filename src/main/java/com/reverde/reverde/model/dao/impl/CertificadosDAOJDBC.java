package com.reverde.reverde.model.dao.impl;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.CertificadosDAO;
import com.reverde.reverde.model.entities.Certificados;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CertificadosDAOJDBC implements CertificadosDAO {

    private Connection conn;

    private static final Map<Integer, CertificateData> CERTIFICATE_DATA = new HashMap<>();
    private static final Map<Integer, byte[]> CERTIFICATE_IMAGE_BYTES_BY_LEVEL = new HashMap<>();

    static {
        // Nomes dos arquivos de imagem corrigidos (sem acentos e espaços por underline)
        CERTIFICATE_DATA.put(1, new CertificateData(100, "Iniciante Sustentável", "Participação no Projeto", "INICIANTE_SUSTENTAVEL.png"));
        CERTIFICATE_DATA.put(2, new CertificateData(250, "Amigo do Meio Ambiente", "Colaborador em Praticas Sustentáveis", "AMIGO_DO_MEIO_AMBIENTE.png"));
        CERTIFICATE_DATA.put(3, new CertificateData(500, "Guardião Verde", "Guardião das Ações Ambientais", "GUARDIAO_VERDE.png"));
        CERTIFICATE_DATA.put(4, new CertificateData(1000, "Defensor da Natureza", "Protagonista em Sustentabilidade", "DEFENSOR_DA_NATUREZA.png"));
        CERTIFICATE_DATA.put(5, new CertificateData(2000, "Embaixador da Sustentabilidade", "Certificado de Impacto Ecológico Positivo", "EMBAIXADOR_DA_SUSTENTABILIDADE.png"));
        CERTIFICATE_DATA.put(6, new CertificateData(3500, "Líder Ecológico", "Liderança em Transformações Sustentáveis", "LIDER_ECOLOGICO.png"));
        CERTIFICATE_DATA.put(7, new CertificateData(7000, "Mestre da Ecologia", "Reconhecimento Máximo em Sustentabilidade", "MESTRE_DA_ECOLOGIA.png"));

        for (Map.Entry<Integer, CertificateData> entry : CERTIFICATE_DATA.entrySet()) {
            int level = entry.getKey();
            String filename = entry.getValue().filename;
            String resourcePath = "/assets/" + filename;
            try (InputStream is = CertificadosDAOJDBC.class.getResourceAsStream(resourcePath)) {
                if (is != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    CERTIFICATE_IMAGE_BYTES_BY_LEVEL.put(level, buffer.toByteArray());
                } else {
                    System.err.println("AVISO: Imagem do certificado NÃO ENCONTRADA para o nível " + level + ": " + filename + ". Verifique se o arquivo está em src/main/resources/assets/ e o nome está correto (case-sensitive).");
                    CERTIFICATE_IMAGE_BYTES_BY_LEVEL.put(level, new byte[0]);
                }
            } catch (IOException e) {
                System.err.println("ERRO ao carregar imagem do certificado para o nível " + level + ": " + filename + " - " + e.getMessage());
                CERTIFICATE_IMAGE_BYTES_BY_LEVEL.put(level, new byte[0]);
            }
        }
    }

    public static class CertificateData {
        public final int pointsRequired;
        public final String title;
        public final String description;
        public final String filename;

        public CertificateData(int pointsRequired, String title, String description, String filename) {
            this.pointsRequired = pointsRequired;
            this.title = title;
            this.description = description;
            this.filename = filename;
        }
    }

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
            if (obj.getIdPonto() != null) {
                st.setInt(2, obj.getIdPonto());
            } else {
                st.setNull(2, Types.INTEGER);
            }
            st.setString(3, obj.getDescricao());

            if (obj.getCertificado() != null && obj.getCertificado().length > 0) {
                st.setBytes(4, obj.getCertificado());
            } else {
                st.setNull(4, Types.LONGVARBINARY);
            }

            st.setTimestamp(5, Timestamp.valueOf(obj.getDataGeracao().atStartOfDay()));

            st.executeUpdate();

            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                obj.setIdCertificado(rs.getInt(1));
            }
            DB.closeResultSet(rs);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir certificado: " + e.getMessage(), e);
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
            throw new RuntimeException("Erro ao deletar certificado: " + e.getMessage(), e);
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
            throw new RuntimeException("Erro ao buscar certificado por ID: " + e.getMessage(), e);
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
            st = conn.prepareStatement("SELECT * FROM Certificados WHERE id_usuario = ? ORDER BY data_geracao DESC");
            st.setInt(1, idUsuario);
            rs = st.executeQuery();

            List<Certificados> list = new ArrayList<>();
            while (rs.next()) {
                list.add(instantiateCertificado(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar certificados por usuário: " + e.getMessage(), e);
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
            rs = st.executeQuery("SELECT * FROM Certificados ORDER BY data_geracao DESC");

            List<Certificados> list = new ArrayList<>();
            while (rs.next()) {
                list.add(instantiateCertificado(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os certificados: " + e.getMessage(), e);
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
            throw new RuntimeException("Erro ao verificar pontos para certificado: " + e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private Certificados instantiateCertificado(ResultSet rs) throws SQLException {
        Certificados obj = new Certificados();
        obj.setIdCertificado(rs.getInt("id_certificado"));
        obj.setIdUsuario(rs.getInt("id_usuario"));

        int idPonto = rs.getInt("id_ponto");
        if (rs.wasNull()) {
            obj.setIdPonto(null);
        } else {
            obj.setIdPonto(idPonto);
        }

        obj.setDescricao(rs.getString("descricao"));

        Blob certificadoBlob = rs.getBlob("certificado");
        if (certificadoBlob != null) {
            try (InputStream is = certificadoBlob.getBinaryStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                obj.setCertificado(buffer.toByteArray());
            } catch (IOException e) {
                System.err.println("Erro ao ler BLOB do certificado: " + e.getMessage());
                obj.setCertificado(new byte[0]);
            }
        } else {
            obj.setCertificado(new byte[0]);
        }

        obj.setDataGeracao(rs.getTimestamp("data_geracao").toLocalDateTime().toLocalDate());
        return obj;
    }

    public int getCurrentUserLevel(int idUsuario) {
        List<Certificados> userCertificates = findByUsuario(idUsuario);
        if (userCertificates.isEmpty()) {
            return 0;
        }

        Optional<Integer> maxLevel = userCertificates.stream()
                .map(cert -> {
                    Optional<Map.Entry<Integer, CertificateData>> matchedEntry = CERTIFICATE_DATA.entrySet().stream()
                            .filter(entry -> entry.getValue().description.equals(cert.getDescricao()))
                            .findFirst();
                    if (matchedEntry.isPresent()) {
                        return matchedEntry.get().getKey();
                    } else {
                        return 0;
                    }
                })
                .max(Comparator.naturalOrder());

        return maxLevel.orElse(0);
    }

    public static CertificateData getCertificateDataByLevel(int level) {
        if (level == 0) {
            return CERTIFICATE_DATA.get(1);
        }
        return CERTIFICATE_DATA.getOrDefault(level, CERTIFICATE_DATA.get(1));
    }

    public static byte[] getCertificateImageBytes(int level) {
        if (level == 0) {
            return CERTIFICATE_IMAGE_BYTES_BY_LEVEL.getOrDefault(1, new byte[0]);
        }
        return CERTIFICATE_IMAGE_BYTES_BY_LEVEL.getOrDefault(level, new byte[0]);
    }

    public static int getNextLevel(int currentLevel) {
        if (currentLevel >= 7) {
            return 7;
        }
        return currentLevel + 1;
    }
}