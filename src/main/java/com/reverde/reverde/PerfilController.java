package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.dao.HabitoDAO;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.dao.UsuarioDAO;
import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;
import com.reverde.reverde.model.dao.impl.HabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.RegistroHabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.UsuarioDAOJDBC;
import com.reverde.reverde.model.entities.EcoPontos;
import com.reverde.reverde.model.entities.Habito;
import com.reverde.reverde.model.entities.RegistroHabito;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.Alertas;
import com.reverde.reverde.util.AppService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


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
    private VBox historyContainer;

    private UsuarioDAO usuarioDAO;
    private EcoPontosDAO ecoPontosDAO;
    private RegistroHabitoDAO registroHabitoDAO;
    private HabitoDAO habitoDAO;
    private AppService appService;

    private Usuario loggedInUser;

    public PerfilController() {
        if (DB.getConnection() != null) {
            this.usuarioDAO = new UsuarioDAOJDBC(DB.getConnection());
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
            this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setUsuarioDAO(UsuarioDAO usuarioDAO) { this.usuarioDAO = usuarioDAO; }
    public void setEcoPontosDAO(EcoPontosDAO ecoPontosDAO) { this.ecoPontosDAO = ecoPontosDAO; }
    public void setRegistroHabitoDAO(RegistroHabitoDAO registroHabitoDAO) { this.registroHabitoDAO = registroHabitoDAO; }
    public void setHabitoDAO(HabitoDAO habitoDAO) { this.habitoDAO = habitoDAO; }
    public void setAppService(AppService appService) { this.appService = appService; }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.usuarioDAO == null) this.usuarioDAO = new UsuarioDAOJDBC(DB.getConnection());
        if (this.ecoPontosDAO == null) this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        if (this.registroHabitoDAO == null) this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        if (this.habitoDAO == null) this.habitoDAO = new HabitoDAOJDBC(DB.getConnection());
        if (this.appService == null) this.appService = App.getInstance();

        loadProfileData();
        loadHabitsHistory();
    }

    private void loadProfileData() {
        try {
            loggedInUser = appService.getLoggedInUser();
            if (loggedInUser == null) {
                Alertas.mostrarAlerta("Erro de Login", null, "Nenhum usuário logado.", Alert.AlertType.ERROR);
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
            Alertas.mostrarAlerta("Erro de Banco de Dados", null, "Não foi possível carregar o perfil: " + e.getMessage(), Alert.AlertType.ERROR);
            userNameLabel.setText("Erro ao carregar");
            ecopontosLabel.setText("Erro ao carregar");
            userLocationLabel.setText("Erro ao carregar");
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro Geral", null, "Ocorreu um erro ao carregar o perfil: " + e.getMessage(), Alert.AlertType.ERROR);
            userNameLabel.setText("Erro ao carregar");
            ecopontosLabel.setText("Erro ao carregar");
            userLocationLabel.setText("Erro ao carregar");
            e.printStackTrace();
        }
    }

    private void loadHabitsHistory() {
        historyContainer.getChildren().clear();

        if (loggedInUser == null) {
            Label noHistoryLabel = new Label("Nenhum usuário logado para exibir histórico.");
            noHistoryLabel.setFont(new Font("System Italic", 12.0));
            noHistoryLabel.setStyle("-fx-text-fill: #808080;");
            historyContainer.getChildren().add(noHistoryLabel);
            return;
        }

        try {
            List<RegistroHabito> allRegistros = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario());

            List<RegistroHabito> validatedRegistros = allRegistros.stream()
                    .filter(RegistroHabito::isValidado)
                    .collect(Collectors.toList());

            if (validatedRegistros.isEmpty()) {
                Label noHistoryLabel = new Label("Nenhum hábito validado ainda.");
                noHistoryLabel.setFont(new Font("System Italic", 12.0));
                noHistoryLabel.setStyle("-fx-text-fill: #808080;");
                historyContainer.getChildren().add(noHistoryLabel);
                return;
            }

            validatedRegistros.forEach(registro -> {
                try {
                    Habito habito = habitoDAO.findById(registro.getIdHabito());
                    if (habito != null) {
                        HBox historyItem = new HBox(10);
                        historyItem.setAlignment(Pos.CENTER_LEFT);
                        historyItem.setPadding(new Insets(8, 15, 8, 15));
                        historyItem.setStyle("-fx-background-color: #F8F8F8; -fx-background-radius: 5; -fx-border-color: #E0E0E0; -fx-border-radius: 5;");
                        HBox.setHgrow(historyItem, Priority.ALWAYS);

                        Label habitName = new Label(habito.getNome());
                        habitName.setFont(new Font("System Bold", 14.0));
                        HBox.setHgrow(habitName, Priority.ALWAYS);

                        Label pointsEarned = new Label("+" + habito.getPontuacao() + " pts");
                        pointsEarned.setFont(new Font("System Bold", 12.0));
                        pointsEarned.setStyle("-fx-text-fill: #00B16A;");

                        Label dateLabel = new Label(registro.getDataRegistro().toString());
                        dateLabel.setFont(new Font("System Italic", 10.0));
                        dateLabel.setStyle("-fx-text-fill: #606060;");
                        HBox.setMargin(dateLabel, new Insets(0, 0, 0, 10));

                        historyItem.getChildren().addAll(habitName, pointsEarned, dateLabel);
                        historyContainer.getChildren().add(historyItem);
                        VBox.setMargin(historyItem, new Insets(0, 0, 5, 0));
                    }
                } catch (RuntimeException e) {
                    Alertas.mostrarAlerta("Erro ao Carregar Hábito", null, "Não foi possível carregar detalhes de um hábito validado no histórico: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            });

        } catch (RuntimeException e) {
            Alertas.mostrarAlerta("Erro de Banco de Dados", null, "Não foi possível carregar o histórico de hábitos: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro Geral", null, "Ocorreu um erro inesperado ao carregar o histórico: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void onRegistrarHabitoClick(ActionEvent event) {
        appService.loadScene("registro_habito.fxml");
    }

    @FXML
    public void onConvertPointsClick(ActionEvent event) {
        appService.loadScene("converter_pontos.fxml");
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }
}