/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
// MIGRACION: package cambiado de "dao" a "com.mundial.app.dao"
package com.mundial.app.dao;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

// MIGRACION: import ajustado de "connection.CreateConnection" a "com.mundial.app.connection.CreateConnection"
import com.mundial.app.connection.CreateConnection;
// MIGRACION: import ajustado de "model.ClienteModel" a "com.mundial.app.model.ClienteModel"
import com.mundial.app.model.ClienteModel;

public class ClienteDao {

    // REGISTRAR - usa sp_cliente_insertar
    public boolean registrarCliente(ClienteModel cm) {
        String sql = "SELECT sp_cliente_insertar(?, ?, ?, ?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cm.getNombre());
            ps.setString(2, cm.getApellido());
            ps.setString(3, cm.getTelefono());
            ps.setString(4, cm.getEmail());
            ps.setString(5, cm.getDireccion());

            ResultSet rs = ps.executeQuery();
            return rs.next(); // retorna true si se insertó y devolvió el id

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ACTUALIZAR - usa sp_actualizar_cliente
    public boolean actualizarCliente(ClienteModel cm) {
        String sql = "SELECT sp_actualizar_cliente(?, ?, ?, ?, ?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cm.getId());
            ps.setString(2, cm.getNombre());
            ps.setString(3, cm.getApellido());
            ps.setString(4, cm.getTelefono());
            ps.setString(5, cm.getEmail());
            ps.setString(6, cm.getDireccion());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1); // el procedure retorna boolean
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // CAMBIAR ESTADO (borrado lógico) - usa sp_cliente_eliminar
    public boolean cambiarEstado(int id) {
        String sql = "SELECT sp_cliente_eliminar(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    }

    // LISTAR TODOS - usa sp_clientes_todos
    public List<ClienteModel> listarClientes() {
        List<ClienteModel> lista = new ArrayList<>();
        String sql = "SELECT * FROM sp_clientes_todos()";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ClienteModel cm = new ClienteModel(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion")
                );
                lista.add(cm);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // BUSCAR POR ID - usa sp_cliente_id
    public ClienteModel buscarPorId(int id) {
        String sql = "SELECT * FROM sp_cliente_id(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ClienteModel(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // BUSCAR POR NOMBRE - usa sp_cliente_buscar_nombre
    public List<ClienteModel> buscarPorNombre(String texto) {
        List<ClienteModel> lista = new ArrayList<>();
        String sql = "SELECT * FROM sp_cliente_buscar_nombre(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, texto);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ClienteModel cm = new ClienteModel(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion")
                );
                lista.add(cm);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}
