package com.datum.mundialfx;

import com.mundial.app.controller.VentaController;
import com.mundial.app.model.ReciboModel;
import com.mundial.app.model.VentasModel;
import com.mundial.app.util.Sesion;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VentasController implements Initializable {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnNuevaVenta;

    @FXML
    private Label lblErrorForm;

    @FXML
    private Label lblResumen;

    @FXML
    private Label lblTituloForm;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<VentasModel> tablaVentas;

    @FXML
    private TableView<ReciboModel> tablaDetalle;

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

    @FXML
    private TextField txtBuscarFactura;

    @FXML
    private TextField txtClienteId;

    @FXML
    private TextArea txtTickets;

    @FXML
    private TextArea txtPrecios;

    private final VentaController controller = new VentaController();

    private final ObservableList<VentasModel> lista = FXCollections.observableArrayList();

    private final DateTimeFormatter formatoFecha
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    //cuando carga la vista por primera vez
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //mapear columnas de ventas
        //aqui le decimos a cada columna que atributo del modelo va a mostrar
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFactura.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVendedor.setCellValueFactory(new PropertyValueFactory<>("vendedor"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        //formatear fecha
        //como la fecha viene LocalDateTime la convierto a texto bonito
        colFecha.setCellValueFactory(cell -> {

            //si no hay fecha no mostrar nada
            if (cell.getValue().getFecha() == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(
                    cell.getValue().getFecha().format(formatoFecha)
            );
        });

        //estado activa o anulada
        //esto es una columna personalizada porque en bd tengo boolean
        colEstado.setCellValueFactory(cell -> {

            //si anulada=true entonces mostrar ANULADA
            String estado = cell.getValue().isAnulada()
                    ? "ANULADA": "ACTIVA";

            return new SimpleStringProperty(estado);
        });

        //pintar estado con colores
        //esto es solo visual para que se vea mejor
        colEstado.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(String estado, boolean empty) {

                super.updateItem(estado, empty);

                //si esta vacio limpiar todo
                if (empty || estado == null) {

                    setText(null);
                    setStyle("");
                } else {
                    //mostrar texto
                    setText(estado);

                    //dependiendo el estado cambia el color
                    switch (estado) {
                        case "ACTIVA" ->
                            setStyle("-fx-text-fill: #0F6E56; -fx-font-weight: bold;");
                        case "ANULADA" ->
                            setStyle("-fx-text-fill: #A32D2D; -fx-font-weight: bold;");
                    }
                }
            }
        });

        //tabla detalle tickets
        //estas columnas son de la tabla de abajo
        colPartido.setCellValueFactory(new PropertyValueFactory<>("partido"));
        colEstadio.setCellValueFactory(new PropertyValueFactory<>("estadio"));
        colAsiento.setCellValueFactory(new PropertyValueFactory<>("numeroAsiento"));
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("seccion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("iva"));

        //crear botones editar/anular/ver
        configurarAcciones();

        //buscar automaticamente mientras escribe
        //cada vez que cambia el texto se llama buscarFactura
        txtBuscarFactura.textProperty().addListener((obs, oldVal, newVal) -> {

            buscarFactura(newVal);
        });

        //cuando selecciona una venta cargar tickets abajo
        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, venta) -> {

            //si selecciono una fila
            if (venta != null) {

                //traer detalle de esa venta
                cargarDetalle(venta.getId());
            }
        });

        //cargar tabla principal
        cargarTabla();
    }

