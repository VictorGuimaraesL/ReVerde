package com.reverde.reverde.util;

import com.reverde.reverde.PerguntaValidacaoAcertoController;
import com.reverde.reverde.PerguntaValidacaoController;
import com.reverde.reverde.model.entities.Habito;
import com.reverde.reverde.model.entities.Usuario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class App extends Application {

    private static App instance;
    private static Stage primaryStage;
    private static Stack<String> sceneHistory = new Stack<>();
    private static Usuario loggedInUser;

    public App() {
        instance = this;
        loggedInUser = new Usuario();
        loggedInUser.setIdUsuario(1);
        loggedInUser.setNome("Alane Damasceno");
    }

    public static App getInstance() {
        return instance;
    }

    // Método para obter o usuário logado
    public static Usuario getLoggedInUser() {
        return loggedInUser;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Reverde App");


        loadScene("perfil.fxml");
    }


    public void loadScene(String fxmlFileName) {
        loadScene(fxmlFileName, null);
    }

    public void loadScene(String fxmlFileName, Object data) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/" + fxmlFileName));
            Parent root = fxmlLoader.load();

            Object controller = fxmlLoader.getController();
            if (data != null) {
                if (controller instanceof PerguntaValidacaoController) {
                    ((PerguntaValidacaoController) controller).initData((Habito) data);
                } else if (controller instanceof PerguntaValidacaoAcertoController) {
                    ((PerguntaValidacaoAcertoController) controller).initData((Integer) data);
                }
            }

            Scene newScene = new Scene(root);

            if (sceneHistory.isEmpty() || !fxmlFileName.equals(sceneHistory.peek())) {
                sceneHistory.push(fxmlFileName);
            }

            primaryStage.setScene(newScene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Carregamento", "Não foi possível carregar a tela: " + fxmlFileName + "\nDetalhes: " + e.getMessage());
        } catch (ClassCastException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Dados", "Tipo de dado incorreto passado para o controlador. Verifique initData.\nDetalhes: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    public void goBack() {
        if (!sceneHistory.isEmpty()) {
            sceneHistory.pop();

            if (!sceneHistory.isEmpty()) {
                String previousFxmlFileName = sceneHistory.peek();
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/" + previousFxmlFileName));
                    Parent root = fxmlLoader.load();
                    Scene previousScene = new Scene(root);
                    primaryStage.setScene(previousScene);
                    primaryStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível voltar para a tela anterior: " + previousFxmlFileName + "\nDetalhes: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Navegação", "Não há mais telas no histórico para voltar.");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Navegação", "Não há telas no histórico para voltar.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}