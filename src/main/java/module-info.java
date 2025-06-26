module com.reverde.reverde {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens com.reverde.reverde to javafx.fxml, javafx.graphics;
    exports com.reverde.reverde.util to javafx.graphics, javafx.fxml;
    exports com.reverde.reverde;
}