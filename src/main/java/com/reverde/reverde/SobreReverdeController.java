package com.reverde.reverde;

import com.reverde.reverde.util.App;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent; // Importar ActionEvent para o botão

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para a tela "Sobre o ReVerde", que exibe informações estáticas sobre o aplicativo.
 */
public class SobreReverdeController implements Initializable {

    private AppService appService;

    /**
     * Construtor padrão. Inicializa o AppService.
     */
    public SobreReverdeController() {
        this.appService = App.getInstance();
    }

    /**
     * Define o serviço da aplicação. Usado para injeção de dependência em testes.
     * @param appService A instância de AppService.
     */
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * @param url O local usado para resolver caminhos relativos para o objeto raiz, ou null se o local não for conhecido.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Garante que o AppService esteja inicializado, caso não tenha sido injetado
        if (this.appService == null) {
            this.appService = App.getInstance();
        }
    }

    /**
     * Lida com o clique no botão "Voltar" ou na imagem de voltar, navegando para a tela anterior.
     * @param event O evento de clique (pode ser MouseEvent ou ActionEvent).
     */
    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }

}
