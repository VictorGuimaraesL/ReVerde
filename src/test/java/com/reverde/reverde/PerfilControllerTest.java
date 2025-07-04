package com.reverde.reverde;

import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.dao.UsuarioDAO;
import com.reverde.reverde.model.entities.EcoPontos;
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
public class PerfilControllerTest extends ApplicationTest {

    @Mock
    private UsuarioDAO mockUsuarioDAO;
    @Mock
    private EcoPontosDAO mockEcoPontosDAO;
    @Mock
    private AppService mockAppService;
    @Mock
    private PerfilController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main/resources/com/reverde/reverde/perfil.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setUsuarioDAO(mockUsuarioDAO);
        controller.setEcoPontosDAO(mockEcoPontosDAO);
        controller.setAppService(mockAppService);

        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() {
        Usuario loggedInUser = new Usuario();
        loggedInUser.setIdUsuario(1);
        loggedInUser.setNome("Alane Damasceno");
        when(mockAppService.getLoggedInUser()).thenReturn(loggedInUser);
    }

    @Test
    @DisplayName("Deve carregar e exibir os dados do usuário e ecopontos na inicialização")
    void initialize_ShouldLoadUserDataAndEcoPontos() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNome("João Silva");
        EcoPontos ecoPontos = new EcoPontos();
        ecoPontos.setTotalPontos(1000);

        when(mockUsuarioDAO.findById(1)).thenReturn(usuario);
        when(mockEcoPontosDAO.findByUsuario(1)).thenReturn(ecoPontos);

        controller.initialize(null, null);

        verifyThat("#userNameLabel", hasText("João Silva"));
        verifyThat("#ecopontosLabel", hasText("1000 Ecopontos"));
        verifyThat("#userLocationLabel", hasText("Aracati, CE"));
    }

    @Test
    @DisplayName("Deve exibir 'Usuário Desconhecido' se o usuário não for encontrado")
    void initialize_ShouldHandleUserNotFound() {
        when(mockUsuarioDAO.findById(anyInt())).thenReturn(null);
        when(mockEcoPontosDAO.findByUsuario(anyInt())).thenReturn(null);

        controller.initialize(null, null);

        verifyThat("#userNameLabel", hasText("Usuário Desconhecido"));
        verifyThat("#ecopontosLabel", hasText("0 Ecopontos"));
        verifyThat("#userLocationLabel", hasText("N/A"));
    }

    @Test
    @DisplayName("Deve chamar goBack ao clicar no botão 'Voltar'")
    void onBackButtonClick_ShouldCallGoBack() {
        clickOn("#backButton");

        verify(mockAppService).goBack();
    }

    @Test
    @DisplayName("Deve navegar para 'converter_pontos.fxml' ao clicar no botão 'Converter Pontos'")
    void onConvertPointsClick_ShouldLoadConvertPointsScene() {
        clickOn("#convertPointsButton");

        verify(mockAppService).loadScene("converter_pontos.fxml");
    }

    @Test
    @DisplayName("Deve navegar para 'registro_habito.fxml' ao clicar no botão 'Registrar Hábito'")
    void onRegistrarHabitoClick_ShouldLoadRegistrarHabitoScene() {
        clickOn("#registerHabitButton");

        verify(mockAppService).loadScene("registro_habito.fxml");
    }
}