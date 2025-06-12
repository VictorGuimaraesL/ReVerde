package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.HabitoDAO;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.dao.impl.HabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.RegistroHabitoDAOJDBC;
import com.reverde.reverde.model.entities.Habito;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
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
    private Usuario loggedInUser;

    public RegistroHabitoController() {
        this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
        this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection()); // Inicializa o registro DAO
        this.loggedInUser = new Usuario();
        this.loggedInUser.setIdUsuario(1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadHabits();
    }

    @FXML
    private void loadHabits() {
        habitsListContainer.getChildren().clear();

        try {
            List<Habito> habits = habitoDAO.findAll();

            if (habits.isEmpty()) {
                Label noHabitsLabel = new Label("Nenhum hábito cadastrado ainda.");
                noHabitsLabel.setStyle("-fx-text-fill: #808080;");
                habitsListContainer.getChildren().add(noHabitsLabel);
                habitsListContainer.setAlignment(Pos.CENTER);
                return;
            } else {
                habitsListContainer.setAlignment(Pos.TOP_LEFT);
            }

            for (Habito habito : habits) {

                HBox habitItem = new HBox(15);
                habitItem.setAlignment(Pos.CENTER_LEFT);
                habitItem.setPrefHeight(60.0);
                habitItem.setPrefWidth(300.0);
                habitItem.setStyle("-fx-background-color: #F8F8F8; -fx-background-radius: 5; -fx-border-color: #E0E0E0; -fx-border-radius: 5;");
                habitItem.setPadding(new Insets(10));
                habitItem.setCursor(javafx.scene.Cursor.HAND);

                habitItem.setOnMouseClicked(event -> onHabitClick(habito));

                Label nameLabel = new Label(habito.getNome());
                nameLabel.setFont(new Font("System Bold", 16.0));
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                Label pointsLabel = new Label("+" + habito.getPontuacao() + " pts");
                pointsLabel.setFont(new Font("System Bold", 14.0));
                pointsLabel.setStyle("-fx-text-fill: #00B16A;");

                habitItem.getChildren().addAll(nameLabel, pointsLabel);
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

        App.getInstance().loadScene("pergunta_validacao.fxml", selectedHabit);
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        App.getInstance().goBack();
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