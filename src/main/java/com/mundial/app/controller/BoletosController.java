/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.controller;

import com.mundial.app.dao.BoletosDao;
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
    private final BoletosDao dao = new BoletosDao();
 
    // Genera un ticket
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
 

    public int generarTicketsEnLote(int partidoId, List<String> asientos, String seccion, double precio) {
        try {
            List<TicketModel> lista = new ArrayList<>();
            for (String asiento : asientos) {
                TicketModel t = new TicketModel();
                t.setPartidoId(partidoId);
                t.setNumeroAsiento(asiento);
                t.setSeccion(seccion);
                t.setPrecio(BigDecimal.valueOf(precio));
                lista.add(t);
            }
            return dao.generarTicketsEnLote(lista);
        } catch (SQLException e) {
            System.err.println("Error al generar tickets " + e.getMessage());
            return 0;
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
 
    //Cambia el estado de un boleto de reservado a vendido
    public boolean confirmarVenta(int idTicket) {
        try {
            return dao.confirmarVenta(idTicket);
        } catch (SQLException e) {
            System.err.println("Error al confirmar venta: " + e.getMessage());
            return false;
        }
    }
 
    //Libera un asiento pasa de reservado a disponible
    public boolean liberarAsiento(int idTicket) {
        try {
            return dao.liberarAsiento(idTicket);
        } catch (SQLException e) {
            System.err.println("Error al liberar asiento: " + e.getMessage());
            return false;
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

