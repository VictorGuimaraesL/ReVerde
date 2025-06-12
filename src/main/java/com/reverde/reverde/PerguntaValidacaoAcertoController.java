package com.reverde.reverde;

import com.reverde.reverde.util.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class PerguntaValidacaoAcertoController implements Initializable {

    @FXML
    private Label pointsEarnedLabel;
    @FXML
    private Integer pointsGained;

    @FXML
    public void initData(Object data) {
        if (data instanceof Integer) {
            this.pointsGained = (Integer) data;
            pointsEarnedLabel.setText("Você ganhou +" + pointsGained + " Ecopontos!");
        } else {
            pointsEarnedLabel.setText("Você ganhou Ecopontos!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void onBackToProfileClick(MouseEvent event) {
        App.getInstance().loadScene("perfil.fxml");
    }
}