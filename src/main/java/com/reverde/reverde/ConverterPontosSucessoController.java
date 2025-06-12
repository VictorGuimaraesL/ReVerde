package com.reverde.reverde;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import com.reverde.reverde.util.App;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ConverterPontosSucessoController implements Initializable {

    @FXML
    private Label successMessageLabel;

    @FXML
    private Integer pointsGained;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    public void onBackToProfileClick(MouseEvent event) {
        App.getInstance().loadScene("perfil.fxml");
    }

    @FXML
    public void onDownloadCertificateClick(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Certificado");
        fileChooser.setInitialFileName("CertificadoEmbaixadorSustentabilidade_" + LocalDate.now() + ".txt"); // Nome padrão do arquivo
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto (*.txt)", "*.txt"));
        // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                String certificateContent = "========================================\n" +
                        "     CERTIFICADO DE RECONHECIMENTO      \n" +
                        "========================================\n\n" +
                        "A Reverde tem a honra de certificar que\n\n" +
                        "       Alane Damasceno\n\n" +
                        "se tornou oficialmente um(a):\n\n" +
                        "  EMBAIXADOR(A) DA SUSTENTABILIDADE\n\n" +
                        "por sua dedicação e contribuições para o meio ambiente.\n\n" +
                        "Data de Emissão: " + LocalDate.now() + "\n\n" +
                        "========================================\n" +
                        "           Reverde - Juntos pelo Futuro\n" +
                        "========================================\n";


                FileWriter writer = new FileWriter(file);
                writer.write(certificateContent);
                writer.close();

//                App.getInstance().showAlert(
//                        javafx.scene.control.Alert.AlertType.INFORMATION,
//                        "Sucesso!",
//                        "Certificado Salvo",
//                        "O certificado foi salvo com sucesso em:\n" + file.getAbsolutePath()
//                );

            } catch (IOException e) {
                e.printStackTrace();
//                App.getInstance().showAlert(
//                        javafx.scene.control.Alert.AlertType.ERROR,
//                        "Erro ao Salvar",
//                        "Não foi possível salvar o certificado.",
//                        "Detalhes: " + e.getMessage()
//                );
            }
//        } else {
//            App.getInstance().showAlert(
//                    javafx.scene.control.Alert.AlertType.INFORMATION,
//                    "Operação Cancelada",
//                    null,
//                    "O salvamento do certificado foi cancelado."
//            );
        }
    }
}