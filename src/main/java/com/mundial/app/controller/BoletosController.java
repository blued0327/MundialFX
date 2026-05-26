/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.controller;

import com.mundial.app.model.TicketModel;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rchar
 */
public class BoletosController {

    private final com.mundial.app.dao.BoletosDao dao = new com.mundial.app.dao.BoletosDao();

    // Genera un ticket individual
    public boolean generarTicket(int partidoId, String numeroAsiento, String seccion, double precio) {
        try {
            TicketModel t = new TicketModel();
            t.setPartidoId(partidoId);
            t.setNumeroAsiento(numeroAsiento);
            t.setSeccion(seccion);
            t.setPrecio(BigDecimal.valueOf(precio));
            return dao.generarTicket(t);
        } catch (SQLException e) {
            System.err.println("Error al generar ticket " + e.getMessage());
            return false;
        }
    }

    //genera todos los tickets de un partido de un solo, devuelve cuantos se crearon
    public int generarMasivo(int partidoId, int cantVip, double precioVip,
            int cantPref, double precioPref,
            int cantGen, double precioGen) {
        try {
            return dao.generarMasivo(
                    partidoId,
                    cantVip, BigDecimal.valueOf(precioVip),
                    cantPref, BigDecimal.valueOf(precioPref),
                    cantGen, BigDecimal.valueOf(precioGen)
            );
        } catch (SQLException e) {
            System.err.println("Error al generar tickets masivos " + e.getMessage());
            return 0;
        }
    }

    //todos los tickets de un partido sin importar el estado
    public List<TicketModel> consultarPorPartido(int partidoId) {
        try {
            return dao.consultarPorPartido(partidoId);
        } catch (SQLException e) {
            System.err.println("Error al consultar tickets del partido " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TicketModel> consultarDisponibles(int partidoId) {
        try {
            return dao.consultarDisponibles(partidoId);
        } catch (SQLException e) {
            System.err.println("Error al consultar " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TicketModel> consultarDisponiblesPorSeccion(int partidoId, String seccion) {
        try {
            return dao.consultarDisponiblesPorSeccion(partidoId, seccion);
        } catch (SQLException e) {
            System.err.println("Error al consultar secciones " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public TicketModel consultarPorId(int idTicket) {
        try {
            return dao.consultarPorId(idTicket);
        } catch (SQLException e) {
            System.err.println("Error al consultar ticket " + e.getMessage());
            return null;
        }
    }

    public boolean actualizarPrecio(int idTicket, double nuevoPrecio) {
        try {
            return dao.actualizarPrecio(idTicket, nuevoPrecio);
        } catch (SQLException e) {
            System.err.println("Error al actualizar precio " + e.getMessage());
            return false;
        }
    }

    //Asignar un asiento de disponible a reservado
    public TicketModel asignarAsiento(int partidoId, String numeroAsiento) {
        try {
            return dao.asignarAsiento(partidoId, numeroAsiento);
        } catch (SQLException e) {
            System.err.println("Error al asignar asiento: " + e.getMessage());
            return null;
        }
    }

    public boolean eliminarTicket(int idTicket) {
        try {
            return dao.eliminarTicket(idTicket);
        } catch (SQLException e) {
            System.err.println("Error al eliminar ticket: " + e.getMessage());
            return false;
        }
    }
}
