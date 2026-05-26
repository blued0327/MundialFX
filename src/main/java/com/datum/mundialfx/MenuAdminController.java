package com.datum.mundialfx;

import com.mundial.app.controller.UsuarioController;
import com.mundial.app.util.Sesion;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuAdminController implements Initializable {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (Sesion.getUsuario() != null) {
            lblBienvenida.setText("Bienvenido, " + Sesion.getUsuario().getUsername());
            lblRol.setText("Rol: " + Sesion.getUsuario().getRol());
        }
    }

    //   ventas
    @FXML
    private void irVentas() {
        try {
            App.setRoot("Ventas");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // clientes
    @FXML
    private void irCliente() {
        //System.out.println("Ir clientes");
        try {
            App.setRoot("Clientes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // partidos
    @FXML
    private void irPartido() {
        System.out.println("Ir partidos");
        try {
            App.setRoot("Partidos");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tickets
    @FXML
    private void irTicket() {
        System.out.println("Ir tickets");
    }

    // reportes
    @FXML
    private void irReporte() {
        System.out.println("Ir reportes");
    }

    // usuarios
    @FXML
    private void irUsuario() {
        //System.out.println("Ir usuarios");
        try {
            App.setRoot("Usuarios");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irLog() {
        try {
            App.setRoot("Log");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // cerrar sesion -- con quetambien guarde el logiut de la ip
    @FXML
    private void cerrarSesion() {

        //ventana para confirmar si si quiere salir
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Cerrar sesion");
        alert.setHeaderText(null);
        alert.setContentText("¿Desea cerrar sesion?");

        //espera la respuesta del usuario
        alert.showAndWait().ifPresent(respuesta -> {

            //si le dio ok entonces sigue
            if (respuesta == ButtonType.OK) {

                //guardar el logout en logs antes de destruir la sesion
                UsuarioController controller = new UsuarioController();

                //agrega el log de salida
                controller.logout(
                        Sesion.getUsuario().getId(),
                        Sesion.obtenerIp()
                );

                //cerrar la sesion global
                Sesion.cerrar();

                try {

                    //regresar al login
                    App.setRoot("Login");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
