package com.reverde.reverde.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Restricoes {


    public static void aplicarRestricaoApenasDigitos(TextField textField, int maxLength) {
        Pattern pattern = (maxLength > 0) ? Pattern.compile("\\d{0," + maxLength + "}") : Pattern.compile("\\d*");
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        });
        textField.setTextFormatter(formatter);
    }


    public static void aplicarRestricaoNumerosDecimais(TextField textField, int decimalPlaces) {
        String regex = "\\d*(\\.\\d{0," + decimalPlaces + "})?";
        Pattern pattern = Pattern.compile(regex);

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        });
        textField.setTextFormatter(formatter);
    }

    public static void aplicarRestricaoApenasLetras(TextField textField) {
        Pattern pattern = Pattern.compile("[a-zA-Z\\s]*");
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        });
        textField.setTextFormatter(formatter);
    }


    public static void aplicarRestricoesPontos(TextField textField) {
        aplicarRestricaoApenasDigitos(textField, 5);
    }
}
