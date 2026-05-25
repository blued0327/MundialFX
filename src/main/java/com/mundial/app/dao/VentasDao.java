package com.mundial.app.dao;

import com.mundial.app.connection.CreateConnection;
import com.mundial.app.model.VentasModel;
import com.mundial.app.model.ReciboModel;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class VentasDao {

    public static final double IVA = 0.12;
    public static final int MINIMO_DESC_5 = 5;    // mas de 5 boletos 5%
    public static final int MINIMO_DESC_7 = 10;   // 10 boletos 7%
    public static final double DESCUENTO_5 = 0.05;
    public static final double DESCUENTO_7 = 0.07;

    public int registrarVenta(VentasModel venta) throws SQLException {
        String sql = "SELECT sp_venta_registrar(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, venta.getClienteId());
            ps.setInt(2, venta.getUsuarioId());

            //convertimos la lista de ids a un array que entiende postgresql
            Integer[] ids = venta.getTicketIds().toArray(new Integer[0]);
            Array pgArray = conn.createArrayOf("integer", ids);
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

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ventaId);
            ps.setInt(2, usuarioId);
            ps.setString(3, motivo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    //esta no toca BD, solo calculos, asi que no necesita conexion
    public VentasModel calcularTotales(int clienteId, int usuarioId,
            List<Integer> ticketIds,
            List<Double> precios) {
        int cantidad = precios.size();

        //subtotal sin descuento ni iva
        double subtotal = precios.stream().mapToDouble(Double::doubleValue).sum();

        //descuento por cantidad
        double pctDescuento = 0;
        if (cantidad >= MINIMO_DESC_7) {
            pctDescuento = DESCUENTO_7;
        } else if (cantidad > MINIMO_DESC_5) {
            pctDescuento = DESCUENTO_5;
        }
        double descuento = subtotal * pctDescuento;

        //base gravable e iva
        double baseGravable = subtotal - descuento;
        double totalIva = baseGravable * IVA;
        double total = baseGravable + totalIva;

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

    public List<ReciboModel> obtenerRecibo(int ventaId) throws SQLException {
        String sql = "SELECT * FROM sp_venta_obtener_recibo(?)";
        List<ReciboModel> filas = new ArrayList<>();

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(mapearRecibo(rs));
                }
            }
        }
        return filas;
    }

    //listar todas las ventas para el historial
    public List<VentasModel> listarTodas() throws SQLException {
        List<VentasModel> lista = new ArrayList<>();
        String sql = "SELECT * FROM sp_venta_listar_todas()";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(cargarVentaResumen(rs));
            }
        }
        return lista;
    }

    //buscar ventas por numero de factura, busqueda parcial
    public List<VentasModel> buscarPorFactura(String numeroFactura) throws SQLException {
        List<VentasModel> lista = new ArrayList<>();
        String sql = "SELECT * FROM sp_venta_buscar_factura(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numeroFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(cargarVentaResumen(rs));
                }
            }
        }
        return lista;
    }

    //carga el recibo completo con todos los joins que hace el sp
    private ReciboModel mapearRecibo(ResultSet rs) throws SQLException {
        ReciboModel recibo = new ReciboModel();

        recibo.setReciboNro(rs.getInt("recibo_nro"));
        recibo.setNumeroFactura(rs.getString("numero_factura"));
        recibo.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        recibo.setClienteNombre(rs.getString("cliente_nombre"));
        recibo.setClienteEmail(rs.getString("cliente_email"));
        recibo.setVendedor(rs.getString("vendedor"));
        recibo.setPartido(rs.getString("partido"));
        recibo.setFechaPartido(rs.getTimestamp("fecha_partido").toLocalDateTime());
        recibo.setEstadio(rs.getString("estadio"));
        recibo.setNumeroAsiento(rs.getString("numero_asiento"));
        recibo.setSeccion(rs.getString("seccion"));
        recibo.setPrecio(rs.getBigDecimal("precio"));
        recibo.setIva(rs.getBigDecimal("iva"));
        recibo.setSubtotal(rs.getBigDecimal("subtotal"));
        recibo.setDescuento(rs.getBigDecimal("descuento"));
        recibo.setTotalIva(rs.getBigDecimal("total_iva"));
        recibo.setTotal(rs.getBigDecimal("total"));
        return recibo;
    }

    //llena el modelo con los datos del resultset
    private VentasModel cargarVentaResumen(ResultSet rs) throws SQLException {
        VentasModel venta = new VentasModel();
        venta.setId(rs.getInt("id"));
        venta.setNumeroFactura(rs.getString("numero_factura"));

        Timestamp fecha = rs.getTimestamp("fecha");
        if (fecha != null) {
            venta.setFecha(fecha.toLocalDateTime());
        }

        venta.setClienteNombre(rs.getString("cliente_nombre"));
        venta.setVendedor(rs.getString("vendedor"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setAnulada(rs.getBoolean("anulada"));
        return venta;
    }
}
