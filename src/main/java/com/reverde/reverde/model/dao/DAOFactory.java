package com.reverde.reverde.model.dao;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.impl.UsuarioDAOJDBC;
import com.reverde.reverde.model.dao.impl.HabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.PerguntaTentativaDAOJDBC;
import com.reverde.reverde.model.dao.impl.PerguntaValidacaoDAOJDBC;
import com.reverde.reverde.model.dao.impl.RegistroHabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;
import com.reverde.reverde.model.dao.impl.CertificadosDAOJDBC;

public class DAOFactory {

    public static UsuarioDAO createUsuarioDAO() {
        return new UsuarioDAOJDBC(DB.getConnection());
    }

    public static HabitoDAO createHabitoDAO() {
        return new HabitoDAOJDBC(DB.getConnection());
    }

    public static PerguntaTentativaDAO createPerguntaTentativaDAO() {
        return new PerguntaTentativaDAOJDBC(DB.getConnection());
    }

    public static PerguntaValidacaoDAO createPerguntaValidacaoDAO() {
        return new PerguntaValidacaoDAOJDBC(DB.getConnection());
    }

    public static RegistroHabitoDAO createRegistroHabitoDAO() {
        return new RegistroHabitoDAOJDBC(DB.getConnection());
    }

    public static EcoPontosDAO createEcoPontosDAO() {
        return new EcoPontosDAOJDBC(DB.getConnection());
    }

    public static CertificadosDAO createCertificadosDAO() {
        return new CertificadosDAOJDBC(DB.getConnection());
    }
}