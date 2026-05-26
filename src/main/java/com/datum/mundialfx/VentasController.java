package com.datum.mundialfx;

import com.mundial.app.controller.PartidoController;
import com.mundial.app.controller.BoletosController;
import com.mundial.app.controller.VentaController;
import com.mundial.app.model.ClienteModel;
import com.mundial.app.model.PartidoModel;
import com.mundial.app.model.ReciboModel;
import com.mundial.app.model.TicketModel;
import com.mundial.app.model.VentasModel;
import com.mundial.app.util.ClienteSeleccionado;
import com.mundial.app.util.Sesion;
import com.mundial.app.util.TicketsUtil;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VentasController implements Initializable {

    //historial ventas
    @FXML
    private TableView<VentasModel> tablaVentas;

    @FXML
    private TableColumn<VentasModel, Integer> colId;

    @FXML
    private TableColumn<VentasModel, String> colFactura;

    @FXML
    private TableColumn<VentasModel, String> colFecha;

    @FXML
    private TableColumn<VentasModel, String> colCliente;

    @FXML
    private TableColumn<VentasModel, String> colVendedor;

    @FXML
    private TableColumn<VentasModel, BigDecimal> colTotal;

    @FXML
    private TableColumn<VentasModel, String> colEstado;

    @FXML
    private TableColumn<VentasModel, Void> colAcciones;

    //detalle recibo
    @FXML
    private TableView<ReciboModel> tablaDetalle;

    @FXML
    private TableColumn<ReciboModel, String> colPartido;

    @FXML
    private TableColumn<ReciboModel, String> colEstadio;

    @FXML
    private TableColumn<ReciboModel, String> colAsiento;

    @FXML
    private TableColumn<ReciboModel, String> colSeccion;

    @FXML
    private TableColumn<ReciboModel, BigDecimal> colPrecio;

    @FXML
    private TableColumn<ReciboModel, BigDecimal> colIva;

    //formulario
    @FXML
    private VBox panelFormulario;

    @FXML
    private Label lblTituloForm;

    @FXML
    private Label lblCliente;

    @FXML
    private ComboBox<PartidoModel> cmbPartido;

    @FXML
    private Label lblResumen;

    @FXML
    private Label lblErrorForm;

    @FXML
    private TextField txtBuscarFactura;

    @FXML
    private Button btnNuevaVenta;

    //tabla tickets disponibles
    @FXML
    private TableView<TicketModel> tablaDisponibles;

    @FXML
    private TableColumn<TicketModel, String> colDispAsiento;

    @FXML
    private TableColumn<TicketModel, String> colDispSeccion;

    @FXML
    private TableColumn<TicketModel, BigDecimal> colDispPrecio;

    @FXML
    private TableColumn<TicketModel, Void> colDispAccion;

    //tabla carrito
    @FXML
    private TableView<TicketModel> tablaCarrito;

    @FXML
    private TableColumn<TicketModel, String> colCarrAsiento;

    @FXML
    private TableColumn<TicketModel, String> colCarrSeccion;

    @FXML
    private TableColumn<TicketModel, BigDecimal> colCarrPrecio;

    @FXML
    private TableColumn<TicketModel, Void> colCarrAccion;

    //controllers
    private final VentaController controller = new VentaController();

    private final PartidoController partidoController = new PartidoController();

    private final BoletosController controllerBo = new BoletosController();

    //listas
    private final ObservableList<VentasModel> lista  = FXCollections.observableArrayList();

    private final ObservableList<TicketModel> disponibles = FXCollections.observableArrayList();

    private final ObservableList<TicketModel> carrito = FXCollections.observableArrayList();

    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    //cliente seleccionado desde otra pantalla
    private ClienteModel clienteActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //tabla ventas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFactura.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVendedor.setCellValueFactory(new PropertyValueFactory<>("vendedor"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        //formatear fecha
        colFecha.setCellValueFactory(cell -> {

            if (cell.getValue().getFecha() == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(
                    cell.getValue().getFecha().format(formatoFecha)
            );
        });

        //estado activa o anulada
        colEstado.setCellValueFactory(cell -> {

            String estado = cell.getValue().isAnulada()
                    ? "ANULADA" : "ACTIVA";

            return new SimpleStringProperty(estado);
        });

        //colores del estado
        colEstado.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(String estado, boolean empty) {

                super.updateItem(estado, empty);

                if (empty || estado == null) {

                    setText(null);
                    setStyle("");

                } else {

                    setText(estado);

                    switch (estado) {

                        case "ACTIVA" ->
                            setStyle("-fx-text-fill: #0F6E56; -fx-font-weight: bold;");

                        case "ANULADA" ->
                            setStyle("-fx-text-fill: #A32D2D; -fx-font-weight: bold;");
                    }
                }
            }
        });

        //detalle recibo
        colPartido.setCellValueFactory(new PropertyValueFactory<>("partido"));
        colEstadio.setCellValueFactory(new PropertyValueFactory<>("estadio"));
        colAsiento.setCellValueFactory(new PropertyValueFactory<>("numeroAsiento"));
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("seccion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("iva"));

        //tickets disponibles
        colDispAsiento.setCellValueFactory(new PropertyValueFactory<>("numeroAsiento"));
        colDispSeccion.setCellValueFactory(new PropertyValueFactory<>("seccion"));
        colDispPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        //tickets carrito
        colCarrAsiento.setCellValueFactory(new PropertyValueFactory<>("numeroAsiento"));
        colCarrSeccion.setCellValueFactory(new PropertyValueFactory<>("seccion"));
        colCarrPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        //crear botones
        configurarBotonAgregar();
        configurarBotonQuitar();
        configurarAcciones();

        //llenar combo partidos
        cmbPartido.setItems(
                FXCollections.observableArrayList(
                        partidoController.listarDisponibles()
                )
        );

        //cuando cambia partido
        cmbPartido.valueProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal != null) {

                cargarDisponibles(newVal.getId());
            }
        });

        //buscar factura automatico
        txtBuscarFactura.textProperty().addListener((obs, oldVal, newVal) -> {

            buscarFactura(newVal);
        });

        //seleccionar venta
        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, venta) -> {

            if (venta != null) {

                cargarDetalle(venta.getId());
            }
        });

        cargarTabla();
    }

    //boton agregar ticket
    private void configurarBotonAgregar() {

        colDispAccion.setCellFactory(col -> new TableCell<>() {

            private final Button btnAgregar = new Button("Agregar");

            {
                btnAgregar.getStyleClass().add("boton-insertar");

                btnAgregar.setPrefWidth(100);

                btnAgregar.setOnAction(e -> {

                    TicketModel t = getTableView().getItems().get(getIndex());

                    agregarAlCarrito(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : btnAgregar);

                setAlignment(Pos.CENTER);
            }
        });
    }

    //boton quitar carrito
    private void configurarBotonQuitar() {

        colCarrAccion.setCellFactory(col -> new TableCell<>() {

            private final Button btnQuitar = new Button("Quitar");

            {
                btnQuitar.getStyleClass().add("boton-eliminar");

                btnQuitar.setPrefWidth(100);

                btnQuitar.setOnAction(e -> {

                    TicketModel t = getTableView().getItems().get(getIndex());

                    quitarDelCarrito(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : btnQuitar);

                setAlignment(Pos.CENTER);
            }
        });
    }

    //acciones historial
    private void configurarAcciones() {

        colAcciones.setCellFactory(col -> new TableCell<>() {

            private final Button btnVer = new Button("Ver");

            private final Button btnAnular = new Button("Anular");

            private final HBox contenedor
                    = new HBox(6, btnVer, btnAnular);

            {
                btnVer.getStyleClass().add("boton-editar");
                btnAnular.getStyleClass().add("boton-eliminar");

                btnVer.setPrefWidth(70);
                btnAnular.setPrefWidth(80);

                contenedor.setAlignment(Pos.CENTER);

                //ver detalle
                btnVer.setOnAction(e -> {

                    VentasModel venta
                            = getTableView().getItems().get(getIndex());

                    cargarDetalle(venta.getId());
                });

                //anular venta
                btnAnular.setOnAction(e -> {

                    VentasModel venta
                            = getTableView().getItems().get(getIndex());

                    anularVenta(venta);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {

                    setGraphic(null);

                } else {

                    VentasModel venta
                            = getTableView().getItems().get(getIndex());

                    if (venta.isAnulada()) {

                        setGraphic(btnVer);

                    } else {

                        setGraphic(contenedor);
                    }
                }
            }
        });
    }

    //cargar historial ventas
    private void cargarTabla() {

        lista.clear();

        lista.addAll(controller.listarTodas());

        tablaVentas.setItems(lista);
    }

    //buscar factura
    private void buscarFactura(String texto) {

        if (texto == null || texto.trim().isEmpty()) {

            cargarTabla();
            return;
        }

        tablaVentas.setItems(
                FXCollections.observableArrayList(
                        controller.buscarPorFactura(texto)
                )
        );
    }

    //detalle venta
    private void cargarDetalle(int ventaId) {

        List<ReciboModel> detalle
                = controller.obtenerRecibo(ventaId);

        tablaDetalle.setItems(
                FXCollections.observableArrayList(detalle)
        );
    }

    //tickets disponibles segun partido
    private void cargarDisponibles(int partidoId) {

        disponibles.clear();

        disponibles.addAll(
                controllerBo.consultarDisponibles(partidoId)
        );

        tablaDisponibles.setItems(disponibles);
    }

    //abrir formulario
    @FXML
    private void abrirFormulario() {

        //validar cliente seleccionado
        if (!ClienteSeleccionado.hayCliente()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Debe seleccionar un cliente primero desde Clientes"
            );

            return;
        }

        clienteActual = ClienteSeleccionado.getCliente();

        lblCliente.setText(
                clienteActual.getNombre()
                + " "
                + clienteActual.getApellido()
        );

        //limpiar carrito
        TicketsUtil.limpiar();

        carrito.clear();

        tablaCarrito.setItems(carrito);

        cmbPartido.setValue(null);

        disponibles.clear();

        lblResumen.setText("Subtotal: Q0.00");

        lblErrorForm.setText("");

        lblTituloForm.setText("Nueva Venta");

        mostrarPanel(true);
    }

    //agregar ticket al carrito
    private void agregarAlCarrito(TicketModel t) {

        TicketsUtil.agregar(t);

        carrito.add(t);

        disponibles.remove(t);

        actualizarResumen();
    }

    //quitar ticket carrito
    private void quitarDelCarrito(TicketModel t) {

        TicketsUtil.quitar(t.getId());

        carrito.remove(t);

        disponibles.add(t);

        actualizarResumen();
    }

    //actualizar totales
    private void actualizarResumen() {

        if (carrito.isEmpty()) {

            lblResumen.setText("Subtotal: Q0.00");
            return;
        }

        List<Integer> ids = new ArrayList<>();

        List<Double> precios = new ArrayList<>();

        for (TicketModel t : carrito) {

            ids.add(t.getId());

            precios.add(t.getPrecio().doubleValue());
        }

        VentasModel calculo = controller.calcularTotales(
                clienteActual.getId(),
                Sesion.getUsuario().getId(),
                ids,
                precios
        );

        lblResumen.setText(
                "Subtotal: Q" + calculo.getSubtotal()
                + "\nDescuento: Q" + calculo.getDescuento()
                + "\nIVA: Q" + calculo.getTotalIva()
                + "\nTOTAL: Q" + calculo.getTotal()
        );
    }

    //cerrar formulario
    @FXML
    private void cerrarFormulario() {

        TicketsUtil.limpiar();

        carrito.clear();

        disponibles.clear();

        mostrarPanel(false);
    }

    //guardar venta
    @FXML
    private void guardarVenta() {

        lblErrorForm.setText("");

        //validar cliente
        if (clienteActual == null) {

            lblErrorForm.setText("Sin cliente seleccionado");

            return;
        }

        //validar carrito
        if (carrito.isEmpty()) {

            lblErrorForm.setText(
                    "Agregue al menos un ticket al carrito"
            );

            return;
        }

        try {

            List<Integer> ticketIds = new ArrayList<>();

            List<Double> precios = new ArrayList<>();

            //recorrer carrito
            for (TicketModel t : carrito) {

                ticketIds.add(t.getId());

                precios.add(t.getPrecio().doubleValue());
            }

            //calcular totales
            VentasModel venta = controller.calcularTotales(
                    clienteActual.getId(),
                    Sesion.getUsuario().getId(),
                    ticketIds,
                    precios
            );

            //guardar venta
            int ventaId = controller.registrarVenta(venta);

            //si guardo correctamente
            if (ventaId > 0) {

                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Venta registrada. Factura "
                        + venta.getNumeroFactura()
                );

                //limpiar datos
                ClienteSeleccionado.limpiar();

                TicketsUtil.limpiar();

                clienteActual = null;

                lblCliente.setText("Sin cliente seleccionado");

                cerrarFormulario();

                cargarTabla();

            } else {

                lblErrorForm.setText(
                        "No se pudo registrar la venta"
                );
            }

        } catch (Exception e) {

            lblErrorForm.setText(
                    "Error: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    //anular venta
    private void anularVenta(VentasModel venta) {

        Alert confirm
                = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Confirmar");

        confirm.setHeaderText(null);

        confirm.setContentText(
                "¿Desea anular la factura "
                + venta.getNumeroFactura()
                + "?"
        );

        confirm.showAndWait().ifPresent(respuesta -> {

            if (respuesta == ButtonType.OK) {

                boolean ok = controller.anularVenta(
                        venta.getId(),
                        Sesion.getUsuario().getId(),
                        "Anulada desde sistema"
                );

                if (ok) {

                    mostrarAlerta(
                            Alert.AlertType.INFORMATION,
                            "Venta anulada correctamente"
                    );

                    cargarTabla();

                    tablaDetalle.getItems().clear();

                } else {

                    mostrarAlerta(
                            Alert.AlertType.ERROR,
                            "No se pudo anular"
                    );
                }
            }
        });
    }

    //volver menu
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

    //mostrar u ocultar panel
    private void mostrarPanel(boolean mostrar) {

        panelFormulario.setVisible(mostrar);

        panelFormulario.setManaged(mostrar);
    }

    //alertas reutilizables
    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {

        Alert alert = new Alert(tipo);

        alert.setTitle("Ventas");

        alert.setHeaderText(null);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}
