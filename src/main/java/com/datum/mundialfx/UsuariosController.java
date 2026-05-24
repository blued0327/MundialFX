/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.datum.mundialfx;

import com.mundial.app.connection.CreateConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.mundial.app.model.UsuarioModel;
import com.mundial.app.controller.UsuarioController;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import com.mundial.app.util.Sesion;

public class UsuariosController implements Initializable {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnNuevo;

    @FXML
    private CheckBox chkEstado;

    //que tipo tienes que ser
    @FXML
    private ComboBox<String> cmbRol;

    //tabla primer ? es de donde jala datos y la otra que tipo es, tengo que oponer las del model porque luego tienen que coincidir
    @FXML
    private TableView<UsuarioModel> tablaUsuarios;
    //esa madre no me las dio en orden
    @FXML
    private TableColumn<UsuarioModel, Boolean> colEstado;

    @FXML
    private TableColumn<UsuarioModel, Integer> colId;

    @FXML
    private TableColumn<UsuarioModel, String> colRol;

    @FXML
    private TableColumn<UsuarioModel, String> colUsername;
    @FXML
    private TableColumn<UsuarioModel, Void> colAcciones;

    @FXML
    private Label lblErrorForm;

    @FXML
    private Label lblTituloForm;

    @FXML
    private VBox panelFormulario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtBuscar;

    //boton volver
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
    //instancias
    private final UsuarioController controller = new UsuarioController();
    private final ObservableList<UsuarioModel> lista = FXCollections.observableArrayList(); //sirve para actualizar atumaticamente una tabla
    private int idEditar = -1; //en la db se empieza en 1 se empieza en -1 ya que ningun usuario puede ser -1

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //asignacion de la cosita esa que daba opciones jajaja XD
        cmbRol.setItems(FXCollections.observableArrayList("ADMIN", "VENDEDOR"));

        //decirle a la tabla que datos mostrar del model
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colEstado.setCellFactory(col -> new TableCell<>() {

            //funcion para cambiar los colorcitos dependiendo si esta activo o no, primero revisa si estaempy
            //ya sobrescribe updateiTem que es de la tabla
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

        // columna acciones con botones Editar e suspender/Activar
        colAcciones.setCellFactory(col -> new TableCell<>() {
            //Creamops los botones
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button();
            private final HBox contenedor = new HBox(6, btnEditar, btnEstado);

            {

                //aqui aplicamos los css de la clase que tengo Style
                btnEditar.getStyleClass().add("boton-editar");
                btnEstado.getStyleClass().add("boton-eliminar");
                contenedor.setAlignment(Pos.CENTER);
                //para que esten alineados los agregue
                btnEditar.setPrefWidth(70);
                btnEstado.setPrefWidth(85);

                btnEditar.setOnAction(e -> {
                    UsuarioModel usuario = getTableView().getItems().get(getIndex());
                    Editar(usuario);
                });

                btnEstado.setOnAction(e -> {
                    UsuarioModel usuario = getTableView().getItems().get(getIndex());
                    cambiarEstado(usuario);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UsuarioModel usuario = getTableView().getItems().get(getIndex());
                    setAlignment(Pos.CENTER);
                    if (usuario.isEstado()) {
                        btnEstado.setText("Suspender");
                        btnEstado.getStyleClass().removeAll("boton-insertar");
                        btnEstado.getStyleClass().add("boton-eliminar");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.getStyleClass().removeAll("boton-eliminar");
                        btnEstado.getStyleClass().add("boton-insertar");
                    }
                    setGraphic(contenedor);
                }
            }
        });

        //cargar tabla al abrir
        cargarTabla();
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            buscarEnTabla(newVal);
        });
    }

    //funcion para buscar
    private void buscarEnTabla(String texto) {
        //si esta vacio mostramos todo
        if (texto == null || texto.trim().isEmpty()) {
            tablaUsuarios.setItems(lista);
            tablaUsuarios.refresh(); //sin esto cada que buscamos empiezan a desaparecer los bontes
            return;
        }

        String filtro = texto.toLowerCase().trim();
        ObservableList<UsuarioModel> filtrados = FXCollections.observableArrayList();

        //recorremos la lista original y guardamos los que coincidan
        for (UsuarioModel u : lista) {
            if (u.getUsername().toLowerCase().contains(filtro)) {
                filtrados.add(u);
            }
        }

        tablaUsuarios.setItems(filtrados);
        tablaUsuarios.refresh();
    }

