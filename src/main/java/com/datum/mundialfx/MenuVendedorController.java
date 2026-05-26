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
import com.mundial.app.controller.UsuarioController;
import com.mundial.app.util.IpUtil;

public class MenuVendedorController {

    private static UsuarioController controller = new UsuarioController();

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
        try {
            App.setRoot("Tickets");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //reportes -- abre la carpeta facturas del proyecto donde se guardan los reportes
    @FXML
    private void irReporte() {
        try {

            //ruta a la carpeta facturas dentro del proyecto MundialFX
            //user.dir = directorio raiz desde donde se ejecuta la app
            java.io.File carpeta = new java.io.File(
                    System.getProperty("user.dir") + "/facturas"
            );

            //si no existe la creamos para que no tire error la primera vez
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            //abre el explorador de windows en esa carpeta
            java.awt.Desktop.getDesktop().open(carpeta);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Reportes");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la carpeta de reportes.");
            alert.showAndWait();
        }
    }

    // cerrar sesion
    @FXML

//cerrar sesion del usuario actual
    private void cerrarSesion() {

        //alerta de confirmacion para evitar cerrar por accidente
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Cerrar sesion");
        alert.setHeaderText(null);
        alert.setContentText("¿Desea cerrar sesion?");

        //espera la respuesta del usuario
        alert.showAndWait().ifPresent(respuesta -> {

            //si presiono OK entonces cierra sesion
            if (respuesta == ButtonType.OK) {

                //guardar el logout en la tabla log_usuario
                //agarra el id del usuario que esta logueado en memoria
                controller.logout(
                        Sesion.getUsuario().getId(), IpUtil.obtenerIp()
                );

                //limpia la sesion actual
                //basicamente deja el usuario en null
                Sesion.cerrar();

                try {

                    //regresa otra vez al login
                    App.setRoot("Login");

                } catch (Exception e) {

                    //si algo explota lo imprime en consola
                    e.printStackTrace();
                }
            }
        });
    }
}
