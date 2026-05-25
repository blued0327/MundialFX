package com.mundial.app.util;

import com.mundial.app.model.ClienteModel;





//funciona igual que Sesion pero para guardar el cliente que el vendedor eligio para la venta
public class ClienteSeleccionado {

    private static ClienteModel clienteActual;

    //cuando el vendedor le da click en seleccionar
    public static void seleccionar(ClienteModel cliente) {
        clienteActual = cliente;
    }

    //para cuando se cancela o ya termino la venta
    public static void limpiar() {
        clienteActual = null;
    }

    public static ClienteModel getCliente() {
        return clienteActual;
    }

    //para saber si ya hay uno cargado antes de mandar a vender
    public static boolean hayCliente() {
        return clienteActual != null;
    }
}
