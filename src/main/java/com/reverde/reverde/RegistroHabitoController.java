package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.HabitoDAO;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.dao.impl.HabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.RegistroHabitoDAOJDBC;
import com.reverde.reverde.model.entities.Habito;
import com.reverde.reverde.model.entities.RegistroHabito;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.Alertas;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class RegistroHabitoController implements Initializable {

    @FXML
    private VBox habitsListContainer;

    private HabitoDAO habitoDAO;
    private RegistroHabitoDAO registroHabitoDAO;
    private AppService appService;

    private Usuario loggedInUser;

    public RegistroHabitoController() {
        if (DB.getConnection() != null) {
            this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setHabitoDAO(HabitoDAO habitoDAO) { this.habitoDAO = habitoDAO; }
    public void setRegistroHabitoDAO(RegistroHabitoDAO registroHabitoDAO) { this.registroHabitoDAO = registroHabitoDAO; }
    public void setAppService(AppService appService) { this.appService = appService; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.habitoDAO == null) this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
        if (this.registroHabitoDAO == null) this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        if (this.appService == null) this.appService = App.getInstance();

        loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado para registrar hábitos.", Alert.AlertType.ERROR);
            return;
        }

        loadHabits();
    }

    private void loadHabits() {
        try {
            List<Habito> allHabits = habitoDAO.findAll();
            List<RegistroHabito> userValidatedRegistrosToday = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario()).stream()
                    .filter(r -> r.getDataRegistro().equals(LocalDate.now()) && r.isValidado())
                    .collect(Collectors.toList());

            habitsListContainer.getChildren().clear();

            if (allHabits.isEmpty()) {
                Label noHabitsLabel = new Label("Nenhum hábito disponível para registro.");
                noHabitsLabel.setFont(new Font("System", 14.0));
                habitsListContainer.getChildren().add(noHabitsLabel);
                return;
            }

            for (Habito habito : allHabits) {
                HBox habitItem = new HBox(10);
                habitItem.setAlignment(Pos.CENTER_LEFT);
                habitItem.setPadding(new Insets(10, 15, 10, 15));
                habitItem.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 10; -fx-border-color: #C0C0C0; -fx-border-radius: 10;");

                boolean isValidatedToday = userValidatedRegistrosToday.stream()
                        .anyMatch(rh -> rh.getIdHabito() == habito.getIdHabito());

                Label nameLabel = new Label(habito.getNome());
                nameLabel.setFont(new Font("System Bold", 16.0));
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                Label pointsLabel = new Label("+" + habito.getPontuacao() + " pts");
                pointsLabel.setFont(new Font("System Bold", 14.0));
                pointsLabel.setStyle("-fx-text-fill: #00B16A;");

                habitItem.getChildren().addAll(nameLabel, pointsLabel);

                if (isValidatedToday) {
                    Label statusLabel = new Label("Validado Hoje");
                    statusLabel.setFont(new Font("System Italic", 12.0));
                    statusLabel.setStyle("-fx-text-fill: #00B16A;"); // Verde
                    HBox.setMargin(statusLabel, new Insets(0, 0, 0, 10));
                    habitItem.getChildren().add(statusLabel);
                    habitItem.setStyle("-fx-background-color: #E0FFE0; -fx-background-radius: 10; -fx-border-color: #B0D0B0; -fx-border-radius: 10;"); // Fundo verde claro
                } else {
                    habitItem.setOnMouseClicked(event -> onHabitClick(habito));
                }

                habitsListContainer.getChildren().add(habitItem);
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAlerta("Erro de Banco de Dados", null, "Não foi possível carregar os hábitos: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro Geral", null, "Ocorreu um erro ao carregar os hábitos: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void onHabitClick(Habito selectedHabit) {
        Usuario loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado para registrar o hábito.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Optional<RegistroHabito> existingRegistroOpt = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario()).stream()
                    .filter(r -> r.getIdHabito() == selectedHabit.getIdHabito() &&
                            r.getDataRegistro().equals(LocalDate.now()))
                    .findFirst();

            RegistroHabito registroToPass;

            if (existingRegistroOpt.isPresent()) {
                registroToPass = existingRegistroOpt.get();
                if (registroToPass.isValidado()) {
                    Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.INFORMATION), null, "Este hábito já foi VALIDADO para hoje.", Alert.AlertType.INFORMATION);
                    loadHabits();
                    return;
                }
            } else {
                RegistroHabito newRegistro = new RegistroHabito();
                newRegistro.setIdUsuario(loggedInUser.getIdUsuario());
                newRegistro.setIdHabito(selectedHabit.getIdHabito());
                newRegistro.setDataRegistro(LocalDate.now());
                newRegistro.setValidado(false);
                registroHabitoDAO.insert(newRegistro);
                registroToPass = newRegistro;
            }

            appService.setData(new Object[]{selectedHabit, registroToPass});
            appService.loadScene("pergunta_validacao.fxml");

        } catch (RuntimeException e) {
            Alertas.mostrarAlerta("Erro de Banco de Dados", null, "Não foi possível registrar ou acessar o hábito: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro Geral", null, "Ocorreu um erro inesperado ao processar o hábito: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }
}