/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.dao;

import com.mundial.app.connection.CreateConnection;
import com.mundial.app.model.TicketModel;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rchar
 */
public class BoletosDao {

    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_RESERVADO = "RESERVADO";
    public static final String ESTADO_VENDIDO = "VENDIDO";

    public static final String SECCION_VIP = "VIP";
    public static final String SECCION_PREFERENCIAL = "PREFERENCIAL";
    public static final String SECCION_GENERAL = "GENERAL";

    public boolean generarTicket(TicketModel ticket) throws SQLException {
        String sql = "SELECT sp_ticket_insertar(?, ?, ?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticket.getPartidoId());
            ps.setString(2, ticket.getNumeroAsiento());
            ps.setString(3, ticket.getSeccion());
            ps.setDouble(4, ticket.getPrecio().doubleValue());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    //sp que genera todos los tickets de un partido segun la cantidad por seccion
    public int generarMasivo(int partidoId, int cantVip, BigDecimal precioVip,
            int cantPref, BigDecimal precioPref,
            int cantGen, BigDecimal precioGen) throws SQLException {
        String sql = "SELECT sp_ticket_generar_masivo(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partidoId);
            ps.setInt(2, cantVip);
            ps.setBigDecimal(3, precioVip);
            ps.setInt(4, cantPref);
            ps.setBigDecimal(5, precioPref);
            ps.setInt(6, cantGen);
            ps.setBigDecimal(7, precioGen);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean actualizarPrecio(int idTicket, double nuevoPrecio) throws SQLException {
        String sql = "SELECT sp_ticket_actualizar_precio(?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTicket);
            ps.setDouble(2, nuevoPrecio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    public boolean eliminarTicket(int idTicket) throws SQLException {
        String sql = "SELECT sp_ticket_eliminar(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTicket);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    //elimina todos los tickets DISPONIBLES y RESERVADOS de un partido en un solo viaje a BD
    //devuelve la cantidad eliminada -- 
    // los tickets VENDIDOS no se tocan, eso se valida en el SP
    public int eliminarPorPartido(int partidoId) throws SQLException {
        String sql = "SELECT sp_ticket_eliminar_por_partido(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partidoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public TicketModel consultarPorId(int idTicket) throws SQLException {
        String sql = "SELECT * FROM sp_ticket_por_id(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTicket);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
            }
        }
        return null;
    }

    //consultar todos los tickets de un partido sin importar el estado
    public List<TicketModel> consultarPorPartido(int partidoId) throws SQLException {
        String sql = "SELECT * FROM sp_ticket_por_partido(?)";
        List<TicketModel> lista = new ArrayList<>();

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partidoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TicketModel t = new TicketModel();
                    t.setId(rs.getInt("id"));
                    t.setPartidoId(partidoId);
                    t.setNumeroAsiento(rs.getString("numero_asiento"));
                    t.setSeccion(rs.getString("seccion"));
                    t.setPrecio(BigDecimal.valueOf(rs.getDouble("precio")));
                    t.setEstado(rs.getString("estado"));
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    public List<TicketModel> consultarDisponibles(int partidoId) throws SQLException {
        return consultarDisponiblesPorSeccion(partidoId, null);
    }

    public List<TicketModel> consultarDisponiblesPorSeccion(int partidoId, String seccion) throws SQLException {
        String sql = "SELECT * FROM sp_ticket_disponibles(?, ?)";
        List<TicketModel> disponibles = new ArrayList<>();

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partidoId);
            if (seccion == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, seccion);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disponibles.add(mapearResultadoDisponible(rs, partidoId));
                }
            }
        }
        return disponibles;
    }

    //reservar un asiento especifico, lo deja en estado RESERVADO
    public TicketModel asignarAsiento(int partidoId, String numeroAsiento) throws SQLException {
        String sql = "SELECT * FROM sp_ticket_reservar(?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partidoId);
            ps.setString(2, numeroAsiento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
            }
        }
        return null;
    }

    //coso de mapeo para devolver id, partido_id, numero_asiento, seccion, precio, estado
    private TicketModel mapearResultado(ResultSet rs) throws SQLException {
        TicketModel t = new TicketModel();
        t.setId(rs.getInt("id"));
        t.setPartidoId(rs.getInt("partido_id"));
        t.setNumeroAsiento(rs.getString("numero_asiento"));
        t.setSeccion(rs.getString("seccion"));
        t.setPrecio(BigDecimal.valueOf(rs.getDouble("precio")));
        t.setEstado(rs.getString("estado"));
        return t;
    }

    private TicketModel mapearResultadoDisponible(ResultSet rs, int partidoId) throws SQLException {
        TicketModel t = new TicketModel();
        t.setId(rs.getInt("id"));
        t.setPartidoId(partidoId);
        t.setNumeroAsiento(rs.getString("numero_asiento"));
        t.setSeccion(rs.getString("seccion"));
        t.setPrecio(BigDecimal.valueOf(rs.getDouble("precio")));
        t.setEstado(ESTADO_DISPONIBLE);
        return t;
    }
}
