package com.mundial.app.dao;



import com.mundial.app.connection.CreateConnection;
import com.mundial.app.model.PartidoModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidoDao {

    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_FINALIZADO = "FINALIZADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    // INSERTAR  devuelve id generado, -1 si falla
    // SP: sp_partido_insertar(local, visitante, fecha, estadio, ciudad, capacidad, estado)
    public int insertar(PartidoModel p) {
        String query = "SELECT sp_partido_insertar(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, p.getEquipoLocal());
            ps.setString(2, p.getEquipoVisitante());
            ps.setTimestamp(3, Timestamp.valueOf(p.getFecha()));
            ps.setString(4, p.getEstadio());
            ps.setString(5, p.getCiudad());
            ps.setInt(6, p.getCapacidad());
            ps.setString(7, p.getEstado());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ACTUALIZAR — devuelve true si modificó
    public boolean actualizar(PartidoModel p) {
        String query = "SELECT sp_partido_actualizar(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, p.getId());
            ps.setString(2, p.getEquipoLocal());
            ps.setString(3, p.getEquipoVisitante());
            ps.setTimestamp(4, Timestamp.valueOf(p.getFecha()));
            ps.setString(5, p.getEstadio());
            ps.setString(6, p.getCiudad());
            ps.setInt(7, p.getCapacidad());
            ps.setString(8, p.getEstado());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ELIMINAR cambia estado a cancelado vía sp y devuelve true si cambió
    /*
    public boolean eliminar(int id) {
        String query = "SELECT sp_partido_eliminar(?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }*/
    // LISTAR TODOS
    public List<PartidoModel> listarTodos() {
        List<PartidoModel> lista = new ArrayList<>();
        String query = "SELECT * FROM sp_partido_todos()";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // BUSCAR POR ID
    // SP: sp_partido_id(id)
    public PartidoModel buscarPorId(int id) {
        String query = "SELECT * FROM sp_partido_id(?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // buscar por partido busqueda parcial ILIKE desde el SP
    public List<PartidoModel> buscarPorEquipo(String texto) {
        List<PartidoModel> lista = new ArrayList<>();
        String query = "SELECT * FROM sp_partido_equipo(?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, texto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    //  ResultSet  PartidoModel
    private PartidoModel mapear(ResultSet rs) throws SQLException {
        PartidoModel p = new PartidoModel();
        p.setId(rs.getInt("id"));
        p.setEquipoLocal(rs.getString("equipo_local"));
        p.setEquipoVisitante(rs.getString("equipo_visitante"));

        Timestamp fecha = rs.getTimestamp("fecha");
        if (fecha != null) {
            p.setFecha(fecha.toLocalDateTime());
        }

        p.setEstadio(rs.getString("estadio"));
        p.setCiudad(rs.getString("ciudad"));
        p.setCapacidad(rs.getInt("capacidad"));
        p.setEstado(rs.getString("estado"));

        Timestamp creadoEn = rs.getTimestamp("creado_en");
        if (creadoEn != null) {
            p.setCreadoEn(creadoEn.toLocalDateTime());
        }

        return p;
    }

    //sp agregado porque no funcionaba el otro
    public boolean cambiarEstado(int id, String estado) {
        String query = "SELECT sp_partido_cambiar_estado(?, ?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.setString(2, estado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