//botones de acciones de la tabla
//esto crea botones dentro de cada fila de la tabla
    private void configurarAcciones() {

        //crear contenido personalizado dentro de la columna
        colAcciones.setCellFactory(col -> new TableCell<>() {

            //crear botones
            private final Button btnVer = new Button("Ver");

            private final Button btnAnular = new Button("Anular");

            //contenedor para meter los botones juntos
            private final HBox contenedor = new HBox(6, btnVer, btnAnular);

            {
                btnVer.getStyleClass().add("boton-editar");
                btnAnular.getStyleClass().add("boton-eliminar");

             
                btnVer.setPrefWidth(70);
                btnAnular.setPrefWidth(80);
                contenedor.setAlignment(Pos.CENTER);
                
                
                //boton ver
                btnVer.setOnAction(e -> {
                    //obtener la fila seleccionada
                    VentasModel venta = getTableView().getItems().get(getIndex());
                    //cargar tickets de la venta
                    cargarDetalle(venta.getId());
                });
                //boton anular
                btnAnular.setOnAction(e -> {
                    //obtener venta actual
                    VentasModel venta = getTableView().getItems().get(getIndex());
                    //mandar a anular
                    anularVenta(venta);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                //si la fila esta vacia no poner nada
                if (empty) {
                    setGraphic(null);
                } else {
                    //obtener venta actual de esa fila
                    VentasModel venta = getTableView().getItems().get(getIndex());
                    //si ya esta anulada solo dejar ver
                    if (venta.isAnulada()) {

                        setGraphic(btnVer);
                    } else {
                        //si esta activa dejar ambos botones
                        setGraphic(contenedor);
                    }
                }
            }
        });
    }

//cargar todas las ventas de bd
    private void cargarTabla() {

        lista.clear();

        //agregar todas las ventas que vienen del controller
        lista.addAll(controller.listarTodas());

        //mandar lista a la tabla
        tablaVentas.setItems(lista);
    }

    //buscar factura por numero
    private void buscarFactura(String texto) {

        //si esta vacio mostrar todo otra vez
        if (texto == null || texto.trim().isEmpty()) {

            cargarTabla();
            return;
        }

        //buscar en bd usando ilike
        tablaVentas.setItems(FXCollections.observableArrayList(controller.buscarPorFactura(texto)
        )
        );
    }

//cargar tickets de una venta
    private void cargarDetalle(int ventaId) {

        //traer recibo completo de bd
        List<ReciboModel> detalle = controller.obtenerRecibo(ventaId);

        //llenar tabla detalle
        tablaDetalle.setItems(FXCollections.observableArrayList(detalle)
        );
    }

    //abrir panel formulario
    @FXML
    private void abrirFormulario() {

        //limpiar datos viejos
        limpiarFormulario();

        //titulo del panel
        lblTituloForm.setText("Nueva Venta");

        //mostrar panel derecho
        mostrarPanel(true);
    }

    //cerrar panel formulario
    @FXML
    private void cerrarFormulario() {

        //limpiar datos
        limpiarFormulario();

        //ocultar panel
        mostrarPanel(false);
    }

//guardar venta
    @FXML
    private void guardarVenta() {

        //limpiar errores viejos
        lblErrorForm.setText("");

        try {

            int clienteId = Integer.parseInt(
                    txtClienteId.getText().trim()
            );

            //usuario que inicio sesion
            int usuarioId = Sesion.getUsuario().getId();

            //lista ids tickets
            List<Integer> ticketIds = new ArrayList<>();

            //separar texto por comas
            //ejemplo 1,2,3,4
            String[] tickets = txtTickets.getText().split(",");

            //recorrer cada numero
            for (String tic : tickets) {
                //agregar a lista convirtiendo a int
                ticketIds.add(Integer.parseInt(tic.trim())
                );
            }

            //lista precios
            List<Double> precios = new ArrayList<>();

            //separar precios por coma
            String[] preciosTxt = txtPrecios.getText().split(",");

            //recorrer precios
            for (String par : preciosTxt) {
                //convertir a double
                precios.add(Double.parseDouble(par.trim())
                );
            }

            //validar cantidades iguales
            //si hay 3 tickets deben haber 3 precios
            if (ticketIds.size() != precios.size()) {

                lblErrorForm.setText("La cantidad de tickets y precios no coincide");
                return;
            }

            //calcular subtotal descuento iva total
            //todo eso lo hace el dao
            VentasModel venta = controller.calcularTotales(
                    clienteId,
                    usuarioId,
                    ticketIds,
                    precios
            );

            //mostrar resumen calculado
            lblResumen.setText(
                    "Subtotal: Q" + venta.getSubtotal() + "\nDescuento: Q" + venta.getDescuento() + "\nIVA: Q" + venta.getTotalIva()
                    + "\nTOTAL: Q" + venta.getTotal()
            );

            //guardar venta en bd
            int ventaId = controller.registrarVenta(venta);

            //si devolvio un id entonces si guardo
            if (ventaId > 0) {

                mostrarAlerta(
                        Alert.AlertType.INFORMATION, "Venta registrada correctamente"
                );

                cerrarFormulario();
                cargarTabla();

            } else {

                lblErrorForm.setText("No se pudo registrar la venta");
            }

        } catch (NumberFormatException e) {

            //si puso letras donde iban numeros
            lblErrorForm.setText("Revise el id y precios");

        } catch (Exception e) {

            //cualquier otro error
            lblErrorForm.setText("Error: " + e.getMessage());

            e.printStackTrace();
        }
    }

//anular venta
    private void anularVenta(VentasModel venta) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);

        confirm.setContentText(
                "¿Desea anular la factura " + venta.getNumeroFactura() + "?"
        );

        //esperar respuesta
        confirm.showAndWait().ifPresent(respuesta -> {

            //si le dio aceptar
            if (respuesta == ButtonType.OK) {

                //mandar a anular
                boolean ok = controller.anularVenta(
                        venta.getId(),
                        Sesion.getUsuario().getId(),
                        "Anulada desde sistema"
                );

                //si todo salio bien
                if (ok) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Venta anulada correctamente"
                    );

                    cargarTabla();
                    //limpiar detalle tickets
                    tablaDetalle.getItems().clear();

                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo anular"
                    );
                }
            }
        });
    }

//volver menu dependiendo el rol
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

//mostrar o ocultar panel derecho
    private void mostrarPanel(boolean mostrar) {

        panelFormulario.setVisible(mostrar);
        //managed = si ocupa espacio
        panelFormulario.setManaged(mostrar);
    }

    //limpiar formulario
    private void limpiarFormulario() {
        txtClienteId.clear();
        txtTickets.clear();
        txtPrecios.clear();
        lblResumen.setText("Subtotal: Q0.00");
        lblErrorForm.setText("");
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
