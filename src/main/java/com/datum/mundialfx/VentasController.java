package com.datum.mundialfx;

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

//imports para el reporte
import net.sf.jasperreports.engine.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;

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

    //filtro por seccion + contador de disponibles
    @FXML
    private ComboBox<String> cmbFiltroSeccion;

    @FXML
    private Label lblConteoSecciones;

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
    private final ObservableList<VentasModel> lista = FXCollections.observableArrayList();

    private final ObservableList<TicketModel> disponibles = FXCollections.observableArrayList();

    //lista master sin filtrar para no tener que ir a la BD cada que cambie el filtro
    private final ObservableList<TicketModel> disponiblesTodos = FXCollections.observableArrayList();

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

                //si cambio de partido y habia tickets en el carrito los borramos
                //para no mezclar tickets de distintos partidos en la misma venta
                if (oldVal != null && !oldVal.equals(newVal) && !carrito.isEmpty()) {
                    TicketsUtil.limpiar();
                    carrito.clear();
                    tablaCarrito.setItems(carrito);
                    actualizarResumen();
                }

                //reseteamos el filtro a Todas para que el vendedor vea de entrada lo que hay
                cmbFiltroSeccion.setValue("Todas");

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

        //llenar combo filtro de secciones
        cmbFiltroSeccion.setItems(
                FXCollections.observableArrayList(
                        "Todas", "VIP", "PREFERENCIAL", "GENERAL"
                )
        );
        cmbFiltroSeccion.setValue("Todas");

        //cuando cambia el filtro reaplicamos sobre la lista master
        cmbFiltroSeccion.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltroSeccion();
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

        //solo el admin puede anular ventas, el vendedor unicamente consulta
        boolean puedeAnular = Sesion.esAdmin();

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

                //ver detalle -- abre el recibo formateado en un dialog
                //para que se pueda mostrar al cliente sin tener que mirar la tabla
                btnVer.setOnAction(e -> {

                    VentasModel venta = getTableView().getItems().get(getIndex());

                    cargarDetalle(venta.getId());
                    mostrarRecibo(venta);
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

                    //si esta anulada o si no tengo permiso para anular
                    //solamente se muestra el boton Ver
                    if (venta.isAnulada() || !puedeAnular) {

                        setGraphic(btnVer);

                    } else {

                        setGraphic(contenedor);
                    }
                }
            }
        });
    }

    //muestra el recibo completo en un dialog con formato de comprobante
    private void mostrarRecibo(VentasModel venta) {

        List<ReciboModel> detalle = controller.obtenerRecibo(venta.getId());

        if (detalle == null || detalle.isEmpty()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "No se encontro el detalle de esta venta"
            );

            return;
        }
        //primer fila tiene los datos generales que se repiten en todas las filas
        ReciboModel cab = detalle.get(0);

        StringBuilder sb = new StringBuilder();

        sb.append("Factura: ").append(cab.getNumeroFactura()).append("\n");

        if (cab.getFecha() != null) {
            sb.append("Fecha:   ").append(cab.getFecha().format(formatoFecha)).append("\n");
        }

        sb.append("Cliente: ").append(cab.getClienteNombre());

        if (cab.getClienteEmail() != null && !cab.getClienteEmail().isBlank()) {
            sb.append(" (").append(cab.getClienteEmail()).append(")");
        }

        sb.append("\n");
        sb.append("Vendedor: ").append(cab.getVendedor()).append("\n");
        sb.append("----------------------------------------\n");

        //listado de tickets
        for (ReciboModel r : detalle) {

            sb.append(r.getPartido())
                    .append("  |  Asiento ").append(r.getNumeroAsiento())
                    .append("  (").append(r.getSeccion()).append(")")
                    .append("  Q").append(r.getPrecio())
                    .append("\n");
        }

        sb.append("----------------------------------------\n");
        sb.append("Subtotal:  Q").append(cab.getSubtotal()).append("\n");
        sb.append("Descuento: Q").append(cab.getDescuento()).append("\n");
        sb.append("IVA:       Q").append(cab.getTotalIva()).append("\n");
        sb.append("TOTAL:     Q").append(cab.getTotal()).append("\n");

        if (venta.isAnulada()) {
            sb.append("\n*** VENTA ANULADA ***");
        }

        Alert recibo = new Alert(Alert.AlertType.INFORMATION);
        recibo.setTitle("Recibo " + cab.getNumeroFactura());
        recibo.setHeaderText("Comprobante de Venta");
        recibo.setContentText(sb.toString());
        recibo.getDialogPane().setPrefWidth(500);
        recibo.showAndWait();
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

        //guardamos en la lista master para poder filtrar sin volver a la BD
        disponiblesTodos.clear();

        disponiblesTodos.addAll(
                controllerBo.consultarDisponibles(partidoId)
        );
        tablaDisponibles.setItems(disponibles);
        aplicarFiltroSeccion();
    }

    //aplica el filtro de seccion sobre la lista master
    private void aplicarFiltroSeccion() {

        String filtro = cmbFiltroSeccion.getValue();

        disponibles.clear();

        if (filtro == null || "Todas".equals(filtro)) {

            //si no hay filtro mostramos todo
            disponibles.addAll(disponiblesTodos);

        } else {

            //solo agregamos los que coincidan con la seccion
            for (TicketModel t : disponiblesTodos) {

                if (filtro.equalsIgnoreCase(t.getSeccion())) {

                    disponibles.add(t);
                }
            }
        }
        actualizarConteoSecciones();
    }

    //Esta funcion lo que hace es recorrer tickets disponibles para que en ventas veamos cuantso hay
    private void actualizarConteoSecciones() {

        int vip = 0, pref = 0, gen = 0;

        for (TicketModel t : disponiblesTodos) {

            if ("VIP".equalsIgnoreCase(t.getSeccion())) {
                vip++;
            } else if ("PREFERENCIAL".equalsIgnoreCase(t.getSeccion())) {
                pref++;
            } else if ("GENERAL".equalsIgnoreCase(t.getSeccion())) {
                gen++;
            }
        }

        lblConteoSecciones.setText(
                "VIP " + vip + "  |  Preferencial " + pref + "  |  General " + gen
        );
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
                clienteActual.getNombre() + " " + clienteActual.getApellido()
        );

        //limpiar carrito
        TicketsUtil.limpiar();

        carrito.clear();

        tablaCarrito.setItems(carrito);

        cmbPartido.setValue(null);

        disponibles.clear();
        disponiblesTodos.clear();

        //regresamos el filtro a Todas y reseteamos contador
        cmbFiltroSeccion.setValue("Todas");
        lblConteoSecciones.setText("");

        lblResumen.setText("Subtotal: Q0.00");

        lblErrorForm.setText("");

        lblTituloForm.setText("Nueva Venta");

        mostrarPanel(true);
    }

    //agregar ticket al carrito
    private void agregarAlCarrito(TicketModel t) {

        TicketsUtil.agregar(t);

        carrito.add(t);

        //hay que sacarlo de la master y refiltrar para que el conteo cuadre
        disponiblesTodos.remove(t);

        aplicarFiltroSeccion();

        actualizarResumen();
    }

    //quitar ticket carrito
    private void quitarDelCarrito(TicketModel t) {

        TicketsUtil.quitar(t.getId());

        carrito.remove(t);
        //regresa a la master y refiltramos para respetar el filtro actual
        disponiblesTodos.add(t);

        aplicarFiltroSeccion();

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

        //mostrar el % de descuento que se aplico segun cantidad de tickets
        int cantidad = carrito.size();
        String infoDesc;
        if (cantidad >= 10) {
            //10 o mas boletos 7% de descuento
            infoDesc = " (descuento 7%)";
        } else if (cantidad > 5) {
            //de 6 a 9 boletos 5% de descuento
            infoDesc = " (descuento 5%)";
        } else {
            infoDesc = "";
        }

        lblResumen.setText(
                "Tickets: " + cantidad + infoDesc
                + "\nSubtotal: Q" + calculo.getSubtotal()
                + "\nDescuento: Q" + calculo.getDescuento()
                + "\nIVA (12%): Q" + calculo.getTotalIva()
                + "\nTOTAL: Q" + calculo.getTotal()
        );
    }

    //cerrar formulario
    @FXML
    private void cerrarFormulario() {

        TicketsUtil.limpiar();
        carrito.clear();
        disponibles.clear();
        //limpiamos tambien la master para no dejar tickets viejos en memoria
        disponiblesTodos.clear();
        mostrarPanel(false);
    }

    //guardar venta
    @FXML
    private void guardarVenta() {
        lblErrorForm.setText("");

        // validar cliente
        if (clienteActual == null) {
            lblErrorForm.setText("Sin cliente seleccionado");
            return;
        }

        // validar carrito
        if (carrito.isEmpty()) {
            lblErrorForm.setText("Agregue al menos un ticket al carrito");
            return;
        }

        try {
            List<Integer> ticketIds = new ArrayList<>();
            List<Double> precios = new ArrayList<>();

            // recorrer carrito
            for (TicketModel t : carrito) {
                ticketIds.add(t.getId());
                precios.add(t.getPrecio().doubleValue());
            }

            // calcular totales
            VentasModel venta = controller.calcularTotales(
                    clienteActual.getId(),
                    Sesion.getUsuario().getId(),
                    ticketIds,
                    precios
            );

            // guardar venta
            int ventaId = controller.registrarVenta(venta);

            // si guardo correctamente
            if (ventaId > 0) {
                // recargar tabla antes del alert para obtener el numero de factura real
                cargarTabla();

                VentasModel registrada = lista.stream()
                        .filter(v -> v.getId() == ventaId)
                        .findFirst()
                        .orElse(null);

                String numFactura = (registrada != null && registrada.getNumeroFactura() != null)
                        ? registrada.getNumeroFactura()
                        : "ID-" + ventaId;

                /////////////Gernerar el pdf
                generarPdfFactura(ventaId, numFactura);

                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Venta registrada. Factura " + numFactura
                );

                // limpiar datos
                ClienteSeleccionado.limpiar();
                TicketsUtil.limpiar();
                clienteActual = null;
                lblCliente.setText("Sin cliente seleccionado");

                cerrarFormulario();

            } else {
                lblErrorForm.setText("No se pudo registrar la venta");
            }

        } catch (Exception e) {
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

    //funcion de reportes
    private void generarPdfFactura(int ventaId, String numFactura) {
        try {
            // 1. Leer el archivo usando InputStream (Evita el error de java.net.URL.getPath())
            // ¡Ojo! Asegúrate de que se llame exactamente "reporte_venta.jasper" con minúsculas/mayúsculas exactas
            InputStream reportStream = getClass().getResourceAsStream("/reportes/reporte_venta.jasper");

            // Validación estricta para saber de inmediato si Java no ve el archivo
            if (reportStream == null) {
                System.err.println("ERROR: No se encontró 'reporte_venta.jasper' en src/main/resources/reportes/");
                System.err.println("Asegúrate de hacer un 'Clean and Build' en tu IDE para que copie el archivo.");
                return;
            }

            // 2. Mapear el parámetro esperado por tu función de PostgreSQL
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("P_venta_id", ventaId);

            // 3. Obtener la conexión activa a la base de datos desde tu controlador
            Connection conexion = controller.getConexion();

            // 4. Llenar el reporte pasando directamente el InputStream
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conexion);

            // 5. Definir la carpeta de destino de las facturas en la raíz
            File directorio = new File("facturas");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            // Limpiar el número de factura de caracteres inválidos para el sistema de archivos
            String nombreArchivo = "factura_" + numFactura.replaceAll("[^a-zA-Z0-9-]", "_") + ".pdf";
            String outputPath = directorio.getAbsolutePath() + File.separator + nombreArchivo;

            // 6. Exportar directamente a PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            System.out.println("Factura PDF guardada exitosamente en: " + outputPath);

        } catch (Exception e) {
            System.err.println("Error al generar el PDF de la factura: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
