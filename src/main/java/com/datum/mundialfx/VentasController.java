package com.datum.mundialfx;

import com.mundial.app.model.ClienteModel;
import com.mundial.app.util.ClienteSeleccionado;
import com.mundial.app.util.Sesion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class VentasController implements Initializable {

    @FXML
    private Label lblClienteSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //verifica si hay cliente seleccionado
        if (ClienteSeleccionado.hayCliente()) {
            ClienteModel c = ClienteSeleccionado.getCliente();
            lblClienteSeleccionado.setText("Cliente: " + c.getNombre() + " " + c.getApellido());
        } else {
            lblClienteSeleccionado.setText("No hay cliente seleccionado");
        }
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
}
