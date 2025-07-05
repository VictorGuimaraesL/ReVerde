package com.reverde.reverde;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ConverterPontosSucessoController implements Initializable {

    @FXML
    private Label certificateTitleLabel;
    @FXML
    private Label certificateDescriptionLabel;
    @FXML
    private ImageView certificateImageView;

    private AppService appService;
    private byte[] certificateImageData;

    public ConverterPontosSucessoController() {
        this.appService = App.getInstance();
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    public void initData(Object data) {
        if (data instanceof Object[]) {
            Object[] dataArray = (Object[]) data;
            if (dataArray.length >= 3 && dataArray[0] instanceof String &&
                    dataArray[1] instanceof String && dataArray[2] instanceof byte[]) {

                String title = (String) dataArray[0];
                String description = (String) dataArray[1];
                this.certificateImageData = (byte[]) dataArray[2];

                if (certificateTitleLabel != null) {
                    certificateTitleLabel.setText(title);
                }
                if (certificateDescriptionLabel != null) {
                    certificateDescriptionLabel.setText(description);
                }
                if (certificateImageView != null && this.certificateImageData != null && this.certificateImageData.length > 0) {
                    try {
                        Image image = new Image(new ByteArrayInputStream(this.certificateImageData));
                        certificateImageView.setImage(image);
                    } catch (Exception e) {
                        System.err.println("ERRO ao criar Image a partir de ByteArrayInputStream: " + e.getMessage());
                        e.printStackTrace();
                        certificateImageView.setImage(null);
                    }
                } else {
                    certificateImageView.setImage(null);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.appService == null) {
            this.appService = App.getInstance();
        }
        Object dataFromAppService = appService.getData();
        if (dataFromAppService != null) {
            initData(dataFromAppService);
            appService.setData(null);
        }
    }

    @FXML
    public void onBackToProfileClick(ActionEvent event) {
        appService.loadScene("perfil.fxml");
    }

    @FXML
    public void onDownloadCertificateClick(ActionEvent event) {
        Stage stage = appService.getPrimaryStage();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Certificado");
        String defaultFileName = (certificateTitleLabel != null && !certificateTitleLabel.getText().isEmpty()) ?
                certificateTitleLabel.getText().replace(" ", "_").replace(":", "") :
                "Certificado";
        fileChooser.setInitialFileName(defaultFileName + "_" + LocalDate.now() + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Imagem", "*.png"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                if (certificateImageData != null && certificateImageData.length > 0) {
                    fos.write(certificateImageData);
                    showAlert(Alert.AlertType.INFORMATION, "Download Concluído", "Certificado salvo em: " + file.getAbsolutePath());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro de Download", "Dados da imagem do certificado não disponíveis.");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Download", "Ocorreu um erro ao salvar o certificado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}