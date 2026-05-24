package com.datum.mundialfx;

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
        System.out.println("Ir ventas");
    }

    // clientes
    @FXML
    private void irCliente() {
        System.out.println("Ir clientes");
    }

    // partidos
    @FXML
    private void irPartido() {
        System.out.println("Ir partidos");
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
        System.out.println("Ir usuarios");
    }

    // cerrar sesion
    @FXML
    
    private void cerrarSesion() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Cerrar sesion");
        alert.setHeaderText(null);
        alert.setContentText("¿Desea cerrar sesion?");

        alert.showAndWait().ifPresent(respuesta -> {

            if (respuesta == ButtonType.OK) {
                Sesion.cerrar();
                try {
                    App.setRoot("Login");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
