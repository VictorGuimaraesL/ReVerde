
package com.reverde.reverde;

import com.reverde.reverde.util.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private static Scene scene;

    public Application() {
    }

    public void start(Stage stage) throws IOException {
        App appInstance = new App();
        appInstance.start(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("perfil.fxml"));
        scene = new Scene((Parent)fxmlLoader.load());
        stage.setTitle("ReVerde");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static Scene getScene() {
        return scene;
    }

    public static Stage newStage(String url) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource(url));
        Scene scene = new Scene((Parent)fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        return stage;
    }

    public static void main(String[] args) {
        launch(new String[0]);
    }
}
