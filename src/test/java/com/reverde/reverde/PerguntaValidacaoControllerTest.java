package com.reverde.reverde;

import com.reverde.reverde.model.dao.*;
import com.reverde.reverde.model.entities.*;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(MockitoExtension.class)
public class PerguntaValidacaoControllerTest extends ApplicationTest {

    @Mock
    private PerguntaValidacaoDAO mockPerguntaValidacaoDAO;
    @Mock
    private PerguntaTentativaDAO mockPerguntaTentativaDAO;
    @Mock
    private RegistroHabitoDAO mockRegistroHabitoDAO;
    @Mock
    private EcoPontosDAO mockEcoPontosDAO;
    @Mock
    private AppService mockAppService;
    @Mock
    private PerguntaValidacaoController controller;
    @Mock
    private Usuario loggedInUser;
    @Mock
    private Habito selectedHabit;
    @Mock
    private RegistroHabito pendingRegistroHabito;
    @Mock
    private PerguntaValidacao mockPergunta;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/resources/pergunta_validacao.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setPerguntaValidacaoDAO(mockPerguntaValidacaoDAO);
        controller.setPerguntaTentativaDAO(mockPerguntaTentativaDAO);
        controller.setRegistroHabitoDAO(mockRegistroHabitoDAO);
        controller.setEcoPontosDAO(mockEcoPontosDAO);
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

        selectedHabit = new Habito();
        selectedHabit.setIdHabito(1);
        selectedHabit.setNome("Hábito Teste");
        selectedHabit.setPontuacao(100);

        pendingRegistroHabito = new RegistroHabito();
        pendingRegistroHabito.setIdRegistro(10);
        pendingRegistroHabito.setIdHabito(selectedHabit.getIdHabito());
        pendingRegistroHabito.setIdUsuario(loggedInUser.getIdUsuario());
        pendingRegistroHabito.setDataRegistro(LocalDate.now());
        pendingRegistroHabito.setValidado(false);

        mockPergunta = new PerguntaValidacao();
        mockPergunta.setIdPergunta(1);
        mockPergunta.setIdHabito(selectedHabit.getIdHabito());
        mockPergunta.setPergunta("Qual a resposta correta?");
        mockPergunta.setRespostaCorreta("Água"); // Resposta correta como String

        when(mockAppService.getLoggedInUser()).thenReturn(loggedInUser);
        when(mockAppService.getData()).thenReturn(selectedHabit);
    }

    @Test
    @DisplayName("Deve carregar e exibir a pergunta de validação na inicialização")
    void initialize_ShouldLoadAndDisplayQuestion() {
        when(mockRegistroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario()))
                .thenReturn(Arrays.asList(pendingRegistroHabito));
        when(mockPerguntaValidacaoDAO.findByHabito(selectedHabit.getIdHabito()))
                .thenReturn((List<PerguntaValidacao>) mockPergunta);

        controller.initialize(null, null);

        verifyThat("#questionLabel", hasText("Qual a resposta correta?"));
        // Assumindo que o FXML vai exibir as opções, e o controller pode setar o texto
        // Para o teste de UI, a ordem das opções na UI não importa tanto quanto a lógica de comparação.
        // O teste irá verificar se o texto que o controller espera comparar está lá.
    }

    @Test
    @DisplayName("Deve processar resposta correta, adicionar ecopontos e navegar para acerto")
    void onOptionClick_CorrectAnswer_ShouldAddEcoPointsAndNavigateToSuccess() {
        EcoPontos existingEcoPontos = new EcoPontos();
        existingEcoPontos.setIdUsuario(loggedInUser.getIdUsuario());
        existingEcoPontos.setTotalPontos(500);

        when(mockRegistroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario()))
                .thenReturn(Arrays.asList(pendingRegistroHabito));
        when(mockPerguntaValidacaoDAO.findByHabito(selectedHabit.getIdHabito()))
                .thenReturn((List<PerguntaValidacao>) mockPergunta);
        when(mockEcoPontosDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(existingEcoPontos);

        controller.initialize(null, null);

        // Para simular a seleção correta, precisamos que o label da opção 1 contenha a resposta correta
        lookup("#option1Label").queryAs(javafx.scene.control.Label.class).setText("Opção 1: Água");
        clickOn("#option1VBox");

        verify(mockPerguntaTentativaDAO).insert(any(PerguntaTentativa.class));
        verify(mockRegistroHabitoDAO).updateValidado(eq(pendingRegistroHabito.getIdRegistro()), eq(true));
        verify(mockEcoPontosDAO).adicionarPontos(eq(loggedInUser.getIdUsuario()), eq(selectedHabit.getPontuacao()));
        verify(mockAppService).loadScene("pergunta_validacao_acerto.fxml", selectedHabit.getPontuacao());
    }

    @Test
    @DisplayName("Deve processar resposta incorreta e navegar para erro")
    void onOptionClick_IncorrectAnswer_ShouldNavigateToError() {
        when(mockRegistroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario()))
                .thenReturn(Arrays.asList(pendingRegistroHabito));
        when(mockPerguntaValidacaoDAO.findByHabito(selectedHabit.getIdHabito()))
                .thenReturn((List<PerguntaValidacao>) mockPergunta);

        controller.initialize(null, null);

        lookup("#option2Label").queryAs(javafx.scene.control.Label.class).setText("Opção 2: Errado");
        clickOn("#option2VBox");

        verify(mockPerguntaTentativaDAO).insert(any(PerguntaTentativa.class));
        verify(mockRegistroHabitoDAO, never()).updateValidado(anyInt(), anyBoolean());
        verify(mockEcoPontosDAO, never()).adicionarPontos(anyInt(), anyInt());
        verify(mockEcoPontosDAO, never()).insert(any(EcoPontos.class));

        verify(mockAppService).loadScene("pergunta_validacao_erro.fxml");
    }

    @Test
    @DisplayName("Deve chamar goBack ao clicar no botão 'Voltar'")
    void onBackButtonClick_ShouldCallGoBack() {
        clickOn("#backButton");

        verify(mockAppService).goBack();
    }
}