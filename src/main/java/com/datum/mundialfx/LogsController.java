/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.datum.mundialfx;

import com.mundial.app.controller.LogController;
import com.mundial.app.model.LogModel;
import com.mundial.app.util.Sesion;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class LogsController implements Initializable {

    @FXML
    private Button btnAnterior;

    @FXML
    private Button btnSiguiente;

    @FXML
    private Label lblPagina;

    @FXML
    private TableView<LogModel> tablaLogs;

    @FXML
    private TableColumn<LogModel, String> colAccion;

    @FXML
    private TableColumn<LogModel, String> colFecha;

    @FXML
    private TableColumn<LogModel, Integer> colId;

    @FXML
    private TableColumn<LogModel, String> colIp;

    @FXML
    private TableColumn<LogModel, Integer> colUsuarioId;

    @FXML
    private TableColumn<LogModel, String> colUsername;

    //instancia del controller del mvc
    private final LogController controller = new LogController();

    //lista observable para la tabla
    private final ObservableList<LogModel> lista = FXCollections.observableArrayList();

    //para darle formato bonito a la fecha
    private final DateTimeFormatter formatoFecha
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //mapear columnas normales
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuarioId.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ip"));

        //formatear fecha porque localdatetime sale feo
        colFecha.setCellValueFactory(cell -> {

            LogModel log = cell.getValue();

            if (log.getFecha() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            return new javafx.beans.property.SimpleStringProperty(
                    log.getFecha().format(formatoFecha)
            );
        });

        //pintar accion segun el tipo
        //asi se mira mas bonito y rapido de identificar
        colAccion.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(String accion, boolean empty) {

                super.updateItem(accion, empty);

                if (empty || accion == null) {

                    setText(null);
                    setStyle("");

                } else {

                    setText(accion);

                    switch (accion) {

                        case "LOGIN" ->
                            setStyle("-fx-text-fill: #0F6E56; -fx-font-weight: bold;");

                        case "LOGOUT" ->
                            setStyle("-fx-text-fill: #185FA5; -fx-font-weight: bold;");

                        case "LOGIN_FALLIDO" ->
                            setStyle("-fx-text-fill: #A32D2D; -fx-font-weight: bold;");

                        default ->
                            setStyle("");
                    }
                }
            }
        });

        //centrar algunas columnas porque sino se mira raro
        centrarColumnas();

        //cargar datos al iniciar
        cargarTabla();
    }

    //carga los logs de la pagina actual
    private void cargarTabla() {

        lista.clear();

        lista.addAll(controller.listarActual());

        tablaLogs.setItems(lista);

        actualizarLabelPagina();

        //desactivar botones cuando ya no haya mas paginas
        btnAnterior.setDisable(controller.getPaginaActual() == 0);

        btnSiguiente.setDisable(
                controller.getPaginaActual() >= controller.totalPaginas() - 1
        );
    }

    //ir a la pagina siguiente
    @FXML
    private void siguientePagina() {

        lista.clear();

        lista.addAll(controller.siguientePagina());

        tablaLogs.setItems(lista);

        actualizarLabelPagina();

        btnAnterior.setDisable(controller.getPaginaActual() == 0);

        btnSiguiente.setDisable(
                controller.getPaginaActual() >= controller.totalPaginas() - 1
        );
    }

    //volver a la pagina anterior
    @FXML
    private void anteriorPagina() {

        lista.clear();

        lista.addAll(controller.anteriorPagina());

        tablaLogs.setItems(lista);

        actualizarLabelPagina();

        btnAnterior.setDisable(controller.getPaginaActual() == 0);

        btnSiguiente.setDisable(
                controller.getPaginaActual() >= controller.totalPaginas() - 1
        );
    }

    //actualiza el label abajo
    //ejemplo:
    //Pagina 1 de 4
    private void actualizarLabelPagina() {

        int actual = controller.getPaginaActual() + 1;

        int total = controller.totalPaginas();

        if (total == 0) {
            total = 1;
        }

        lblPagina.setText("Pagina " + actual + " de " + total);
    }

    //centrar texto de columnas
    //porque algunas se miraban chuecas xd
    private void centrarColumnas() {

        colId.setStyle("-fx-alignment: CENTER;");
        colUsuarioId.setStyle("-fx-alignment: CENTER;");
        colFecha.setStyle("-fx-alignment: CENTER;");
        colAccion.setStyle("-fx-alignment: CENTER;");
    }

    @FXML
    private void volverMenu() {

        try {

            //si es admin vuelve al menu admin
            if (Sesion.esAdmin()) {

                App.setRoot("MenuAdmin");

            } else {

                App.setRoot("MenuVendedor");
            }

        } catch (Exception e) {

            mostrarError("No se pudo volver al menu");

            e.printStackTrace();
        }
    }

    //alerta simple reutilizable
    private void mostrarError(String mensaje) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Logs");

        alert.setHeaderText(null);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}
