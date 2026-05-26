package com.datum.mundialfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;

//
import com.mundial.app.controller.UsuarioController;
import com.mundial.app.model.UsuarioModel;
import com.mundial.app.util.Sesion;
import javafx.scene.control.Alert;

import com.mundial.app.util.IpUtil;

/**
 * FXML Controller class
 *
 * @author jdmm0
 */
public class LoginController {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnIngresar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblError;
    private final UsuarioController controller = new UsuarioController();

    @FXML

    //abrir menu
    private void abrirMenu() {
        try {
            if (Sesion.esAdmin()) {
                App.setRoot("MenuAdmin"); //appp es el cambia la vista
            } else {
                App.setRoot("MenuVendedor");
            }
        } catch (Exception e) {
            lblError.setText("Error al abrir el Menu");
            e.printStackTrace();
        }
    }

    //ingresar
    @FXML // QUE NO SE ME OLVIDE PONERLE ESTO
    private void ingresar() {
        //sirve para limpiar
        lblError.setText("");
        //capturar info ---- trim quita espacios al inicio y al final
        String user = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        //validaciones
        if (user.isEmpty()) {
            lblError.setText("Ingrese un usuario");
            //pone el mouse en el campo(en este caso usuario)
            txtUsuario.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            lblError.setText("Ingrese la contrasña");
            txtPassword.requestFocus();
            return;
        }
        if (user.length() < 3) {
            lblError.setText("usuario muy corto");
            txtUsuario.requestFocus();
            return;
        }

        //parte donde si encuentra --primero recibe los datos el model ------- aqui se a;ade la ip 
        UsuarioModel usuario = controller.login(user, password, IpUtil.obtenerIp());

  
  
        if (usuario != null) {
            if (!usuario.isEstado()) {
                //verifica que el usuario esta en false o true
                lblError.setText("Usuario inactivo");
                return;
            }
            //aqui se guarda en el metodo iniciar de la clase session y asi esta en todo el programa globalmente
            Sesion.iniciar(usuario);                    //aqui traera admin o user de la db osea que soy
            mostrarMensaje("Bienvenido,  " + usuario.getUsername() + "!");
            //menu principal
            abrirMenu();

        } else {
            lblError.setText("Usuario o contraseña incorrectos");
            txtPassword.clear();//limpia contraseña
            txtPassword.requestFocus();
        }

    }

    //cancelar
    @FXML
    private void cancelar() {
        //System.out.println("Click en cancelar");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); //esto es para confirmacion con ok o cancel 
        alert.setTitle("Confirmar");    //titulo de la ventana
        alert.setHeaderText(null);      //para que el header no sea grande
        alert.setContentText("¿Desea salir del sistema?"); //lo que va a dercir 
        alert.showAndWait().ifPresent(confirmacion -> {  // si el usuario presenta(hace algo) hace la confirmacion va a hacer al boton ok
            if (confirmacion == ButtonType.OK) {
                Platform.exit();  //cierra
            }
        });
    }

    //mensaje exitoso
    @FXML
    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acceso exitoso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait(); //abre un pop-up creo que asi se escribe xD

    }

}
