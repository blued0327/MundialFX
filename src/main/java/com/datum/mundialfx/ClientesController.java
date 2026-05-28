package com.datum.mundialfx;

import com.mundial.app.controller.ClienteController;
import com.mundial.app.model.ClienteModel;
import com.mundial.app.util.ClienteSeleccionado;
import com.mundial.app.util.Sesion;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClientesController implements Initializable {

    //tabla
    @FXML
    private TableView<ClienteModel> tablaClientes;
    @FXML
    private TableColumn<ClienteModel, Integer> colId;
    @FXML
    private TableColumn<ClienteModel, String> colNombre;
    @FXML
    private TableColumn<ClienteModel, String> colApellido;
    @FXML
    private TableColumn<ClienteModel, String> colTelefono;
    @FXML
    private TableColumn<ClienteModel, String> colEmail;
    @FXML
    private TableColumn<ClienteModel, Boolean> colEstado;
    @FXML
    private TableColumn<ClienteModel, Void> colAcciones;

    //formulario
    @FXML
    private VBox panelFormulario;
    @FXML
    private Label lblTituloForm;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellido;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextArea txtDireccion;
    @FXML
    private Label lblErrorForm;

    @FXML
    private TextField txtBuscar;

    //instancias
    //ojo: ClienteController es el de logica que esta en com.mundial.app.controller, no el de la vista
    private final ClienteController controller = new ClienteController();
    private final ObservableList<ClienteModel> lista = FXCollections.observableArrayList();
    private int idEditar = -1;

    //me di cuenta que no tenia como regresar jajaj
    @FXML
    private void volverMenu() {
        try {
            //segun el rol regresa al menu que corresponde
            if (Sesion.esAdmin()) {
                App.setRoot("MenuAdmin");
            } else {
                App.setRoot("MenuVendedor");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //decirle a la tabla que datos mostrar del model
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        //funcion para cambiar los colorcitos dependiendo si esta activo o no
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                } else {
                    setText(estado ? "Activo" : "Inactivo");
                    setStyle(estado ? "-fx-text-fill: #0F6E56; -fx-font-weight: bold;" : "-fx-text-fill: #A32D2D; -fx-font-weight: bold;");
                }
            }
        });

        //segun el rol acomoda las columnas y los botones
        configurarSegunRol();

        //listener del buscador, cada que escribe filtra la tabla
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarTabla(newVal));

        cargarTabla();
    }

    //esta funcion decide que ve el admin y que ve el vendedor
    //admin ve todo y puede editar otodo
    // no ve varios datos, pero le vamos a dar el boton para que vaya a vender
    private void configurarSegunRol() {
        boolean esAdmin = Sesion.esAdmin();

        //ocultamos columnas que el vendedor no necesita ver
        colId.setVisible(esAdmin);
        colEstado.setVisible(esAdmin);

        //la columna de acciones se arma distinto segun quien sea
        colAcciones.setCellFactory(col -> new TableCell<>() {
            //creamos los botones
            private final Button btnPrincipal = new Button();
            private final Button btnEstado = new Button();
            private final HBox contenedor = new HBox(6);

            {//lo alineamos
                contenedor.setAlignment(Pos.CENTER);
                btnPrincipal.setPrefWidth(90);
                btnEstado.setPrefWidth(85);

                //como vamos a hacer distinsion de mostrar si es admin o no
                if (esAdmin) {
                    //admin: Editar + Inactivar/Activar
                    btnPrincipal.setText("Editar");
                    btnPrincipal.getStyleClass().add("boton-editar");
                    contenedor.getChildren().addAll(btnPrincipal, btnEstado);

                    btnPrincipal.setOnAction(e -> {
                        ClienteModel cliente = getTableView().getItems().get(getIndex());
                        editar(cliente);
                    });

                    btnEstado.setOnAction(e -> {
                        ClienteModel cliente = getTableView().getItems().get(getIndex());
                        cambiarEstado(cliente);
                    });
                } else {
                    //vendedor solo Seleccionar
                    btnPrincipal.setText("Seleccionar");
                    btnPrincipal.getStyleClass().add("boton-insertar");
                    contenedor.getChildren().add(btnPrincipal);

                    btnPrincipal.setOnAction(e -> {
                        ClienteModel cliente = getTableView().getItems().get(getIndex());
                        seleccionarParaVenta(cliente);
                    });
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    //solo el admin necesita que el boton cambie segun el estado del cliente
                    if (esAdmin) {
                        ClienteModel cliente = getTableView().getItems().get(getIndex());
                        if (cliente.isEstado()) {
                            btnEstado.setText("Inactivar");
                            btnEstado.getStyleClass().removeAll("boton-insertar");
                            btnEstado.getStyleClass().add("boton-eliminar");
                        } else {
                            btnEstado.setText("Activar");
                            btnEstado.getStyleClass().removeAll("boton-eliminar");
                            btnEstado.getStyleClass().add("boton-insertar");
                        }
                    }
                    setGraphic(contenedor);
                }
            }
        });
    }

    //funcion para cargar la tabla
    private void cargarTabla() {
        lista.clear();
        List<ClienteModel> datos = controller.obtenerClientes();
        lista.addAll(datos);
        tablaClientes.setItems(lista);
    }

    //busca por nombre o apellido, ignora mayus/minus
    private void filtrarTabla(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            tablaClientes.setItems(lista);
            tablaClientes.refresh();
            return;
        }

        String filtro = texto.toLowerCase().trim();
        ObservableList<ClienteModel> filtrados = FXCollections.observableArrayList();

        //recorre la lista original y mete los que coincidan
        for (ClienteModel c : lista) {
            if (c.getNombre().toLowerCase().contains(filtro) || c.getApellido().toLowerCase().contains(filtro)) {
                filtrados.add(c);
            }
        }

        tablaClientes.setItems(filtrados);
        tablaClientes.refresh();
    }

    @FXML
    private void abrirFormulario() {
        idEditar = -1;
        lblTituloForm.setText("Nuevo Cliente");
        limpiarFormulario();
        mostrarPanel(true);
    }

    private void editar(ClienteModel cliente) {
        idEditar = cliente.getId();
        lblTituloForm.setText("Editar Cliente");
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtTelefono.setText(cliente.getTelefono());
        txtEmail.setText(cliente.getEmail());
        txtDireccion.setText(cliente.getDireccion());
        lblErrorForm.setText("");
        mostrarPanel(true);
    }

    private void cambiarEstado(ClienteModel cliente) {
        String accion = cliente.isEstado() ? "inactivar" : "activar";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Desea " + accion + " al cliente " + cliente.getNombre() + " " + cliente.getApellido() + "?");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                boolean correcto = controller.cambiarEstado(cliente.getId(), !cliente.isEstado());
                if (correcto) {
                    cargarTabla();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo cambiar el estado.");
                }
            }
        });
    }

    //cree esta clase para el boton,asi supongo que seria mas facul para el vendedor
    //tiene una validacion en el cual estado del cliente es inactivo tire un mensaje
    private void seleccionarParaVenta(ClienteModel cliente) {
        if (!cliente.isEstado()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "El cliente " + cliente.getNombre() + " " + cliente.getApellido()
                    + " esta inactivo y no puede realizar compras ");
            return;
        }
        ClienteSeleccionado.seleccionar(cliente);
        try {
            App.setRoot("Ventas");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir el modulo de Ventas.");
        }
    }

    @FXML
    private void cerrarFormulario() {
        mostrarPanel(false);
        limpiarFormulario();
    }

    @FXML
    private void guardar() {
        //obtener datos
        lblErrorForm.setText("");

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String direccion = txtDireccion.getText().trim();

        //validaciones
        if (nombre.isEmpty()) {
            lblErrorForm.setText("El nombre es obligatorio.");
            txtNombre.requestFocus();
            return;
        }
        if (apellido.isEmpty()) {
            lblErrorForm.setText("El apellido es obligatorio.");
            txtApellido.requestFocus();
            return;
        }
        //el email puede ir vacio, pero si lo escribieron al menos que tenga @
        if (!email.isEmpty() && !email.contains("@")) {
            lblErrorForm.setText("Email invalido.");
            txtEmail.requestFocus();
            return;
        }

        try {
            if (idEditar == -1) {
                //insertar
                boolean correcto = controller.registrarCliente(nombre, apellido, telefono, email, direccion);
                if (correcto) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente registrado correctamente.");
                    cerrarFormulario();
                    cargarTabla();
                } else {
                    lblErrorForm.setText("No se pudo registrar. Verifique los datos.");
                }
            } else {
                //actualizar
                boolean correcto = controller.actualizarCliente(idEditar, nombre, apellido, telefono, email, direccion);
                if (correcto) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente actualizado correctamente.");
                    cerrarFormulario();
                    cargarTabla();
                } else {
                    lblErrorForm.setText("No se pudo actualizar. Verifique los datos.");
                }
            }
        } catch (Exception e) {
            lblErrorForm.setText("Error: " + e.getMessage());
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
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        lblErrorForm.setText("");
        idEditar = -1;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Clientes");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
