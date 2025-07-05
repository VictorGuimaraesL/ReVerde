package com.reverde.reverde;

import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent; 

import java.net.URL;
import java.util.ResourceBundle;


public class SobreReverdeController implements Initializable {

    private AppService appService;

    
    public SobreReverdeController() {
        this.appService = App.getInstance();
    }

    
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Garante que o AppService esteja inicializado, caso não tenha sido injetado
        if (this.appService == null) {
            this.appService = App.getInstance();
        }
    }

    
    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }

}
