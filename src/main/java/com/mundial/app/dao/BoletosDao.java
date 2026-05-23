/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
// MIGRACION: package cambiado de "dao" a "com.mundial.app.dao"
package com.mundial.app.dao;

// MIGRACION: import ajustado de "connection.CreateConnection" a "com.mundial.app.connection.CreateConnection"
import com.mundial.app.connection.CreateConnection;
// MIGRACION: import ajustado de "model.BoletosModel" a "com.mundial.app.model.BoletosModel"
import com.mundial.app.model.BoletosModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rchar
 */
public class BoletosDao {

    private final Connection connection;

    // ── Estados válidos según CHECK de la tabla ────────────────────────────────
    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_RESERVADO  = "RESERVADO";
    public static final String ESTADO_VENDIDO    = "VENDIDO";

    // ── Secciones válidas según CHECK de la tabla ──────────────────────────────
    public static final String SECCION_VIP         = "VIP";
    public static final String SECCION_PREFERENCIAL = "PREFERENCIAL";
    public static final String SECCION_GENERAL      = "GENERAL";

    public BoletosDao() {
        this.connection = CreateConnection.getInstancia().getConnection();
    }

    public boolean generarTicket(BoletosModel boleto) throws SQLException {
        String sql = """
                INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, Integer.parseInt(boleto.getPartidoAsociado()));
            ps.setString(2, boleto.getAsiento() + "");   // numero_asiento es VARCHAR
            ps.setString(3, boleto.getSeccion());
            ps.setBigDecimal(4, new java.math.BigDecimal(boleto.getPrecio()).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(5, ESTADO_DISPONIBLE);

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) boleto.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }


    public int generarTicketsEnLote(List<BoletosModel> boletos) throws SQLException {
        String sql = """
                INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
                VALUES (?, ?, ?, ?, ?)
                """;

        int insertados = 0;
        connection.setAutoCommit(false);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (BoletosModel b : boletos) {
                ps.setInt(1, Integer.parseInt(b.getPartidoAsociado()));
                ps.setString(2, b.getAsiento() + "");
                ps.setString(3, b.getSeccion());
                ps.setBigDecimal(4, new java.math.BigDecimal(b.getPrecio()).setScale(2, java.math.RoundingMode.HALF_UP));
                ps.setString(5, ESTADO_DISPONIBLE);
                ps.addBatch();
            }

            int[] resultados = ps.executeBatch();
            connection.commit();
            for (int r : resultados) if (r > 0) insertados++;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }

        return insertados;
    }

    public List<BoletosModel> consultarDisponibles(int partidoId) throws SQLException {
        String sql = """
                SELECT id, partido_id, numero_asiento, seccion, precio, estado
                FROM ticket
                WHERE partido_id = ? AND estado = ?
                ORDER BY seccion, numero_asiento
                """;

        List<BoletosModel> disponibles = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, partidoId);
            ps.setString(2, ESTADO_DISPONIBLE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) disponibles.add(mapearResultado(rs));
            }
        }
        return disponibles;
    }

    public List<BoletosModel> consultarDisponiblesPorSeccion(int partidoId,
                                                              String seccion) throws SQLException {
        String sql = """
                SELECT id, partido_id, numero_asiento, seccion, precio, estado
                FROM ticket
                WHERE partido_id = ? AND seccion = ? AND estado = ?
                ORDER BY numero_asiento
                """;

        List<BoletosModel> disponibles = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, partidoId);
            ps.setString(2, seccion);
            ps.setString(3, ESTADO_DISPONIBLE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) disponibles.add(mapearResultado(rs));
            }
        }
        return disponibles;
    }

    public List<Object[]> resumenDisponibilidadPorSeccion(int partidoId) throws SQLException {
        String sql = """
                SELECT seccion, COUNT(*) AS total_disponibles
                FROM ticket
                WHERE partido_id = ? AND estado = ?
                GROUP BY seccion
                ORDER BY seccion
                """;

        List<Object[]> resumen = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, partidoId);
            ps.setString(2, ESTADO_DISPONIBLE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resumen.add(new Object[]{
                        rs.getString("seccion"),
                        rs.getInt("total_disponibles")
                    });
                }
            }
        }
        return resumen;
    }

    public BoletosModel asignarAsiento(int partidoId, String numeroAsiento) throws SQLException {
        String sqlSelect = """
                SELECT id, partido_id, numero_asiento, seccion, precio, estado
                FROM ticket
                WHERE partido_id = ? AND numero_asiento = ? AND estado = ?
                FOR UPDATE
                """;

        String sqlUpdate = """
                UPDATE ticket SET estado = ? WHERE id = ?
                """;

        connection.setAutoCommit(false);
        try {
            BoletosModel boleto = null;

            try (PreparedStatement ps = connection.prepareStatement(sqlSelect)) {
                ps.setInt(1, partidoId);
                ps.setString(2, numeroAsiento);
                ps.setString(3, ESTADO_DISPONIBLE);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) boleto = mapearResultado(rs);
                }
            }

            if (boleto == null) {
                connection.rollback();
                return null;    // asiento no disponible o no existe
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                ps.setString(1, ESTADO_RESERVADO);
                ps.setInt(2, boleto.getId());
                ps.executeUpdate();
            }

            boleto.setEstado(ESTADO_RESERVADO);
            connection.commit();
            return boleto;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }


    public boolean confirmarVenta(int idTicket) throws SQLException {
        String sql = """
                UPDATE ticket SET estado = ? WHERE id = ? AND estado = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ESTADO_VENDIDO);
            ps.setInt(2, idTicket);
            ps.setString(3, ESTADO_RESERVADO);
            return ps.executeUpdate() > 0;
        }
    }


    public boolean liberarAsiento(int idTicket) throws SQLException {
        String sql = """
                UPDATE ticket SET estado = ? WHERE id = ? AND estado = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ESTADO_DISPONIBLE);
            ps.setInt(2, idTicket);
            ps.setString(3, ESTADO_RESERVADO);
            return ps.executeUpdate() > 0;
        }
    }

    private BoletosModel mapearResultado(ResultSet rs) throws SQLException {
        return new BoletosModel(
            rs.getInt("id"),
            String.valueOf(rs.getInt("partido_id")),  // partidoAsociado ← partido_id
            Integer.parseInt(rs.getString("numero_asiento").replaceAll("[^0-9]", "")),
            rs.getString("seccion"),
            rs.getDouble("precio"),
            rs.getString("estado")
        );
    }
}
