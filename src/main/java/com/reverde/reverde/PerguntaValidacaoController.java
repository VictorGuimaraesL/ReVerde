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

    @FXML
    public void initData(Object data) {
        if (data instanceof Object[]) {
            Object[] dataArray = (Object[]) data;
            if (dataArray.length >= 2 && dataArray[0] instanceof Habito && dataArray[1] instanceof RegistroHabito) {
                this.selectedHabit = (Habito) dataArray[0];
                this.currentRegistroHabito = (RegistroHabito) dataArray[1];
            } else {
                Alertas.mostrarAlerta("Erro de Inicialização", null, "Dados de hábito e/ou registro inválidos. Esperado Object[]{Habito, RegistroHabito}.", Alert.AlertType.ERROR);
                appService.goBack();
            }
        } else {
            Alertas.mostrarAlerta("Erro de Inicialização", null, "Dados passados inválidos. Esperado Object[].", Alert.AlertType.ERROR);
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


        Object dataFromAppService = appService.getData();
        if (dataFromAppService instanceof Object[]) {
            initData(dataFromAppService);
        } else {
            Alertas.mostrarAlerta("Erro de Dados", null, "Dados de hábito e/ou registro não foram passados corretamente.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }

        if (selectedHabit == null || currentRegistroHabito == null) {
            Alertas.mostrarAlerta("Erro de Dados", null, "Hábito ou registro de hábito não foram passados corretamente após o carregamento.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }

        Usuario loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado para validar hábito.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }

        try {
            if (currentRegistroHabito.getIdUsuario() != loggedInUser.getIdUsuario() || currentRegistroHabito.isValidado()) {
                Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.ERROR), null, "Registro de hábito inválido ou já validado para hoje.", Alert.AlertType.ERROR);
                appService.goBack();
                return;
            }

            List<PerguntaValidacao> questions = perguntaValidacaoDAO.findByHabito(selectedHabit.getIdHabito());

            if (!questions.isEmpty()) {
                Collections.shuffle(questions);

                Object potentialQuestion = questions.get(0);
                if (potentialQuestion instanceof PerguntaValidacao) {
                    currentQuestion = (PerguntaValidacao) potentialQuestion;
                } else {
                    throw new ClassCastException("O elemento obtido da lista de perguntas não é do tipo PerguntaValidacao. Tipo encontrado: " + (potentialQuestion != null ? potentialQuestion.getClass().getName() : "null"));
                }

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
                Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.INFORMATION), null, "Não há perguntas de validação configuradas para este hábito.", Alert.AlertType.INFORMATION);
                appService.goBack();
            }

        } catch (ClassCastException e) {
            Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.ERROR), "Erro de Tipo de Dados", "Problema ao processar a pergunta: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            appService.goBack();
        } catch (RuntimeException e) {
            Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.ERROR), null, "Ocorreu um erro ao carregar a pergunta de validação: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            appService.goBack();
        } catch (Exception e) {
            Alertas.mostrarAlerta(String.valueOf(Alert.AlertType.ERROR), null, "Ocorreu um erro inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            appService.goBack();
        }
    }

    @FXML
    public void onOptionClick(MouseEvent event) {
        String selectedOption = (String) ((VBox) event.getSource()).getUserData();
        boolean isCorrect = selectedOption.equals(currentQuestion.getRespostaCorreta());

        Usuario loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado para registrar a tentativa.", Alert.AlertType.ERROR);
            return;
        }

        try {
            PerguntaTentativa tentativa = new PerguntaTentativa();
            tentativa.setIdUsuario(loggedInUser.getIdUsuario());
            tentativa.setIdPergunta(currentQuestion.getIdPergunta());
            tentativa.setRespostaUsuario(selectedOption);
            tentativa.setCorreta(isCorrect);
            tentativa.setDataTentativa(LocalDate.now());
            perguntaTentativaDAO.insert(tentativa);

            if (isCorrect) {
                if (currentRegistroHabito != null) {
                    registroHabitoDAO.updateValidado(currentRegistroHabito.getIdRegistro(), true);
                }

                EcoPontos userEcoPontos = ecoPontosDAO.findByUsuario(loggedInUser.getIdUsuario());
                if (userEcoPontos == null) {
                    userEcoPontos = new EcoPontos();
                    userEcoPontos.setIdUsuario(loggedInUser.getIdUsuario());
                    userEcoPontos.setTotalPontos(selectedHabit.getPontuacao());
                    userEcoPontos.setAtualizacao(LocalDate.now());
                    ecoPontosDAO.insert(userEcoPontos);
                } else {
                    ecoPontosDAO.adicionarPontos(loggedInUser.getIdUsuario(), selectedHabit.getPontuacao());
                }
                System.out.println("Ecopontos adicionados: " + selectedHabit.getPontuacao());

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
