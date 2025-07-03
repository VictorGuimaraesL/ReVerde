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
import com.reverde.reverde.util.Alertas;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional; // Importe Optional

public class PerguntaValidacaoController implements Initializable {

    @FXML
    private Label questionLabel;
    @FXML
    private Label option1Label;
    @FXML
    private Label option2Label;
    @FXML
    private Label habitNameLabel;
    @FXML
    private VBox option1VBox;
    @FXML
    private VBox option2VBox;

    private PerguntaTentativaDAO perguntaTentativaDAO;
    private PerguntaValidacaoDAO perguntaValidacaoDAO;
    private RegistroHabitoDAO registroHabitoDAO;
    private EcoPontosDAO ecoPontosDAO;
    private AppService appService;

    private Habito selectedHabit;
    private PerguntaValidacao currentQuestion;
    private RegistroHabito currentRegistroHabito;
    private Usuario loggedInUser;

    public PerguntaValidacaoController() {
        if (DB.getConnection() != null) {
            this.perguntaTentativaDAO = new PerguntaTentativaDAOJDBC(DB.getConnection());
            this.perguntaValidacaoDAO = new PerguntaValidacaoDAOJDBC(DB.getConnection());
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
            this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        }
        this.appService = App.getInstance();
    }

    public void setPerguntaTentativaDAO(PerguntaTentativaDAO perguntaTentativaDAO) { this.perguntaTentativaDAO = perguntaTentativaDAO; }
    public void setPerguntaValidacaoDAO(PerguntaValidacaoDAO perguntaValidacaoDAO) { this.perguntaValidacaoDAO = perguntaValidacaoDAO; }
    public void setRegistroHabitoDAO(RegistroHabitoDAO registroHabitoDAO) { this.registroHabitoDAO = registroHabitoDAO; }
    public void setEcoPontosDAO(EcoPontosDAO ecoPontosDAO) { this.ecoPontosDAO = ecoPontosDAO; }
    public void setAppService(AppService appService) { this.appService = appService; }

    public void initData(Object data) {
        if (data instanceof Habito) {
            this.selectedHabit = (Habito) data;
            this.currentRegistroHabito = null;
        } else {
            Alertas.mostrarAlerta("Erro de Inicialização", null, "Dados passados inválidos.", Alert.AlertType.ERROR);
            appService.goBack();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.perguntaTentativaDAO == null) this.perguntaTentativaDAO = new PerguntaTentativaDAOJDBC(DB.getConnection());
        if (this.perguntaValidacaoDAO == null) this.perguntaValidacaoDAO = new PerguntaValidacaoDAOJDBC(DB.getConnection());
        if (this.registroHabitoDAO == null) this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        if (this.ecoPontosDAO == null) this.ecoPontosDAO = new EcoPontosDAOJDBC(DB.getConnection());
        if (this.appService == null) this.appService = App.getInstance();

        loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }

        Object dataFromAppService = appService.getData();
        initData(dataFromAppService);

        if (selectedHabit == null) {
            Alertas.mostrarAlerta("Erro de Dados", null, "Hábito não foi passado corretamente.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }


        if (habitNameLabel == null || questionLabel == null || option1Label == null || option2Label == null) {
            Alertas.mostrarAlerta("Erro de Interface", null, "Componentes da tela de validação não foram carregados. Verifique o arquivo FXML.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }

        loadQuestionAndOptions();
    }

    private void loadQuestionAndOptions() {
        try {

            List<PerguntaValidacao> questions = perguntaValidacaoDAO.findByHabito(selectedHabit.getIdHabito());

            if (!questions.isEmpty()) {
                Collections.shuffle(questions);
                currentQuestion = questions.get(0);

                habitNameLabel.setText(selectedHabit.getNome());
                questionLabel.setText(currentQuestion.getPergunta());

                List<String> options = new ArrayList<>();
                options.add(currentQuestion.getRespostaCorreta());
                options.add(currentQuestion.getRespostaIncorreta());
                Collections.shuffle(options);

                option1Label.setText(options.get(0));
                option2Label.setText(options.get(1));

                option1VBox.setUserData(options.get(0));
                option2VBox.setUserData(options.get(1));

            } else {
                Alertas.mostrarAlerta("Informação", null, "Não há perguntas de validação para este hábito.", Alert.AlertType.INFORMATION);
                appService.goBack();
            }

        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro", null, "Ocorreu um erro ao carregar a pergunta: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            appService.goBack();
        }
    }


    @FXML
    public void onOptionClick(MouseEvent event) {
        String selectedOption = (String) ((VBox) event.getSource()).getUserData();
        boolean isCorrect = selectedOption.equals(currentQuestion.getRespostaCorreta());

        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado para registrar a tentativa.", Alert.AlertType.ERROR);
            return;
        }
        if (selectedHabit == null) {
            Alertas.mostrarAlerta("Erro", null, "Hábito não selecionado.", Alert.AlertType.ERROR);
            return;
        }
        if (currentQuestion == null) {
            Alertas.mostrarAlerta("Erro", null, "Pergunta de validação não carregada.", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (currentRegistroHabito == null) {
                currentRegistroHabito = new RegistroHabito();
                currentRegistroHabito.setIdUsuario(loggedInUser.getIdUsuario());
                currentRegistroHabito.setIdHabito(selectedHabit.getIdHabito());
                currentRegistroHabito.setDataRegistro(LocalDate.now());
                currentRegistroHabito.setValidado(false);

                registroHabitoDAO.insert(currentRegistroHabito);
            }

            PerguntaTentativa tentativa = new PerguntaTentativa();
            tentativa.setIdUsuario(loggedInUser.getIdUsuario());
            tentativa.setIdTentativa(currentRegistroHabito.getIdRegistro());
            tentativa.setIdPergunta(currentQuestion.getIdPergunta());
            tentativa.setRespostaUsuario(selectedOption);
            tentativa.setCorreta(isCorrect);
            tentativa.setDataTentativa(LocalDate.now());
            perguntaTentativaDAO.insert(tentativa);

            registroHabitoDAO.updateValidado(currentRegistroHabito.getIdRegistro(), isCorrect);

            if (isCorrect) {
                ecoPontosDAO.adicionarPontos(loggedInUser.getIdUsuario(), selectedHabit.getPontuacao());

                appService.loadScene("pergunta_validacao_acerto.fxml", selectedHabit.getPontuacao());
            } else {
                appService.loadScene("pergunta_validacao_erro.fxml");
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.ERROR), null, "Ocorreu um erro ao registrar a tentativa: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.ERROR), null, "Ocorreu um erro inesperado ao processar a resposta: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }
}