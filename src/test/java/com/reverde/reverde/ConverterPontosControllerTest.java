package com.reverde.reverde;

import com.reverde.reverde.model.dao.CertificadosDAO;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.entities.Certificados;
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
public class ConverterPontosControllerTest extends ApplicationTest {

    @Mock
    private EcoPontosDAO mockEcoPontosDAO;
    @Mock
    private CertificadosDAO mockCertificadosDAO;
    @Mock
    private AppService mockAppService;
    @Mock
    private ConverterPontosController controller;
    @Mock
    private Usuario loggedInUser;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/resources/converter_pontos.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setEcoPontosDAO(mockEcoPontosDAO);
        controller.setCertificadosDAO(mockCertificadosDAO);
        controller.setAppService(mockAppService);

        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() {
        loggedInUser = new Usuario();
        loggedInUser.setIdUsuario(1);
        loggedInUser.setNome("Teste");
        when(mockAppService.getLoggedInUser()).thenReturn(loggedInUser);
    }

    @Test
    @DisplayName("Deve carregar e exibir o total de ecopontos do usuário na inicialização")
    void initialize_ShouldLoadAndDisplayUserEcoPoints() {
        EcoPontos ecoPontos = new EcoPontos();
        ecoPontos.setTotalPontos(1500);
        when(mockEcoPontosDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(ecoPontos);

        controller.initialize(null, null);

        verifyThat("#ecopontosToConvertLabel", hasText("Seus Ecopontos: 1500"));
    }

    @Test
    @DisplayName("Deve exibir 0 Ecopontos se o usuário não tiver registros")
    void initialize_ShouldDisplayZeroEcoPointsIfNotFound() {
        when(mockEcoPontosDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(null);

        controller.initialize(null, null);

        verifyThat("#ecopontosToConvertLabel", hasText("Seus Ecopontos: 0"));
    }

    @Test
    @DisplayName("Deve converter pontos e navegar para sucesso se tiver pontos suficientes")
    void onConvertNowClick_ShouldConvertPointsAndNavigateToSuccess() {
        EcoPontos ecoPontos = new EcoPontos();
        ecoPontos.setTotalPontos(1000);
        when(mockEcoPontosDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(ecoPontos);
        when(mockCertificadosDAO.podeTrocarPorCertificado(loggedInUser.getIdUsuario(), 1000)).thenReturn(true);

        controller.initialize(null, null);
        clickOn("#convertNowButton");

        verify(mockEcoPontosDAO).update(argThat(ep -> ep.getTotalPontos() == 0));
        verify(mockCertificadosDAO).insert(any(Certificados.class));
        verify(mockAppService).loadScene("converter_pontos_sucesso.fxml");
    }

    @Test
    @DisplayName("Não deve converter pontos se tiver pontos insuficientes")
    void onConvertNowClick_ShouldNotConvertPointsIfInsufficient() {
        EcoPontos ecoPontos = new EcoPontos();
        ecoPontos.setTotalPontos(500);
        when(mockEcoPontosDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(ecoPontos);
        when(mockCertificadosDAO.podeTrocarPorCertificado(loggedInUser.getIdUsuario(), 1000)).thenReturn(false);

        controller.initialize(null, null);
        clickOn("#convertNowButton");

        verify(mockCertificadosDAO, never()).insert(any(Certificados.class));
        verify(mockEcoPontosDAO, never()).update(any(EcoPontos.class));
        verify(mockAppService, never()).loadScene(anyString());
    }

    @Test
    @DisplayName("Deve chamar goBack ao clicar no botão 'Voltar'")
    void onBackButtonClick_ShouldCallGoBack() {
        clickOn("#backButton");

        verify(mockAppService).goBack();
    }
}