package com.reverde.reverde;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import com.reverde.reverde.model.entities.Usuario;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ConverterPontosSucessoController implements Initializable {

    @FXML
    private Label successMessageLabel;
    @FXML
    private Integer pointsGained;
    @FXML
    private AppService appService;
    @FXML
    private byte[] certificateImageData;

    public ConverterPontosSucessoController() {
        this.appService = App.getInstance();
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    public void setCertificateImageData(byte[] certificateImageData) {
        this.certificateImageData = certificateImageData;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.appService == null) {
            this.appService = App.getInstance();
        }
    }

    @FXML
    public void onBackToProfileClick(MouseEvent event) {
        appService.loadScene("perfil.fxml");
    }

    @FXML
    public void onDownloadCertificateClick(ActionEvent event) {
        Stage stage = appService.getPrimaryStage();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Certificado");
        fileChooser.setInitialFileName("CertificadoEmbaixadorSustentabilidade_" + LocalDate.now() + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Imagem", "*.png"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                if (certificateImageData != null) {
                    fos.write(certificateImageData);
                } else {
                    System.err.println("Erro: Dados da imagem do certificado não disponíveis.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
