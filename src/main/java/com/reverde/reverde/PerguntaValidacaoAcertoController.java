package com.reverde.reverde;

import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PerguntaValidacaoAcertoController implements Initializable {

    @FXML
    private Label pointsEarnedLabel;
    @FXML
    private Integer pointsGained;
    @FXML
    private AppService appService;

    public PerguntaValidacaoAcertoController() {
        this.appService = App.getInstance();
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

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
        if (this.appService == null) {
            this.appService = App.getInstance();
        }
    }

    @FXML
    public void onBackToProfileClick(ActionEvent event) {
        appService.loadScene("perfil.fxml");
    }
}
