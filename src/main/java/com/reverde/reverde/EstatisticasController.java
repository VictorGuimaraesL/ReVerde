package com.reverde.reverde;

import com.reverde.reverde.db.DB;
import com.reverde.reverde.model.dao.RegistroHabitoDAO;
import com.reverde.reverde.model.dao.impl.RegistroHabitoDAOJDBC;
import com.reverde.reverde.model.entities.RegistroHabito;
import com.reverde.reverde.model.entities.Usuario;
import com.reverde.reverde.util.App;
import com.reverde.reverde.util.Alertas;
import com.reverde.reverde.util.AppService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class EstatisticasController implements Initializable {

    @FXML
    private LineChart<String, Number> weeklyChart;
    @FXML
    private CategoryAxis weeklyXAxis;
    @FXML
    private NumberAxis weeklyYAxis;

    @FXML
    private LineChart<String, Number> monthlyChart;
    @FXML
    private CategoryAxis monthlyXAxis;
    @FXML
    private NumberAxis monthlyYAxis;

    private AppService appService;
    private RegistroHabitoDAO registroHabitoDAO;
    private Usuario loggedInUser;

    public EstatisticasController() {
        this.appService = App.getInstance();
        if (DB.getConnection() != null) {
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        }
    }


    public void setAppService(AppService appService) {
        this.appService = appService;
    }


    public void setRegistroHabitoDAO(RegistroHabitoDAO registroHabitoDAO) {
        this.registroHabitoDAO = registroHabitoDAO;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.registroHabitoDAO == null) {
            this.registroHabitoDAO = new RegistroHabitoDAOJDBC(DB.getConnection());
        }
        if (this.appService == null) {
            this.appService = App.getInstance();
        }

        loggedInUser = appService.getLoggedInUser();
        if (loggedInUser == null) {
            Alertas.mostrarAlerta("Erro", null, "Nenhum usuário logado para carregar estatísticas.", Alert.AlertType.ERROR);
            appService.goBack();
            return;
        }

        loadWeeklyStats();
        loadMonthlyStats();
    }

    private void loadWeeklyStats() {
        try {
            List<RegistroHabito> userRegistros = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario());

            Map<LocalDate, Long> dailyHabitCounts = userRegistros.stream()
                    .filter(RegistroHabito::isValidado)
                    .collect(Collectors.groupingBy(
                            RegistroHabito::getDataRegistro,
                            Collectors.counting()
                    ));

            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            Map<String, Number> weeklyData = new LinkedHashMap<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

            for (int i = 0; i < 7; i++) {
                LocalDate date = startOfWeek.plusDays(i);
                String formattedDate = date.format(dateFormatter);
                weeklyData.put(formattedDate, 0);
            }

            for (Map.Entry<LocalDate, Long> entry : dailyHabitCounts.entrySet()) {
                LocalDate date = entry.getKey();
                if (!date.isBefore(startOfWeek) && !date.isAfter(startOfWeek.plusDays(6))) {
                    String formattedDate = date.format(dateFormatter);
                    weeklyData.put(formattedDate, entry.getValue().intValue());
                }
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Hábitos Concluídos");

            for (Map.Entry<String, Number> entry : weeklyData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            weeklyChart.getData().clear();
            weeklyChart.getData().add(series);
            weeklyChart.setTitle("Hábitos Concluídos na Semana Atual");

        } catch (RuntimeException e) {
            Alertas.mostrarAlerta("Erro de Banco de Dados", null, "Não foi possível carregar estatísticas semanais: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro Geral", null, "Ocorreu um erro ao carregar estatísticas semanais: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadMonthlyStats() {
        try {
            List<RegistroHabito> userRegistros = registroHabitoDAO.findByUsuario(loggedInUser.getIdUsuario());

            Map<Month, Long> monthlyHabitCounts = userRegistros.stream()
                    .filter(RegistroHabito::isValidado)
                    .filter(rh -> rh.getDataRegistro().getYear() == LocalDate.now().getYear())
                    .collect(Collectors.groupingBy(
                            rh -> rh.getDataRegistro().getMonth(),
                            Collectors.counting()
                    ));

            Map<String, Number> monthlyData = new LinkedHashMap<>();
            Locale ptBrLocale = new Locale("pt", "BR");

            for (int i = 1; i <= 12; i++) {
                Month month = Month.of(i);
                monthlyData.put(month.getDisplayName(TextStyle.SHORT, ptBrLocale), 0);
            }

            for (Map.Entry<Month, Long> entry : monthlyHabitCounts.entrySet()) {
                monthlyData.put(entry.getKey().getDisplayName(TextStyle.SHORT, ptBrLocale), entry.getValue().intValue());
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Hábitos Concluídos");

            for (Map.Entry<String, Number> entry : monthlyData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            monthlyChart.getData().clear();
            monthlyChart.getData().add(series);
            monthlyChart.setTitle("Hábitos Concluídos por Mês (Ano Atual)");

        } catch (RuntimeException e) {
            Alertas.mostrarAlerta("Erro de Banco de Dados", null, "Não foi possível carregar estatísticas mensais: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Alertas.mostrarAlerta("Erro Geral", null, "Ocorreu um erro ao carregar estatísticas mensais: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonClick(MouseEvent event) {
        appService.goBack();
    }
}
