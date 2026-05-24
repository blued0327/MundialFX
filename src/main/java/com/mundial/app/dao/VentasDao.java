// MIGRACION: package cambiado de "dao" a "com.mundial.app.dao"
package com.mundial.app.dao;


import com.mundial.app.connection.CreateConnection;
import com.mundial.app.model.VentasModel;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;


public class VentasDao {
    
    private final Connection connection;
 
    
    public static final double IVA           = 0.12;
    public static final int    MINIMO_DESC_5 = 5;    // más de 5 boletos 5%
    public static final int    MINIMO_DESC_7 = 10;   // 10 boletos 7%
    public static final double DESCUENTO_5   = 0.05;
    public static final double DESCUENTO_7   = 0.07;
 
    public VentasDao() {
        this.connection = CreateConnection.getInstancia().getConnection();
    }
 
    
    public int registrarVenta(VentasModel venta) throws SQLException {
        String sql = "SELECT sp_venta_registrar(?, ?, ?, ?, ?, ?, ?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, venta.getClienteId());
            ps.setInt(2, venta.getUsuarioId());
 
            Integer[] ids = venta.getTicketIds().toArray(new Integer[0]);
            Array pgArray = connection.createArrayOf("integer", ids);
            ps.setArray(3, pgArray);
 
            ps.setBigDecimal(4, venta.getSubtotal());
            ps.setBigDecimal(5, venta.getDescuento());
            ps.setBigDecimal(6, venta.getTotalIva());
            ps.setBigDecimal(7, venta.getTotal());
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ventaId = rs.getInt(1);
                    venta.setId(ventaId);
                    return ventaId;
                }
            }
        }
        return -1;
    }
 
     public boolean anularVenta(int ventaId, int usuarioId, String motivo) throws SQLException {
        String sql = "SELECT sp_venta_anular(?, ?, ?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            ps.setInt(2, usuarioId);
            ps.setString(3, motivo);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        }
        return false;
    
    }
 
    public VentasModel calcularTotales(int clienteId, int usuarioId,
                                      List<Integer> ticketIds,
                                      List<Double> precios) {
        int cantidad = precios.size();
 
        // Subtotal sin descuento ni IVA
        double subtotal = precios.stream().mapToDouble(Double::doubleValue).sum();
 
        // Descuento
         double pctDescuento = 0;
        if (cantidad >= MINIMO_DESC_7) {
            pctDescuento = DESCUENTO_7;
        } else if (cantidad > MINIMO_DESC_5) {
            pctDescuento = DESCUENTO_5;
        }
        double descuento = subtotal * pctDescuento;
 
        // Base gravable e IVA
        double baseGravable = subtotal - descuento;
        double totalIva     = baseGravable * IVA;
        double total        = baseGravable + totalIva;
 
        return new VentasModel(
            clienteId,
            usuarioId,
            BigDecimal.valueOf(subtotal),
            BigDecimal.valueOf(descuento),
            BigDecimal.valueOf(totalIva),
            BigDecimal.valueOf(total),
            ticketIds
        );
    }
}
