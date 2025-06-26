package com.reverde.reverde;

import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class PerguntaValidacaoErroController implements Initializable {
    @FXML
    private AppService appService;

    public PerguntaValidacaoErroController() {
        this.appService = App.getInstance();
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
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