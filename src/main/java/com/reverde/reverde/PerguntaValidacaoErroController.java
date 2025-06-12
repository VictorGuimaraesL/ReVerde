package com.reverde.reverde;

import com.reverde.reverde.util.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class PerguntaValidacaoErroController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    public void onBackToProfileClick(MouseEvent event) {
        App.getInstance().loadScene("perfil.fxml");
    }
}