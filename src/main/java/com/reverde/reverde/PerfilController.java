package com.reverde.reverde;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

import com.reverde.reverde.util.App;
import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.UsuarioDAO;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.dao.impl.UsuarioDAOJDBC;
import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.model.entities.EcoPontos;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label ecopontosLabel;
    @FXML
    private Label userLocationLabel;
    @FXML
    private Button convertPointsButton;
    @FXML
    private Button registerHabitButton;
    @FXML
    private UsuarioDAO usuarioDAO;
    @FXML
    private EcoPontosDAO ecoPontosDAO;
    @FXML
    private Usuario loggedInUser;

    public PerfilController() {
        this.usuarioDAO = new UsuarioDAOJDBC(DB.getConnection());
        this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        this.loggedInUser = new Usuario();
        this.loggedInUser.setIdUsuario(1);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadUserProfile();
    }

    @FXML
    private void loadUserProfile() {
        try {
            Optional<Usuario> usuarioOpt = Optional.ofNullable(usuarioDAO.findById(loggedInUser.getIdUsuario()));
            if (usuarioOpt.isPresent()) {
                userNameLabel.setText(usuarioOpt.get().getNome());
            } else {
                userNameLabel.setText("Usuário Desconhecido");
            }

            Optional<EcoPontos> ecoPontosOpt = Optional.ofNullable(ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario()));
            if (ecoPontosOpt.isPresent()) {
                ecopontosLabel.setText(ecoPontosOpt.get().getTotalPontos() + " Ecopontos");
            } else {
                ecopontosLabel.setText("0 Ecopontos");
            }

            userLocationLabel.setText("Aracati, CE");

        } catch (RuntimeException e) {
            showAlert("Erro de Banco de Dados", "Não foi possível carregar o perfil: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erro Geral", "Ocorreu um erro ao carregar o perfil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        App.getInstance().goBack();
    }

    @FXML
    public void onConvertPointsClick(ActionEvent event) {
        App.getInstance().loadScene("converter_pontos.fxml");
    }

    @FXML
    public void onRegistrarHabitoClick(ActionEvent event) {
        App.getInstance().loadScene("registro_habito.fxml");
    }

    @FXML
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}