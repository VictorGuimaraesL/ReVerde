package com.reverde.reverde.util;

import com.reverde.reverde.model.entities.Usuario;
import javafx.stage.Stage;

public interface AppService {
    Usuario getLoggedInUser();
    void setLoggedInUser(Usuario user);
    Object getData();
    void setData(Object data);
    void loadScene(String fxmlPath);
    void loadScene(String fxmlPath, Object data);
    void goBack();
    Stage getPrimaryStage();
}
