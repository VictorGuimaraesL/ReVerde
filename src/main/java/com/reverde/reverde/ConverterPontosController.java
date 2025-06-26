package com.reverde.reverde;


import com.reverde.reverde.db.DB;

import com.reverde.reverde.model.dao.CertificadosDAO;

import com.reverde.reverde.model.dao.EcoPontosDAO;

import com.reverde.reverde.model.dao.impl.CertificadosDAOJDBC;

import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;

import com.reverde.reverde.model.entities.Certificados;

import com.reverde.reverde.model.entities.EcoPontos;

import com.reverde.reverde.model.entities.Usuario;

import javafx.fxml.FXML;

import javafx.fxml.Initializable;

import javafx.scene.control.Alert;

import javafx.scene.control.Label;

import javafx.scene.input.MouseEvent;

import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;


import java.net.URL;

import java.time.LocalDate;

import java.util.Optional;

import java.util.ResourceBundle;


public class ConverterPontosController implements Initializable {


    @FXML
    private Label ecopontosToConvertLabel;
    @FXML
    private EcoPontosDAO ecoPontosDAO;
    @FXML
    private CertificadosDAO certificadosDAO;
    @FXML
    private AppService appService;

    private final int POINTS_REQUIRED_FOR_CERTIFICATE = 1000;


    public ConverterPontosController() {
        if (DB.getConnection() != null) {
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
            this.certificadosDAO = new CertificadosDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setEcoPontosDAO(EcoPontosDAO ecoPontosDAO) {
        this.ecoPontosDAO = ecoPontosDAO;
    }

    public void setCertificadosDAO(CertificadosDAO certificadosDAO) {
        this.certificadosDAO = certificadosDAO;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.ecoPontosDAO == null) {
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
            this.certificadosDAO = new CertificadosDAOJDBC(DB.getConnection());
        }
        if (this.appService == null) {
            this.appService = App.getInstance();
        }

        Usuario loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para carregar ecopontos.");
            ecopontosToConvertLabel.setText("Seus Ecopontos: N/A");
            return;
        }

        try {
            EcoPontos userEcoPontos = ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario());
            if (userEcoPontos != null) {
                ecopontosToConvertLabel.setText("Seus Ecopontos: " + userEcoPontos.getTotalPontos());
            } else {
                ecopontosToConvertLabel.setText("Seus Ecopontos: 0");
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar seus ecopontos: " + e.getMessage());
            ecopontosToConvertLabel.setText("Erro ao carregar Ecopontos");
        }
    }


    @FXML
    public void onConvertNowClick(javafx.event.ActionEvent event) {
        Usuario loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para converter pontos.");
            return;
        }

        try {
            if (certificadosDAO.podeTrocarPorCertificado(loggedInUser.getIdUsuario(), POINTS_REQUIRED_FOR_CERTIFICATE)) { // Corrigido aqui
                EcoPontos userEcoPontos = ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario());
                if (userEcoPontos != null) {
                    userEcoPontos.setTotalPontos(userEcoPontos.getTotalPontos() - POINTS_REQUIRED_FOR_CERTIFICATE);
                    ecoPontosDAO.update(userEcoPontos);

                    Certificados newCertificado = new Certificados();
                    newCertificado.setIdUsuario(loggedInUser.getIdUsuario());
                    newCertificado.setDataGeracao(LocalDate.now());
                    newCertificado.setDescricao("Certificado de Embaixador da Sustentabilidade");
                    newCertificado.setCertificado(null);

                    certificadosDAO.insert(newCertificado);

                    appService.loadScene("converter_pontos_sucesso.fxml");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível recuperar seus ecopontos para conversão.");
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Pontos Insuficientes",
                        "Você precisa de " + POINTS_REQUIRED_FOR_CERTIFICATE + " ecopontos para gerar um certificado. Você tem menos que isso.");
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Ocorreu um erro ao tentar converter pontos: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro inesperado: " + e.getMessage());
        }
    }


    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }

    @FXML
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}