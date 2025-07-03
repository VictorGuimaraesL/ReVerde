package com.reverde.reverde;

import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(MockitoExtension.class)
public class ConverterPontosSucessoControllerTest extends ApplicationTest {

    @Mock
    private AppService mockAppService;
    @Mock
    private Stage mockStage;
    @Mock
    private ConverterPontosSucessoController controller;
    @Mock
    private byte[] dummyImageData = new byte[]{0x11, 0x22, 0x33, 0x44};

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/resources/converter_pontos_sucesso.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setAppService(mockAppService);
        controller.setCertificateImageData(dummyImageData);

        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() {
        Usuario loggedInUser = new Usuario();
        loggedInUser.setIdUsuario(1);
        loggedInUser.setNome("João Teste");
        when(mockAppService.getLoggedInUser()).thenReturn(loggedInUser);
        when(mockAppService.getPrimaryStage()).thenReturn(mockStage);
    }

    @Test
    @DisplayName("Deve navegar para 'perfil.fxml' ao clicar no botão 'Voltar ao Perfil'")
    void onBackToProfileClick_ShouldLoadProfileScene() {
        clickOn("#backToProfileButton");

        verify(mockAppService).loadScene("perfil.fxml");
    }

    @Test
    @DisplayName("Deve tentar baixar o certificado ao clicar no botão 'Baixar Certificado'")
    void onDownloadCertificateClick_ShouldAttemptDownload() {
        clickOn("#downloadCertificateButton");

        verify(mockAppService).getPrimaryStage();
    }
}
