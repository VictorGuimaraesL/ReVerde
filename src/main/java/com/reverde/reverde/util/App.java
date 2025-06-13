package com.reverde.reverde.util;

import com.reverde.reverde.PerguntaValidacaoAcertoController;
import com.reverde.reverde.model.entities.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class App extends Application implements AppService {

    private static App instance;
    private static Stage primaryStage;
    private static Stack<String> sceneHistory = new Stack<>();
    private static Usuario loggedInUser;
    private static Object data;

    public App() {
        if (instance == null) {
            instance = this;
            loggedInUser = new Usuario();
            loggedInUser.setIdUsuario(1);
            loggedInUser.setNome("Alane Damasceno");
        }
    }

    @Override
    public Usuario getLoggedInUser() {
        return loggedInUser;
    }

    @Override
    public void setLoggedInUser(Usuario user) {
        loggedInUser = user;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        App.data = data;
    }

    @Override
    public void loadScene(String fxmlPath) {
        loadScene(fxmlPath, null);
    }

    @Override
    public void loadScene(String fxmlPath, Object data) {
        try {
            sceneHistory.push(fxmlPath);
            // Caminho corrigido para a estrutura do seu projeto: src/main/resources/com/reverde/reverde/
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/reverde/reverde/" + fxmlPath));

            Parent root = fxmlLoader.load();
            Scene newScene = new Scene(root);

            Object controller = fxmlLoader.getController();
            if (controller instanceof PerguntaValidacaoAcertoController) {
                ((PerguntaValidacaoAcertoController) controller).initData(data);
            }

            primaryStage.setScene(newScene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a cena: " + fxmlPath + "\nDetalhes: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro Geral", "Ocorreu um erro inesperado ao carregar a cena: " + fxmlPath + "\nDetalhes: " + e.getMessage());
        }
    }

    @Override
    public void goBack() {
        if (!sceneHistory.isEmpty()) {
            sceneHistory.pop();

            if (!sceneHistory.isEmpty()) {
                String previousFxmlFileName = sceneHistory.peek();
                try {
                    // Caminho corrigido para a estrutura do seu projeto: src/main/resources/com/reverde/reverde/
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

    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Reverde App");

        loadScene("perfil.fxml");
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