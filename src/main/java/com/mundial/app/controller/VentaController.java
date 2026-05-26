/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.controller;

import com.mundial.app.dao.VentasDao;
import com.mundial.app.model.VentasModel;
import com.mundial.app.model.ReciboModel;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import com.mundial.app.connection.CreateConnection;

/**
 *
 * @author rchar
 */
public class VentaController {
    
    private final VentasDao dao = new VentasDao();

    public VentasModel calcularTotales(int clienteId, int usuarioId,
            List<Integer> ticketIds,
            List<Double> precios) {
        return dao.calcularTotales(clienteId, usuarioId, ticketIds, precios);
    }

    public int registrarVenta(VentasModel venta) {
        try {
            return dao.registrarVenta(venta);
        } catch (Exception e) {
            System.err.println("Error al registrar venta " + e.getMessage());
            return -1;
        }
    }

    public boolean anularVenta(int ventaId, int usuarioId, String motivo) {
        try {
            return dao.anularVenta(ventaId, usuarioId, motivo);
        } catch (Exception e) {
            System.err.println("Error al anular venta " + e.getMessage());
            return false;
        }
    }

    public List<ReciboModel> obtenerRecibo(int ventaId) {
        try {
            return dao.obtenerRecibo(ventaId);
        } catch (Exception e) {
            System.err.println("Error al obtener recibo: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<VentasModel> listarTodas() {

        try {
            return dao.listarTodas();
        } catch (Exception e) {

            System.err.println(
                    "Error listando ventas: "
                    + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    public List<VentasModel> buscarPorFactura(String numeroFactura) {

        try {
            return dao.buscarPorFactura(numeroFactura);

        } catch (Exception e) {

            System.err.println(
                    "Error buscando factura: "
                    + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

   public Connection getConexion() {
    return CreateConnection.getInstancia().getConnection();
}
}
