package com.datum.mundialfx;

import com.mundial.app.controller.PartidoController;
import com.mundial.app.controller.BoletosController;
import com.mundial.app.model.PartidoModel;
import com.mundial.app.model.TicketModel;
import com.mundial.app.util.Sesion;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PartidosController implements Initializable {

    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnNuevo;

    //combos de la hora y minutos
    @FXML
    private ComboBox<String> cmbHora;
    @FXML
    private ComboBox<String> cmbMinuto;

    //columnas de la tabla
    @FXML
    private TableColumn<PartidoModel, Void> colAcciones;
    @FXML
    private TableColumn<PartidoModel, String> colEstadio;
    @FXML
    private TableColumn<PartidoModel, String> colEstado;
    @FXML
    private TableColumn<PartidoModel, String> colFecha;
    @FXML
    private TableColumn<PartidoModel, Integer> colId;
    @FXML
    private TableColumn<PartidoModel, String> colPartido;

    @FXML
    private DatePicker dpFecha;
    @FXML
    private Label lblErrorForm;
    @FXML
    private Label lblTituloForm;
    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<PartidoModel> tablaPartidos;

    @FXML
    private TextField txtBuscar;
    @FXML
    private TextField txtCapacidad;
    @FXML
    private TextField txtCiudad;
    @FXML
    private TextField txtEstadio;
    @FXML
    private TextField txtLocal;
    @FXML
    private TextField txtVisitante;

    //instancias
    private final PartidoController controller = new PartidoController();
    //tambien necesitamos el controller de boletos para tronar los tickets cuando se cancela un partido
    private final BoletosController controllerBo = new BoletosController();
    private final ObservableList<PartidoModel> lista = FXCollections.observableArrayList();
    private int idEditar = -1;

    //para formatear la fecha en la tabla
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //llenar combos de hora 00-23 y minutos 00-59
        //se llena de 0 hasta 23 se auto incrementa un for nomrla
        for (int i = 0; i < 24; i++) {
            cmbHora.getItems().add(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i++) {
            cmbMinuto.getItems().add(String.format("%02d", i));
        }

        //mapear columnas--- decir que datos va a mostrar
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEstadio.setCellValueFactory(new PropertyValueFactory<>("estadio"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        //columna "Local vs Visitante" es combinada (luego voy a ver si la dejo asi)
        colPartido.setCellValueFactory(cell -> {
            PartidoModel p = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(p.getEquipoLocal() + " vs " + p.getEquipoVisitante());
        });

        //columna fecha tambien formateada
        colFecha.setCellValueFactory(cell -> {
            PartidoModel p = cell.getValue();
            if (p.getFecha() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            return new javafx.beans.property.SimpleStringProperty(p.getFecha().format(formatoFecha));
        });

        //pinta el estado con color segun corresponda
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                } else {
                    setText(estado);
                    switch (estado) {
                        case "DISPONIBLE" ->
                            setStyle("-fx-text-fill: #0F6E56; -fx-font-weight: bold;");
                        case "FINALIZADO" ->
                            setStyle("-fx-text-fill: #185FA5; -fx-font-weight: bold;");
                        case "CANCELADO" ->
                            setStyle("-fx-text-fill: #A32D2D; -fx-font-weight: bold;");
                    }
                }
            }
        });

        //arma columna de acciones segun el rol
        configurarSegunRol();

        //buscador en tiempo real
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarTabla(newVal));

        cargarTabla();
    }

    //decide que ve el admin y que ve el vendedor
    //admin: ve todos los partidos y puede editar/cancelar
    //vendedor: solo ve los DISPONIBLES y sin botones de accion
    private void configurarSegunRol() {
        boolean esAdmin = Sesion.esAdmin();

        //al vendedor le oculto las  columnas y el boton de nuevo, le voy a restringir eso(voy a ver luego si estado tambien)--- mmm mejor le voy a dejar solo estados disponibles
        if (!esAdmin) {
            btnNuevo.setVisible(false);
            btnNuevo.setManaged(false);
            colAcciones.setVisible(false);
            colEstado.setVisible(false);
        }

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox contenedor = new HBox(6, btnEditar, btnCancelar);

            {
                btnEditar.getStyleClass().add("boton-editar");
                btnCancelar.getStyleClass().add("boton-eliminar");
                btnEditar.setPrefWidth(80);
                btnCancelar.setPrefWidth(85);
                contenedor.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(e -> {
                    PartidoModel p = getTableView().getItems().get(getIndex());
                    editar(p);
                });

                btnCancelar.setOnAction(e -> {
                    PartidoModel p = getTableView().getItems().get(getIndex());
                    cancelarPartido(p);
                });
            }

            //lo cambiare debido a que necesito cambiar los estados
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    PartidoModel p = getTableView().getItems().get(getIndex());
                    contenedor.getChildren().clear();

                    switch (p.getEstado()) {
                        case "DISPONIBLE" -> {
                            //partido activo: se puede editar o cancelar
                            contenedor.getChildren().addAll(btnEditar, btnCancelar);
                        }
                        case "CANCELADO" -> {
                            //partido cancelado: se puede reabrir
                            Button btnReabrir = new Button("Reabrir");
                            btnReabrir.getStyleClass().add("boton-insertar");
                            btnReabrir.setPrefWidth(85);
                            btnReabrir.setOnAction(e -> reabrirPartido(p));
                            contenedor.getChildren().add(btnReabrir);
                        }
                        case "FINALIZADO" -> {
                            //finalizado no se toca
                        }
                    }

                    setGraphic(contenedor);
                }
            }
        });
    }

    //se crea funcion extra para reabrir
    private void reabrirPartido(PartidoModel partido) {
        
        //trarea el id y y lo pasara a disponible
        boolean conf = controller.cambiarEstado(partido.getId(), "DISPONIBLE");
        if (conf) {
            cargarTabla();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo reabrir el partido.");
        }
    }

    //funcion para cargar la tabla
    private void cargarTabla() {
        lista.clear();
        List<PartidoModel> datos = controller.listarPartidos();

        //si un partido sigue como DISPONIBLE pero la fecha ya paso
        //automaticamente lo pasamos a FINALIZADO para que no se vea raro
        //esto se hace antes de mostrar nada
        LocalDateTime ahora = LocalDateTime.now();
        for (PartidoModel p : datos) {
            if ("DISPONIBLE".equals(p.getEstado())
                    && p.getFecha() != null
                    && p.getFecha().isBefore(ahora)) {

                //actualizamos en la BD y el estado del modelo en memoria
                boolean ok = controller.cambiarEstado(p.getId(), "FINALIZADO");
                if (ok) {
                    p.setEstado("FINALIZADO");
                }
            }
        }

        //si es vendedor solo muestra los disponibles
        if (!Sesion.esAdmin()) {
            datos.removeIf(p -> !"DISPONIBLE".equals(p.getEstado()));
        }

        lista.addAll(datos);
        tablaPartidos.setItems(lista);
    }

    //filtra por equipo local o visitante
    private void filtrarTabla(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            tablaPartidos.setItems(lista);
            tablaPartidos.refresh();
            return;
        }

        String filtro = texto.toLowerCase().trim();
        ObservableList<PartidoModel> filtrados = FXCollections.observableArrayList();
        //reccorrer todos los datos de la tabla 
        for (PartidoModel partido : lista) {
            if (partido.getEquipoLocal().toLowerCase().contains(filtro) || partido.getEquipoVisitante().toLowerCase().contains(filtro)) {
                filtrados.add(partido);
            }
        }

        tablaPartidos.setItems(filtrados);
        tablaPartidos.refresh();
    }

    @FXML
    private void abrirFormulario() {
        idEditar = -1;
        lblTituloForm.setText("Nuevo Partido");
        limpiarFormulario();
        mostrarPanel(true);
    }

    private void editar(PartidoModel partido) {
        idEditar = partido.getId();
        lblTituloForm.setText("Editar Partido");
        txtLocal.setText(partido.getEquipoLocal());
        txtVisitante.setText(partido.getEquipoVisitante());

        //separar la fecha y la hora del LocalDateTime
        if (partido.getFecha() != null) {
            dpFecha.setValue(partido.getFecha().toLocalDate());
            cmbHora.setValue(String.format("%02d", partido.getFecha().getHour()));
            cmbMinuto.setValue(String.format("%02d", partido.getFecha().getMinute()));
        }

        txtEstadio.setText(partido.getEstadio());
        txtCiudad.setText(partido.getCiudad());
        txtCapacidad.setText(String.valueOf(partido.getCapacidad()));
        lblErrorForm.setText("");
        mostrarPanel(true);
    }

    //cuando admin cancela un partido, este cambia a estado CANCELADO
    //y aparte mandamos a tronar todos los tickets DISPONIBLES de ese partido
    //para que nadie pueda seguir vendiendolos
    //los tickets VENDIDOS los respetamos porque ya existen ventas registradas con ellos
    //el admin tendria que anular esas ventas aparte si quiere reembolsar
    private void cancelarPartido(PartidoModel partido) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Desea cancelar el partido " + partido.getEquipoLocal() + " vs " + partido.getEquipoVisitante() + "?"
                + "\n\nSe eliminaran todos los tickets disponibles. Los tickets ya vendidos no se tocan ");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                boolean ok = controller.cambiarEstado(partido.getId(), "CANCELADO");
                if (ok) {

                    //antes contabamos los vendidos recorriendo todos los tickets uno por uno
                    //y luego los eliminabamos uno por uno tambien -- con 20000 tickets era lentisimo
                    //ahora contamos los vendidos con un solo query y eliminamos en batch con otro
                    //pasamos de N+1 viajes a la BD a solo 2 viajes

                    int vendidosIntactos = 0;

                    //recorremos solo para contar los VENDIDOS (no se eliminan)
                    //esto se podria optimizar tambien con un COUNT en BD si se vuelve lento
                    for (TicketModel t : controllerBo.consultarPorPartido(partido.getId())) {
                        if ("VENDIDO".equals(t.getEstado())) {
                            vendidosIntactos++;
                        }
                    }

                    //elimina TODOS los DISPONIBLE y RESERVADO en una sola operacion
                    //el SP devuelve la cantidad eliminada
                    int eliminados = controllerBo.eliminarPorPartido(partido.getId());

                    //mensaje final con el resumen para el admin
                    String msg = "Partido cancelado. Tickets eliminados: " + eliminados + ".";
                    if (vendidosIntactos > 0) {
                        msg += "\nHay " + vendidosIntactos + " ticket(s) ya vendido(s), anule esas ventas manualmente si va a reembolsar ";
                    }
                    mostrarAlerta(Alert.AlertType.INFORMATION, msg);

                    cargarTabla();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo cancelar el partido.");
                }
            }
        });
    }

    @FXML
    private void cerrarFormulario() {
        mostrarPanel(false);
        limpiarFormulario();
    }

    @FXML
    private void guardar() {
        lblErrorForm.setText("");

        String local = txtLocal.getText().trim();
        String visitante = txtVisitante.getText().trim();
        String estadio = txtEstadio.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        String capacidadStr = txtCapacidad.getText().trim();

        //validaciones
        if (local.isEmpty()) {
            lblErrorForm.setText("El equipo local es obligatorio ");
            txtLocal.requestFocus();
            return;
        }

        //
        if (visitante.isEmpty()) {
            lblErrorForm.setText("El equipo visitante es obligatorio ");
            txtVisitante.requestFocus();
            return;
        }
        if (local.equalsIgnoreCase(visitante)) {
            lblErrorForm.setText("El equipo local y visitante no pueden ser el mismo");
            return;
        }
        if (dpFecha.getValue() == null) {
            lblErrorForm.setText("Seleccione la fecha del partido.");
            return;
        }
        if (cmbHora.getValue() == null || cmbMinuto.getValue() == null) {
            lblErrorForm.setText("Seleccione la hora del partido.");
            return;
        }
        if (estadio.isEmpty()) {
            lblErrorForm.setText("El estadio es obligatorio.");
            txtEstadio.requestFocus();
            return;
        }

        //capacidad debe ser un numero valido
        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadStr);
            if (capacidad <= 0) {
                lblErrorForm.setText("La capacidad debe ser mayor a 0 ");
                return;
            }
        } catch (NumberFormatException e) {
            lblErrorForm.setText("La capacidad debe ser un numero ");
            txtCapacidad.requestFocus();
            return;
        }

        //armamos el LocalDateTime juntando fecha + hora + minutos
        //recordar que use 2 combo box para almacnar la hora
        int hora = Integer.parseInt(cmbHora.getValue());
        int minuto = Integer.parseInt(cmbMinuto.getValue());
        LocalDateTime fechaCompleta = dpFecha.getValue().atTime(LocalTime.of(hora, minuto));

        try {
            if (idEditar == -1) {
                //si la fecha que pone el admin ya paso lo metemos directo como FINALIZADO
                //sino arranca como DISPONIBLE normal
                String estadoNuevo = fechaCompleta.isBefore(LocalDateTime.now())
                        ? "FINALIZADO" : "DISPONIBLE";

                //insertar con el estado que toque
                boolean confirm = controller.insertarPartido(local, visitante, fechaCompleta, estadio, ciudad, capacidad, estadoNuevo);
                if (confirm) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Partido registrado correctamente.");
                    cerrarFormulario();
                    cargarTabla();
                } else {
                    lblErrorForm.setText("No se pudo registrar. Verifique los datos.");
                }
            } else {
                //al editar mantenemos el estado actual del partido
                PartidoModel actual = controller.buscarPorId(idEditar);
                String estadoActual = (actual != null) ? actual.getEstado() : "DISPONIBLE";

                //si la fecha nueva ya paso y el partido estaba DISPONIBLE
                //lo pasamos a FINALIZADO directamente, no tiene sentido dejarlo abierto
                if ("DISPONIBLE".equals(estadoActual)
                        && fechaCompleta.isBefore(LocalDateTime.now())) {
                    estadoActual = "FINALIZADO";
                }

                //true si se gizo
                boolean confirm = controller.actualizarPartido(idEditar, local, visitante, fechaCompleta, estadio, ciudad, capacidad, estadoActual);
                if (confirm) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Partido actualizado correctamente ");
                    cerrarFormulario();
                    cargarTabla();
                } else {
                    lblErrorForm.setText("No se pudo actualizar.");
                }
            }
        } catch (Exception e) {
            lblErrorForm.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void volverMenu() {
        try {
            if (Sesion.esAdmin()) {
                App.setRoot("MenuAdmin");
            } else {
                App.setRoot("MenuVendedor");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //mostrar/ocultar el panel del formulario
    private void mostrarPanel(boolean mostrar) {
        panelFormulario.setVisible(mostrar);
        panelFormulario.setManaged(mostrar);
    }

    //dejar el formulario por defecto
    private void limpiarFormulario() {
        txtLocal.clear();
        txtVisitante.clear();
        dpFecha.setValue(null);
        cmbHora.setValue(null);
        cmbMinuto.setValue(null);
        txtEstadio.clear();
        txtCiudad.clear();
        txtCapacidad.clear();
        lblErrorForm.setText("");
        idEditar = -1;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Partidos");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
