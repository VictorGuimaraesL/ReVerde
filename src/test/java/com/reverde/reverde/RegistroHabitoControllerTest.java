package com.reverde.reverde;

import com.reverde.reverde.model.dao.HabitoDAO;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.entities.Habito;
import com.reverde.reverde.model.entities.RegistroHabito;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(MockitoExtension.class)
public class RegistroHabitoControllerTest extends ApplicationTest {

    @Mock
    private HabitoDAO mockHabitoDAO;
    @Mock
    private RegistroHabitoDAO mockRegistroHabitoDAO;
    @Mock
    private AppService mockAppService;
    @Mock
    private RegistroHabitoController controller;
    @Mock
    private Usuario loggedInUser;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/resources/registro_habito.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setHabitoDAO(mockHabitoDAO);
        controller.setRegistroHabitoDAO(mockRegistroHabitoDAO);
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
    @DisplayName("Deve carregar e exibir a lista de hábitos na inicialização")
    void initialize_ShouldLoadAndDisplayHabits() {
        Habito habito1 = new Habito();
        habito1.setIdHabito(1);
        habito1.setNome("Hábito A");
        habito1.setDescricao("Desc A");
        habito1.setPontuacao(50);

        Habito habito2 = new Habito();
        habito2.setIdHabito(2);
        habito2.setNome("Hábito B");
        habito2.setDescricao("Desc B");
        habito2.setPontuacao(75);
        List<Habito> habits = Arrays.asList(habito1, habito2);

        when(mockHabitoDAO.findAll()).thenReturn(habits);
        when(mockRegistroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(Arrays.asList());

        controller.initialize(null, null);

        List<String> habitNames = lookup(".hbox").queryAll().stream()
                .skip(1)
                .flatMap(hbox -> ((HBox) hbox).getChildren().stream())
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node).getText())
                .filter(text -> text.startsWith("Hábito"))
                .collect(Collectors.toList());

        assertTrue(habitNames.contains("Hábito A"));
        assertTrue(habitNames.contains("Hábito B"));
        assertEquals(2, habitNames.size());
    }

    @Test
    @DisplayName("Deve registrar hábito e navegar para validação ao clicar em um hábito")
    void onHabitClick_ShouldRegisterHabitAndNavigateToValidation() {
        Habito selectedHabito = new Habito();
        selectedHabito.setIdHabito(1);
        selectedHabito.setNome("Hábito Teste");
        selectedHabito.setPontuacao(100);

        when(mockHabitoDAO.findAll()).thenReturn(Arrays.asList(selectedHabito));
        when(mockRegistroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(Arrays.asList());


        controller.initialize(null, null);

        clickOn((HBox) lookup(".hbox").nth(1).query());

        verify(mockRegistroHabitoDAO).insert(any(RegistroHabito.class));
        verify(mockAppService).loadScene("pergunta_validacao.fxml", selectedHabito);
    }

    @Test
    @DisplayName("Deve mostrar 'Registrado Hoje' para hábitos já registrados hoje")
    void initialize_ShouldMarkHabitsAsRegisteredToday() {
        Habito habito1 = new Habito();
        habito1.setIdHabito(1);
        habito1.setNome("Hábito A");
        habito1.setPontuacao(50);
        Habito habito2 = new Habito();
        habito2.setIdHabito(2);
        habito2.setNome("Hábito B");
        habito2.setPontuacao(75);
        List<Habito> habits = Arrays.asList(habito1, habito2);

        RegistroHabito registeredToday = new RegistroHabito();
        registeredToday.setIdHabito(habito1.getIdHabito());
        registeredToday.setIdUsuario(loggedInUser.getIdUsuario());
        registeredToday.setDataRegistro(LocalDate.now());
        registeredToday.setValidado(false);

        when(mockHabitoDAO.findAll()).thenReturn(habits);
        when(mockRegistroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario())).thenReturn(Arrays.asList(registeredToday));

        controller.initialize(null, null);

        HBox firstHabitHBox = lookup(".hbox").nth(1).query();
        assertTrue(firstHabitHBox.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node).getText())
                .anyMatch(text -> text.contains("Registrado Hoje")));

        HBox secondHabitHBox = lookup(".hbox").nth(2).query();
        assertFalse(secondHabitHBox.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node).getText())
                .anyMatch(text -> text.contains("Registrado Hoje")));
    }

    @Test
    @DisplayName("Deve chamar goBack ao clicar no botão 'Voltar'")
    void onBackButtonClick_ShouldCallGoBack() {
        clickOn("#backButton");

        verify(mockAppService).goBack();
    }
}
