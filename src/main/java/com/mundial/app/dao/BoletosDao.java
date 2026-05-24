/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
// MIGRACION: package cambiado de "dao" a "com.mundial.app.dao"
package com.mundial.app.dao;
 
import com.mundial.app.connection.CreateConnection;
// MIGRACION: model cambiado de BoletosModel a TicketModel
import com.mundial.app.model.TicketModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 *
 * @author rchar
 */
public class BoletosDao {
 
    private final Connection connection;
 
    public static final String ESTADO_DISPONIBLE  = "DISPONIBLE";
    public static final String ESTADO_RESERVADO   = "RESERVADO";
    public static final String ESTADO_VENDIDO     = "VENDIDO";
 
    public static final String SECCION_VIP          = "VIP";
    public static final String SECCION_PREFERENCIAL = "PREFERENCIAL";
    public static final String SECCION_GENERAL      = "GENERAL";
 
    public BoletosDao() {
        this.connection = CreateConnection.getInstancia().getConnection();
    }
 
    public boolean generarTicket(TicketModel ticket) throws SQLException {
        String sql = "SELECT sp_ticket_insertar(?, ?, ?, ?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
 
    public int generarTicketsEnLote(List<TicketModel> tickets) throws SQLException {
        String sql = "SELECT sp_ticket_insertar(?, ?, ?, ?)";
 
        int insertados = 0;
        connection.setAutoCommit(false);
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (TicketModel t : tickets) {
                ps.setInt(1, t.getPartidoId());
                ps.setString(2, t.getNumeroAsiento());
                ps.setString(3, t.getSeccion());
                ps.setDouble(4, t.getPrecio().doubleValue());
 
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        t.setId(rs.getInt(1));
                        insertados++;
                    }
                }
            }
            connection.commit();
 
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
 
        return insertados;
    }
 
    public boolean actualizarPrecio(int idTicket, double nuevoPrecio) throws SQLException {
        String sql = "SELECT sp_ticket_actualizar_precio(?, ?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idTicket);
            ps.setDouble(2, nuevoPrecio);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        }
        return false;
    }
 
    public boolean eliminarTicket(int idTicket) throws SQLException {
        String sql = "SELECT sp_ticket_eliminar(?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idTicket);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        }
        return false;
    }
 
    public TicketModel consultarPorId(int idTicket) throws SQLException {
        String sql = "SELECT * FROM sp_ticket_por_id(?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idTicket);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearResultado(rs);
            }
        }
        return null;
    }
 
    public List<TicketModel> consultarDisponibles(int partidoId) throws SQLException {
        return consultarDisponiblesPorSeccion(partidoId, null);
    }
 
    public List<TicketModel> consultarDisponiblesPorSeccion(int partidoId,
                                                             String seccion) throws SQLException {
        String sql = "SELECT * FROM sp_ticket_disponibles(?, ?)";
 
        List<TicketModel> disponibles = new ArrayList<>();
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
 
 
    public TicketModel asignarAsiento(int partidoId, String numeroAsiento) throws SQLException {
    String sql = "SELECT * FROM sp_ticket_reservar(?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setInt(1, partidoId);
        ps.setString(2, numeroAsiento);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapearResultado(rs);
        }
    }
    return null;
}
 
    public boolean confirmarVenta(int idTicket) throws SQLException {
        String sql = "UPDATE ticket SET estado = ? WHERE id = ? AND estado = ?";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ESTADO_VENDIDO);
            ps.setInt(2, idTicket);
            ps.setString(3, ESTADO_RESERVADO);
            return ps.executeUpdate() > 0;
        }
    }
 
    public boolean liberarAsiento(int idTicket) throws SQLException {
        String sql = "UPDATE ticket SET estado = ? WHERE id = ? AND estado = ?";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ESTADO_DISPONIBLE);
            ps.setInt(2, idTicket);
            ps.setString(3, ESTADO_RESERVADO);
            return ps.executeUpdate() > 0;
        }
    }
 // coso de mapeo para devolver id, partido_id, numero_asiento, seccion, precio, estado
    private TicketModel mapearResultado(ResultSet rs) throws SQLException {
        TicketModel t = new TicketModel();
        t.setId(rs.getInt("id"));
        t.setPartidoId(rs.getInt("partido_id"));
        t.setNumeroAsiento(rs.getString("numero_asiento"));
        t.setSeccion(rs.getString("seccion"));
        t.setPrecio(java.math.BigDecimal.valueOf(rs.getDouble("precio")));
        t.setEstado(rs.getString("estado"));
        return t;
    }
 
    private TicketModel mapearResultadoDisponible(ResultSet rs, int partidoId) throws SQLException {
        TicketModel t = new TicketModel();
        t.setId(rs.getInt("id"));
        t.setPartidoId(partidoId);
        t.setNumeroAsiento(rs.getString("numero_asiento"));
        t.setSeccion(rs.getString("seccion"));
        t.setPrecio(java.math.BigDecimal.valueOf(rs.getDouble("precio")));
        t.setEstado(ESTADO_DISPONIBLE);
        return t;
    }
}
 