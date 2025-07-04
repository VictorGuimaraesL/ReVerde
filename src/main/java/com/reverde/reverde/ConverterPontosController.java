package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.CertificadosDAO;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.dao.impl.CertificadosDAOJDBC;
import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;
import com.reverde.reverde.model.entities.Certificados;
import com.reverde.reverde.model.entities.EcoPontos;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ConverterPontosController implements Initializable {

    @FXML
    private Label ecopontosToConvertLabel;
    @FXML
    private Label currentLevelTitleLabel;
    @FXML
    private Label currentLevelDescriptionLabel;
    @FXML
    private ImageView currentLevelImageView;
    @FXML
    private Button convertNowButton;

    private EcoPontosDAO ecoPontosDAO;
    private CertificadosDAO certificadosDAO;
    private AppService appService; // Usando AppService

    private Usuario loggedInUser;
    private int userTotalEcoPoints;
    private int userCurrentLevel;
    private int nextLevelToAchieve;
    private int pointsNeededForNextLevel;

    public ConverterPontosController() {
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
        try {
            if (DB.getConnection() == null) {
                showAlert(Alert.AlertType.ERROR, "Erro de Conexão", "Não foi possível conectar ao banco de dados. A conexão é nula.");
                disableUI();
                return;
            }
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
            this.certificadosDAO = new CertificadosDAOJDBC(DB.getConnection());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Inicialização do DB", "Erro ao inicializar DAOs: " + e.getMessage());
            e.printStackTrace();
            disableUI();
            return;
        }

        loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para carregar ecopontos. Por favor, faça login.");
            disableUI();
            return;
        }

        loadEcoPointsAndCertificateInfo();
    }

    private void disableUI() {
        ecopontosToConvertLabel.setText("Seus Ecopontos: N/A");
        currentLevelTitleLabel.setText("Nível: N/A");
        currentLevelDescriptionLabel.setText("Informações de certificado não disponíveis.");
        currentLevelImageView.setImage(null);
        convertNowButton.setDisable(true);
    }

    private void loadEcoPointsAndCertificateInfo() {
        try {
            EcoPontos userEcoPontos = ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario());
            userTotalEcoPoints = (userEcoPontos != null) ? userEcoPontos.getTotalPontos() : 0;
            ecopontosToConvertLabel.setText(userTotalEcoPoints + " Ecopontos");

            userCurrentLevel = ((CertificadosDAOJDBC) certificadosDAO).getCurrentUserLevel(loggedInUser.getIdUsuario());

            if (userCurrentLevel == 0) {
                currentLevelTitleLabel.setText("Nível: Sem Certificado");
                currentLevelDescriptionLabel.setText("Conquiste seu primeiro certificado!");
                currentLevelImageView.setImage(null);
                nextLevelToAchieve = 1;
            } else {
                CertificadosDAOJDBC.CertificateData currentCertData = CertificadosDAOJDBC.getCertificateDataByLevel(userCurrentLevel);
                currentLevelTitleLabel.setText("Nível " + userCurrentLevel + " - " + currentCertData.title);
                currentLevelDescriptionLabel.setText(currentCertData.description);

                byte[] currentCertificateImageBytes = CertificadosDAOJDBC.getCertificateImageBytes(userCurrentLevel);
                if (currentCertificateImageBytes != null && currentCertificateImageBytes.length > 0) {
                    Image image = new Image(new ByteArrayInputStream(currentCertificateImageBytes));
                    currentLevelImageView.setImage(image);
                } else {
                    currentLevelImageView.setImage(null);
                }
                nextLevelToAchieve = CertificadosDAOJDBC.getNextLevel(userCurrentLevel);
            }

            if (nextLevelToAchieve <= 7) {
                CertificadosDAOJDBC.CertificateData nextCertData = CertificadosDAOJDBC.getCertificateDataByLevel(nextLevelToAchieve);
                pointsNeededForNextLevel = nextCertData.pointsRequired;

                convertNowButton.setText("Gerar Certificado: " + nextCertData.title + " (" + pointsNeededForNextLevel + " Ecopontos)");
                convertNowButton.setDisable(userTotalEcoPoints < pointsNeededForNextLevel);
            } else {
                nextLevelToAchieve = 7;
                pointsNeededForNextLevel = 0;
                convertNowButton.setText("Você alcançou o Nível Máximo!");
                convertNowButton.setDisable(true);
            }

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar seus ecopontos ou informações de certificado: " + e.getMessage());
            disableUI();
            e.printStackTrace();
        }
    }

    @FXML
    public void onConvertNowClick(ActionEvent event) {
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para converter pontos.");
            return;
        }

        if (nextLevelToAchieve > 7) {
            showAlert(Alert.AlertType.INFORMATION, "Nível Máximo Atingido", "Você já alcançou o nível máximo de sustentabilidade!");
            return;
        }

        if (userTotalEcoPoints < pointsNeededForNextLevel) {
            showAlert(Alert.AlertType.INFORMATION, "Pontos Insuficientes",
                    "Você precisa de " + pointsNeededForNextLevel + " ecopontos para avançar para o próximo nível. Você tem apenas " + userTotalEcoPoints + ".");
            return;
        }

        try {
            if (certificadosDAO.podeTrocarPorCertificado(loggedInUser.getIdUsuario(), pointsNeededForNextLevel)) {
                ecoPontosDAO.removerPontos(loggedInUser.getIdUsuario(), pointsNeededForNextLevel);

                CertificadosDAOJDBC.CertificateData certData = CertificadosDAOJDBC.getCertificateDataByLevel(nextLevelToAchieve);
                byte[] certificateImageBytes = CertificadosDAOJDBC.getCertificateImageBytes(nextLevelToAchieve);

                Certificados newCertificado = new Certificados();
                newCertificado.setIdUsuario(loggedInUser.getIdUsuario());
                newCertificado.setDescricao(certData.description);
                newCertificado.setCertificado(certificateImageBytes);
                newCertificado.setDataGeracao(LocalDate.now());

                certificadosDAO.insert(newCertificado);

                loadEcoPointsAndCertificateInfo();

                appService.loadScene("converter_pontos_sucesso.fxml", new Object[]{
                        certData.title,
                        certData.description,
                        certificateImageBytes
                });
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Pontos Insuficientes",
                        "Você precisa de " + pointsNeededForNextLevel + " ecopontos para gerar este certificado. Você tem menos que isso.");
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Ocorreu um erro ao tentar converter pontos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}