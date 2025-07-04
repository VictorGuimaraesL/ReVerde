package com.reverde.reverde;

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

@ExtendWith(MockitoExtension.class)
public class PerguntaValidacaoErroControllerTest extends ApplicationTest {

    @Mock
    private AppService mockAppService;
    @Mock
    private PerguntaValidacaoErroController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/resources/pergunta_validacao_erro.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setAppService(mockAppService);

        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve navegar para 'perfil.fxml' ao clicar no botão 'Voltar ao Perfil'")
    void onBackToProfileClick_ShouldLoadProfileScene() {
        clickOn("#backToProfileButton");

        verify(mockAppService).loadScene("perfil.fxml");
    }
}