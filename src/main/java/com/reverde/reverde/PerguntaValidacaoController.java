package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.PerguntaTentativaDAO;
import com.reverde.reverde.model.dao.PerguntaValidacaoDAO;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.dao.EcoPontosDAO;
import com.reverde.reverde.model.dao.impl.PerguntaTentativaDAOJDBC;
import com.reverde.reverde.model.dao.impl.PerguntaValidacaoDAOJDBC;
import com.reverde.reverde.model.dao.impl.RegistroHabitoDAOJDBC;
import com.reverde.reverde.model.dao.impl.EcoPontosDAOJDBC;
import com.reverde.reverde.model.entities.EcoPontos;
import com.reverde.reverde.model.entities.Habito;
import com.reverde.reverde.model.entities.PerguntaTentativa;
import com.reverde.reverde.model.entities.PerguntaValidacao;
import com.reverde.reverde.model.entities.RegistroHabito;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Random;

public class PerguntaValidacaoController implements Initializable {

    @FXML
    private Label habitNameLabel;
    @FXML
    private Label questionLabel;
    @FXML
    private VBox option1VBox;
    @FXML
    private Label option1Label;
    @FXML
    private VBox option2VBox;
    @FXML
    private Label option2Label;
    @FXML
    private Habito selectedHabit;
    @FXML
    private PerguntaValidacao currentQuestion;
    @FXML
    private PerguntaValidacaoDAO perguntaValidacaoDAO;
    @FXML
    private PerguntaTentativaDAO perguntaTentativaDAO;
    @FXML
    private RegistroHabitoDAO registroHabitoDAO;
    @FXML
    private EcoPontosDAO ecoPontosDAO;
    @FXML
    private Usuario loggedInUser;

    public PerguntaValidacaoController() {
        this.perguntaValidacaoDAO = new PerguntaValidacaoDAOJDBC(DB.getConnection());
        this.perguntaTentativaDAO = new PerguntaTentativaDAOJDBC(DB.getConnection());
        this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());

        this.loggedInUser = new Usuario();
        this.loggedInUser.setIdUsuario(1);
    }

    @FXML
    public void initData(Habito habito) {
        this.selectedHabit = habito;
        habitNameLabel.setText("Hábito: " + selectedHabit.getNome());
        loadQuestionForHabit(selectedHabit.getIdHabito());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void loadQuestionForHabit(int idHabito) {
        try {

            List<PerguntaValidacao> perguntas = perguntaValidacaoDAO.findByHabito(idHabito);

            if (perguntas.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Nenhuma Pergunta", "Não há perguntas de validação cadastradas para este hábito.");
                questionLabel.setText("Nenhuma pergunta disponível.");
                option1VBox.setVisible(false);
                option2VBox.setVisible(false);
                return;
            }


            currentQuestion = perguntas.get(0);
            questionLabel.setText(currentQuestion.getPergunta());


            String correctAnswer = currentQuestion.getRespostaCorreta();
            String wrongAnswer = generateWrongAnswer(correctAnswer);

            Random random = new Random();
            if (random.nextBoolean()) {
                option1Label.setText(correctAnswer);
                option1VBox.setUserData(true);
                option2Label.setText(wrongAnswer);
                option2VBox.setUserData(false);
            } else {
                option1Label.setText(wrongAnswer);
                option1VBox.setUserData(false);
                option2Label.setText(correctAnswer);
                option2VBox.setUserData(true);
            }

            option1VBox.setVisible(true);
            option2VBox.setVisible(true);

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar a pergunta: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro ao carregar a pergunta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private String generateWrongAnswer(String correctAnswer) {
        if (correctAnswer.contains("Sim")) {
            return "Não";
        } else if (correctAnswer.contains("Verdadeiro")) {
            return "Falso";
        } else if (correctAnswer.contains("5 minutos")) {
            return "30 minutos";
        } else if (correctAnswer.contains("Água")) {
            return "Refrigerante";
        }
        return "Resposta Incorreta"; // Padrão
    }

    @FXML
    public void onOptionClick(MouseEvent event) {
        VBox clickedOption = (VBox) event.getSource();
        boolean isCorrect = (boolean) clickedOption.getUserData();
        Label clickedLabel = (Label) clickedOption.getChildren().get(0); // Pega o Label dentro do VBox
        String respostaUsuario = clickedLabel.getText();

        PerguntaTentativa tentativa = new PerguntaTentativa();
        tentativa.setIdUsuario(loggedInUser.getIdUsuario());
        tentativa.setIdPergunta(currentQuestion.getIdPergunta());
        tentativa.setRespostaUsuario(respostaUsuario);
        tentativa.setCorreta(isCorrect);
        tentativa.setDataTentativa(LocalDate.now());

        try {
            perguntaTentativaDAO.insert(tentativa);

            if (isCorrect) {
                List<RegistroHabito> registros = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario());
                RegistroHabito registroParaValidar = registros.stream()
                        .filter(r -> r.getIdHabito() == selectedHabit.getIdHabito() && !r.isValidado())
                        .findFirst()
                        .orElse(null);

                if (registroParaValidar != null) {
                    registroHabitoDAO.updateValidado(registroParaValidar.getIdRegistro(), true);
                    System.out.println("Hábito validado: " + selectedHabit.getNome());

                    Optional<EcoPontos> ecoPontosOpt = Optional.ofNullable(ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario()));                    EcoPontos userEcoPontos;
                    if (ecoPontosOpt.isPresent()) {
                        userEcoPontos = ecoPontosOpt.get();
                        userEcoPontos.setTotalPontos(userEcoPontos.getTotalPontos() + selectedHabit.getPontuacao());
                        ecoPontosDAO.update(userEcoPontos);
                    } else {
                        userEcoPontos = new EcoPontos();
                        userEcoPontos.setIdUsuario(loggedInUser.getIdUsuario());
                        userEcoPontos.setTotalPontos(selectedHabit.getPontuacao());
                        userEcoPontos.setAtualizacao(LocalDate.now());
                        ecoPontosDAO.insert(userEcoPontos);
                    }
                    System.out.println("Ecopontos adicionados: " + selectedHabit.getPontuacao());

                    App.getInstance().loadScene("pergunta_validacao_acerto.fxml", selectedHabit.getPontuacao()); // Passa os pontos ganhos
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Hábito Já Validado", "Este hábito já foi validado para hoje ou não há um registro pendente.");
                    App.getInstance().goBack();
                }

            } else {
                App.getInstance().loadScene("pergunta_validacao_erro.fxml");
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