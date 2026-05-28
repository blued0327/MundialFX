package com.mundial.app.dao;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;


import com.mundial.app.connection.CreateConnection;

import com.mundial.app.model.UsuarioModel;

public class UsuarioDao {

    //insertar , lo cambie a int para que devuelva el id que se incerto
    public int insertar(UsuarioModel user) {

        //query -- se cambia por los procedures sp--tengan cuidado con esto!!!!!
        String query = "select sp_usuario_insertar(?,?,?,?)";
        //try
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRol());
            ps.setBoolean(4, user.isEstado());

            ResultSet rs = ps.executeQuery();
            //parte donde se incerta el id-- se hizo en el procedure
            if (rs.next()) {
                return rs.getInt(1);
            }
            //usaremos -1 para errores ya que tiene que retorna positivo
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

    }
    //actualizar

    public boolean actualizar(UsuarioModel user) {

        //query
        String query = "SELECT sp_usuario_actualizar(?, ?, ?, ?, ?)";

        //try
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRol());
            ps.setBoolean(5, user.isEstado());

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

    //consultar
    public List<UsuarioModel> listar() {
        List<UsuarioModel> lista = new ArrayList<>();
        String query = "SELECT * FROM sp_usuario_todos()";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioModel u = new UsuarioModel();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getBoolean("estado"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;

    }

    //cambiar estado solo servia para poner el otro estado
    /*
    public boolean cambiarEstado(int id, boolean estado) {
        //query
        String query = "SELECT sp_usuario_eliminar(?)";
        //try
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
    public boolean cambiarEstado(int id, boolean estado) {
        String query = "SELECT sp_usuario_cambiar_estado(?, ?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.setBoolean(2, estado);
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

    //buscar por username
    public UsuarioModel buscarPorUsername(String username) {
        String query = "SELECT * FROM sp_usuario(?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UsuarioModel user = new UsuarioModel();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRol(rs.getString("rol"));
                user.setEstado(rs.getBoolean("estado"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //buscar usuario por id
    public UsuarioModel buscarPorId(int id) {

        String query = "SELECT * FROM sp_usuario_por_id(?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            //mandar id
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            //si encontro usuario
            if (rs.next()) {

                UsuarioModel user = new UsuarioModel();

                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRol(rs.getString("rol"));
                user.setEstado(rs.getBoolean("estado"));

                return user;
            }

            return null;

        } catch (SQLException e) {

            e.printStackTrace();

            return null;
        }
    }
}
