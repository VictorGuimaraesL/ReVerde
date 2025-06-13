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
import java.util.ResourceBundle;

public class RegistroHabitoController implements Initializable {

    @FXML
    private VBox habitsListContainer;
    @FXML
    private HabitoDAO habitoDAO;
    @FXML
    private RegistroHabitoDAO registroHabitoDAO;
    @FXML
    private AppService appService;

    private Usuario loggedInUser;

    public RegistroHabitoController() {
        if (DB.getConnection() != null) {
            this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setHabitoDAO(HabitoDAO habitoDAO) {
        this.habitoDAO = habitoDAO;
    }

    public void setRegistroHabitoDAO(RegistroHabitoDAO registroHabitoDAO) {
        this.registroHabitoDAO = registroHabitoDAO;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.habitoDAO == null) {
            this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
        }
        if (this.registroHabitoDAO == null) {
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        }
        if (this.appService == null) {
            this.appService = App.getInstance();
        }

        loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para registrar hábitos.");
            appService.goBack();
            return;
        }

        loadHabits();
    }

    private void loadHabits() {
        habitsListContainer.getChildren().clear();
        try {
            List<Habito> habits = habitoDAO.findAll();
            List<RegistroHabito> allUserRegistros = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario());

            for (Habito habito : habits) {
                boolean canRegister = allUserRegistros.stream()
                        .noneMatch(r -> r.getIdHabito() == habito.getIdHabito() &&
                                r.getDataRegistro().equals(LocalDate.now()) &&
                                !r.isValidado());

                HBox habitItem = new HBox(10);
                habitItem.setAlignment(Pos.CENTER_LEFT);
                habitItem.setPadding(new Insets(10));
                habitItem.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 10; -fx-border-color: #C0C0C0; -fx-border-radius: 10;");

                Label nameLabel = new Label(habito.getNome());
                nameLabel.setFont(new Font("System Bold", 16.0));
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                Label pointsLabel = new Label("+" + habito.getPontuacao() + " pts");
                pointsLabel.setFont(new Font("System Bold", 14.0));
                pointsLabel.setStyle("-fx-text-fill: #00B16A;");

                if (!canRegister) {
                    Label statusLabel = new Label("Registrado Hoje");
                    statusLabel.setFont(new Font("System Italic", 12.0));
                    statusLabel.setStyle("-fx-text-fill: #808080;");
                    habitItem.getChildren().addAll(nameLabel, pointsLabel, statusLabel);
                } else {
                    habitItem.getChildren().addAll(nameLabel, pointsLabel);
                    habitItem.setOnMouseClicked(event -> onHabitClick(habito));
                }

                habitsListContainer.getChildren().add(habitItem);
            }

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar os hábitos: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro ao carregar os hábitos: " + e.getMessage());
        }
    }

    @FXML
    private void onHabitClick(Habito selectedHabit) {
        try {
            RegistroHabito newRegistro = new RegistroHabito();
            newRegistro.setIdUsuario(loggedInUser.getIdUsuario());
            newRegistro.setIdHabito(selectedHabit.getIdHabito());
            newRegistro.setDataRegistro(LocalDate.now());
            newRegistro.setValidado(false);

            registroHabitoDAO.insert(newRegistro);

            appService.loadScene("pergunta_validacao.fxml", selectedHabit);
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Registrar Hábito", "Não foi possível registrar o hábito para validação: " + e.getMessage());
        }
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }

    @FXML
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}