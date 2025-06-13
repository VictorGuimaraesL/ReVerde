package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.*;
import com.reverde.reverde.model.dao.impl.*;
import com.reverde.reverde.model.entities.*;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class PerguntaValidacaoController implements Initializable {

    @FXML
    private Label questionLabel;
    @FXML
    private Label option1Label;
    @FXML
    private Label option2Label;
    @FXML
    private VBox option1VBox;
    @FXML
    private VBox option2VBox;
    @FXML
    private PerguntaValidacaoDAO perguntaValidacaoDAO;
    @FXML
    private PerguntaTentativaDAO perguntaTentativaDAO;
    @FXML
    private RegistroHabitoDAO registroHabitoDAO;
    @FXML
    private EcoPontosDAO ecoPontosDAO;
    @FXML
    private AppService appService;
    @FXML
    private Usuario loggedInUser;
    @FXML
    private Habito selectedHabit;
    @FXML
    private PerguntaValidacao currentPergunta;
    @FXML
    private RegistroHabito currentRegistroHabito;

    public PerguntaValidacaoController() {
        if (DB.getConnection() != null) {
            this.perguntaValidacaoDAO = new PerguntaValidacaoDAOJDBC(DB.getConnection());
            this.perguntaTentativaDAO = new PerguntaTentativaDAOJDBC(DB.getConnection());
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setPerguntaValidacaoDAO(PerguntaValidacaoDAO dao) { this.perguntaValidacaoDAO = dao; }
    public void setPerguntaTentativaDAO(PerguntaTentativaDAO dao) { this.perguntaTentativaDAO = dao; }
    public void setRegistroHabitoDAO(RegistroHabitoDAO dao) { this.registroHabitoDAO = dao; }
    public void setEcoPontosDAO(EcoPontosDAO dao) { this.ecoPontosDAO = dao; }
    public void setAppService(AppService appService) { this.appService = appService; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.perguntaValidacaoDAO == null) {
            this.perguntaValidacaoDAO = new PerguntaValidacaoDAOJDBC(DB.getConnection());
            this.perguntaTentativaDAO = new PerguntaTentativaDAOJDBC(DB.getConnection());
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        }
        if (this.appService == null) {
            this.appService = App.getInstance();
        }

        loggedInUser = appService.getLoggedInUser();
        selectedHabit = (Habito) appService.getData();
        if (loggedInUser == null || selectedHabit == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Dados do usuário ou hábito não disponíveis.");
            appService.goBack();
            return;
        }

        try {
            Optional<RegistroHabito> pendingOpt = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario()).stream()
                    .filter(r -> r.getIdHabito() == selectedHabit.getIdHabito() &&
                            r.getDataRegistro().equals(LocalDate.now()) &&
                            !r.isValidado())
                    .findFirst();

            if (pendingOpt.isPresent()) {
                currentRegistroHabito = pendingOpt.get();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Hábito Já Validado", "Este hábito já foi validado para hoje ou não há um registro pendente.");
                appService.goBack();
                return;
            }

            currentPergunta = (PerguntaValidacao) perguntaValidacaoDAO.findByHabito(selectedHabit.getIdHabito());
            if (currentPergunta != null) {
                questionLabel.setText(currentPergunta.getPergunta());
                option1Label.setText("Opção 1: " + currentPergunta.getRespostaCorreta());
                option2Label.setText("Opção 2: Incorreta");
            } else {
                showAlert(Alert.AlertType.WARNING, "Aviso", "Nenhuma pergunta de validação encontrada para este hábito.");
                appService.goBack();
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar a pergunta: " + e.getMessage());
            appService.goBack();
        }
    }

    @FXML
    public void onOptionClick(MouseEvent event) {
        if (currentPergunta == null || currentRegistroHabito == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhuma pergunta ou registro de hábito disponível.");
            return;
        }

        String selectedOptionText = "";
        if (event.getSource() == option1VBox) {
            selectedOptionText = option1Label.getText().substring(option1Label.getText().indexOf(":") + 2); // Extrai apenas a resposta
        } else if (event.getSource() == option2VBox) {
            selectedOptionText = option2Label.getText().substring(option2Label.getText().indexOf(":") + 2);
        }

        boolean isCorrect = selectedOptionText.equals(currentPergunta.getRespostaCorreta());

        try {
            PerguntaTentativa tentativa = new PerguntaTentativa();
            tentativa.setIdUsuario(loggedInUser.getIdUsuario());
            tentativa.setIdPergunta(currentPergunta.getIdPergunta());
            tentativa.setRespostaUsuario(selectedOptionText);
            tentativa.setCorreta(isCorrect);
            tentativa.setDataTentativa(LocalDate.now());

            perguntaTentativaDAO.insert(tentativa);

            if (isCorrect) {
                registroHabitoDAO.updateValidado(currentRegistroHabito.getIdRegistro(), true);

                EcoPontos userEcoPontos = ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario());
                if (userEcoPontos != null) {
                    ecoPontosDAO.adicionarPontos(loggedInUser.getIdUsuario(), selectedHabit.getPontuacao());
                } else {
                    userEcoPontos = new EcoPontos();
                    userEcoPontos.setIdUsuario(loggedInUser.getIdUsuario());
                    userEcoPontos.setTotalPontos(selectedHabit.getPontuacao());
                    userEcoPontos.setAtualizacao(LocalDate.now());
                    ecoPontosDAO.insert(userEcoPontos);
                }
                System.out.println("Ecopontos adicionados: " + selectedHabit.getPontuacao());

                appService.loadScene("pergunta_validacao_acerto.fxml", selectedHabit.getPontuacao());
            } else {
                appService.loadScene("pergunta_validacao_erro.fxml");
            }

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Ocorreu um erro ao registrar a tentativa: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro inesperado ao processar a resposta: " + e.getMessage());
            e.printStackTrace();
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