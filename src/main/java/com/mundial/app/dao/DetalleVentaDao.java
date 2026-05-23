

//Nos sireve para traer las herramientas que necesita la conexion y el model
// MIGRACION: package cambiado de "dao" a "com.mundial.app.dao"
package com.mundial.app.dao;

// MIGRACION: import ajustado de "connection.CreateConnection" a "com.mundial.app.connection.CreateConnection"
import com.mundial.app.connection.CreateConnection;
// MIGRACION: import ajustado de "model.DetalleVentaModel" a "com.mundial.app.model.DetalleVentaModel"
import com.mundial.app.model.DetalleVentaModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDao {
/*
    // INSERTAR un detalle de venta
    public boolean insertar(DetalleVentaModel detalle) { //Recibe un detalle lleno y lo va a guardaar en la base de datos
        String query = "INSERT INTO detalle_venta(venta_id, ticket_id, precio, iva) VALUES (?, ?, ?, ?)"; //Esto evita que alguien ingrese codigo pegriloso en la DB

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, detalle.getVentaId());
            ps.setInt(2, detalle.getTicketId());
            ps.setDouble(3, detalle.getPrecio());
            ps.setDouble(4, detalle.getIva());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // LISTAR los detalles de una venta específica. Va a recibir el id de una venta y luego devuelve los boletos que se compraron en esa venta
    public List<DetalleVentaModel> listarPorVenta(int ventaId) {
        List<DetalleVentaModel> lista = new ArrayList<>();
        String query = "SELECT * FROM detalle_venta WHERE venta_id = ?";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, ventaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleVentaModel detalle = new DetalleVentaModel(
                    rs.getInt("id"),
                    rs.getInt("venta_id"),
                    rs.getInt("ticket_id"),
                    rs.getDouble("precio"),
                    rs.getDouble("iva")
                );
                lista.add(detalle);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }*/
}
