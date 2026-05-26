/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.datum.mundialfx;

import com.mundial.app.controller.PartidoController;
import com.mundial.app.controller.BoletosController;
import com.mundial.app.model.PartidoModel;
import com.mundial.app.model.TicketModel;
import com.mundial.app.util.Sesion;
import java.math.BigDecimal;
import java.net.URL;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TicketController implements Initializable {

    //top
    @FXML
    private ComboBox<PartidoModel> cmbPartido;
    @FXML
    private Button btnGenerarMasivo;

    //tabla
    @FXML
    private TableView<TicketModel> tablaTickets;
    @FXML
    private TableColumn<TicketModel, Integer> colId;
    @FXML
    private TableColumn<TicketModel, String> colAsiento;
    @FXML
    private TableColumn<TicketModel, String> colSeccion;
    @FXML
    private TableColumn<TicketModel, BigDecimal> colPrecio;
    @FXML
    private TableColumn<TicketModel, String> colEstado;
    @FXML
    private TableColumn<TicketModel, Void> colAcciones;

    //formulario de generar masivo
    @FXML
    private VBox panelFormulario;
    @FXML
    private Label lblTituloForm;
    @FXML
    private TextField txtCantVip;
    @FXML
    private TextField txtPrecioVip;
    @FXML
    private TextField txtCantPref;
    @FXML
    private TextField txtPrecioPref;
    @FXML
    private TextField txtCantGen;
    @FXML
    private TextField txtPrecioGen;
    @FXML
    private Label lblErrorForm;

    //instancias
    private final BoletosController controller = new BoletosController();
    private final PartidoController partidoController = new PartidoController();
    private final ObservableList<TicketModel> lista = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //decirle a la tabla que datos mostrar del model
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAsiento.setCellValueFactory(new PropertyValueFactory<>("numeroAsiento"));
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("seccion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        //pinta los estados con color
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
                        case "VENDIDO" ->
                            setStyle("-fx-text-fill: #185FA5; -fx-font-weight: bold;");
                        case "RESERVADO" ->
                            setStyle("-fx-text-fill: #B57E00; -fx-font-weight: bold;");
                    }
                }
            }
        });

        //cargar lista de partidos al combo
        cargarPartidos();

        //cuando el admin cambia el partido del combo se cargan sus tickets
        cmbPartido.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarTickets(newVal.getId());
            }
        });

        //segun el rol acomoda lo que se ve
        configurarSegunRol();
    }

    //llena el combo con los partidos
    private void cargarPartidos() {
        List<PartidoModel> partidos = partidoController.listarPartidos();
        cmbPartido.setItems(FXCollections.observableArrayList(partidos));
    }

    //el admin gestiona todo, el vendedor no deberia llegar aqui pero por si acaso ocultamos
    private void configurarSegunRol() {
        boolean esAdmin = Sesion.esAdmin();

        if (!esAdmin) {
            btnGenerarMasivo.setVisible(false);
            btnGenerarMasivo.setManaged(false);
            colAcciones.setVisible(false);
        }

        //configurar botones de la columna acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnPrecio = new Button("Cambiar precio");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox contenedor = new HBox(6, btnPrecio, btnEliminar);

            {
                btnPrecio.getStyleClass().add("boton-editar");
                btnEliminar.getStyleClass().add("boton-eliminar");
                btnPrecio.setPrefWidth(110);
                btnEliminar.setPrefWidth(90);
                contenedor.setAlignment(Pos.CENTER);

                btnPrecio.setOnAction(e -> {
                    TicketModel t = getTableView().getItems().get(getIndex());
                    cambiarPrecio(t);
                });

                btnEliminar.setOnAction(e -> {
                    TicketModel t = getTableView().getItems().get(getIndex());
                    eliminarTicket(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    TicketModel t = getTableView().getItems().get(getIndex());
                    //si esta VENDIDO no se puede tocar
                    if ("VENDIDO".equals(t.getEstado())) {
                        setGraphic(null);
                    } else {
                        setGraphic(contenedor);
                    }
                }
            }
        });
    }

    //trae los tickets de un partido y los mete en la tabla
    private void cargarTickets(int partidoId) {
        lista.clear();
        lista.addAll(controller.consultarPorPartido(partidoId));
        tablaTickets.setItems(lista);
    }

    @FXML
    private void abrirGenerarMasivo() {
        //hay que tener un partido seleccionado primero
        if (cmbPartido.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un partido primero.");
            return;
        }
        limpiarFormulario();
        lblTituloForm.setText("Generar Tickets - " + cmbPartido.getValue());
        mostrarPanel(true);
    }

    @FXML
    private void cerrarFormulario() {
        mostrarPanel(false);
        limpiarFormulario();
    }

    @FXML
    private void generarMasivo() {
        lblErrorForm.setText("");

        PartidoModel partido = cmbPartido.getValue();
        if (partido == null) {
            lblErrorForm.setText("Seleccione un partido.");
            return;
        }

        try {
            //leer cantidades y precios
            int cantVip = parsearEntero(txtCantVip.getText());
            double precioVip = parsearDecimal(txtPrecioVip.getText());
            int cantPref = parsearEntero(txtCantPref.getText());
            double precioPref = parsearDecimal(txtPrecioPref.getText());
            int cantGen = parsearEntero(txtCantGen.getText());
            double precioGen = parsearDecimal(txtPrecioGen.getText());

            //verificar que al menos haya algo que generar
            if (cantVip == 0 && cantPref == 0 && cantGen == 0) {
                lblErrorForm.setText("Ingrese al menos una cantidad mayor a cero.");
                return;
            }

            //si una seccion tiene cantidad debe tener precio valido
            if (cantVip > 0 && precioVip <= 0) {
                lblErrorForm.setText("El precio VIP debe ser mayor a 0.");
                txtPrecioVip.requestFocus();
                return;
            }
            if (cantPref > 0 && precioPref <= 0) {
                lblErrorForm.setText("El precio Preferencial debe ser mayor a 0.");
                txtPrecioPref.requestFocus();
                return;
            }
            if (cantGen > 0 && precioGen <= 0) {
                lblErrorForm.setText("El precio General debe ser mayor a 0.");
                txtPrecioGen.requestFocus();
                return;
            }

            //verificar que no se pase de la capacidad del estadio
            int totalGenerar = cantVip + cantPref + cantGen;
            if (totalGenerar > partido.getCapacidad()) {
                lblErrorForm.setText("El total supera la capacidad (" + partido.getCapacidad() + ").");
                return;
            }

            int generados = controller.generarMasivo(
                    partido.getId(),
                    cantVip, precioVip,
                    cantPref, precioPref,
                    cantGen, precioGen
            );

            if (generados > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Se generaron " + generados + " tickets correctamente.");
                cerrarFormulario();
                cargarTickets(partido.getId());
            } else {
                lblErrorForm.setText("No se pudo generar.");
            }
        } catch (NumberFormatException e) {
            lblErrorForm.setText("Revise que los numeros sean validos.");
        } catch (Exception e) {
            lblErrorForm.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //abre un cuadro pequeno para escribir el precio nuevo
    private void cambiarPrecio(TicketModel t) {
        TextInputDialog dialog = new TextInputDialog(t.getPrecio().toString());
        dialog.setTitle("Cambiar precio");
        dialog.setHeaderText("Ticket " + t.getNumeroAsiento());
        dialog.setContentText("Nuevo precio:");

        dialog.showAndWait().ifPresent(valor -> {
            try {
                double nuevo = Double.parseDouble(valor.trim());
                if (nuevo <= 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "El precio debe ser mayor a 0.");
                    return;
                }
                boolean ok = controller.actualizarPrecio(t.getId(), nuevo);
                if (ok) {
                    cargarTickets(cmbPartido.getValue().getId());
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Precio invalido.");
            }
        });
    }

    private void eliminarTicket(TicketModel t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Desea eliminar el ticket " + t.getNumeroAsiento() + "?");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                boolean ok = controller.eliminarTicket(t.getId());
                if (ok) {
                    cargarTickets(cmbPartido.getValue().getId());
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar.");
                }
            }
        });
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

    //convierte texto vacio en 0 para no tronar
    private int parsearEntero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(texto.trim());
    }

    private double parsearDecimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(texto.trim());
    }

    private void mostrarPanel(boolean mostrar) {
        panelFormulario.setVisible(mostrar);
        panelFormulario.setManaged(mostrar);
    }

    private void limpiarFormulario() {
        txtCantVip.clear();
        txtPrecioVip.clear();
        txtCantPref.clear();
        txtPrecioPref.clear();
        txtCantGen.clear();
        txtPrecioGen.clear();
        lblErrorForm.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Tickets");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
