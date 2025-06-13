package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.dao.UsuarioDAO;
import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;
import com.reverde.reverde.model.dao.impl.UsuarioDAOJDBC;
import com.reverde.reverde.model.entities.EcoPontos;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

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
    private AppService appService;

    public PerfilController() {
        if (DB.getConnection() != null) {
            this.usuarioDAO = new UsuarioDAOJDBC(DB.getConnection());
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void setEcoPontosDAO(EcoPontosDAO ecoPontosDAO) {
        this.ecoPontosDAO = ecoPontosDAO;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.usuarioDAO == null) {
            this.usuarioDAO = new UsuarioDAOJDBC(DB.getConnection());
        }
        if (this.ecoPontosDAO == null) {
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        }
        if (this.appService == null) {
            this.appService = App.getInstance();
        }

        loadProfileData();
    }

    private void loadProfileData() {
        try {
            Usuario loggedInUser = appService.getLoggedInUser();
            if (loggedInUser == null) {
                showAlert("Erro de Login", "Nenhum usuário logado.", Alert.AlertType.ERROR);
                userNameLabel.setText("Usuário Desconhecido");
                ecopontosLabel.setText("0 Ecopontos");
                userLocationLabel.setText("N/A");
                return;
            }

            Optional<Usuario> userOpt = Optional.ofNullable(usuarioDAO.findById(loggedInUser.getIdUsuario()));
            if (userOpt.isPresent()) {
                userNameLabel.setText(userOpt.get().getNome());
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
        appService.goBack();
    }

    @FXML
    public void onConvertPointsClick(ActionEvent event) {
        appService.loadScene("converter_pontos.fxml");
    }

    @FXML
    public void onRegistrarHabitoClick(ActionEvent event) {
        appService.loadScene("registro_habito.fxml");
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