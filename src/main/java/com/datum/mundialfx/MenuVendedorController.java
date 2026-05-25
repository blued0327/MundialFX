package com.datum.mundialfx;

import com.mundial.app.util.Sesion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class MenuVendedorController {

    private Label lblBienvenida;

    @FXML
    private Label lblRol;

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
