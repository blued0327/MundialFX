/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.controller;

import com.mundial.app.dao.VentasDao;
import com.mundial.app.model.VentasModel;
import java.util.List;
/**
 *
 * @author rchar
 */
public class VentasController {
    
    private final VentasDao dao = new VentasDao();
 
    // calcular totales
    public VentasModel calcularTotales(int clienteId, int usuarioId,
                                       List<Integer> ticketIds,
                                       List<Double> precios) {
        return dao.calcularTotales(clienteId, usuarioId, ticketIds, precios);
    }
 
    // Rregistrar ventas
    public int registrarVenta(VentasModel venta) {
        try {
            return dao.registrarVenta(venta);
        } catch (Exception e) {
            System.err.println("Error al registrar venta " + e.getMessage());
            return -1;
        }
    }
 
    // anular ventas
    public boolean anularVenta(int ventaId, int usuarioId, String motivo) {
        try {
            return dao.anularVenta(ventaId, usuarioId, motivo);
        } catch (Exception e) {
            System.err.println("Error al anular venta " + e.getMessage());
            return false;
        }
    }
}
