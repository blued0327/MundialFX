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

    //comision del 2% que cobra el banco si el cliente paga con tarjeta
    //esta comision la paga el cliente, se suma al total
    public static final double COMISION_TARJETA = 0.02;

    //metodos de pago permitidos (deben coincidir con el CHECK de la BD)
    public static final String PAGO_EFECTIVO = "EFECTIVO";
    public static final String PAGO_TARJETA = "TARJETA";

    public int registrarVenta(VentasModel venta) throws SQLException { //sirve para avisar que vamos atrabajar con SQL y puede que hayn errores
        //el SP ahora recibe 10 parametros: agregamos nit, metodo_pago y comision al final
        String sql = "SELECT sp_venta_registrar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, venta.getClienteId());
            ps.setInt(2, venta.getUsuarioId());

            //convertimos la lista de ids a un array que entiende postgresql ya que postgress tiene un array propio no una list
            Integer[] ids = venta.getTicketIds().toArray(new Integer[0]);
            Array pgArray = conn.createArrayOf("integer", ids);
            ps.setArray(3, pgArray);

            ps.setBigDecimal(4, venta.getSubtotal());
            ps.setBigDecimal(5, venta.getDescuento());
            ps.setBigDecimal(6, venta.getTotalIva());
            ps.setBigDecimal(7, venta.getTotal());

            //campos nuevos: nit, metodo de pago y comision del 2%
            //si por alguna razon vienen nulos ponemos los defaults para no tronar
            ps.setString(8, venta.getNit() != null ? venta.getNit() : "CF");
            ps.setString(9, venta.getMetodoPago() != null ? venta.getMetodoPago() : PAGO_EFECTIVO);
            ps.setBigDecimal(10, venta.getComision() != null ? venta.getComision() : BigDecimal.ZERO);

            //aqui atrapamos el v_venta_id de la db
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ventaId = rs.getInt(1); //si es exitoso  trae el id
                    venta.setId(ventaId);   // lo seteamos en venta model y retornamos
                    return ventaId;
                }
            }
        }
        return -1; // si se salta el if llega a -1 dandonos a entender que no se incerto naday no devolvio el id
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
    //ahora recibe tambien nit y metodoPago para calcular la comision del 2% si es tarjeta
    public VentasModel calcularTotales(int clienteId, int usuarioId,
            List<Integer> ticketIds,
            List<Double> precios,
            String nit,
            String metodoPago) {
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

        //preTotal antes de la comision (lo que pagaria si fuera efectivo)
        double preTotal = baseGravable + totalIva;

        //si el cliente paga con tarjeta cobramos 2% extra de comision
        //esa comision la paga el cliente, se suma al total
        double comision = 0;
        if (PAGO_TARJETA.equalsIgnoreCase(metodoPago)) {
            comision = preTotal * COMISION_TARJETA;
        }

        //total final con comision incluida si aplica
        double total = preTotal + comision;

        //construimos el modelo y le seteamos los campos nuevos
        VentasModel venta = new VentasModel(
                clienteId,
                usuarioId,
                BigDecimal.valueOf(subtotal),
                BigDecimal.valueOf(descuento),
                BigDecimal.valueOf(totalIva),
                BigDecimal.valueOf(total),
                ticketIds
        );

        //si el nit viene vacio o null lo guardamos como CF (consumidor final)
        venta.setNit((nit == null || nit.isBlank()) ? "CF" : nit.trim());
        venta.setMetodoPago(metodoPago != null ? metodoPago : PAGO_EFECTIVO);
        venta.setComision(BigDecimal.valueOf(comision));

        return venta;
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

        //campos nuevos: nit, metodo de pago, comision
        //se envuelven en try porque si el SP aun no devuelve estas columnas no queremos que truene
        //una vez que actualices sp_venta_obtener_recibo para incluirlas, funcionara automatico
        try {
            recibo.setNit(rs.getString("nit"));
        } catch (SQLException e) {
            recibo.setNit("CF");
        }
        try {
            recibo.setMetodoPago(rs.getString("metodo_pago"));
        } catch (SQLException e) {
            recibo.setMetodoPago(PAGO_EFECTIVO);
        }
        try {
            recibo.setComision(rs.getBigDecimal("comision"));
        } catch (SQLException e) {
            recibo.setComision(BigDecimal.ZERO);
        }

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

        //tambien intentamos leer los campos nuevos si el SP los devuelve
        //si no, queda con valores por defecto y no truena
        try {
            venta.setNit(rs.getString("nit"));
        } catch (SQLException e) {
            venta.setNit("CF");
        }
        try {
            venta.setMetodoPago(rs.getString("metodo_pago"));
        } catch (SQLException e) {
            venta.setMetodoPago(PAGO_EFECTIVO);
        }
        try {
            venta.setComision(rs.getBigDecimal("comision"));
        } catch (SQLException e) {
            venta.setComision(BigDecimal.ZERO);
        }

        return venta;
    }
}