    //funcion para cargar la tabla
    private void cargarTabla() {
        lista.clear();
        List<UsuarioModel> datos = controller.listarUsuarios();
        lista.addAll(datos);
        tablaUsuarios.setItems(lista);
    }

    @FXML
    private void abrirFormulario() {
        idEditar = -1;
        lblTituloForm.setText("Nuevo Usuario");
        limpiarFormulario();
        mostrarPanel(true);
    }

    private void Editar(UsuarioModel usuario) {
        idEditar = usuario.getId();
        lblTituloForm.setText("Editar Usuario");
        txtUsername.setText(usuario.getUsername());
        txtPassword.clear(); //por seguridad no se muestra el hash
        cmbRol.setValue(usuario.getRol());
        chkEstado.setSelected(usuario.isEstado());
        lblErrorForm.setText("");
        mostrarPanel(true);
    }
    //nuevo cambiar estado con sp-- es igual al que ya tenia 

    private void cambiarEstado(UsuarioModel usuario) {
        String accion = usuario.isEstado() ? "inactivar" : "activar";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Desea " + accion + " al usuario " + usuario.getUsername() + "?");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                boolean confirmacion = controller.cambiarEstadoUsuario(usuario.getId(), !usuario.isEstado());
                if (confirmacion) {
                    cargarTabla();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo cambiar el estado. No se puede quedar sin administradores");
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

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String rol = cmbRol.getValue();
        boolean estado = chkEstado.isSelected();

        // validaciones
        //si esta vacion
        if (username.isEmpty()) {
            lblErrorForm.setText("El username es obligatorio.");
            txtUsername.requestFocus();
            return;
        }
        //si esta muy corto
        if (username.length() < 3) {
            lblErrorForm.setText("El usuario debe tener al menos 3 caracteres.");
            txtUsername.requestFocus();
            return;
        }
        // si no selecciona alguno de los roles, esta es la del cmb
        if (rol == null) {
            lblErrorForm.setText("Seleccione un rol.");
            return;
        }

        try {
            //no estamos editando
            if (idEditar == -1) {
                // INSERTAR — password obligatoria
                if (password.isEmpty()) {
                    lblErrorForm.setText("Ingrese la contraseña ");
                    txtPassword.requestFocus();
                    return;
                }

                //nuestra variable agarra lo que arroje nuestro controlador true si lo resgistro o false sino
                boolean correcto = controller.insertarUsuario(username, password, rol);
                if (correcto) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario registrado correctamente.");
                    cerrarFormulario();
                    cargarTabla();
                } else {
                    lblErrorForm.setText("No se pudo registrar. Verifique los datos.");
                }
            } else {
                // ACTUALIZAR 
                //si deja password vacía se mantiene la anterior
                if (password.isEmpty()) {
                    // traemos el hash actual para no pisarlo
                    UsuarioModel actual = controller.listarUsuarios()
                            .stream()
                            .filter(u -> u.getId() == idEditar)
                            .findFirst()
                            .orElse(null);
                    if (actual == null) {
                        lblErrorForm.setText("Usuario no encontrado.");
                        return;
                    }
                    password = actual.getPassword(); // mantiene el hash existente
                }
                boolean ok = controller.actualizarUsuario(idEditar, username, password, rol, estado);
                if (ok) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario actualizado correctamente.");
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

    //esta parte del codigo ayuda al formulario
    //para mostrar el formulario
    private void mostrarPanel(boolean mostrar) {
        panelFormulario.setVisible(mostrar);
        panelFormulario.setManaged(mostrar);
    }

    //dejar el formulario por defectp
    private void limpiarFormulario() {
        txtUsername.clear();
        txtPassword.clear();
        cmbRol.setValue(null);
        //por defecto se va a quedar en true
        chkEstado.setSelected(true);
        lblErrorForm.setText("");
        //lo que explque no estamos editando
        idEditar = -1;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        //crear la alerta(esto estan los comentarios en el login)
        Alert alert = new Alert(tipo);
        alert.setTitle("Usuarios");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
